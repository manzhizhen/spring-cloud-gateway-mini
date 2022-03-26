package com.manzhizhen.spring.cloud.gateway.mini.util;

import static com.manzhizhen.spring.cloud.gateway.mini.constant.ScgMiniConstant.GATEWAY_ALREADY_ROUTED_ATTR;
import org.springframework.web.server.ServerWebExchange;

/**
 * 工具类
 *
 * @author: yizhenqiang
 * @date: 2022/3/27 12:28 上午
 */
public class ScgMiniUtils {

    public static boolean isAlreadyRouted(ServerWebExchange exchange) {
        return exchange.getAttributeOrDefault(GATEWAY_ALREADY_ROUTED_ATTR, false);
    }

    public static void setAlreadyRouted(ServerWebExchange exchange) {
        exchange.getAttributes().put(GATEWAY_ALREADY_ROUTED_ATTR, true);
    }
}
