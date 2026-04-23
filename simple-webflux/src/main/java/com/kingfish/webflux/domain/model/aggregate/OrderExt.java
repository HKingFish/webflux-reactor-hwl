package com.kingfish.webflux.domain.model.aggregate;

import com.kingfish.webflux.domain.model.entity.OrderDO;
import com.kingfish.webflux.domain.model.entity.OrderLogisticsDO;
import com.kingfish.webflux.domain.model.entity.ProductDO;
import com.kingfish.webflux.domain.model.entity.UserDO;
import lombok.Data;

import java.util.List;

/**
 * 订单聚合对象
 * 聚合订单主表、物流、用户、商品等关联信息
 *
 * @Author : haowl
 * @Date : 2026/4/17 21:23
 * @Desc :
 */
@Data
public class OrderExt {

    /**
     * 订单主表信息
     */
    private OrderDO order;

    /**
     * 物流信息列表
     */
    private List<OrderLogisticsDO> orderLogistics;

    /**
     * 下单用户信息（脱敏后）
     */
    private UserDO user;

    /**
     * 订单关联商品信息
     */
    private ProductDO product;

    public static OrderExt from(OrderDO order, List<OrderLogisticsDO> orderLogistics, UserDO user, ProductDO product) {
        OrderExt orderExt = new OrderExt();
        orderExt.setOrder(order);
        orderExt.setOrderLogistics(orderLogistics);
        orderExt.setUser(user);
        orderExt.setProduct(product);
        return orderExt;
    }
}