package com.manzhizhen.spring.cloud.gateway.mini.filter;

import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

/**
 * 允许 {@link WebFilter} 委托给链中的下一个
 *
 * @author: yizhenqiang
 * @date: 2022/3/26 10:43 上午
 */
public interface GatewayFilterChain {

    /**
     * 委托给链中的下一个 {@code WebFilter}
     *
     * @param exchange the current server exchange
     * @return {@code Mono<Void>} to indicate when request handling is complete
     */
    Mono<Void> filter(ServerWebExchange exchange);
}
