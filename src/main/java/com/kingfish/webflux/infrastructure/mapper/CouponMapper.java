package com.kingfish.webflux.infrastructure.mapper;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.kingfish.webflux.domain.model.entity.CouponDO;
import org.apache.ibatis.annotations.Mapper;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.util.List;

/**
 * 优惠券 Mapper
 *
 * @Author : haowl
 * @Date : 2026/4/18
 * @Desc : 优惠券数据访问层
 */
@Mapper
public interface CouponMapper extends BaseMapper<CouponDO> {

    /**
     * 根据用户ID查询所有优惠券
     *
     * @param userId 用户ID
     * @return 优惠券列表
     */
    default Mono<List<CouponDO>> findByUserId(Long userId) {
        return Mono.fromCallable(() -> selectList(new LambdaQueryWrapper<CouponDO>()
                        .eq(CouponDO::getUserId, userId)))
                .subscribeOn(Schedulers.boundedElastic());
    }
}
