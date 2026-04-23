package com.kingfish.webflux.domain.model.vo;

import lombok.Data;

import java.math.BigDecimal;

/**
 * 用户导出 VO
 * 管理后台批量导出用户数据时使用，聚合多张表信息
 */
@Data
public class UserExportVO {

    /**
     * 用户ID
     */
    private Long userId;

    /**
     * 用户名
     */
    private String username;

    /**
     * 真实姓名
     */
    private String realName;

    /**
     * 手机号
     */
    private String phone;

    /**
     * 会员等级
     */
    private Integer memberLevel;

    /**
     * 累计消费金额
     */
    private BigDecimal totalSpent;

    /**
     * 订单总数
     */
    private Integer orderCount;

    /**
     * 用户状态描述
     */
    private String statusDesc;
}
