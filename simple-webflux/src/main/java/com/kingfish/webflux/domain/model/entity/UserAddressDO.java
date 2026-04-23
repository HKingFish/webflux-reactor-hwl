package com.kingfish.webflux.domain.model.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

/**
 * 用户收货地址表
 */
@Data
@TableName("user_address")
public class UserAddressDO extends BaseDO {

    /**
     * 地址ID
     */
    private Long addressId;

    /**
     * 用户ID
     */
    private Long userId;

    /**
     * 收件人姓名
     */
    private String receiver;

    /**
     * 收件人电话
     */
    private String phone;

    /**
     * 省份
     */
    private String province;

    /**
     * 城市
     */
    private String city;

    /**
     * 详细地址
     */
    private String detail;

    /**
     * 是否默认地址
     * 0-否 1-是
     */
    private Integer isDefault;
}