package com.manzhizhen.spring.cloud.gateway.mini.config;

import com.manzhizhen.spring.cloud.gateway.mini.predicate.Predicate;
import com.manzhizhen.spring.cloud.gateway.mini.route.Route;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.server.ServerWebExchange;

/**
 * 谓词配置
 *
 * @author: yizhenqiang
 * @date: 2022/3/25 11:40 下午
 */
@Configuration
public class PredicateConfig {

    /**
     * HTTP HEADER 中有mzz这个key则匹配这个谓词
     *
     * @param firstRoute
     * @return
     */
    @Bean
    public Predicate myFirstPredicate(@Qualifier("myFirstRoute") final Route firstRoute) {
        return new Predicate() {
            @Override
            public boolean match(ServerWebExchange exchange) {
                return exchange.getRequest().getHeaders().containsKey("mzz");
            }

            @Override
            public Route getRoute() {
                return firstRoute;
            }

            @Override
            public int getOrder() {
                return 0;
            }
        };
    }

    /**
     * 查询参数中包含mzz这个key则匹配这个谓词
     *
     * @param secondRoute
     * @return
     */
    @Bean
    public Predicate mySecondPredicate(@Qualifier("mySecondRoute") final Route secondRoute) {
        return new Predicate() {
            @Override
            public boolean match(ServerWebExchange exchange) {
                return exchange.getRequest().getQueryParams().containsKey("mzz");
            }

            @Override
            public Route getRoute() {
                return secondRoute;
            }

            @Override
            public int getOrder() {
                return 1;
            }
        };
    }

    /**
     * 查询参数包含love则匹配这个谓词
     *
     * @param thirdRoute
     * @return
     */
    @Bean
    public Predicate myThirdPredicate(@Qualifier("myThirdRoute")final Route thirdRoute) {
        return new Predicate() {
            @Override
            public boolean match(ServerWebExchange exchange) {
                return exchange.getRequest().getQueryParams().containsKey("love");
            }

            @Override
            public Route getRoute() {
                return thirdRoute;
            }

            @Override
            public int getOrder() {
                return 2;
            }
        };
    }
}
