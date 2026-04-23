package com.kingfish.webflux.domain.model.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

/**
 * 订单物流信息表
 */
@Data
@TableName("order_logistics")
public class OrderLogisticsDO extends BaseDO {

    /**
     * 物流ID
     */
    private Long logisticsId;

    /**
     * 订单ID
     */
    private Long orderId;

    /**
     * 物流公司
     */
    private String company;

    /**
     * 物流单号
     */
    private String logisticsNo;

    /**
     * 物流状态
     */
    private String status;
}