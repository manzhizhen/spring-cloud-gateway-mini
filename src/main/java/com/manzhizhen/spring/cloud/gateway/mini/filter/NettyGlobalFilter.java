package com.manzhizhen.spring.cloud.gateway.mini.filter;

import com.manzhizhen.spring.cloud.gateway.mini.config.HttpClientProperties;
import static com.manzhizhen.spring.cloud.gateway.mini.constant.ScgMiniConstant.CLIENT_RESPONSE_ATTR;
import static com.manzhizhen.spring.cloud.gateway.mini.constant.ScgMiniConstant.CLIENT_RESPONSE_CONN_ATTR;
import static com.manzhizhen.spring.cloud.gateway.mini.constant.ScgMiniConstant.CLIENT_RESPONSE_HEADER_NAMES;
import static com.manzhizhen.spring.cloud.gateway.mini.constant.ScgMiniConstant.GATEWAY_REQUEST_URL_ATTR;
import static com.manzhizhen.spring.cloud.gateway.mini.constant.ScgMiniConstant.GATEWAY_ROUTE_ATTR;
import static com.manzhizhen.spring.cloud.gateway.mini.constant.ScgMiniConstant.ORIGINAL_RESPONSE_CONTENT_TYPE;
import static com.manzhizhen.spring.cloud.gateway.mini.constant.ScgMiniConstant.PRESERVE_HOST_HEADER_ATTRIBUTE;
import com.manzhizhen.spring.cloud.gateway.mini.exception.TimeoutException;
import com.manzhizhen.spring.cloud.gateway.mini.filter.header.HttpHeadersFilter;
import com.manzhizhen.spring.cloud.gateway.mini.filter.header.HttpHeadersFilter.Type;
import static com.manzhizhen.spring.cloud.gateway.mini.filter.header.HttpHeadersFilter.filterRequest;
import com.manzhizhen.spring.cloud.gateway.mini.route.Route;
import static com.manzhizhen.spring.cloud.gateway.mini.route.Route.CONNECT_TIMEOUT_ATTR;
import static com.manzhizhen.spring.cloud.gateway.mini.route.Route.RESPONSE_TIMEOUT_ATTR;
import static com.manzhizhen.spring.cloud.gateway.mini.util.ScgMiniUtils.isAlreadyRouted;
import static com.manzhizhen.spring.cloud.gateway.mini.util.ScgMiniUtils.setAlreadyRouted;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelOption;
import io.netty.handler.codec.http.DefaultHttpHeaders;
import io.netty.handler.codec.http.HttpMethod;
import java.net.URI;
import java.time.Duration;
import java.util.List;
import javax.annotation.PostConstruct;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.core.io.buffer.DefaultDataBuffer;
import org.springframework.core.io.buffer.NettyDataBuffer;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.AbstractServerHttpResponse;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.http.server.reactive.ServerHttpResponseDecorator;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.netty.http.client.HttpClient;
import reactor.netty.http.client.HttpClientResponse;

/**
 * Netty ????????????????????????????????????
 * <p>
 * from NettyRoutingFilter
 *
 * @author: yizhenqiang
 * @date: 2022/3/26 3:28 ??????
 */
@Component
public class NettyGlobalFilter implements GlobalFilter {

    private static final Log log = LogFactory.getLog(NettyGlobalFilter.class);

    @Autowired
    private HttpClient httpClient;

    @Autowired
    private HttpClientProperties properties;

    @Autowired(required = false)
    private List<HttpHeadersFilter> headersFilters;

    /**
     * ??????????????????SCG????????????????????????HttpClient???????????????????????????????????????????????????????????????????????????HttpClient??????????????????????????????????????????????????????????????????????????????
     */
    private ThreadPoolTaskExecutor httpClientThreadPoolTaskExecutor;

    @PostConstruct
    public void init() {
        httpClientThreadPoolTaskExecutor = new ThreadPoolTaskExecutor();
        httpClientThreadPoolTaskExecutor.setCorePoolSize(6);
        httpClientThreadPoolTaskExecutor.setMaxPoolSize(6);
        httpClientThreadPoolTaskExecutor.setQueueCapacity(100);
        httpClientThreadPoolTaskExecutor.setThreadNamePrefix("yzq-test-");
        httpClientThreadPoolTaskExecutor.initialize();
    }

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        URI requestUrl = exchange.getRequiredAttribute(GATEWAY_REQUEST_URL_ATTR);

        String scheme = requestUrl.getScheme();
        if (isAlreadyRouted(exchange) || (!"http".equalsIgnoreCase(scheme) && !"https".equalsIgnoreCase(scheme))) {
            // ?????????????????? http ?????? https??????????????????dubbo??????
            return chain.filter(exchange);
        }
        setAlreadyRouted(exchange);

        ServerHttpRequest request = exchange.getRequest();

        final HttpMethod method = HttpMethod.valueOf(request.getMethod().name());
        final String url = requestUrl.toASCIIString();

        /**
         * ??????????????????HTTP header???????????????????????????????????? {@link HttpHeadersFilter} ??????
         */
        HttpHeaders filtered = filterRequest(headersFilters, exchange);
        final DefaultHttpHeaders httpHeaders = new DefaultHttpHeaders();
        filtered.forEach(httpHeaders::set);

        boolean preserveHost = exchange.getAttributeOrDefault(PRESERVE_HOST_HEADER_ATTRIBUTE, false);
        Route route = exchange.getAttribute(GATEWAY_ROUTE_ATTR);

        // ?????????????????????????????????????????????????????????????????????????????????????????????????????????HttpClient
        Flux<HttpClientResponse> responseFlux = getHttpClient(route, exchange)
                .headers(headers -> {
                    headers.add(httpHeaders);
                    // Will either be set below, or later by Netty
                    headers.remove(HttpHeaders.HOST);
                    if (preserveHost) {
                        String host = request.getHeaders().getFirst(HttpHeaders.HOST);
                        headers.add(HttpHeaders.HOST, host);
                    }

                }).request(method).uri(url).send((req, nettyOutbound) -> {
                    if (log.isTraceEnabled()) {
                        nettyOutbound.withConnection(connection -> log.trace("outbound route: "
                                + connection.channel().id().asShortText() + ", inbound: " + exchange.getLogPrefix()));
                    }

                    // ?????????????????????
                    return nettyOutbound.send(request.getBody().map(this::getByteBuf));

                }).responseConnection((res, connection) -> {

                    // Defer committing the response until all route filters have run
                    // Put client response as ServerWebExchange attribute and write
                    // response later NettyWriteResponseFilter
                    exchange.getAttributes().put(CLIENT_RESPONSE_ATTR, res);
                    exchange.getAttributes().put(CLIENT_RESPONSE_CONN_ATTR, connection);

                    ServerHttpResponse response = exchange.getResponse();
                    // put headers and status so filters can modify the response
                    HttpHeaders headers = new HttpHeaders();

                    res.responseHeaders().forEach(entry -> headers.add(entry.getKey(), entry.getValue()));

                    String contentTypeValue = headers.getFirst(HttpHeaders.CONTENT_TYPE);
                    if (StringUtils.hasLength(contentTypeValue)) {
                        exchange.getAttributes().put(ORIGINAL_RESPONSE_CONTENT_TYPE, contentTypeValue);
                    }

                    setResponseStatus(res, response);

                    // make sure headers filters run after setting status so it is
                    // available in response
                    HttpHeaders filteredResponseHeaders = HttpHeadersFilter.filter(headersFilters, headers, exchange,
                            Type.RESPONSE);

                    if (!filteredResponseHeaders.containsKey(HttpHeaders.TRANSFER_ENCODING)
                            && filteredResponseHeaders.containsKey(HttpHeaders.CONTENT_LENGTH)) {
                        // It is not valid to have both the transfer-encoding header and
                        // the content-length header.
                        // Remove the transfer-encoding header in the response if the
                        // content-length header is present.
                        response.getHeaders().remove(HttpHeaders.TRANSFER_ENCODING);
                    }

                    exchange.getAttributes().put(CLIENT_RESPONSE_HEADER_NAMES, filteredResponseHeaders.keySet());

                    response.getHeaders().addAll(filteredResponseHeaders);

                    return Mono.just(res);
                });

        Duration responseTimeout = getResponseTimeout(route);
        if (responseTimeout != null) {
            responseFlux = responseFlux
                    .timeout(responseTimeout,
                            Mono.error(new TimeoutException("Response took longer than timeout: " + responseTimeout)))
                    .onErrorMap(TimeoutException.class,
                            th -> new ResponseStatusException(HttpStatus.GATEWAY_TIMEOUT, th.getMessage(), th));
        }

        return responseFlux
               // .publishOn(Schedulers.fromExecutor(httpClientThreadPoolTaskExecutor))  ????????????????????? responseFlux ?????????????????????????????????????????????????????????????????????????????????????????????
                .then(chain.filter(exchange));
    }

    static Integer getInteger(Object connectTimeoutAttr) {
        Integer connectTimeout;
        if (connectTimeoutAttr instanceof Integer) {
            connectTimeout = (Integer) connectTimeoutAttr;
        }
        else {
            connectTimeout = Integer.parseInt(connectTimeoutAttr.toString());
        }
        return connectTimeout;
    }

    /**
     * ???????????????????????????????????????????????? HttpClient??????????????????????????????
     * ???????????????????????????????????????????????????????????? super.getHttpClient()
     * <p>
     * ???????????????SCG?????????????????????????????????????????????????????????
     *
     * @param route    the current route.
     * @param exchange the current ServerWebExchange.
     * @return the configured HttpClient.
     */
    protected HttpClient getHttpClient(Route route, ServerWebExchange exchange) {
        Object connectTimeoutAttr = route.getMetadata().get(CONNECT_TIMEOUT_ATTR);
        if (connectTimeoutAttr != null) {
            Integer connectTimeout = getInteger(connectTimeoutAttr);
            return this.httpClient.option(ChannelOption.CONNECT_TIMEOUT_MILLIS, connectTimeout);
        }
        return httpClient;
    }

    protected ByteBuf getByteBuf(DataBuffer dataBuffer) {
        if (dataBuffer instanceof NettyDataBuffer) {
            NettyDataBuffer buffer = (NettyDataBuffer) dataBuffer;
            return buffer.getNativeBuffer();
        }
        // MockServerHttpResponse creates these
        else if (dataBuffer instanceof DefaultDataBuffer) {
            DefaultDataBuffer buffer = (DefaultDataBuffer) dataBuffer;
            return Unpooled.wrappedBuffer(buffer.getNativeBuffer());
        }
        throw new IllegalArgumentException("Unable to handle DataBuffer of type " + dataBuffer.getClass());
    }

    private Duration getResponseTimeout(Route route) {
        Object responseTimeoutAttr = route.getMetadata().get(RESPONSE_TIMEOUT_ATTR);
        if (responseTimeoutAttr != null && responseTimeoutAttr instanceof Number) {
            Long routeResponseTimeout = ((Number) responseTimeoutAttr).longValue();
            if (routeResponseTimeout >= 0) {
                return Duration.ofMillis(routeResponseTimeout);
            }
            else {
                return null;
            }
        }
        return properties.getResponseTimeout();
    }

    private void setResponseStatus(HttpClientResponse clientResponse, ServerHttpResponse response) {
        HttpStatus status = HttpStatus.resolve(clientResponse.status().code());
        if (status != null) {
            response.setStatusCode(status);
        }
        else {
            while (response instanceof ServerHttpResponseDecorator) {
                response = ((ServerHttpResponseDecorator) response).getDelegate();
            }
            if (response instanceof AbstractServerHttpResponse) {
                ((AbstractServerHttpResponse) response).setRawStatusCode(clientResponse.status().code());
            }
            else {
                // TODO: log warning here, not throw error?
                throw new IllegalStateException("Unable to set status code " + clientResponse.status().code()
                        + " on response of type " + response.getClass().getName());
            }
        }
    }

    @Override
    public int getOrder() {
        // ?????????????????????GlobalFilter???????????????????????????????????????????????????
        return Integer.MAX_VALUE;
    }
}
