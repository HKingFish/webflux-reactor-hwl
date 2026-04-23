package com.kingfish.webflux.domain.model.vo;

import com.kingfish.webflux.domain.model.entity.CouponDO;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

/**
 * 购物车结算 VO
 * 聚合购物车、会员折扣、优惠券信息，计算最终金额
 */
@Data
public class CartSettlementVO {

    /**
     * 有效购物车项列表
     */
    private List<CartItemVO> cartItems;

    /**
     * 商品总金额（未优惠）
     */
    private BigDecimal totalAmount;

    /**
     * 会员折扣率
     */
    private BigDecimal memberDiscount;

    /**
     * 会员折扣后金额
     */
    private BigDecimal memberDiscountAmount;

    /**
     * 可用优惠券列表
     */
    private List<CouponDO> availableCoupons;

    /**
     * 最终应付金额（会员折扣后金额，优惠券由用户选择，此处不自动抵扣）
     */
    private BigDecimal finalAmount;
}
