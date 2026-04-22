package com.kingfish.webflux.infrastructure.config;

import com.kingfish.webflux.domain.model.vo.Result;
import org.springframework.core.ReactiveAdapterRegistry;
import org.springframework.http.codec.ServerCodecConfigurer;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.HandlerResult;
import org.springframework.web.reactive.accept.RequestedContentTypeResolver;
import org.springframework.web.reactive.result.method.annotation.ResponseBodyResultHandler;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

/**
 * 响应式全局响应包装
 * 拦截 Controller 返回的 Mono，对 empty 和异常统一处理
 */
@Component
public class ReactiveResponseWrapper extends ResponseBodyResultHandler {

    public ReactiveResponseWrapper(
            ServerCodecConfigurer configurer,
            RequestedContentTypeResolver resolver,
            ReactiveAdapterRegistry registry) {
        super(configurer.getWriters(), resolver, registry);
        // 设置优先级高于默认的 ResponseBodyResultHandler（默认是 100）
        setOrder(99);
    }

    @Override
    public Mono<Void> handleResult(ServerWebExchange exchange, HandlerResult result) {
        Object body = result.getReturnValue();

        if (body instanceof Mono<?> mono) {
            Mono<Object> wrapped = mono
                    .map(v -> (Object) v)
                    .defaultIfEmpty(Result.success(null))
                    .onErrorResume(e -> Mono.just(Result.fail(e.getMessage())));

            HandlerResult wrappedResult = new HandlerResult(
                    result.getHandler(),
                    wrapped,
                    result.getReturnTypeSource());
            return super.handleResult(exchange, wrappedResult);
        }

        return super.handleResult(exchange, result);
    }
}
