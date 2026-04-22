package com.kingfish.webflux.domain.model.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;

/**
 * 购物车表
 */
@Data
@TableName("cart")
public class CartDO extends BaseDO {

    /**
     * 购物车项ID
     */
    private Long cartId;

    /**
     * 用户ID
     */
    private Long userId;

    /**
     * 商品ID
     */
    private Long productId;

    /**
     * 购买数量
     */
    private Integer quantity;

    /**
     * 加入购物车时的单价
     */
    private BigDecimal price;
}