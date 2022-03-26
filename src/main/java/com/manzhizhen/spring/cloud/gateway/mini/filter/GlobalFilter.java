package com.manzhizhen.spring.cloud.gateway.mini.filter;

import org.springframework.core.Ordered;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

/**
 * 全局筛选器
 * SCG其实有 GatewayFilter 和 GlobalFilter 两种筛选器，但mini中为了简单就只实现 GlobalFilter
 * <p>
 * from {@link org.springframework.cloud.gateway.filter.GlobalFilter}
 *
 * @author: yizhenqiang
 * @date: 2022/3/26 9:13 上午
 */
public interface GlobalFilter extends Ordered {

    /**
     * 处理Web请求并且 (可选) 通过给的 {@link GatewayFilterChain} 委托给下一个{@code WebFilter}
     *
     * @param exchange the current server exchange
     * @param chain    provides a way to delegate to the next filter
     * @return {@code Mono<Void>} to indicate when request processing is complete
     */
    Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain);
}
