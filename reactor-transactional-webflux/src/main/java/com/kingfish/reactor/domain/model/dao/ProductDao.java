package com.kingfish.reactor.domain.model.dao;

import com.kingfish.reactor.domain.model.entity.ProductDO;
import org.jooq.Field;
import org.jooq.Table;
import org.jooq.impl.DSL;
import org.jooq.impl.SQLDataType;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.Function;

/**
 * 商品数据访问层（jOOQ + R2DBC）
 */
@Component
public class ProductDao extends ReactiveBaseDao<ProductDO> {

    public static final Table<?> TABLE = DSL.table("product");
    public static final Field<Long> ID = DSL.field("id", SQLDataType.BIGINT);
    public static final Field<Long> PRODUCT_ID = DSL.field("product_id", SQLDataType.BIGINT);
    public static final Field<String> PRODUCT_NAME =
            DSL.field("product_name", SQLDataType.VARCHAR(128));
    public static final Field<BigDecimal> PRICE =
            DSL.field("price", SQLDataType.DECIMAL(10, 2));
    public static final Field<Integer> STOCK = DSL.field("stock", SQLDataType.INTEGER);
    public static final Field<Integer> STATUS = DSL.field("status", SQLDataType.INTEGER);

    @Override
    protected Table<?> getTable() {
        return TABLE;
    }

    @Override
    protected Field<Long> getIdField() {
        return ID;
    }

    @Override
    protected Function<Map<String, Object>, ProductDO> getRowMapper() {
        return row -> {
            ProductDO product = new ProductDO();
            product.setId((Long) row.get("id"));
            product.setProductId((Long) row.get("product_id"));
            product.setProductName((String) row.get("product_name"));
            product.setPrice((BigDecimal) row.get("price"));
            product.setStock((Integer) row.get("stock"));
            product.setStatus((Integer) row.get("status"));
            product.setCreateTime((LocalDateTime) row.get("create_time"));
            product.setUpdateTime((LocalDateTime) row.get("update_time"));
            product.setDeleted((Integer) row.get("deleted"));
            return product;
        };
    }

    /**
     * 根据商品ID查询商品
     *
     * @param productId 商品ID
     * @return 商品信息
     */
    public Mono<ProductDO> findByProductId(Long productId) {
        return findOne(
                PRODUCT_ID.eq(DSL.param("productId", Long.class)),
                Map.of("productId", productId)
        );
    }

    /**
     * 扣减库存（乐观方式，仅库存充足时扣减）
     * <p>
     * 使用 SQL 表达式 stock = stock - :quantity，
     * 通过 {@link ReactiveBaseDao#updateBySql} 实现，避免 R2dbcEntityTemplate 不支持表达式的问题。
     *
     * @param productId 商品ID
     * @param quantity  扣减数量
     * @return 影响行数（0 表示库存不足或商品不存在）
     */
    public Mono<Long> deductStock(Long productId, Integer quantity) {
        // 使用 renderInlined 将条件内联，SET 子句使用命名参数
        String setSql = "stock = stock - :quantity";
        // 条件：product_id = productId AND stock >= quantity（内联渲染，无需绑定）
        org.jooq.Condition condition = PRODUCT_ID.eq(productId).and(STOCK.greaterOrEqual(quantity));

        return updateBySql(setSql, condition, Map.of("quantity", quantity));
    }
}
