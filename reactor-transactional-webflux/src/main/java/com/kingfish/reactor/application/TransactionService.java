package com.kingfish.reactor.application;

import com.kingfish.reactor.domain.model.entity.OrderDO;
import com.kingfish.reactor.domain.model.entity.ProductDO;
import com.kingfish.reactor.infrastructure.repository.OrderRepository;
import com.kingfish.reactor.infrastructure.repository.ProductRepository;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.jooq.DSLContext;
import org.jooq.Field;
import org.jooq.Table;
import org.jooq.impl.DSL;
import org.jooq.impl.SQLDataType;
import org.springframework.r2dbc.core.DatabaseClient;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.ThreadLocalRandom;

/**
 * 事务验证服务（R2DBC 响应式事务版本）
 * <p>
 * 与 transactional-webflux 模块中的 JDBC 版本不同，本类基于 R2DBC 实现，
 * {@code @Transactional} 底层通过 Reactor Context 传递事务状态，
 * 不依赖 ThreadLocal，无需将操作包裹在同步方法中，事务天然跟随响应式订阅链路传播。
 *
 * @Author : haowl
 * @Date : 2026/4/23 21:02
 */
@Slf4j
@Service
public class TransactionService {

    private static final DateTimeFormatter ORDER_NO_FORMATTER =
            DateTimeFormatter.ofPattern("yyyyMMddHHmmssSSS");

    /** 订单初始支付状态：未支付 */
    private static final int PAY_STATUS_UNPAID = 0;

    /** 订单初始状态：待付款 */
    private static final int ORDER_STATUS_PENDING = 0;

    /** 扣减库存失败的影响行数 */
    private static final long DEDUCT_STOCK_FAIL_ROWS = 0L;

    @Resource
    private OrderRepository orderRepository;

    @Resource
    private ProductRepository productRepository;

    @Resource
    private DSLContext dslContext;

    @Resource
    private DatabaseClient databaseClient;

    /**
     * 创建订单并扣减库存（R2DBC 响应式事务）
     * <p>
     * 整条 Reactor 链路被 {@code @Transactional} 包裹，事务通过 Reactor Context 传递，
     * 无论中途如何切换线程，事务上下文始终跟随订阅链路，不会丢失。
     * <p>
     * 业务流程：查询商品 → 构建订单并插入 → 扣减库存（为验证事务回滚，故意将扣减放在插入之后）
     *
     * @param productId 商品ID
     * @param quantity  购买数量
     * @return 创建成功的订单信息
     */
    @Transactional(rollbackFor = Exception.class)
    public Mono<OrderDO> createOrder(Long productId, Integer quantity) {
        // 1. 查询商品
        return productRepository.findByProductId(productId)
                .switchIfEmpty(Mono.error(
                        new IllegalArgumentException("商品不存在，商品ID：" + productId)))
                // 2. 构建订单并插入（为验证事务，先插入订单再扣库存）
                .flatMap(product -> {
                    OrderDO order = buildOrder(productId, quantity, product.getPrice());
                    return orderRepository.save(order);
                })
                .doOnNext(order -> log.info("订单插入成功，订单ID：{}，订单号：{}",
                        order.getId(), order.getOrderNo()))
                // 3. 扣减库存（乐观方式），若失败则抛异常触发事务回滚，前面插入的订单也会回滚
                .flatMap(order -> productRepository.deductStock(productId, quantity)
                        .flatMap(updatedRows -> {
                            if (updatedRows == DEDUCT_STOCK_FAIL_ROWS) {
                                return Mono.error(new IllegalStateException(
                                        "扣减库存失败，可能存在并发竞争，商品ID：" + productId));
                            }
                            return Mono.just(order);
                        }))
                .doOnSuccess(order -> log.info("下单成功，订单号：{}，商品ID：{}，数量：{}",
                        order.getOrderNo(), productId, quantity))
                .doOnError(e -> log.error("下单失败，商品ID：{}，数量：{}，原因：{}",
                        productId, quantity, e.getMessage()));
    }

    /**
     * 使用 jOOQ DSL 创建订单并扣减库存（演示 jOOQ + R2DBC 配合使用）
     * <p>
     * jOOQ 在此场景中仅作为类型安全的 SQL 构造器，生成 SQL 字符串后，
     * 通过 Spring 的 {@link DatabaseClient} 执行，底层走 R2DBC 非阻塞通道。
     * <p>
     * 与 {@link #createOrder} 的区别：前者使用 Spring Data R2DBC Repository，
     * 本方法使用 jOOQ DSL 手动构建 SQL，适合复杂查询场景。
     *
     * @param productId 商品ID
     * @param quantity  购买数量
     * @return 创建成功的订单信息
     */
    @Transactional(rollbackFor = Exception.class)
    public Mono<OrderDO> createOrderWithJooq(Long productId, Integer quantity) {
        // jOOQ 表和字段定义（生产环境建议通过 jOOQ 代码生成器自动生成）
        Table<?> productTable = DSL.table("product");
        Field<Long> productIdField = DSL.field("product_id", SQLDataType.BIGINT);
        Field<BigDecimal> priceField = DSL.field("price", SQLDataType.DECIMAL(10, 2));
        Field<Integer> stockField = DSL.field("stock", SQLDataType.INTEGER);

        Table<?> orderTable = DSL.table("`order`");
        Field<Long> orderIdField = DSL.field("order_id", SQLDataType.BIGINT);
        Field<Long> userIdField = DSL.field("user_id", SQLDataType.BIGINT);
        Field<Long> orderProductIdField = DSL.field("product_id", SQLDataType.BIGINT);
        Field<Integer> quantityField = DSL.field("quantity", SQLDataType.INTEGER);
        Field<String> orderNoField = DSL.field("order_no", SQLDataType.VARCHAR(64));
        Field<BigDecimal> totalAmountField = DSL.field("total_amount", SQLDataType.DECIMAL(10, 2));
        Field<Integer> payStatusField = DSL.field("pay_status", SQLDataType.INTEGER);
        Field<Integer> orderStatusField = DSL.field("order_status", SQLDataType.INTEGER);

        // 1. 使用 jOOQ 构建查询商品的 SQL
        String selectSql = dslContext.select(priceField, stockField)
                .from(productTable)
                .where(productIdField.eq(productId))
                .getSQL();

        return databaseClient.sql(selectSql)
                .bind(0, productId)
                .map(row -> {
                    ProductDO product = new ProductDO();
                    product.setPrice(row.get("price", BigDecimal.class));
                    product.setStock(row.get("stock", Integer.class));
                    return product;
                })
                .one()
                .switchIfEmpty(Mono.error(
                        new IllegalArgumentException("商品不存在，商品ID：" + productId)))
                // 2. 使用 jOOQ 构建插入订单的 SQL
                .flatMap(product -> {
                    OrderDO order = buildOrder(productId, quantity, product.getPrice());

                    String insertSql = dslContext.insertInto(orderTable)
                            .columns(orderIdField, userIdField, orderProductIdField,
                                    quantityField, orderNoField, totalAmountField,
                                    payStatusField, orderStatusField)
                            .values(DSL.param(orderIdField), DSL.param(userIdField),
                                    DSL.param(orderProductIdField), DSL.param(quantityField),
                                    DSL.param(orderNoField), DSL.param(totalAmountField),
                                    DSL.param(payStatusField), DSL.param(orderStatusField))
                            .getSQL();

                    return databaseClient.sql(insertSql)
                            .bind(0, order.getOrderId())
                            .bind(1, order.getUserId())
                            .bind(2, order.getProductId())
                            .bind(3, order.getQuantity())
                            .bind(4, order.getOrderNo())
                            .bind(5, order.getTotalAmount())
                            .bind(6, order.getPayStatus())
                            .bind(7, order.getOrderStatus())
                            .fetch()
                            .rowsUpdated()
                            .doOnNext(rows -> log.info("jOOQ 订单插入成功，订单号：{}", order.getOrderNo()))
                            .thenReturn(order);
                })
                // 3. 使用 jOOQ 构建扣减库存的 SQL
                .flatMap(order -> {
                    String updateSql = dslContext.update(productTable)
                            .set(stockField, stockField.minus(quantity))
                            .where(productIdField.eq(productId)
                                    .and(stockField.greaterOrEqual(quantity)))
                            .getSQL();

                    return databaseClient.sql(updateSql)
                            .bind(0, quantity)
                            .bind(1, productId)
                            .bind(2, quantity)
                            .fetch()
                            .rowsUpdated()
                            .flatMap(updatedRows -> {
                                if (updatedRows == DEDUCT_STOCK_FAIL_ROWS) {
                                    return Mono.error(new IllegalStateException(
                                            "扣减库存失败，可能存在并发竞争，商品ID：" + productId));
                                }
                                return Mono.just(order);
                            });
                })
                .doOnSuccess(order -> log.info("jOOQ 下单成功，订单号：{}，商品ID：{}，数量：{}",
                        order.getOrderNo(), productId, quantity))
                .doOnError(e -> log.error("jOOQ 下单失败，商品ID：{}，数量：{}，原因：{}",
                        productId, quantity, e.getMessage()));
    }

    /**
     * 构建订单对象
     *
     * @param productId 商品ID
     * @param quantity  购买数量
     * @param price     商品单价
     * @return 订单对象
     */
    private OrderDO buildOrder(Long productId, Integer quantity, BigDecimal price) {
        OrderDO order = new OrderDO();
        order.setOrderId(System.currentTimeMillis());
        order.setOrderNo(generateOrderNo());
        order.setProductId(productId);
        order.setQuantity(quantity);
        order.setUserId(1L);
        order.setTotalAmount(price.multiply(BigDecimal.valueOf(quantity)));
        order.setPayStatus(PAY_STATUS_UNPAID);
        order.setOrderStatus(ORDER_STATUS_PENDING);
        return order;
    }

    /**
     * 生成订单编号（时间戳 + 随机数，简单实现，仅用于验证场景）
     *
     * @return 订单编号
     */
    private String generateOrderNo() {
        String timestamp = LocalDateTime.now().format(ORDER_NO_FORMATTER);
        int randomSuffix = ThreadLocalRandom.current().nextInt(1000, 9999);
        return "ORD" + timestamp + randomSuffix;
    }
}
