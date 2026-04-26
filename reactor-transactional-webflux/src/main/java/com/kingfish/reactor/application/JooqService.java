package com.kingfish.reactor.application;

import com.kingfish.reactor.domain.model.dao.OrderDao;
import com.kingfish.reactor.domain.model.dao.ProductDao;
import com.kingfish.reactor.domain.model.entity.OrderDO;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.ThreadLocalRandom;

/**
 * 基于 jOOQ + R2DBC 的事务验证服务
 * <p>
 * 与 {@link TransactionService} 的区别：
 * TransactionService 使用 Spring Data R2DBC Repository，
 * 本类使用 {@link com.kingfish.reactor.domain.model.dao.ReactiveBaseDao} 基类，
 * 通过 jOOQ DSL 构建 SQL，DatabaseClient 执行，适合复杂查询场景。
 *
 * @Author : haowl
 * @Date : 2026/4/26
 */
@Slf4j
@Service
public class JooqService {

    private static final DateTimeFormatter ORDER_NO_FORMATTER =
            DateTimeFormatter.ofPattern("yyyyMMddHHmmssSSS");

    /** 订单初始支付状态：未支付 */
    private static final int PAY_STATUS_UNPAID = 0;

    /** 订单初始状态：待付款 */
    private static final int ORDER_STATUS_PENDING = 0;

    /** 扣减库存失败的影响行数 */
    private static final long DEDUCT_STOCK_FAIL_ROWS = 0L;

    @Resource
    private OrderDao orderDao;

    @Resource
    private ProductDao productDao;

    /**
     * 创建订单并扣减库存（jOOQ + R2DBC 响应式事务）
     * <p>
     * 业务流程与 {@link TransactionService#createOrder} 完全一致：
     * 查询商品 → 构建订单并插入 → 扣减库存。
     * 区别在于数据访问层使用 jOOQ 基类 {@link com.kingfish.reactor.domain.model.dao.ReactiveBaseDao}，
     * 通过类型安全的 DSL 构建 SQL。
     *
     * @param productId 商品ID
     * @param quantity  购买数量
     * @return 创建成功的订单信息
     */
    @Transactional(rollbackFor = Exception.class)
    public Mono<OrderDO> createOrder(Long productId, Integer quantity) {
        // 1. 通过 ProductDao 查询商品
        return productDao.findByProductId(productId)
                .switchIfEmpty(Mono.error(
                        new IllegalArgumentException("商品不存在，商品ID：" + productId)))
                // 2. 构建订单并通过 OrderDao 插入
                .flatMap(product -> {
                    OrderDO order = buildOrder(productId, quantity, product.getPrice());
                    return orderDao.insert(orderDao.toColumnMap(order))
                            .doOnNext(rows -> log.info("jOOQ 订单插入成功，订单号：{}",
                                    order.getOrderNo()))
                            .thenReturn(order);
                })
                // 3. 通过 ProductDao 扣减库存
                .flatMap(order -> productDao.deductStock(productId, quantity)
                        .flatMap(updatedRows -> {
                            if (updatedRows == DEDUCT_STOCK_FAIL_ROWS) {
                                return Mono.error(new IllegalStateException(
                                        "扣减库存失败，可能存在并发竞争，商品ID：" + productId));
                            }
                            return Mono.just(order);
                        }))
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
