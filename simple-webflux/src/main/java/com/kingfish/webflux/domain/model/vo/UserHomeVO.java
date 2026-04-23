package com.kingfish.webflux.domain.model.vo;

import com.kingfish.webflux.domain.model.entity.OrderDO;
import com.kingfish.webflux.domain.model.entity.UserAddressDO;
import com.kingfish.webflux.domain.model.entity.UserDO;
import com.kingfish.webflux.domain.model.entity.UserExtDO;
import lombok.Data;

/**
 * 用户个人中心首页 VO
 * 聚合用户基本信息、扩展信息、会员、订单统计、消息、地址等
 */
@Data
public class UserHomeVO {

    /**
     * 用户基本信息
     */
    private UserDO user;

    /**
     * 用户扩展信息
     */
    private UserExtDO userExt;

    /**
     * 会员等级信息
     */
    private MemberLevelVO memberLevel;

    /**
     * 待付款订单数
     */
    private Long pendingPaymentCount;

    /**
     * 待收货订单数
     */
    private Long pendingReceiveCount;

    /**
     * 最新一笔订单（可能为空）
     */
    private OrderDO latestOrder;

    /**
     * 未读消息数
     */
    private Long unreadMessageCount;

    /**
     * 默认收货地址（可能为空）
     */
    private UserAddressDO defaultAddress;
}
