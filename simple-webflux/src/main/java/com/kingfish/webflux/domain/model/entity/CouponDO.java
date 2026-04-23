package com.kingfish.webflux.domain.model.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 用户优惠券表
 */
@Data
@TableName("coupon")
public class CouponDO extends BaseDO {

    /**
     * 优惠券ID
     */
    private Long couponId;

    /**
     * 用户ID
     */
    private Long userId;

    /**
     * 使用门槛（满多少可用）
     */
    private BigDecimal threshold;

    /**
     * 优惠金额
     */
    private BigDecimal discount;

    /**
     * 状态
     * 0-未使用 1-已使用 2-已过期
     */
    private Integer status;

    /**
     * 过期时间
     */
    private LocalDateTime expireTime;
}