package com.kingfish.webflux.infrastructure.mapper;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.kingfish.webflux.domain.model.entity.CartDO;
import org.apache.ibatis.annotations.Mapper;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.util.List;

/**
 * 购物车 Mapper
 *
 * @Author : haowl
 * @Date : 2026/4/18
 * @Desc : 购物车数据访问层
 */
@Mapper
public interface CartMapper extends BaseMapper<CartDO> {

    /**
     * 根据用户ID查询购物车列表
     *
     * @param userId 用户ID
     * @return 购物车列表
     */
    default Mono<List<CartDO>> findByUserId(Long userId) {
        return Mono.fromCallable(() -> selectList(new LambdaQueryWrapper<CartDO>()
                        .eq(CartDO::getUserId, userId)))
                .subscribeOn(Schedulers.boundedElastic());
    }

    /**
     * 根据用户ID删除购物车记录
     *
     * @param userId 用户ID
     * @return 删除的记录数
     */
    default Mono<Integer> deleteByUserId(Long userId) {
        return Mono.fromCallable(() -> delete(new LambdaQueryWrapper<CartDO>()
                        .eq(CartDO::getUserId, userId)))
                .subscribeOn(Schedulers.boundedElastic());
    }
}
