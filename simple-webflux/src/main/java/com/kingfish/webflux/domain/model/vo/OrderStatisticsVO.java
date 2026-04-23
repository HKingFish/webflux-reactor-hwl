package com.kingfish.webflux.domain.model.vo;

import lombok.Data;

/**
 * 订单统计 VO
 * 用于返回用户待付款/待收货订单数量统计
 */
@Data
public class OrderStatisticsVO {

    /**
     * 待付款订单数量
     */
    private Long pendingPaymentCount;

    /**
     * 待收货订单数量
     */
    private Long pendingReceiveCount;
}
