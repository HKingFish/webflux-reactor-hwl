package com.kingfish.webflux.infrastructure.mapper;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.kingfish.webflux.domain.model.entity.MemberDO;
import org.apache.ibatis.annotations.Mapper;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.util.List;

/**
 * @Author : haowl
 * @Date : 2026/4/17 21:38
 * @Desc :
 */
@Mapper
public interface MemberMapper extends BaseMapper<MemberDO> {


    default Mono<MemberDO> getMemberByUserId(Long userId) {
        return Mono.fromCallable(() -> selectOne(new LambdaQueryWrapper<MemberDO>()
                .eq(MemberDO::getUserId, userId)))
                .subscribeOn(Schedulers.boundedElastic());
    }

    default Mono<List<MemberDO>> getMemberByUserIds(List<Long> userIds) {
        return Mono.fromCallable(() -> selectBatchIds(userIds))
                .subscribeOn(Schedulers.boundedElastic());
    }

}