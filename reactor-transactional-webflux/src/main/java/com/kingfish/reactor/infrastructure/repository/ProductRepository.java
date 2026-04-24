package com.kingfish.reactor.infrastructure.repository;

import com.kingfish.reactor.domain.model.entity.ProductDO;
import jakarta.annotation.Resource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.r2dbc.core.R2dbcEntityTemplate;
import org.springframework.data.r2dbc.repository.Modifying;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.r2dbc.repository.R2dbcRepository;
import org.springframework.data.relational.core.query.Update;
import org.springframework.r2dbc.core.DatabaseClient;
import reactor.core.publisher.Mono;

import static org.springframework.data.relational.core.query.Criteria.where;
import static org.springframework.data.relational.core.query.Query.query;

/**
 * 商品响应式仓库（R2DBC）
 * <p>
 * 对应原 transactional-webflux 模块中的 ProductMapper，
 * 基于 Spring Data R2DBC 实现，所有方法原生返回响应式类型。
 */
public interface ProductRepository extends R2dbcRepository<ProductDO, Long> {

    /**
     * 根据商品ID查询商品
     *
     * @param productId 商品ID
     * @return 商品信息
     */
    Mono<ProductDO> findByProductId(Long productId);

    /**
     * 扣减库存（乐观方式，仅库存充足时扣减）
     * <p>
     * 通过 WHERE 条件 stock >= :quantity 保证不会超卖，
     * 返回影响行数，0 表示库存不足或商品不存在。
     *
     * @param productId 商品ID
     * @param quantity  扣减数量
     * @return 影响行数
     */
    @Modifying
    @Query("UPDATE product SET stock = stock - :quantity WHERE product_id = :productId AND stock >= :quantity")
    Mono<Long> deductStock(Long productId, Integer quantity);


    @Autowired
    DatabaseClient databaseClient;
    /**
     * 查询有效商品（状态为上架且库存充足）
     * <p>
     * 等价于原 ProductMapper#findValidProduct，
     * 校验逻辑建议在 Service 层通过 Reactor 操作符实现，此处仅提供基础查询。
     *
     * @param productId 商品ID
     * @param status    商品状态
     * @return 商品信息
     */
    Mono<ProductDO> findByProductIdAndStatus(Long productId, Integer status);
}
