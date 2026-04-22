package com.kingfish.webflux.application;

import com.kingfish.webflux.domain.model.entity.UserAddressDO;
import com.kingfish.webflux.infrastructure.mapper.UserAddressMapper;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

/**
 * 用户地址服务
 *
 * @Author : haowl
 * @Date : 2026/4/14 21:42
 * @Desc :
 */
@Slf4j
@Service
public class UserAddressService {

    @Resource
    private UserAddressMapper userAddressMapper;

    /**
     * 获取用户默认收货地址
     *
     * 场景：下单确认页、结算页、个人中心等多处需要展示默认地址
     *
     * 查询该用户所有地址，优先取 isDefault=1 的地址
     * 没有默认地址则取第一条
     * 无任何地址返回空 Mono
     *
     * @param userId 用户ID
     * @return 默认收货地址
     */
    public Mono<UserAddressDO> getDefaultAddress(Long userId) {
        return userAddressMapper.getDefaultAddress(userId);
    }
}
