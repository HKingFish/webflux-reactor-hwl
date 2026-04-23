package com.kingfish.webflux.domain.model.vo;

import lombok.Data;
import lombok.experimental.Accessors;

import java.math.BigDecimal;

/**
 * 会员等级 VO
 */
@Accessors(chain = true)
@Data
public class MemberLevelVO {

    /**
     * 会员等级
     */
    private Integer level;

    /**
     * 折扣率
     */
    private BigDecimal discount;


    public static MemberLevelVO defaultVO(){
        return new MemberLevelVO()
                .setLevel(1)
                .setDiscount(BigDecimal.ONE);
    }
}
