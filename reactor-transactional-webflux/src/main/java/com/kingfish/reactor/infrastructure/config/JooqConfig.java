package com.kingfish.reactor.infrastructure.config;

import io.r2dbc.spi.ConnectionFactory;
import org.jooq.DSLContext;
import org.jooq.SQLDialect;
import org.jooq.impl.DSL;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * jOOQ 配置类
 * <p>
 * 基于 R2DBC ConnectionFactory 创建 DSLContext，
 * 不依赖 JDBC，与纯 R2DBC 环境兼容。
 */
@Configuration
public class JooqConfig {

    /**
     * 创建 jOOQ DSLContext
     * <p>
     * 此处仅用于构建 SQL 语句（DSL 构造器），
     * 实际执行通过 R2DBC 的 DatabaseClient 完成。
     *
     * @return DSLContext 实例
     */
    @Bean
    public DSLContext dslContext() {
        return DSL.using(SQLDialect.MYSQL);
    }
}
