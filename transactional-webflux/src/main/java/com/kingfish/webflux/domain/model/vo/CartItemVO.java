package com.kingfish.webflux.domain.model.vo;

import lombok.Data;

import java.math.BigDecimal;

/**
 * 购物车项 VO（含商品详情）
 */
@Data
public class CartItemVO {

    /**
     * 购物车项ID
     */
    private Long cartId;

    /**
     * 商品ID
     */
    private Long productId;

    /**
     * 商品名称
     */
    private String productName;

    /**
     * 商品单价（最新价格）
     */
    private BigDecimal price;

    /**
     * 购买数量
     */
    private Integer quantity;

    /**
     * 小计金额（单价 × 数量）
     */
    private BigDecimal subtotal;

    /**
     * 是否有效（商品上架且库存充足）
     */
    private Boolean valid;

    /**
     * 无效原因（商品下架/库存不足等）
     */
    private String invalidReason;
}
