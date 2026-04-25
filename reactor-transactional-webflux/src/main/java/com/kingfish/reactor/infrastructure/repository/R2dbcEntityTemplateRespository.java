package com.kingfish.reactor.infrastructure.repository;

import com.kingfish.reactor.domain.model.entity.OrderDO;
import com.kingfish.reactor.domain.model.entity.ProductDO;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.Resource;
import org.springframework.data.r2dbc.core.R2dbcEntityTemplate;
import org.springframework.data.relational.core.query.Criteria;
import org.springframework.data.relational.core.query.Query;
import org.springframework.r2dbc.core.DatabaseClient;
import org.springframework.stereotype.Component;
import org.springframework.transaction.support.TransactionTemplate;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;

import static org.springframework.data.relational.core.query.Criteria.where;

/**
 * @Author : haowl
 * @Date : 2026/4/25 21:40
 * @Desc : 针对 R2dbcEntityTemplate 的语法练习
 */
@Component
public class R2dbcEntityTemplateRespository {

    @Resource
    private R2dbcEntityTemplate template;

    @Resource
    private DatabaseClient databaseClient;

    /**
     * 根据条件动态查询商品
     */
    public Mono<ProductDO> findProduct(Long productId, Integer status) {
        return template.select(ProductDO.class)
                .matching(Query.query(where("product_id").is(productId)
                        .and("status").is(status)))
                .one();
    }


    @PostConstruct
    public void init() {
        System.out.println("R2dbcEntityTemplateRespository 初始化完成");
        findOrder()
                .subscribe(System.out::println);
    }

    /**
     * 查找订单，and or 语法
     *
     * @return
     */
    public Flux<OrderDO> findOrder() {
        // 待发货订单 且 一天内创建的订单
        Criteria criteria = where("createTime")
                .greaterThan(LocalDateTime.now().minusDays(1));

        Criteria group1 = where("order_status")
                .is(1);

        // 金额大于 100 元的订单 且 一天内创建的订单
        Criteria group2 = where("totalAmount")
                .greaterThan(100);
        // 执行的SQL 如下：SELECT `order`.* FROM `order` WHERE `order`.create_time > ? AND (`order`.order_status = ? OR (`order`.total_amount > ?))
        return template.select(OrderDO.class)
                .matching(
                        Query.query(criteria.and(group1.or(group2)))
                ).all();
    }

    /**
     * 扣减库存（通过 DatabaseClient 编程式实现）
     */
    public Mono<Long> deductStock(Long productId, int quantity) {
        return databaseClient.sql("UPDATE product SET stock = stock - :quantity " +
                        "WHERE product_id = :productId AND stock = :quantity")
                .bind("productId", productId)
                .bind("quantity", quantity)
                .fetch()
                .rowsUpdated();
    }
}