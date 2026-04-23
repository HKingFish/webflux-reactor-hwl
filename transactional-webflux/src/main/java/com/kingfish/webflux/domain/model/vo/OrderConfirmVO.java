package com.kingfish.webflux.domain.model.vo;

import com.kingfish.webflux.domain.model.entity.CouponDO;
import com.kingfish.webflux.domain.model.entity.ProductDO;
import com.kingfish.webflux.domain.model.entity.UserAddressDO;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

/**
 * 订单确认页 VO
 * 用户从商品详情页点击「立即购买」后进入的确认页面数据
 */
@AllArgsConstructor
@NoArgsConstructor
@Data
public class OrderConfirmVO {

    /**
     * 商品信息
     */
    private ProductDO product;

    /**
     * 购买数量
     */
    private Integer quantity;

    /**
     * 商品金额（单价 × 数量）
     */
    private BigDecimal productAmount;

    /**
     * 会员折扣率
     */
    private BigDecimal memberDiscount;

    /**
     * 会员折扣后金额
     */
    private BigDecimal discountAmount;

    /**
     * 可用优惠券列表（满足门槛的）
     */
    private List<CouponDO> availableCoupons;

    /**
     * 默认收货地址
     */
    private UserAddressDO defaultAddress;

    /**
     * 用户手机号（脱敏，如 138****1001）
     */
    private String maskedPhone;
}
