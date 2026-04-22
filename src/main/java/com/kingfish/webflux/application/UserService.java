package com.kingfish.webflux.application;

import com.kingfish.webflux.domain.model.entity.OrderDO;
import com.kingfish.webflux.domain.model.entity.UserAddressDO;
import com.kingfish.webflux.domain.model.entity.UserDO;
import com.kingfish.webflux.domain.model.entity.UserExtDO;
import com.kingfish.webflux.domain.model.vo.MemberLevelVO;
import com.kingfish.webflux.domain.model.vo.UserExportVO;
import com.kingfish.webflux.domain.model.vo.UserHomeVO;
import com.kingfish.webflux.infrastructure.mapper.MemberMapper;
import com.kingfish.webflux.infrastructure.mapper.UserExtMapper;
import com.kingfish.webflux.infrastructure.mapper.UserMapper;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * 用户服务
 *
 * @Author : haowl
 * @Date : 2026/4/12 11:47
 * @Desc :
 */
@Slf4j
@Service
public class UserService {

    @Resource
    private UserMapper userMapper;

    @Resource
    private UserExtMapper userExtMapper;

    @Resource
    private MemberMapper memberMapper;

    @Resource
    private OrderService orderService;

    @Resource
    private MemberService memberService;

    @Resource
    private MessageService messageService;

    @Resource
    private UserAddressService userAddressService;

    /**
     * 用户个人中心首页数据聚合接口
     * <p>
     * 场景：用户打开 APP 个人中心页面，一次请求拿到所有展示数据
     * <p>
     * 并行查询以下数据：
     * - 用户基本信息（强依赖，查不到直接抛 IllegalArgumentException，密码需脱敏）
     * - 用户扩展信息（手机号、实名等）
     * - 会员等级和折扣（调用 memberService.getMemberLevel，超时或异常降级为默认值）
     * - 订单统计（待付款数、待收货数，需要从全量订单中按状态分组统计）
     * - 最新一笔有效订单（排除已取消和已删除的，按时间倒序取第一条）
     * - 未读消息数（调用 messageService.getUnreadCount，异常返回 0）
     * - 默认收货地址（调用 userAddressService.getDefaultAddress）
     * <p>
     * 弱依赖（会员、消息、地址等）异常不影响整体返回
     * 组装成 UserHomeVO 返回
     *
     * @param userId 用户ID
     * @return 个人中心首页数据
     */
    public Mono<UserHomeVO> getUserHomePage(Long userId) {
        // 强依赖：用户基本信息，查不到直接抛异常
        Mono<UserDO> userMono = userMapper.findById(userId)
                .switchIfEmpty(Mono.error(new IllegalArgumentException("用户不存在")))
                .map(user -> {
                    user.setPassword(null);
                    return user;
                });

        // 弱依赖：用户扩展信息
        Mono<UserExtDO> userExtMono = userExtMapper.findByUserId(userId)
                .defaultIfEmpty(new UserExtDO());

        // 弱依赖：会员等级（memberService 内部已有超时降级）
        Mono<MemberLevelVO> memberLevelMono = memberService.getMemberLevel(userId);

        // 订单数据只查一次，缓存后复用
        Mono<List<OrderDO>> ordersMono = orderService.getByUserId(userId)
                .defaultIfEmpty(List.of())
                .cache();

        // 从缓存的订单数据中提取：订单统计（待付款数、待收货数）
        Mono<Map<Integer, Long>> orderStatsMono = ordersMono
                .map(orders -> orders.stream()
                        .collect(Collectors.groupingBy(OrderDO::getOrderStatus, Collectors.counting())));

        // 从缓存的订单数据中提取：最新一笔有效订单（用 Optional 包装，防止 zip 因 empty 失效）
        Mono<Optional<OrderDO>> latestOrderMono = ordersMono
                .map(orders -> orders.stream()
                        .filter(o -> o.getOrderStatus() != 4 && o.getDeleted() != 1)
                        .max(Comparator.comparing(OrderDO::getCreateTime)))
                .defaultIfEmpty(Optional.empty());

        // 弱依赖：未读消息数（messageService 内部已有容错）
        Mono<Long> unreadCountMono = messageService.getUnreadCount(userId);

        // 弱依赖：默认收货地址（用 Optional 包装，无地址不影响整体）
        Mono<Optional<UserAddressDO>> addressMono = userAddressService.getDefaultAddress(userId)
                .map(Optional::of)
                .defaultIfEmpty(Optional.empty())
                .onErrorResume(e -> {
                    log.warn("查询默认地址失败，用户ID：{}，异常信息：{}", userId, e.getMessage());
                    return Mono.just(Optional.empty());
                });

        // 8 个源并行查询，组装 UserHomeVO
        return Mono.zip(userMono, userExtMono, memberLevelMono,
                        orderStatsMono, latestOrderMono, unreadCountMono, addressMono)
                .log()
                .map(t -> {
                    UserHomeVO vo = new UserHomeVO();
                    vo.setUser(t.getT1());
                    vo.setUserExt(t.getT2());
                    vo.setMemberLevel(t.getT3());

                    Map<Integer, Long> stats = t.getT4();
                    vo.setPendingPaymentCount(stats.getOrDefault(0, 0L));
                    vo.setPendingReceiveCount(stats.getOrDefault(2, 0L));

                    vo.setLatestOrder(t.getT5().orElse(null));
                    vo.setUnreadMessageCount(t.getT6());
                    vo.setDefaultAddress(t.getT7().orElse(null));
                    return vo;
                });
    }

    /**
     * 管理后台：批量导出用户数据
     * <p>
     * 场景：运营后台选中一批用户ID，导出用户详细数据用于分析
     * <p>
     * 传入用户ID列表，对每个用户并行聚合以下信息：
     * - 用户基本信息（username、status → statusDesc：1="正常" / 0="禁用"）
     * - 用户扩展信息（realName、phone）
     * - 会员等级（查不到默认 level=0）
     * - 该用户所有已支付订单(payStatus=1)的 totalAmount 求和 → totalSpent
     * - 该用户订单总数 → orderCount
     * <p>
     * 某个用户查询失败不影响其他用户，失败的用户跳过并打印 warn 日志
     * 返回结果保持传入顺序，查询失败的用户对应位置为 null
     *
     * @param userIds 用户ID列表
     * @return 用户导出数据列表（与传入顺序一致）
     */
    public Mono<List<UserExportVO>> batchExportUsers(List<Long> userIds) {
        return Flux.fromIterable(userIds)
                .flatMapSequential(userId -> buildUserExportVO(userId)
                        .map(Optional::of)
                        .onErrorResume(e -> {
                            log.warn("导出用户数据失败，用户ID：{}，异常信息：{}", userId, e.getMessage());
                            // 如果用 Mono.empty()，这个元素在 Flux 中就直接消失了，collectList 收集的列表里不会有这个位置。
                            // 传入 5 个用户 ID，第 3 个失败了，最终列表只有 4 个元素，位置就对不上了。所以这里使用 Optional.empty() 占位
                            return Mono.just(Optional.empty());
                        }))
                .mapNotNull(opt -> opt.orElse(null))
                .collectList();
    }

    /**
     * 构建单个用户的导出数据（内部方法）
     */
    private Mono<UserExportVO> buildUserExportVO(Long userId) {
        Mono<UserDO> userMono = userMapper.findById(userId)
                .switchIfEmpty(Mono.error(new IllegalArgumentException("用户不存在，ID：" + userId)));

        Mono<UserExtDO> userExtMono = userExtMapper.findByUserId(userId)
                .defaultIfEmpty(new UserExtDO());

        Mono<MemberLevelVO> memberLevelMono = memberService.getMemberLevel(userId);

        // 订单只查一次，用 cache() 缓存结果，避免重复查询
        Mono<List<OrderDO>> ordersMono = orderService.getByUserId(userId).cache();

        Mono<BigDecimal> totalSpentMono = ordersMono
                .map(orders -> orders.stream()
                        .filter(o -> o.getPayStatus() == 1)
                        .map(OrderDO::getTotalAmount)
                        .reduce(BigDecimal.ZERO, BigDecimal::add));

        Mono<Integer> orderCountMono = ordersMono.map(List::size);

        return Mono.zip(userMono, userExtMono, memberLevelMono, totalSpentMono, orderCountMono)
                .map(t -> {
                    UserExportVO vo = new UserExportVO();
                    vo.setUserId(t.getT1().getId());
                    vo.setUsername(t.getT1().getUsername());
                    vo.setStatusDesc(t.getT1().getStatus() == 1 ? "正常" : "禁用");
                    vo.setRealName(t.getT2().getRealName());
                    vo.setPhone(t.getT2().getPhone());
                    vo.setMemberLevel(t.getT3().getLevel());
                    vo.setTotalSpent(t.getT4());
                    vo.setOrderCount(t.getT5());
                    return vo;
                });
    }
}
