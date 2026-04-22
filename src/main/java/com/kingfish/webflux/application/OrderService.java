package com.kingfish.webflux.application;

import com.kingfish.webflux.domain.model.aggregate.OrderExt;
import com.kingfish.webflux.domain.model.entity.*;
import com.kingfish.webflux.domain.model.vo.MemberLevelVO;
import com.kingfish.webflux.domain.model.vo.OrderConfirmVO;
import com.kingfish.webflux.infrastructure.mapper.*;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;
import java.util.List;

/**
 * 订单服务
 *
 * @Author : haowl
 * @Date : 2026/4/12 11:47
 * @Desc :
 */
@Slf4j
@Service
public class OrderService {

    @Resource
    private OrderMapper orderMapper;

    @Resource
    private OrderLogisticsMapper orderLogisticsMapper;

    @Resource
    private UserMapper userMapper;

    @Resource
    private UserExtMapper userExtMapper;

    @Resource
    private ProductMapper productMapper;

    @Resource
    private CartMapper cartMapper;

    @Resource
    private CouponMapper couponMapper;

    @Resource
    private UserAddressMapper userAddressMapper;

    @Resource
    private MemberService memberService;

    /**
     * 根据用户ID查询所有订单（基础方法，供其他方法调用）
     *
     * @param userId 用户ID
     * @return 订单列表
     */
    public Mono<List<OrderDO>> getByUserId(Long userId) {
        return orderMapper.findByUserId(userId);
    }

    /**
     * 订单详情页接口
     * <p>
     * 场景：用户点击某笔订单进入详情页，需要一次性展示所有关联信息
     * <p>
     * 根据订单ID查询订单，订单存在后并行查询：
     * - 物流信息列表
     * - 下单用户信息（密码脱敏）
     * - 订单关联商品信息
     * 全部组装到 OrderExt 中返回，订单不存在返回空 Mono
     *
     * @param orderId 订单ID
     * @return 订单详情聚合对象
     */
    public Mono<OrderExt> getOrderFullDetail(Long orderId) {
        return orderMapper.findById(orderId)
                .flatMap(order -> {
                    Mono<List<OrderLogisticsDO>> logisticsMono =
                            orderLogisticsMapper.findByOrderId(orderId);
                    Mono<UserDO> userMono = userMapper.findById(order.getUserId())
                            .map(userDO -> {
                                userDO.setPassword(null);
                                return userDO;
                            });

                    Mono<ProductDO> productMono =
                            productMapper.getProductById(order.getProductId());
                    return Mono.zip(Mono.just(order), logisticsMono, userMono, productMono)
                            .map(tuple ->
                                    OrderExt.from(tuple.getT1(), tuple.getT2(), tuple.getT3(), tuple.getT4()));
                });


    }

    /**
     * 订单确认页接口（立即购买）
     * <p>
     * 场景：用户在商品详情页点击「立即购买」，跳转到订单确认页
     * 需要一次性聚合展示商品信息、价格计算、可用优惠券、收货地址、联系方式
     * <p>
     * 业务流程：
     * 1. 校验用户状态正常（status=1），否则抛出 IllegalStateException("用户状态异常")
     * 2. 校验商品存在且上架(status=1)，否则抛出 IllegalArgumentException("商品不存在或已下架")
     * 3. 校验库存充足(stock >= quantity)，否则抛出 IllegalArgumentException("库存不足")
     * 4. 以上校验通过后，并行查询：
     * - 用户会员等级（调用 memberService.getMemberLevel，异常降级为默认值）
     * - 用户可用优惠券列表（过滤：未使用、未过期、未删除，且门槛 <= 商品金额）
     * - 用户默认收货地址
     * - 用户手机号（从 user_ext 查询，脱敏处理，如 138****1001）
     * 5. 计算金额：
     * - 商品金额 = 单价 × 数量
     * - 会员折扣后金额 = 商品金额 × 会员折扣率
     * 6. 组装成 OrderConfirmVO 返回
     *
     * @param userId    用户ID
     * @param productId 商品ID
     * @param quantity  购买数量
     * @return 订单确认页数据
     */
    public Mono<OrderConfirmVO> getOrderConfirmPage(Long userId, Long productId, Integer quantity) {
        // userMono 和 productMono 并行查询，如果查询压力大，应该串行查询，串行写都太丑了，嵌套一层又一层
        Mono<UserDO> userMono = userMapper.findValidUser(userId);
        Mono<ProductDO> productMono = productMapper.findValidProduct(productId, quantity);

        return Mono.zip(userMono, productMono)
                .flatMap(objects -> {
                    UserDO user = objects.getT1();
                    ProductDO product = objects.getT2();
                    Mono<MemberLevelVO> memberLevelMono = memberService.getMemberLevel(userId);
                    // 为空也是一个 Mono<List<CouponDO>>，不是 Mono.empty 所以不用 switchIfEmpty 拦截也不阻塞 zip
                    Mono<List<CouponDO>> couponMono = couponMapper.findByUserId(userId)
                            .map(c -> c.stream()
                                    .filter(couponDO -> couponDO.getStatus() == 0)
                                    .filter(couponDO -> couponDO.getThreshold()
                                            .compareTo(product.getPrice()
                                                    .multiply(BigDecimal.valueOf(quantity))) <= 0)
                                    .toList());

                    // 非空判断不能在 zip 中进行，因为 zip 元素存在一个空的，其 map、flapmap 都不会执行，而是返回一个 Mono.empty()
                    Mono<UserAddressDO> userAddressMono =
                            userAddressMapper.getDefaultAddress(userId)
                                    .switchIfEmpty(Mono.error(new IllegalArgumentException("用户未设置默认地址")));

                    Mono<String> phoneMono = userExtMapper.findByUserId(userId)
                            .filter(ue -> ue != null && StringUtils.isNotBlank(ue.getPhone()))
                            .map(ext -> ext.getPhone().substring(0, 3) + "****" + ext.getPhone().substring(7))
                            .switchIfEmpty(Mono.error(new IllegalArgumentException("用户未设置手机号")));

                    return Mono.zip(memberLevelMono, couponMono, userAddressMono, phoneMono)
                            .map(tuple -> {
                                MemberLevelVO memberLevelVO = tuple.getT1();
                                List<CouponDO> couponDOList = tuple.getT2();
                                UserAddressDO userAddressDO = tuple.getT3();
                                String phone = tuple.getT4();

                                OrderConfirmVO confirmVO = new OrderConfirmVO();
                                confirmVO.setProduct(product);
                                confirmVO.setQuantity(quantity);
                                confirmVO.setProductAmount(product.getPrice().multiply(BigDecimal.valueOf(quantity)));
                                confirmVO.setMemberDiscount(memberLevelVO.getDiscount());
                                confirmVO.setDiscountAmount(product.getPrice()
                                        .multiply(BigDecimal.valueOf(quantity)).multiply(memberLevelVO.getDiscount()));
                                confirmVO.setAvailableCoupons(couponDOList);
                                confirmVO.setDefaultAddress(userAddressDO);
                                confirmVO.setMaskedPhone(phone);
                                return confirmVO;
                            });
                });
    }

    /**
     * 创建订单（下单接口，需响应式事务）
     * <p>
     * 场景：用户在订单确认页点击「提交订单」
     * <p>
     * 业务流程：
     * 1. 校验用户状态正常（status=1），否则抛出 IllegalArgumentException("用户状态异常")
     * 2. 校验商品存在且上架，库存充足，否则抛出对应异常
     * 3. 扣减库存，扣减失败（并发冲突导致影响行数为0）抛出 IllegalStateException("扣减库存失败")
     * 4. 创建订单记录（设置 userId、productId、quantity、totalAmount = 单价 × 数量）
     * 5. 清空该用户购物车
     * <p>
     * 以上步骤在同一个响应式事务中执行，任何一步失败整体回滚
     * 成功时打印 info 日志，失败时打印 error 日志
     *
     * @param userId    用户ID
     * @param productId 商品ID
     * @param quantity  购买数量
     * @return 创建成功的订单
     */
    public Mono<OrderDO> createOrder(Long userId, Long productId, Integer quantity) {
        // 注意：MyBatis-Plus（JDBC）场景下，R2DBC 的 TransactionalOperator 无法管理 JDBC 事务
        // 这里仅作为 Reactor 链式编排练习，真实项目应使用 @Transactional + Mono.fromCallable 方案

        // 第一阶段：并行校验用户和商品
        return Mono.zip(
                        userMapper.findValidUser(userId),
                        productMapper.findValidProduct(productId, quantity))
                // 第二阶段：扣减库存
                .flatMap(tuple -> {
                    ProductDO product = tuple.getT2();
                    return productMapper.deductStock(productId, quantity)
                            .thenReturn(product);
                })
                // 第三阶段：创建订单
                .flatMap(product -> {
                    OrderDO orderDO = new OrderDO()
                            .setUserId(userId)
                            .setProductId(productId)
                            .setQuantity(quantity)
                            .setTotalAmount(product.getPrice().multiply(BigDecimal.valueOf(quantity)));
                    return orderMapper.create(orderDO).thenReturn(orderDO);
                })
                // 第四阶段：清空购物车
                .flatMap(orderDO -> cartMapper.deleteByUserId(userId).thenReturn(orderDO))
                .doOnSuccess(order -> log.info("创建订单成功，用户ID：{}，商品ID：{}，数量：{}",
                        userId, productId, quantity))
                .doOnError(e -> log.error("创建订单失败，用户ID：{}，异常信息：{}", userId, e.getMessage()));
    }
}
