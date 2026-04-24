package com.kingfish.reactor.domain.model.entity;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 商品表（R2DBC 实体）
 */
@Accessors(chain = true)
@Data
@Table("product")
public class ProductDO {

    /**
     * 主键ID
     */
    @Id
    private Long id;

    /**
     * 商品ID
     */
    @Column("product_id")
    private Long productId;

    /**
     * 商品名称
     */
    @Column("product_name")
    private String productName;

    /**
     * 销售价格
     */
    private BigDecimal price;

    /**
     * 库存数量
     */
    private Integer stock;

    /**
     * 商品状态
     * 0-下架 1-上架
     */
    private Integer status;

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
