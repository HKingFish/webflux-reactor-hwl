package com.kingfish.webflux.application;

import com.kingfish.webflux.domain.model.vo.MemberLevelVO;
import com.kingfish.webflux.infrastructure.mapper.MemberMapper;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.time.Duration;

/**
 * 会员服务
 *
 * @Author : haowl
 * @Date : 2026/4/13 22:04
 * @Desc :
 */
@Slf4j
@Service
public class MemberService {

    @Resource
    private MemberMapper memberMapper;

    /**
     * 查询用户会员等级信息（带超时降级）
     * <p>
     * 场景：被多个上游接口调用（个人中心、结算页、订单确认页等），
     * 会员服务不稳定时不能拖垮调用方
     * <p>
     * 查询用户会员信息，设置 500ms 超时
     * 正常返回时映射为 MemberLevelVO（level + discount）
     * 超时或查询异常时降级返回默认会员（level=1，discount=1.0）
     * 降级时打印 warn 日志，包含用户ID和异常信息
     *
     * @param userId 用户ID
     * @return 会员等级信息
     */
    public Mono<MemberLevelVO> getMemberLevel(Long userId) {
        return memberMapper.getMemberByUserId(userId)
                .timeout(Duration.ofMillis(500))
                .map(m -> {
                    MemberLevelVO memberLevelVO = new MemberLevelVO();
                    memberLevelVO.setLevel(m.getLevel());
                    memberLevelVO.setDiscount(m.getDiscount());
                    return memberLevelVO;
                })
                // 用户没有会员记录时降级为默认值（empty 不是异常，onErrorResume 捕获不到）
                .defaultIfEmpty(MemberLevelVO.defaultVO())
                .onErrorResume(throwable -> {
                    log.warn("用户{}查询会员等级异常，降级返回默认会员, error: {}", userId, throwable.getMessage());
                    return Mono.just(MemberLevelVO.defaultVO());
                });
    }

}
