package com.kingfish.webflux.interfaces;

import com.kingfish.webflux.application.ProductService;
import com.kingfish.webflux.domain.model.vo.CartItemVO;
import com.kingfish.webflux.domain.model.vo.CartSettlementVO;
import com.kingfish.webflux.domain.model.vo.Result;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

import java.util.List;

/**
 * 商品/购物车接口
 */
@RestController
@RequestMapping("/product")
public class ProductController {

    @Resource
    private ProductService productService;

    @GetMapping("/cart/{userId}")
    public Mono<Result<List<CartItemVO>>> getCartWithProductInfo(@PathVariable Long userId) {
        return productService.getCartWithProductInfo(userId).map(Result::success);
    }

    @GetMapping("/cart/{userId}/settlement")
    public Mono<Result<CartSettlementVO>> getCartSettlement(@PathVariable Long userId) {
        return productService.getCartSettlement(userId).map(Result::success);
    }
}
