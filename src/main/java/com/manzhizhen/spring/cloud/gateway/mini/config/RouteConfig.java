package com.manzhizhen.spring.cloud.gateway.mini.config;

import com.manzhizhen.spring.cloud.gateway.mini.route.Route;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashMap;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 路由配置
 *
 * @author: yizhenqiang
 * @date: 2022/3/25 11:44 下午
 */
@Configuration
public class RouteConfig {

    @Bean
    public Route myFirstRoute() {
        try {
            return new Route("1", new URI("http://localhost:8081"), new HashMap<>());
        } catch (URISyntaxException e) {
            throw new IllegalStateException(e);
        }
    }

    @Bean
    public Route mySecondRoute() {
        try {
            return new Route("2", new URI("http://localhost:8082"), new HashMap<>());
        } catch (URISyntaxException e) {
            throw new IllegalStateException(e);
        }
    }

    @Bean
    public Route myThirdRoute() {
        try {
            return new Route("3", new URI("http://localhost:8083"), new HashMap<>());
        } catch (URISyntaxException e) {
            throw new IllegalStateException(e);
        }
    }
}
