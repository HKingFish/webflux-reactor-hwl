package com.kingfish.webflux.application;

import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.kingfish.webflux.domain.model.entity.OrderDO;
import com.kingfish.webflux.domain.model.entity.ProductDO;
import com.kingfish.webflux.infrastructure.mapper.OrderMapper;
import com.kingfish.webflux.infrastructure.mapper.ProductMapper;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.ThreadLocalRandom;

/**
 * 事务验证服务
 * 用于验证 Reactor 场景下 JDBC 事务的提交与回滚行为
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

    @Resource
    private OrderMapper orderMapper;

    @Resource
    private ProductMapper productMapper;

    @Resource
    @Lazy
    private TransactionService transactionService;

    /**
     * 创建订单并扣减库存（事务方法）
     * <p>
     * 核心逻辑：查询商品 → 校验库存 → 扣减库存 → 创建订单，整个流程在同一事务中完成。
     * 由于使用 JDBC 数据源，事务绑定在线程上，因此将所有阻塞操作包裹在
     * {@code Mono.fromCallable()} 中，通过 {@code Schedulers.boundedElastic()} 调度，
     * 并在同一个 Callable 内完成，确保事务生效。
     *
     * @param productId 商品ID
     * @param quantity  购买数量
     * @return 创建成功的订单信息
     */
    public Mono<OrderDO> createOrder(Long productId, Integer quantity) {
        // 将整个事务操作包裹在同一个 Callable 中，保证 JDBC 事务在同一线程内执行
        return Mono.fromCallable(() -> transactionService.executeCreateOrder(productId, quantity))
                .subscribeOn(Schedulers.boundedElastic())
                .doOnSuccess(order -> log.info("下单成功，订单号：{}，商品ID：{}，数量：{}",
                        order.getOrderNo(), productId, quantity))
                .doOnError(e -> log.error("下单失败，商品ID：{}，数量：{}，原因：{}",
                        productId, quantity, e.getMessage()));
    }

    /**
     * 执行创建订单的事务操作（同步阻塞方法）
     * <p>
     * 使用 {@code @Transactional} 保证查询商品、扣减库存、插入订单在同一事务中，
     * 任一步骤异常将触发回滚。
     *
     * @param productId 商品ID
     * @param quantity  购买数量
     * @return 创建成功的订单
     */
    @Transactional(rollbackFor = Exception.class)
    public OrderDO executeCreateOrder(Long productId, Integer quantity) {

        // 1. 查询商品并校验状态、库存
        ProductDO product = productMapper.selectById(productId);
        // TODO : 正常应该校验库存，再扣减库存再创建订单，这里为了验证事务，调整了顺序
//        if (product == null) {
//            throw new IllegalArgumentException("商品不存在，商品ID：" + productId);
//        }
//        if (product.getStock() < quantity) {
//            throw new IllegalStateException("库存不足，当前库存：" + product.getStock()
//                    + "，需要数量：" + quantity);
//        }



        // 3. 构建订单并插入
        OrderDO order = buildOrder(productId, quantity, product.getPrice());
        orderMapper.insert(order);
        log.info("订单插入成功，订单ID：{}，订单号：{}", order.getId(), order.getOrderNo());

        // 2. 扣减库存（乐观方式，仅库存充足时扣减）
        LambdaUpdateWrapper<ProductDO> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.eq(ProductDO::getProductId, productId)
                .ge(ProductDO::getStock, quantity)
                .setSql("stock = stock - " + quantity);
        int updatedRows = productMapper.update(null, updateWrapper);
        if (updatedRows == 0) {
            throw new IllegalStateException("扣减库存失败，可能存在并发竞争，商品ID：" + productId);
        }

        return order;
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
