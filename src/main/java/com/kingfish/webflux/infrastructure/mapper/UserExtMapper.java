package com.kingfish.webflux.infrastructure.mapper;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.kingfish.webflux.domain.model.entity.UserExtDO;
import org.apache.ibatis.annotations.Mapper;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.util.List;

/**
 * @author : wylon
 * @date : 2026/4/13 19:40
 * @desc :
 */
@Mapper
public interface UserExtMapper extends BaseMapper<UserExtDO> {

    default Mono<UserExtDO> findByUserId(Long userId){
        return Mono.fromCallable(() -> selectOne(new LambdaQueryWrapper<UserExtDO>()
                .eq(UserExtDO::getUserId, userId)))
                .subscribeOn(Schedulers.boundedElastic());
    }

    default Mono<List<UserExtDO>> findByUserIds(List<Long> userIds){
        return Mono.fromCallable(() -> selectList(new LambdaQueryWrapper<UserExtDO>()
                .in(UserExtDO::getUserId, userIds)))
                .subscribeOn(Schedulers.boundedElastic());
    }

}
