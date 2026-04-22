package com.kingfish.webflux.infrastructure.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.kingfish.webflux.domain.model.entity.UserDO;
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
public interface UserMapper extends BaseMapper<UserDO> {

    /**
     * 根据 ID 查询用户
     *
     * @param id
     * @return
     */
    default Mono<UserDO> findById(Long id) {
        return Mono.fromCallable(() -> selectById(id))
                // 将阻塞操作放入 弹性线程池
                .subscribeOn(Schedulers.boundedElastic());
    }


    /**
     * 批量根据用户 ID 查询用户
     *
     * @param ids
     * @return
     */
    default Mono<List<UserDO>> batchFindByIds(List<Long> ids) {
        return Mono.fromCallable(() -> selectBatchIds(ids))
                // 将阻塞操作放入 弹性线程池
                .subscribeOn(Schedulers.boundedElastic());
    }

    default Mono<UserDO> findValidUser(Long userId) {
        return findById(userId)
                .switchIfEmpty(Mono.error(new IllegalArgumentException("用户不存在")))
                .filter(userDO -> userDO.getStatus() == 1)
                .switchIfEmpty(Mono.error(new IllegalStateException("用户状态异常")));
    }
}
