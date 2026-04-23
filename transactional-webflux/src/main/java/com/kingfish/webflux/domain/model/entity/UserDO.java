package com.kingfish.webflux.domain.model.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

/**
 * 用户基础信息表
 */
@Data
@TableName("user")
public class UserDO extends BaseDO {

    /**
     * 登录账号（用户名/手机号）
     */
    private String username;

    /**
     * 加密后密码
     */
    private String password;

    /**
     * 用户状态
     * 0-禁用 1-正常
     */
    private Integer status;
}