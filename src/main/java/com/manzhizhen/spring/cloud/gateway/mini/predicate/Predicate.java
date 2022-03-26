package com.manzhizhen.spring.cloud.gateway.mini.predicate;

import com.manzhizhen.spring.cloud.gateway.mini.route.Route;
import org.springframework.core.Ordered;
import org.springframework.web.server.ServerWebExchange;

/**
 * 谓词接口
 * 注意，一个谓词只能对应一个路由，但如果有必要，多个谓词可以绑定到同一个路由上
 *
 * @author: yizhenqiang
 * @date: 2022/3/25 11:22 下午
 */
public interface Predicate extends Ordered {

    /**
     * 是否命中
     *
     * @param exchange
     * @return
     */
    boolean match(ServerWebExchange exchange);

    /**
     * 获取谓词对应的路由信息
     * @return
     */
    Route getRoute();
}
