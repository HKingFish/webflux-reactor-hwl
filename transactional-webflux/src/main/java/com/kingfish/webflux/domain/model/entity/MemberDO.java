package com.kingfish.webflux.domain.model.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 会员信息表
 */
@Data
@TableName("member")
public class MemberDO extends BaseDO {

    /**
     * 用户ID
     */
    private Long userId;

    /**
     * 会员等级
     */
    private Integer level;

    /**
     * 折扣率（如 0.9 表示9折）
     */
    private BigDecimal discount;

    /**
     * 总积分
     */
    private Long totalPoints;

    /**
     * 会员过期时间
     */
    private LocalDateTime expireTime;
}