package com.kingfish.webflux.application;

import com.kingfish.webflux.domain.model.entity.CartDO;
import com.kingfish.webflux.domain.model.entity.CouponDO;
import com.kingfish.webflux.domain.model.entity.ProductDO;
import com.kingfish.webflux.domain.model.vo.CartItemVO;
import com.kingfish.webflux.domain.model.vo.CartSettlementVO;
import com.kingfish.webflux.domain.model.vo.MemberLevelVO;
import com.kingfish.webflux.infrastructure.mapper.CartMapper;
import com.kingfish.webflux.infrastructure.mapper.CouponMapper;
import com.kingfish.webflux.infrastructure.mapper.ProductMapper;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;

/**
 * 商品/购物车服务
 *
 * @Author : haowl
 * @Date : 2026/4/13 22:03
 * @Desc :
 */
@Slf4j
@Service
public class ProductService {

    @Resource
    private ProductMapper productMapper;

    @Resource
    private CartMapper cartMapper;

    @Resource
    private CouponMapper couponMapper;

    @Resource
    private MemberService memberService;

    /**
     * 购物车列表接口（含商品实时校验）
     * <p>
     * 场景：用户打开购物车页面，需要展示每个商品的最新状态
     * 商品可能已下架、价格可能已变动、库存可能不足
     * <p>
     * 查询用户购物车列表，对每个购物车项查询对应商品的最新信息：
     * - 商品存在、已上架(status=1)、库存充足(stock >= 购买数量)：
     * 标记 valid=true，使用商品最新价格计算小计(subtotal = price × quantity)
     * - 商品不存在或已下架：标记 valid=false，invalidReason="商品已下架"
     * - 库存不足：标记 valid=false，invalidReason="库存不足"
     * <p>
     * 单个商品查询失败不影响其他商品，失败的标记为无效并记录日志
     *
     * @param userId 用户ID
     * @return 购物车项列表（含商品实时状态）
     */
    public Mono<List<CartItemVO>> getCartWithProductInfo(Long userId) {
        // TODO : 这种场景可以转换为 Flux 来处理
        return cartMapper.findByUserId(userId)
                .flatMapMany(Flux::fromIterable)
                .flatMap(cart -> productMapper.getProductById(cart.getProductId())
                        .map(product -> buildCartItemVO(cart, product))
                        .defaultIfEmpty(buildInvalidCartItemVO(cart, "商品已下架"))
                        .onErrorResume(e -> {
                            log.warn("查询商品信息失败，商品ID：{}，异常信息：{}", cart.getProductId(), e.getMessage());
                            return Mono.just(buildInvalidCartItemVO(cart, "商品信息查询失败"));
                        }))
                .collectList();
    }

    /**
     * 根据商品状态和库存构建购物车项 VO
     */
    private CartItemVO buildCartItemVO(CartDO cart, ProductDO product) {
        CartItemVO vo = new CartItemVO();
        vo.setCartId(cart.getCartId());
        vo.setProductId(cart.getProductId());
        vo.setProductName(product.getProductName());
        vo.setPrice(product.getPrice());
        vo.setQuantity(cart.getQuantity());

        if (product.getStatus() != 1) {
            vo.setValid(false);
            vo.setInvalidReason("商品已下架");
        } else if (product.getStock() < cart.getQuantity()) {
            vo.setValid(false);
            vo.setInvalidReason("库存不足");
        } else {
            vo.setValid(true);
            vo.setSubtotal(product.getPrice().multiply(BigDecimal.valueOf(cart.getQuantity())));
        }
        return vo;
    }

    /**
     * 构建无效购物车项 VO（商品查不到或查询异常时使用）
     */
    private CartItemVO buildInvalidCartItemVO(CartDO cart, String reason) {
        CartItemVO vo = new CartItemVO();
        vo.setCartId(cart.getCartId());
        vo.setProductId(cart.getProductId());
        vo.setQuantity(cart.getQuantity());
        vo.setValid(false);
        vo.setInvalidReason(reason);
        return vo;
    }

    /**
     * 购物车结算页数据聚合接口
     * <p>
     * 场景：用户在购物车页面点击「去结算」，进入结算页
     * 需要一次性聚合购物车商品、会员折扣、可用优惠券，并计算最终金额
     * <p>
     * 并行查询以下数据：
     * - 购物车项列表（调用 getCartWithProductInfo，含商品实时校验）
     * - 用户会员信息（调用 memberService.getMemberLevel，超时降级）
     * - 可用优惠券列表（未使用、未过期、未删除，按优惠金额倒序）
     * <p>
     * 聚合计算：
     * - 商品总金额 = 有效购物车项的 subtotal 求和
     * - 会员折扣后金额 = 总金额 × 会员折扣率
     * - 最终应付金额 = 会员折扣后金额（优惠券由用户手动选择，此处不自动抵扣）
     * <p>
     * 组装成 CartSettlementVO 返回
     *
     * @param userId 用户ID
     * @return 购物车结算页数据
     */
    public Mono<CartSettlementVO> getCartSettlement(Long userId) {
        Mono<List<CartItemVO>> cardMono = getCartWithProductInfo(userId);
        Mono<MemberLevelVO> memberLevelMono = memberService.getMemberLevel(userId);
        Mono<List<CouponDO>> couponMono = couponMapper.findByUserId(userId)
                .map(coupons -> coupons.stream()
                        .filter(c -> c.getStatus() == 0
                                && c.getExpireTime().isAfter(LocalDateTime.now())
                                && c.getDeleted() == 0)
                        .sorted(Comparator.comparing(CouponDO::getDiscount).reversed())
                        .toList());

        return Mono.zip(cardMono, memberLevelMono, couponMono)
                .map(t -> {
                    List<CartItemVO> cartItems = t.getT1();
                    MemberLevelVO memberLevel = t.getT2();
                    List<CouponDO> coupons = t.getT3();

                    BigDecimal totalAmount = cartItems.stream()
                            .filter(CartItemVO::getValid)
                            .map(CartItemVO::getSubtotal)
                            .reduce(BigDecimal.ZERO, BigDecimal::add);

                    CartSettlementVO vo = new CartSettlementVO();
                    vo.setCartItems(cartItems);
                    vo.setTotalAmount(totalAmount);
                    vo.setMemberDiscount(memberLevel.getDiscount());
                    vo.setMemberDiscountAmount(totalAmount.multiply(memberLevel.getDiscount()));
                    vo.setAvailableCoupons(coupons);
                    vo.setFinalAmount(totalAmount.multiply(memberLevel.getDiscount()));
                    return vo;
                });


    }
}
