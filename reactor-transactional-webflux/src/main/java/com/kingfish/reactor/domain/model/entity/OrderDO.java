package com.kingfish.reactor.domain.model.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 订单主表（R2DBC 实体）
 */
@Accessors(chain = true)
@NoArgsConstructor
@AllArgsConstructor
@Data
@Table("`order`")
public class OrderDO {

    /**
     * 主键ID
     */
    @Id
    private Long id;

    /**
     * 订单ID
     */
    @Column("order_id")
    private Long orderId;

    /**
     * 用户ID
     */
    @Column("user_id")
    private Long userId;

    /**
     * 商品ID
     */
    @Column("product_id")
    private Long productId;

    /**
     * 商品数量
     */
    private Integer quantity;

    /**
     * 订单编号
     */
    @Column("order_no")
    private String orderNo;

    /**
     * 订单总金额
     */
    @Column("total_amount")
    private BigDecimal totalAmount;

    /**
     * 支付状态
     * 0-未支付 1-已支付
     */
    @Column("pay_status")
    private Integer payStatus;

    /**
     * 订单状态
     * 0-待付款 1-待发货 2-待收货 3-已完成 4-已取消
     */
    @Column("order_status")
    private Integer orderStatus;

    /**
     * 创建时间
     */
    @Column("create_time")
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    @Column("update_time")
    private LocalDateTime updateTime;

    /**
     * 删除标记 0-未删除 1-已删除（逻辑删除）
     */
    private Integer deleted;
}
