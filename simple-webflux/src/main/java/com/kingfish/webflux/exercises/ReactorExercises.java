package com.kingfish.webflux.exercises;//package com.kingfish.webflux.exercises;
//
//import cn.hutool.core.thread.ThreadUtil;
//import com.kingfish.webflux.domain.model.entity.UserDO;
//import com.kingfish.webflux.infrastructure.mapper.UserMapper;
//import jakarta.annotation.Resource;
//import lombok.SneakyThrows;
//import org.reactivestreams.Publisher;
//import org.reactivestreams.Subscriber;
//import org.reactivestreams.Subscription;
//import org.springframework.stereotype.Component;
//import org.springframework.transaction.annotation.Transactional;
//import reactor.core.publisher.BufferOverflowStrategy;
//import reactor.core.publisher.Flux;
//import reactor.core.publisher.GroupedFlux;
//import reactor.core.publisher.Mono;
//
//import java.time.Duration;
//import java.util.List;
//import java.util.concurrent.CountDownLatch;
//import java.util.function.BiConsumer;
//import java.util.function.Consumer;
//import java.util.function.Function;
//
///**
// * @Author : haowl
// * @Date : 2026/4/11 21:49
// * @Desc :
// */
//@Component
//public class ReactorExercises {
//
//    @Resource
//    private UserMapper userMapper;
//
//
//    @SneakyThrows
//    public static void main(String[] args) {
//        ReactorExercises reactorExercises = new ReactorExercises();
////        reactorExercises.exercises12();
////        reactorExercises.exercises13();
////        reactorExercises.exercises14();
////        reactorExercises.exercises21();
//        reactorExercises.exercises24();
//
//        new CountDownLatch(1).await();
//    }
//
//
//    // ==================== Mono 进阶练习题 ====================
//
//    /**
//     * 1. 延迟 + 超时 + 降级
//     * 需求：
//     * Mono 延迟 2 秒
//     * 设置超时 1 秒
//     * 超时后返回默认值 "timeout"
//     * 考点：delayElement, timeout, onErrorReturn
//     */
//    public void exercises01() {
//        // Mono.delay 与 Mono.delayElement 含义不同, 不能随意替换，否则会丢失原数据（变成 0L）。
//
//        // Mono.delay 用于延迟 Mono 发送时间，会在延迟指定时间后发送一个 0L 元素
//        Mono.delay(Duration.ofSeconds(1))
//                .then(Mono.just("ok"))
//                .subscribe(System.out::println);
//        // Mono.delayElement 用于延迟 Mono 发送元素的时间，在延迟指定事件后会发送原元素。
//        Mono.just("ok")
//                .delayElement(Duration.ofSeconds(2))
//                .timeout(Duration.ofSeconds(1))
//                .onErrorReturn("timeout")
//                .subscribe(System.out::println);
//    }
//
//    /**
//     * 2. 条件切换
//     * 需求：
//     * Mono.just(18)
//     * 如果值 >= 18 → 返回 "adult"
//     * 否则返回 "minor"
//     * 考点：map, 条件逻辑
//     */
//    public void exercises02() {
//        // 请在这里实现
//        Mono.just(18)
//                .map(age -> age >= 18 ? "adult" : "minor")
//                .subscribe(System.out::println);
//    }
//
//    /**
//     * 3. 空值处理
//     * 需求：
//     * Mono.justOrEmpty(null)
//     * 如果为空，返回 "default"
//     * 否则返回原值
//     * 考点：justOrEmpty, defaultIfEmpty
//     */
//    public void exercises03() {
//        Mono.justOrEmpty(null)
//                .defaultIfEmpty("default")
//                .subscribe(System.out::println);
//
//        Mono.empty()
//                .defaultIfEmpty("default")
//                .subscribe(System.out::println);
//    }
//
//    /**
//     * 4. 异步串行（flatMap 链式）
//     * 需求：
//     * 1. Mono.just("id123")
//     * 2. 根据 id 异步查询用户 → Mono.just("user:id123")
//     * 3. 再根据用户查询订单 → Mono.just("order:user123")
//     * 4. 最终返回订单字符串
//     * 考点：flatMap 链式调用
//     */
//    public void exercises04() {
//        // 这里 flatMap 并不需要使用 Mono.defer, 因为 flatMap 本身就是懒加载了，当订阅后才会触发 flatMap 才会触发 Mono.just 发送元素。
//        Mono.just("id123")
//                .flatMap(id -> Mono.just("user:" + id))
//                .flatMap(user -> Mono.just("order:" + user))
//                .subscribe(System.out::println);
//
//
//        // 这里会创建 Mono 对象，但不会执行
//        // 这段代码真正的问题是，多次订阅时使用的是同一个 Mono 对象，也就是复用了查询结果
//
//        // 如果需要每次订阅都查询一次，应该使用 Mono.defer, 这样每次订阅都会创建一个新的 Mono 对象，避免了复用查询结果的问题。
//        Mono<UserDO> userMono = userMapper.findById(100L);
//
//        Mono.just(100L)
//                .flatMap(id -> userMono)
//                .subscribe(); // 到这里才真正执行查询
//
//        // 多次订阅也只会查询一次 DB
//        userMono.subscribe(System.out::println);
//        userMono.subscribe(System.out::println);
//
//    }
//
//    /**
//     * 5. 错误捕获并替换
//     * 需求：
//     * Mono.error(new RuntimeException("error"))
//     * 捕获异常，返回 "recovered"
//     * 考点：onErrorResume
//     */
//    public void exercises05() {
//        // 请在这里实现
//        Mono.error(new RuntimeException())
//                .onErrorReturn("error")
//                .subscribe(System.out::println);
//    }
//
//    /**
//     * 6. Mono 转 Flux
//     * 需求：
//     * Mono.just("hello")
//     * 转成 Flux<String>
//     * 考点：flux()
//     */
//    public void exercises06() {
//        // 请在这里实现
//
//        Mono.just(1)
//                .flux()
//                .subscribe(System.out::println);
//    }
//
//    /**
//     * 7. zip 组合 2 个 Mono
//     * 需求：
//     * Mono.just("a")
//     * Mono.just("b")
//     * 组合成字符串 "a-b"
//     * 考点：Mono.zip
//     */
//    public void exercises07() {
//        // 输出 1_a_X, zip 会按照元素一个个拼接，以最短的为准, 这里因为 Mono.just 只有一个元素，所以这里输出也只有一个元素
//        Flux.zip(Flux.just(1, 2, 3), Flux.just("a", "b", "c", "d"), Mono.just("X").flux())
//                .map(objects -> objects.getT1() + "_" + objects.getT2() + "_" + objects.getT3())
//                .subscribe(System.out::println);
//
//        // 输出为 1_a_X, 会一次拼接
//        Mono.just(1)
//                .zipWith(Mono.just("a"), (integer, s) -> integer + "_" + s)
//                .zipWith(Mono.just("X"), (integer, s) -> integer + "_" + s)
//                .subscribe(System.out::println);
//
//    }
//
//    /**
//     * 8. zip 组合 3 个 Mono
//     * 需求：
//     * 组合 Mono1、Mono2、Mono3，返回拼接字符串 a-b-c
//     * 考点：Mono.zip
//     */
//    public void exercises08() {
//        Mono.zip(objects -> objects[0] + "-" + objects[1] + "-" + objects[2],
//                        Mono.just("a"), Mono.just("b"), Mono.just("c"))
//                .subscribe(System.out::println);
//    }
//
//    /**
//     * 9. doOnSuccess / doOnError 日志
//     * 需求：
//     * 成功时打印 "success"
//     * 失败时打印 "error"
//     * 考点：doOnSuccess, doOnError
//     */
//    public void exercises09() {
//        Mono.delay(Duration.ofSeconds(2))
//                .timeout(Duration.ofSeconds(1))
//                .onErrorReturn(-1L)
//                .onErrorComplete(throwable -> false)
//                .doOnSuccess(System.out::println)
//                .doOnError(System.out::println)
//                .subscribe();
//    }
//
//    /**
//     * 10. 忽略结果，只关心完成
//     * 需求：
//     * Mono.just(1)
//     * 订阅时不处理数据，只在完成时打印 "done"
//     * 考点：then()
//     */
//    public void exercises10() {
//        Mono.just(1)
//                .then(Mono.fromRunnable(() -> System.out.println("good")))
//                .subscribe(System.out::println);
//    }
//
//    // ==================== Flux 进阶练习题 ====================
//
//    /**
//     * 11. Flux 延迟发射
//     * 需求：
//     * 每秒发射 1、2、3、4、5
//     * 打印
//     * 考点：Flux.interval, map, take
//     */
//    public void exercises11() {
////        Flux.just(1, 2, 3, 4, 5)
////                .delayElements(Duration.ofSeconds(1))
////                .subscribe(System.out::println);
//        // 每 2 秒钟，发射一个从 0 开始递增的长整型数字（0、1、2、3……），无限循环下去。一般和 take 一起使用
//        Flux.interval(Duration.ofSeconds(2))
//                .map(i -> i + 1)
//                .take(5)
//                .subscribe(System.out::println);
//    }
//
//    /**
//     * 12. 过滤 + 转换
//     * 需求：
//     * Flux.range(1, 10)
//     * 过滤偶数
//     * 每个值 ×3
//     * 收集为 List 并打印
//     * 考点：filter, map, collectList
//     */
//    public void exercises12() {
//        Flux.range(1, 20)
//                .filter(integer -> integer % 2 == 0)
//                .collectList()
//                .subscribe(System.out::println);
//
//    }
//
//    /**
//     * 13. 跳过、限制
//     * 需求：
//     * Flux 1~10
//     * 跳过前 2 个
//     * 取 5 个
//     * 考点：skip, take
//     */
//    public void exercises13() {
//        Flux.range(1, 20)
//                .skip(2)
//                .take(5)
//                .subscribe(System.out::println);
//    }
//
//    /**
//     * 14. 合并（merge）
//     * 需求：
//     * flux1: 1,2,3
//     * flux2: 4,5,6
//     * 合并成一个 Flux
//     * 考点：Flux.merge
//     */
//    public void exercises14() {
//        // 并行合并（按元素产生顺序输出）
//        Flux.merge(Flux.just(1, 2, 3), Flux.just(4, 5, 6))
//                .subscribe(System.out::println);
//    }
//
//    /**
//     * 15. 顺序连接（concat）
//     * 需求：
//     * flux1 执行完再执行 flux2
//     * 顺序输出
//     * 考点：concat
//     */
//    public void exercises15() {
//        // 顺序合并（先完第一个，再执行第二个）
//        Flux.concat(Flux.just(1, 2, 3), Flux.just(4, 5, 6))
//                .subscribe(System.out::println);
//    }
//
//    /**
//     * 16. 去重
//     * 需求：
//     * Flux.just(1,2,2,3,3,3)
//     * 去重
//     * 考点：distinct
//     */
//    public void exercises16() {
//        Flux.just(1, 2, 2, 3, 3, 3)
//                .distinct().subscribe(System.out::println);
//    }
//
//
//    /**
//     * 17. 统计数量
//     * 需求：
//     * Flux 发射 1~10
//     * 统计总数
//     * 考点：count()
//     */
//    public void exercises17() {
//        // 请在这里实现
//
//        Flux.range(1, 20)
//                .count()
//                .subscribe(System.out::println);
//    }
//
//    /**
//     * 18. 异常处理：出错继续
//     * 需求：
//     * Flux.just(1,2,3)
//     * map 中遇到 2 抛出异常
//     * 要求：出错不中断，继续处理后面元素
//     * 考点：onErrorContinue
//     */
//    public void exercises18() {
//        // 请在这里实现
//        Flux.just(1, 2, 3)
//                .map(i -> {
//                    if (i == 2) {
//                        throw new RuntimeException("error");
//                    }
//                    return i;
//                })
//                .onErrorContinue(RuntimeException.class, new BiConsumer<Throwable, Object>() {
//                    @Override
//                    public void accept(Throwable throwable, Object o) {
//                        System.out.println("error : " + throwable.getMessage());
//                    }
//                })
//                .subscribe(System.out::println);
//
//    }
//
//    /**
//     * 19. Flux 转 Mono（取第一个）
//     * 需求：
//     * Flux.just(10,20,30)
//     * 取第一个 → Mono
//     * 考点：next()
//     */
//    public void exercises19() {
//        // Flux 转 Mono 取第一个元素
//        Flux.just(10, 20, 30)
//                .next()
//                .subscribe(System.out::println);
//
//        // 二者效果相同
//        Flux.just(10, 20, 30)
//                .elementAt(0)
//                .subscribe(System.out::println);
//
//    }
//
//    /**
//     * 20. 背压：缓存
//     * 需求：
//     * Flux 快速发射 1~100
//     * 使用 onBackpressureBuffer 缓冲
//     * 考点：onBackpressureBuffer
//     */
//    public void exercises20() {
//        // 请在这里实现
//
//        Flux.range(1, 100)
////                 设置背压缓冲队列大小 10 : 超过会溢出异常
////                .onBackpressureBuffer(10)
//
//                // 指定背压异常的处理 策略
////                .onBackpressureBuffer(10, BufferOverflowStrategy.DROP_LATEST)
//
//                // 背压策略 只保留最新的, 是上面的简化版本
//                .onBackpressureLatest()
//
//                .subscribe(new Consumer<Integer>() {
//                    @Override
//                    public void accept(Integer integer) {
//                        System.out.println("integer = " + integer);
//                        ThreadUtil.safeSleep(100);
//                    }
//                });
//
//    }
//
//    /**
//     * 21. 扁平化集合
//     * 需求：
//     * Flux.just(List.of(1,2), List.of(3,4))
//     * 打平成 Flux<Integer>
//     * 考点：flatMapIterable
//     */
//    public void exercises21() {
//
//        // 普通版
//        Flux.just(List.of(1, 2), List.of(3, 4))
//                .flatMap(Flux::fromIterable)
//                .subscribe(System.out::println);
//
//        // 扁平化版 : 效果与上面相同
//        Flux.just(List.of(1, 2), List.of(3, 4))
//                .flatMapIterable(integers -> integers)
//                .subscribe(System.out::println);
//
//    }
//
//    /**
//     * 22. 分组 groupBy
//     * 需求：
//     * Flux 1~10
//     * 按奇偶分组
//     * 考点：groupBy
//     */
//    public void exercises22() {
//
//        // 虽然功能能实现，但是要避免嵌套 subscribe， 否则排查时比较麻烦
//        Flux.range(1, 10)
//                .groupBy(new Function<Integer, String>() {
//                    @Override
//                    public String apply(Integer integer) {
//                        return integer % 2 == 0 ? "偶数" : "奇数";
//                    }
//                })
//                .subscribe(new Consumer<GroupedFlux<String, Integer>>() {
//                    @Override
//                    public void accept(GroupedFlux<String, Integer> groupedFlux) {
//                        groupedFlux.subscribe(new Consumer<Integer>() {
//                            @Override
//                            public void accept(Integer integer) {
//                                System.out.println(groupedFlux.key() + " : " + integer);
//                            }
//                        });
//                    }
//                });
//
//        // 语义跟上面的完全相同, 更推荐这种写法，上面的 【嵌套 subscribe】不推荐
//        Flux.range(1,10)
//                .groupBy(i -> i % 2 == 0 ? "偶数" : "奇数")
//                .flatMap(group -> group.collectList().map(list -> group.key() + ": " + list))
//                .subscribe(System.out::println);
//
//    }
//
//    /**
//     * 23. 默认空值
//     * 需求：
//     * Flux.empty()
//     * 为空时发射 "default"
//     * 考点：defaultIfEmpty
//     */
//    public void exercises23() {
//        Flux.empty()
//                .defaultIfEmpty("default")
//                .subscribe(System.out::println);
//    }
//
//    /**
//     * 24. 日志跟踪
//     * 需求：
//     * 任何 Flux
//     * 开启全套日志：onNext, onComplete, onError
//     * 考点：log()
//     */
//    public void exercises24() {
//        // 会打印所有事件日志
//        Flux.just(1,2,3)
//                .log()
//                .subscribe();
//
//
//        Flux.range(1, 10)
//                .subscribe(new Subscriber<Integer>() {
//                    @Override
//                    public void onSubscribe(Subscription subscription) {
//                        System.out.println("ReactorExercises.onSubscribe");
//                        // 自定义Subscriber 需要自己手动调用 背压信息
//                        subscription.request(Integer.MAX_VALUE);
//                    }
//
//                    @Override
//                    public void onNext(Integer integer) {
//                        System.out.println("ReactorExercises.onNext");
//                    }
//
//                    @Override
//                    public void onError(Throwable throwable) {
//                        System.out.println("ReactorExercises.onError");
//                    }
//
//                    @Override
//                    public void onComplete() {
//                        System.out.println("ReactorExercises.onComplete");
//                    }
//                });
//    }
//
//    /**
//     * 25. 模拟分页查询
//     * 需求：
//     * 1. 返回 Flux<String>
//     * 2. 模拟 3 页数据
//     * 3. 每页 2 条记录
//     * 4. 顺序拼接所有记录
//     * 考点：concat
//     */
//    public void exercises25() {
//        // 请在这里实现
//        Flux<String> page1 = Flux.just("p1-r1", "p1-r2");
//        Flux<String> page2 = Flux.just("p2-r1", "p2-r2");
//        Flux<String> page3 = Flux.just("p3-r1", "p3-r2");
//
//        Flux.concat(page1, page2, page3)
//                .subscribe(System.out::println);
//    }
//
//
//}