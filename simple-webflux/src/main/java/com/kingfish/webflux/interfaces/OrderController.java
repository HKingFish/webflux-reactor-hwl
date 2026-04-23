package com.kingfish.webflux.interfaces;

import com.kingfish.webflux.application.OrderService;
import com.kingfish.webflux.domain.model.aggregate.OrderExt;
import com.kingfish.webflux.domain.model.entity.OrderDO;
import com.kingfish.webflux.domain.model.vo.OrderConfirmVO;
import com.kingfish.webflux.domain.model.vo.Result;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

/**
 * 订单接口
 */
@RestController
@RequestMapping("/order")
public class OrderController {

    @Resource
    private OrderService orderService;

    @GetMapping("/{orderId}/detail")
    public Mono<Result<OrderExt>> getOrderFullDetail(@PathVariable Long orderId) {
        return orderService.getOrderFullDetail(orderId).map(Result::success);
    }

    @GetMapping("/confirm")
    public Mono<Result<OrderConfirmVO>> getOrderConfirmPage(
            @RequestParam Long userId,
            @RequestParam Long productId,
            @RequestParam Integer quantity) {
        return orderService.getOrderConfirmPage(userId, productId, quantity).map(Result::success);
    }

    @PostMapping("/create")
    public Mono<Result<OrderDO>> createOrder(
            @RequestParam Long userId,
            @RequestParam Long productId,
            @RequestParam Integer quantity) {
        return orderService.createOrder(userId, productId, quantity).map(Result::success);
    }
}
