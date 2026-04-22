package com.kingfish.webflux.interfaces;

import com.kingfish.webflux.application.UserAddressService;
import com.kingfish.webflux.domain.model.entity.UserAddressDO;
import com.kingfish.webflux.domain.model.vo.Result;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

/**
 * 收货地址接口
 */
@RestController
@RequestMapping("/address")
public class AddressController {

    @Resource
    private UserAddressService userAddressService;

    @GetMapping("/{userId}/default")
    public Mono<Result<UserAddressDO>> getDefaultAddress(@PathVariable Long userId) {
        return userAddressService.getDefaultAddress(userId).map(Result::success);
    }
}
