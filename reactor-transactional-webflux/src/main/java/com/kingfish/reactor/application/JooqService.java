package com.kingfish.reactor.application;

import com.kingfish.reactor.domain.model.entity.OrderDO;
import com.kingfish.reactor.domain.model.entity.ProductDO;
import jakarta.annotation.Resource;
import org.jooq.DSLContext;
import org.jooq.Field;
import org.jooq.Table;
import org.jooq.impl.DSL;
import org.jooq.impl.SQLDataType;
import org.springframework.r2dbc.core.DatabaseClient;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;

/**
 * @Author : haowl
 * @Date : 2026/4/25 22:13
 * @Desc :
 */
@Service
public class JooqService {

    @Resource
    private DSLContext dslContext;


}