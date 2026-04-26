package com.kingfish.reactor.interfaces;

import com.kingfish.reactor.application.JooqService;
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

    @Resource
    private JooqService jooqService;

    /**
     * 创建订单接口（Spring Data R2DBC Repository 版本）
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
     * 创建订单接口（jOOQ 基类版本，演示 ReactiveBaseDao + jOOQ DSL）
     *
     * @param productId 商品ID
     * @param quantity  购买数量
     * @return 统一响应结果
     */
    @PostMapping("/createOrderByJooq")
    public Mono<Result<OrderDO>> createOrderByJooq(@RequestParam Long productId,
                                                   @RequestParam Integer quantity) {
        return jooqService.createOrder(productId, quantity)
                .map(Result::success)
                .onErrorResume(e -> Mono.just(Result.fail(e.getMessage())));
    }
}
