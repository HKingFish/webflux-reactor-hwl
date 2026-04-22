package com.kingfish.webflux.interfaces;

import com.kingfish.webflux.application.UserService;
import com.kingfish.webflux.domain.model.vo.Result;
import com.kingfish.webflux.domain.model.vo.UserExportVO;
import com.kingfish.webflux.domain.model.vo.UserHomeVO;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

import java.util.List;

/**
 * 用户接口
 */
@RestController
@RequestMapping("/user")
public class UserController {

    @Resource
    private UserService userService;

    @GetMapping("/{userId}/home")
    public Mono<Result<UserHomeVO>> getUserHomePage(@PathVariable Long userId) {
        return userService.getUserHomePage(userId).map(Result::success);
    }

    @GetMapping("/export")
    public Mono<Result<List<UserExportVO>>> batchExportUsers(@RequestParam List<Long> userIds) {
        return userService.batchExportUsers(userIds).map(Result::success);
    }
}
