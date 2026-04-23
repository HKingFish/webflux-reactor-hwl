package com.kingfish.webflux.domain.model.aggregate;

import com.kingfish.webflux.domain.model.entity.OrderDO;
import com.kingfish.webflux.domain.model.entity.UserDO;
import com.kingfish.webflux.domain.model.entity.UserExtDO;
import lombok.Data;

import java.util.List;

/**
 * @Author : haowl
 * @Date : 2026/4/12 18:17
 * @Desc : 用户聚合根
 */
@Data
public class User {
    private UserDO user;

    private UserExtDO userExt;

    private List<OrderDO> orders;
}