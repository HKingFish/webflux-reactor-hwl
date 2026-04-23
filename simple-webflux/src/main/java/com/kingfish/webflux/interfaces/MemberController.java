package com.kingfish.webflux.interfaces;

import com.kingfish.webflux.application.MemberService;
import com.kingfish.webflux.domain.model.vo.MemberLevelVO;
import com.kingfish.webflux.domain.model.vo.Result;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

/**
 * 会员接口
 */
@RestController
@RequestMapping("/member")
public class MemberController {

    @Resource
    private MemberService memberService;

    @GetMapping("/{userId}/level")
    public Mono<Result<MemberLevelVO>> getMemberLevel(@PathVariable Long userId) {
        return memberService.getMemberLevel(userId).map(Result::success);
    }
}
