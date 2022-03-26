package com.manzhizhen.spring.cloud.gateway.mini.constant;

/**
 * 常量管理
 *
 * @author: yizhenqiang
 * @date: 2022/3/26 12:34 上午
 */
public class ScgMiniConstant {

    /**
     * Gateway route attribute name.
     */
    public static final String GATEWAY_ROUTE_ATTR = "GATEWAY_ROUTE_ATTR";

    /**
     * Gateway request URL attribute name.
     */
    public static final String GATEWAY_REQUEST_URL_ATTR = "GATEWAY_REQUEST_URL_ATTR";

    /**
     * Gateway LoadBalancer {@link Response} attribute name.
     */
    public static final String GATEWAY_LOADBALANCER_RESPONSE_ATTR = "GATEWAY_LOADBALANCER_RESPONSE_ATTR";

    /**
     * Used when a routing filter has been successfully called. Allows users to write
     * custom routing filters that disable built in routing filters.
     */
    public static final String GATEWAY_ALREADY_ROUTED_ATTR = "GATEWAY_ALREADY_ROUTED_ATTR";

    /**
     * Preserve-Host header attribute name.
     */
    public static final String PRESERVE_HOST_HEADER_ATTRIBUTE = "PRESERVE_HOST_HEADER_ATTRIBUTE";

    /**
     * Client response attribute name.
     */
    public static final String CLIENT_RESPONSE_ATTR = "CLIENT_RESPONSE_ATTR";

    /**
     * Client response connection attribute name.
     */
    public static final String CLIENT_RESPONSE_CONN_ATTR = "CLIENT_RESPONSE_CONN_ATTR";

    /**
     * Client response header names attribute name.
     */
    public static final String CLIENT_RESPONSE_HEADER_NAMES = "CLIENT_RESPONSE_HEADER_NAMES";

    /**
     * Original response Content-Type attribute name.
     */
    public static final String ORIGINAL_RESPONSE_CONTENT_TYPE = "ORIGINAL_RESPONSE_CONTENT_TYPE";
}
