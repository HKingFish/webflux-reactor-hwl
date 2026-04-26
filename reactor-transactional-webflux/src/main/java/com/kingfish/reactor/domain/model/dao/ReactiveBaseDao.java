package com.kingfish.reactor.domain.model.dao;

import jakarta.annotation.Resource;
import org.jooq.Condition;
import org.jooq.DSLContext;
import org.jooq.Field;
import org.jooq.Record;
import org.jooq.SelectFieldOrAsterisk;
import org.jooq.Table;
import org.jooq.UpdateSetStep;
import org.jooq.impl.DSL;
import org.springframework.r2dbc.core.DatabaseClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

/**
 * jOOQ + R2DBC 响应式基础数据访问层
 * <p>
 * 封装通用的增删改查操作，子类只需定义表结构和字段映射即可使用。
 * jOOQ 负责类型安全的 SQL 构建，DatabaseClient 负责响应式执行。
 *
 * @param <T> 实体类型
 */
public abstract class ReactiveBaseDao<T> {

    @Resource
    protected DSLContext dslContext;

    @Resource
    protected DatabaseClient databaseClient;

    /**
     * 返回当前操作的表
     *
     * @return jOOQ 表定义
     */
    protected abstract Table<?> getTable();

    /**
     * 返回主键字段
     *
     * @return 主键 Field 定义
     */
    protected abstract Field<Long> getIdField();

    /**
     * 将数据库行映射为实体对象
     *
     * @return 行映射函数
     */
    protected abstract Function<Map<String, Object>, T> getRowMapper();

    /**
     * 根据主键查询单条记录
     *
     * @param id 主键值
     * @return 实体对象
     */
    public Mono<T> findById(Long id) {
        String sql = dslContext.select(DSL.asterisk())
                .from(getTable())
                .where(getIdField().eq(DSL.param("id", Long.class)))
                .getSQL();

        return databaseClient.sql(sql)
                .bind("id", id)
                .fetch()
                .one()
                .map(getRowMapper());
    }

    /**
     * 根据条件查询单条记录
     *
     * @param condition 查询条件
     * @param params    绑定参数（按顺序对应 SQL 中的占位符）
     * @return 实体对象
     */
    public Mono<T> findOne(Condition condition, Map<String, Object> params) {
        String sql = dslContext.select(DSL.asterisk())
                .from(getTable())
                .where(condition)
                .getSQL();

        DatabaseClient.GenericExecuteSpec spec = databaseClient.sql(sql);
        for (Map.Entry<String, Object> entry : params.entrySet()) {
            spec = spec.bind(entry.getKey(), entry.getValue());
        }
        return spec.fetch().one().map(getRowMapper());
    }

    /**
     * 根据条件查询多条记录
     *
     * @param condition 查询条件
     * @param params    绑定参数
     * @return 实体列表
     */
    public Flux<T> findList(Condition condition, Map<String, Object> params) {
        String sql = dslContext.select(DSL.asterisk())
                .from(getTable())
                .where(condition)
                .getSQL();

        DatabaseClient.GenericExecuteSpec spec = databaseClient.sql(sql);
        for (Map.Entry<String, Object> entry : params.entrySet()) {
            spec = spec.bind(entry.getKey(), entry.getValue());
        }
        return spec.fetch().all().map(getRowMapper());
    }

    /**
     * 插入单条记录
     *
     * @param columns 字段与值的映射（有序，保证字段和值一一对应）
     * @return 影响行数
     */
    public Mono<Long> insert(LinkedHashMap<Field<?>, Object> columns) {
        // 构建 INSERT INTO table (col1, col2, ...) VALUES (?, ?, ...)
        Field<?>[] fields = columns.keySet().toArray(new Field[0]);
        Object[] values = columns.values().toArray();

        // 使用参数化占位符
        org.jooq.Param<?>[] params = new org.jooq.Param[fields.length];
        for (int i = 0; i < fields.length; i++) {
            params[i] = DSL.param(fields[i].getName(), fields[i].getDataType());
        }

        @SuppressWarnings("unchecked")
        String sql = dslContext.insertInto(getTable())
                .columns(fields)
                .values(params)
                .getSQL();

        DatabaseClient.GenericExecuteSpec spec = databaseClient.sql(sql);
        for (int i = 0; i < fields.length; i++) {
            spec = spec.bind(fields[i].getName(), values[i]);
        }
        return spec.fetch().rowsUpdated();
    }

    /**
     * 根据条件更新记录（支持 SQL 表达式，如 stock = stock - ?）
     *
     * @param setSql    SET 子句的原生 SQL（如 "stock = stock - :quantity"）
     * @param condition WHERE 条件
     * @param params    绑定参数
     * @return 影响行数
     */
    public Mono<Long> updateBySql(String setSql, Condition condition, Map<String, Object> params) {
        // 手动拼接 UPDATE 语句，因为 jOOQ 的 set() 不支持原生 SQL 表达式赋值
        String whereSql = dslContext.renderInlined(condition);
        String sql = "UPDATE " + getTable().getName() + " SET " + setSql + " WHERE " + whereSql;

        DatabaseClient.GenericExecuteSpec spec = databaseClient.sql(sql);
        for (Map.Entry<String, Object> entry : params.entrySet()) {
            spec = spec.bind(entry.getKey(), entry.getValue());
        }
        return spec.fetch().rowsUpdated();
    }

    /**
     * 根据条件更新记录（固定值赋值）
     *
     * @param setValues 字段与新值的映射
     * @param condition WHERE 条件
     * @param params    WHERE 条件中的绑定参数
     * @return 影响行数
     */
    public Mono<Long> update(Map<Field<?>, Object> setValues, Condition condition,
                             Map<String, Object> params) {
        UpdateSetStep<?> step = dslContext.update(getTable());
        // 逐个设置字段值
        var setStep = step.set(DSL.field("1"), (Object) null);
        boolean first = true;
        StringBuilder setSqlBuilder = new StringBuilder();
        Map<String, Object> allParams = new LinkedHashMap<>(params);

        for (Map.Entry<Field<?>, Object> entry : setValues.entrySet()) {
            if (!first) {
                setSqlBuilder.append(", ");
            }
            String paramName = "set_" + entry.getKey().getName();
            setSqlBuilder.append(entry.getKey().getName()).append(" = :").append(paramName);
            allParams.put(paramName, entry.getValue());
            first = false;
        }

        String whereSql = dslContext.renderInlined(condition);
        String sql = "UPDATE " + getTable().getName()
                + " SET " + setSqlBuilder
                + " WHERE " + whereSql;

        DatabaseClient.GenericExecuteSpec spec = databaseClient.sql(sql);
        for (Map.Entry<String, Object> entry : allParams.entrySet()) {
            spec = spec.bind(entry.getKey(), entry.getValue());
        }
        return spec.fetch().rowsUpdated();
    }

    /**
     * 根据主键删除记录
     *
     * @param id 主键值
     * @return 影响行数
     */
    public Mono<Long> deleteById(Long id) {
        String sql = dslContext.deleteFrom(getTable())
                .where(getIdField().eq(DSL.param("id", Long.class)))
                .getSQL();

        return databaseClient.sql(sql)
                .bind("id", id)
                .fetch()
                .rowsUpdated();
    }
}
