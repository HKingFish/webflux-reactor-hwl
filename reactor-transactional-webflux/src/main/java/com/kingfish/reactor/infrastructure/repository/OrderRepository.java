package com.kingfish.reactor.infrastructure.repository;

import com.kingfish.reactor.domain.model.entity.OrderDO;
import org.springframework.data.r2dbc.repository.R2dbcRepository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * 订单响应式仓库（R2DBC）
 * <p>
 * 对应原 transactional-webflux 模块中的 OrderMapper，
 * 基于 Spring Data R2DBC 实现，所有方法原生返回响应式类型，无需手动包装。
 */
public interface OrderRepository extends R2dbcRepository<OrderDO, Long> {

    /**
     * 根据用户ID查询订单列表
     *
     * @param userId 用户ID
     * @return 订单列表
     */
    Flux<OrderDO> findByUserId(Long userId);

    /**
     * 根据订单ID查询订单
     *
     * @param orderId 订单ID
     * @return 订单信息
     */
    Mono<OrderDO> findByOrderId(Long orderId);
}
