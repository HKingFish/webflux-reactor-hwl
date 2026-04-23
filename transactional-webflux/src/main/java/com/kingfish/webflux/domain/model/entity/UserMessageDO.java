package com.kingfish.webflux.domain.model.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

/**
 * 用户消息表
 */
@Data
@TableName("user_message")
public class UserMessageDO extends BaseDO {

    /**
     * 消息ID
     */
    private Long msgId;

    /**
     * 用户ID
     */
    private Long userId;

    /**
     * 消息标题
     */
    private String title;

    /**
     * 消息内容
     */
    private String content;

    /**
     * 是否已读
     * 0-未读 1-已读
     */
    private Integer isRead;
}