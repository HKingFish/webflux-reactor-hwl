package com.kingfish.webflux.infrastructure.mapper;

import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.kingfish.webflux.domain.model.entity.ProductDO;
import org.apache.ibatis.annotations.Mapper;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

/**
 * @Author : haowl
 * @Date : 2026/4/18 10:10
 * @Desc :
 */
@Mapper
public interface ProductMapper extends BaseMapper<ProductDO> {

    default Mono<ProductDO> getProductById(Long productId) {
        return Mono.fromCallable(() -> selectById(productId))
                .subscribeOn(Schedulers.boundedElastic());
    }

    /**
     * 扣减库存（乐观方式，仅库存充足时扣减）
     *
     * @param productId 商品ID
     * @param quantity  扣减数量
     * @return 影响行数（0表示扣减失败）
     */
    default Mono<Integer> deductStock(Long productId, Integer quantity) {
        return Mono.fromCallable(() -> {
                    LambdaUpdateWrapper<ProductDO> wrapper = new LambdaUpdateWrapper<>();
                    wrapper.eq(ProductDO::getProductId, productId)
                            .ge(ProductDO::getStock, quantity)
                            .setSql("stock = stock - " + quantity);
                    return update(null, wrapper);
                })
                .filter(i -> i > 0)
                .switchIfEmpty(Mono.error(new IllegalArgumentException("扣减库存失败")))
                .subscribeOn(Schedulers.boundedElastic());
    }

    default Mono<ProductDO> findValidProduct(Long productId, Integer quantity) {
       return getProductById(productId)
                .switchIfEmpty(Mono.error(new IllegalArgumentException("商品不存在")))
                .filter(p -> p.getStatus() == 1)
                .switchIfEmpty(Mono.error(new IllegalArgumentException("商品不存在或已下架")))
                .filter(p -> p.getStock() >= quantity)
                .switchIfEmpty(Mono.error(new IllegalArgumentException("库存不足")));
    }
}
