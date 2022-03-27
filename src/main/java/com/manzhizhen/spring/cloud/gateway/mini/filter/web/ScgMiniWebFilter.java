package com.manzhizhen.spring.cloud.gateway.mini.filter.web;

import org.springframework.core.Ordered;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;

/**
 *
 * from WeightCalculatorWebFilter
 *
 * @author: yizhenqiang
 * @date: 2022/3/27 5:16 下午
 */
@Component
public class ScgMiniWebFilter implements WebFilter, Ordered {

    @Override
    public int getOrder() {
        return 10000;
    }

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {
        return chain.filter(exchange);
    }
}
