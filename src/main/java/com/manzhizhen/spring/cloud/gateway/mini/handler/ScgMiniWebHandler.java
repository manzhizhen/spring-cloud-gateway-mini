package com.manzhizhen.spring.cloud.gateway.mini.handler;

import com.manzhizhen.spring.cloud.gateway.mini.filter.GatewayFilterChain;
import com.manzhizhen.spring.cloud.gateway.mini.filter.GlobalFilter;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebHandler;
import reactor.core.publisher.Mono;

/**
 * 委托给 {@link GlobalFilter} 实例链的 WebHandler 和
 * {@link GatewayFilterFactory} 实例然后到目标 {@link WebHandler}。
 * <p>
 * from FilteringWebHandler
 *
 * @author: yizhenqiang
 * @date: 2022/3/26 12:35 上午
 */
@Component
public class ScgMiniWebHandler implements WebHandler {

    @Autowired
    private List<GlobalFilter> globalFilters;

    @Override
    public Mono<Void> handle(ServerWebExchange exchange) {
        // 这里不需要去将 globalFilters 和 Route 中的 gatewayFilters 做合并，因为我们mini中没打算实现 GatewayFilter 这种过滤器。
        return new DefaultGatewayFilterChain(globalFilters).filter(exchange);
    }

    private static class DefaultGatewayFilterChain implements GatewayFilterChain {

        private final int index;

        private final List<GlobalFilter> filters;

        DefaultGatewayFilterChain(List<GlobalFilter> filters) {
            this.filters = filters;
            this.index = 0;
        }

        private DefaultGatewayFilterChain(DefaultGatewayFilterChain parent, int index) {
            this.filters = parent.getFilters();
            this.index = index;
        }

        public List<GlobalFilter> getFilters() {
            return filters;
        }

        @Override
        public Mono<Void> filter(ServerWebExchange exchange) {

            /**
             * 这段代码可能会难以理解，看起来只是执行了第1个GlobalFilter#filter，但是实际上我们结合GlobalFilter#filter的实现即可看出它实际是驱动了一条执行链：
             *     @Override
             *     public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
             *         return chain.filter(exchange);
             *     }
             * 而且可以由链条中的任何一个GlobalFilter来决定是继续执行下去还是提前返回。
             */
            return Mono.defer(() -> {
                if (this.index < filters.size()) {
                    GlobalFilter globalFilter = filters.get(this.index);
                    DefaultGatewayFilterChain chain = new DefaultGatewayFilterChain(this, this.index + 1);
                    return globalFilter.filter(exchange, chain);
                } else {
                    return Mono.empty(); // complete
                }
            });
        }
    }
}
