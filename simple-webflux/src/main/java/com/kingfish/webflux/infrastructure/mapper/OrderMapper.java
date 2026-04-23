package com.kingfish.webflux.infrastructure.mapper;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.kingfish.webflux.domain.model.entity.OrderDO;
import org.apache.ibatis.annotations.Mapper;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.util.List;

/**
 * @Author : haowl
 * @Date : 2026/4/12 11:49
 * @Desc :
 */
@Mapper
public interface OrderMapper extends BaseMapper<OrderDO> {

    /**
     * 根据 ID 查询用户
     *
     * @param userId
     * @return
     */
    default Mono<List<OrderDO>> findByUserId(Long userId) {
        return Mono.fromCallable(() -> selectList(new LambdaQueryWrapper<OrderDO>()
                        .eq(OrderDO::getUserId, userId)))
                .subscribeOn(Schedulers.boundedElastic());
    }


    default Mono<OrderDO> findById(Long orderId) {
        return Mono.fromCallable(() -> selectById(orderId))
                .subscribeOn(Schedulers.boundedElastic());
    }


    default Mono<Long> create(OrderDO orderDO) {
        return Mono.fromCallable(() -> {
                    insert(orderDO);
                    return orderDO.getId();
                })
                .subscribeOn(Schedulers.boundedElastic());
    }


}
