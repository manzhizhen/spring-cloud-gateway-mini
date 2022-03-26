package com.manzhizhen.spring.cloud.gateway.mini;

import com.manzhizhen.spring.cloud.gateway.mini.config.HttpClientProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@EnableConfigurationProperties(HttpClientProperties.class)
@SpringBootApplication
public class SpringCloudGatewayMiniApplication {

    public static void main(String[] args) {
        SpringApplication.run(SpringCloudGatewayMiniApplication.class, args);
    }

}
