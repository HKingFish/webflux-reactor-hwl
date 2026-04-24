package com.kingfish.reactor.interfaces;

import com.kingfish.reactor.application.TransactionService;
import com.kingfish.reactor.domain.model.entity.OrderDO;
import com.kingfish.reactor.domain.model.vo.Result;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

/**
 * 事务验证控制器
 *
 * @Author : haowl
 * @Date : 2026/4/23 21:02
 */
@RestController
@RequestMapping("/trx")
public class TransactionController {

    @Resource
    private TransactionService transactionService;

    /**
     * 创建订单接口（用于验证 Reactor 场景下的事务行为）
     *
     * @param productId 商品ID
     * @param quantity  购买数量
     * @return 统一响应结果
     */
    @PostMapping("/createOrder")
    public Mono<Result<OrderDO>> createOrder(@RequestParam Long productId,
                                             @RequestParam Integer quantity) {
        return transactionService.createOrder(productId, quantity)
                .map(Result::success)
                .onErrorResume(e -> Mono.just(Result.fail(e.getMessage())));
    }

    /**
     * 创建订单接口（jOOQ 版本，演示 jOOQ DSL + R2DBC 配合使用）
     *
     * @param productId 商品ID
     * @param quantity  购买数量
     * @return 统一响应结果
     */
    @PostMapping("/createOrderWithJooq")
    public Mono<Result<OrderDO>> createOrderWithJooq(@RequestParam Long productId,
                                                     @RequestParam Integer quantity) {
        return transactionService.createOrderWithJooq(productId, quantity)
                .map(Result::success)
                .onErrorResume(e -> Mono.just(Result.fail(e.getMessage())));
    }
}
