package com.kingfish.webflux.interfaces;

import com.kingfish.webflux.application.MessageService;
import com.kingfish.webflux.domain.model.entity.UserMessageDO;
import com.kingfish.webflux.domain.model.vo.Result;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

import java.util.List;

/**
 * 消息接口
 */
@RestController
@RequestMapping("/message")
public class MessageController {

    @Resource
    private MessageService messageService;

    @GetMapping("/{userId}/unread-count")
    public Mono<Result<Long>> getUnreadCount(@PathVariable Long userId) {
        return messageService.getUnreadCount(userId).map(Result::success);
    }

    @PostMapping("/{userId}/read-all")
    public Mono<Result<List<UserMessageDO>>> readAllUnreadMessages(@PathVariable Long userId) {
        return messageService.readAllUnreadMessages(userId).map(Result::success);
    }
}
