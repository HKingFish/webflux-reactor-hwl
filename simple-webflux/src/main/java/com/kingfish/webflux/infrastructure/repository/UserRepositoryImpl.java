package com.kingfish.webflux.infrastructure.repository;

import com.kingfish.webflux.domain.repository.UserRepository;
import com.kingfish.webflux.infrastructure.mapper.UserMapper;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Repository;

/**
 * @Author : haowl
 * @Date : 2026/4/12 11:50
 * @Desc :
 */
@Repository
public class UserRepositoryImpl implements UserRepository {

    @Resource
    private UserMapper userMapper;


}