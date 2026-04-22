package com.kingfish.webflux.infrastructure.mapper;

import cn.hutool.core.collection.CollUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.kingfish.webflux.domain.model.entity.UserAddressDO;
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
public interface UserAddressMapper extends BaseMapper<UserAddressDO> {


    default Mono<UserAddressDO> getDefaultAddress(Long userId) {
        return getAddress(userId)
                .flatMap(list -> {
                    if (CollUtil.isEmpty(list)) {
                        return Mono.empty();
                    }
                    return Mono.just(list.stream()
                            .filter(u -> u.getIsDefault() == 1)
                            .findFirst()
                            .orElseGet(() -> list.get(0)));
                });
    }


    default Mono<List<UserAddressDO>> getAddress(Long userId) {
        return Mono.fromCallable(() ->
                selectList(new LambdaQueryWrapper<UserAddressDO>()
                        .eq(UserAddressDO::getUserId, userId)))
                .subscribeOn(Schedulers.boundedElastic());
    }

}
