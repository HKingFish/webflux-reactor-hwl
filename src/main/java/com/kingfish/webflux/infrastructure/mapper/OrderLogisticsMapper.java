package com.kingfish.webflux.infrastructure.mapper;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.kingfish.webflux.domain.model.entity.OrderLogisticsDO;
import org.apache.ibatis.annotations.Mapper;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.util.List;

/**
 * @Author : haowl
 * @Date : 2026/4/17 21:21
 * @Desc :
 */
@Mapper
public interface OrderLogisticsMapper extends BaseMapper<OrderLogisticsDO> {

    default Mono<List<OrderLogisticsDO>> findByOrderId(Long orderId) {
        return Mono.fromCallable(() -> selectList(new LambdaQueryWrapper<OrderLogisticsDO>()
                .eq(OrderLogisticsDO::getOrderId, orderId))).subscribeOn(Schedulers.boundedElastic());
    }
}
