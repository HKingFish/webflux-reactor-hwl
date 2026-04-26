package com.kingfish.reactor.domain.model.dao;

import com.kingfish.reactor.domain.model.entity.OrderDO;
import org.jooq.Field;
import org.jooq.Table;
import org.jooq.impl.DSL;
import org.jooq.impl.SQLDataType;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.Function;

/**
 * 订单数据访问层（jOOQ + R2DBC）
 */
@Component
public class OrderDao extends ReactiveBaseDao<OrderDO> {

    public static final Table<?> TABLE = DSL.table("`order`");
    public static final Field<Long> ID = DSL.field("id", SQLDataType.BIGINT);
    public static final Field<Long> ORDER_ID = DSL.field("order_id", SQLDataType.BIGINT);
    public static final Field<Long> USER_ID = DSL.field("user_id", SQLDataType.BIGINT);
    public static final Field<Long> PRODUCT_ID = DSL.field("product_id", SQLDataType.BIGINT);
    public static final Field<Integer> QUANTITY = DSL.field("quantity", SQLDataType.INTEGER);
    public static final Field<String> ORDER_NO = DSL.field("order_no", SQLDataType.VARCHAR(64));
    public static final Field<BigDecimal> TOTAL_AMOUNT =
            DSL.field("total_amount", SQLDataType.DECIMAL(10, 2));
    public static final Field<Integer> PAY_STATUS = DSL.field("pay_status", SQLDataType.INTEGER);
    public static final Field<Integer> ORDER_STATUS =
            DSL.field("order_status", SQLDataType.INTEGER);

    @Override
    protected Table<?> getTable() {
        return TABLE;
    }

    @Override
    protected Field<Long> getIdField() {
        return ID;
    }

    @Override
    protected Function<Map<String, Object>, OrderDO> getRowMapper() {
        return row -> {
            OrderDO order = new OrderDO();
            order.setId((Long) row.get("id"));
            order.setOrderId((Long) row.get("order_id"));
            order.setUserId((Long) row.get("user_id"));
            order.setProductId((Long) row.get("product_id"));
            order.setQuantity((Integer) row.get("quantity"));
            order.setOrderNo((String) row.get("order_no"));
            order.setTotalAmount((BigDecimal) row.get("total_amount"));
            order.setPayStatus((Integer) row.get("pay_status"));
            order.setOrderStatus((Integer) row.get("order_status"));
            order.setCreateTime((LocalDateTime) row.get("create_time"));
            order.setUpdateTime((LocalDateTime) row.get("update_time"));
            order.setDeleted((Integer) row.get("deleted"));
            return order;
        };
    }

    /**
     * 将订单实体转换为字段映射（用于插入操作）
     *
     * @param order 订单实体
     * @return 字段与值的有序映射
     */
    public LinkedHashMap<Field<?>, Object> toColumnMap(OrderDO order) {
        LinkedHashMap<Field<?>, Object> columns = new LinkedHashMap<>();
        columns.put(ORDER_ID, order.getOrderId());
        columns.put(USER_ID, order.getUserId());
        columns.put(PRODUCT_ID, order.getProductId());
        columns.put(QUANTITY, order.getQuantity());
        columns.put(ORDER_NO, order.getOrderNo());
        columns.put(TOTAL_AMOUNT, order.getTotalAmount());
        columns.put(PAY_STATUS, order.getPayStatus());
        columns.put(ORDER_STATUS, order.getOrderStatus());
        return columns;
    }
}
