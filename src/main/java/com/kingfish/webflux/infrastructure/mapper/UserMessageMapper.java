package com.kingfish.webflux.infrastructure.mapper;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.kingfish.webflux.domain.model.entity.UserMessageDO;
import org.apache.ibatis.annotations.Mapper;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.util.List;

/**
 * 用户消息 Mapper
 *
 * @Author : haowl
 * @Date : 2026/4/18
 * @Desc : 用户消息数据访问层
 */
@Mapper
public interface UserMessageMapper extends BaseMapper<UserMessageDO> {

    /**
     * 根据用户ID查询所有消息
     *
     * @param userId 用户ID
     * @return 消息列表
     */
    default Mono<List<UserMessageDO>> findByUserId(Long userId) {
        return Mono.fromCallable(() -> selectList(new LambdaQueryWrapper<UserMessageDO>()
                        .eq(UserMessageDO::getUserId, userId)))
                .subscribeOn(Schedulers.boundedElastic());
    }

    /**
     * 根据用户ID查询未读消息列表
     *
     * @param userId 用户ID
     * @return 未读消息列表
     */
    default Mono<List<UserMessageDO>> findUnreadByUserId(Long userId) {
        return Mono.fromCallable(() -> selectList(new LambdaQueryWrapper<UserMessageDO>()
                        .eq(UserMessageDO::getUserId, userId)
                        .eq(UserMessageDO::getIsRead, 0)))
                .subscribeOn(Schedulers.boundedElastic());
    }

    /**
     * 批量标记消息为已读
     *
     * @param messageIds 消息ID列表
     * @return 影响行数
     */
    default Mono<Integer> batchMarkAsRead(List<Long> messageIds) {
        return Mono.fromCallable(() -> {
                    LambdaUpdateWrapper<UserMessageDO> wrapper = new LambdaUpdateWrapper<>();
                    wrapper.in(UserMessageDO::getMsgId, messageIds)
                            .set(UserMessageDO::getIsRead, 1);
                    return update(null, wrapper);
                })
                .subscribeOn(Schedulers.boundedElastic());
    }
}
