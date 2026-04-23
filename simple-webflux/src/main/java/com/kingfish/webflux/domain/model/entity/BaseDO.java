package com.kingfish.webflux.domain.model.entity;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 基础DO，抽取所有表的公共字段
 */
@Data
public class BaseDO {

    /**
     * 主键ID
     */
    private Long id;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    private LocalDateTime updateTime;

    /**
     * 删除标记 0-未删除 1-已删除（逻辑删除）
     */
    private Integer deleted;
}