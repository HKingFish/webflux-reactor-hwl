package com.kingfish.webflux.domain.model.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import java.math.BigDecimal;

/**
 * 订单主表
 */
@Accessors(chain = true)
@NoArgsConstructor
@AllArgsConstructor
@Data
@TableName("`order`")
public class OrderDO extends BaseDO {

    /**
     * 订单ID
     */
    private Long orderId;

    /**
     * 用户ID
     */
    private Long userId;

    /**
     * 商品ID
     */
    private Long productId;

    /**
     * 商品数量
     */
    private Integer quantity;

    /**
     * 订单编号
     */
    private String orderNo;

    /**
     * 订单总金额
     */
    private BigDecimal totalAmount;

    /**
     * 支付状态
     * 0-未支付 1-已支付
     */
    private Integer payStatus;

    /**
     * 订单状态
     * 0-待付款 1-待发货 2-待收货 3-已完成 4-已取消
     */
    private Integer orderStatus;
}