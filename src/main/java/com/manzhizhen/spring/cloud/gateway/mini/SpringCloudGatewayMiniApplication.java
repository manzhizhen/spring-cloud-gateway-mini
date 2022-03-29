package com.manzhizhen.spring.cloud.gateway.mini;

import com.manzhizhen.spring.cloud.gateway.mini.config.HttpClientProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;


/**
 * curl -XPOST http://localhost:8080/manzhizhen/openfeign/queryBaseInfo?love=ch --header 'Content-Type: application/json' --data-raw '{"orderId":"sdfafsdfsdfaf"}'
 */
@EnableConfigurationProperties(HttpClientProperties.class)
@SpringBootApplication
public class SpringCloudGatewayMiniApplication {

    public static void main(String[] args) {
        SpringApplication.run(SpringCloudGatewayMiniApplication.class, args);
    }

}
