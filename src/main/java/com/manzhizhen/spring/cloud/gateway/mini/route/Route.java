package com.manzhizhen.spring.cloud.gateway.mini.route;

import com.manzhizhen.spring.cloud.gateway.mini.filter.GlobalFilter;
import java.net.URI;
import java.util.Map;

/**
 * 路由地址信息
 * <p>
 * 注意，由于在 {@link GlobalFilter} 中我们已经说明mini中不再实现 SCG 中的GatewayFilter，
 * 所以在 mini 的 Route 中不会有 private final List<GatewayFilter> gatewayFilters;
 *
 * @author: yizhenqiang
 * @date: 2022/3/25 11:34 下午
 */
public class Route {

    /**
     * Response timeout attribute name.
     */
    public static final String RESPONSE_TIMEOUT_ATTR = "response-timeout";

    /**
     * Connect timeout attribute name.
     */
    public static final String CONNECT_TIMEOUT_ATTR = "connect-timeout";

    private final String id;

    private final URI uri;

    private final Map<String, Object> metadata;

    public Route(String id, URI uri, Map<String, Object> metadata) {
        this.id = id;
        this.uri = uri;
        this.metadata = metadata;
    }

    public String getId() {
        return id;
    }

    public URI getUri() {
        return uri;
    }

    public Map<String, Object> getMetadata() {
        return metadata;
    }
}
