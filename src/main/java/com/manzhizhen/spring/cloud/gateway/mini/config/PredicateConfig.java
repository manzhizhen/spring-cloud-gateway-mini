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

    @Bean
    public Predicate myThirdPredicate(@Qualifier("myThirdRoute")final Route thirdRoute) {
        return new Predicate() {
            @Override
            public boolean match(ServerWebExchange exchange) {
                return exchange.getRequest().getPath().toString().startsWith("/mzz");
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
