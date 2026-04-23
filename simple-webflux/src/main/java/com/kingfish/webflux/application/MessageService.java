package com.kingfish.webflux.application;

import com.kingfish.webflux.domain.model.entity.UserMessageDO;
import com.kingfish.webflux.infrastructure.mapper.UserMessageMapper;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.util.List;

/**
 * 消息服务
 *
 * @Author : haowl
 * @Date : 2026/4/18
 * @Desc :
 */
@Slf4j
@Service
public class MessageService {

    @Resource
    private UserMessageMapper userMessageMapper;

    /**
     * 查询用户未读消息数量（角标展示，容错接口）
     * <p>
     * 场景：个人中心首页消息角标，查询异常时不能影响整个页面渲染
     * <p>
     * 查询用户未读消息列表，返回数量（Long 类型）
     * 查询异常时返回 0L，不影响调用方，并打印 warn 日志
     *
     * @param userId 用户ID
     * @return 未读消息数量
     */
    public Mono<Long> getUnreadCount(Long userId) {
        return userMessageMapper.findUnreadByUserId(userId)
                // 非阻塞操作，应该不需要使用 flatmap
                .map(us -> (long) us.size())
                .onErrorResume(throwable -> {
                    log.warn("查询用户未读消息数量异常，userId: {}, 异常信息: {}", userId, throwable.getMessage());
                    return Mono.just(0L);
                });
    }

    /**
     * 一键已读（消息中心接口）
     * <p>
     * 场景：用户在消息中心点击「全部已读」按钮
     * <p>
     * 查询用户所有未读消息，如果有未读消息则提取所有 msgId 批量标记为已读
     * 返回标记前的未读消息列表（前端需要展示这些消息内容）
     * 没有未读消息时返回空列表
     *
     * @param userId 用户ID
     * @return 未读消息列表（已触发标记已读操作）
     */
    public Mono<List<UserMessageDO>> readAllUnreadMessages(Long userId) {
//        return userMessageMapper.findUnreadByUserId(userId)
//                .flatMap(us -> Mono.defer(() -> {
//                     List<Long> unReadMsgIds = us.stream()
//                             .map(UserMessageDO::getMsgId)
//                             .toList();
//                     // TODO : 没有没订阅，不会执行的
//                     userMessageMapper.batchMarkAsRead(unReadMsgIds);
//                     return Mono.just(us);
//                 }))
//                .switchIfEmpty(Mono.just(List.of()));


        return userMessageMapper.findUnreadByUserId(userId)
                .flatMap(us -> {
                    if (us.isEmpty()) {
                        return Mono.just(us);
                    }
                    List<Long> unReadMsgIds = us.stream()
                            .map(UserMessageDO::getMsgId)
                            .toList();
                    return userMessageMapper.batchMarkAsRead(unReadMsgIds)
                            .thenReturn(us);
                })
                .defaultIfEmpty(List.of());



    }
}
