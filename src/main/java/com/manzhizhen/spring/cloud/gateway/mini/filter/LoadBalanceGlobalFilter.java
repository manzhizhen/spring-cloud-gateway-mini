package com.manzhizhen.spring.cloud.gateway.mini.filter;

import static com.manzhizhen.spring.cloud.gateway.mini.constant.ScgMiniConstant.GATEWAY_LOADBALANCER_RESPONSE_ATTR;
import static com.manzhizhen.spring.cloud.gateway.mini.constant.ScgMiniConstant.GATEWAY_REQUEST_URL_ATTR;
import com.manzhizhen.spring.cloud.gateway.mini.exception.NotFoundException;
import java.net.URI;
import java.util.Set;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.loadbalancer.CompletionContext;
import org.springframework.cloud.client.loadbalancer.DefaultRequest;
import org.springframework.cloud.client.loadbalancer.LoadBalancerLifecycle;
import org.springframework.cloud.client.loadbalancer.LoadBalancerLifecycleValidator;
import static org.springframework.cloud.client.loadbalancer.LoadBalancerUriTools.reconstructURI;
import org.springframework.cloud.client.loadbalancer.Request;
import org.springframework.cloud.client.loadbalancer.RequestData;
import org.springframework.cloud.client.loadbalancer.RequestDataContext;
import org.springframework.cloud.client.loadbalancer.Response;
import org.springframework.cloud.client.loadbalancer.ResponseData;
import org.springframework.cloud.loadbalancer.core.ReactorLoadBalancer;
import org.springframework.cloud.loadbalancer.core.ReactorServiceInstanceLoadBalancer;
import org.springframework.cloud.loadbalancer.support.LoadBalancerClientFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

/**
 * 负载均衡过滤器
 * <p>
 * from ReactiveLoadBalancerClientFilter
 *
 * @author: yizhenqiang
 * @date: 2022/3/26 3:27 下午
 */
@Component
public class LoadBalanceGlobalFilter implements GlobalFilter {

    @Autowired
    private LoadBalancerClientFactory clientFactory;

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        URI requestUri = exchange.getRequiredAttribute(GATEWAY_REQUEST_URL_ATTR);
        String scheme = requestUri.getScheme();
        if (!"lb".equals(scheme)) {
            // 如果不是lb的scheme，就下一个
            return chain.filter(exchange);
        }

        String serviceId = requestUri.getHost();

        Set<LoadBalancerLifecycle> supportedLifecycleProcessors = LoadBalancerLifecycleValidator
                .getSupportedLifecycleProcessors(clientFactory.getInstances(serviceId, LoadBalancerLifecycle.class),
                        RequestDataContext.class, ResponseData.class, ServiceInstance.class);

        DefaultRequest<RequestDataContext> lbRequest = new DefaultRequest<>(
                new RequestDataContext(new RequestData(exchange.getRequest()), "default"));

        return choose(lbRequest, serviceId, supportedLifecycleProcessors)
                .doOnNext(response -> {

                    if (!response.hasServer()) {
                        // 哪怕执行失败，也需要调用负载均衡器生命周期的onComplete方法
                        supportedLifecycleProcessors.forEach(lifecycle -> lifecycle
                                .onComplete(new CompletionContext<>(CompletionContext.Status.DISCARD, lbRequest, response)));
                        throw NotFoundException.create(true, "Unable to find instance for " + requestUri.getHost());
                    }

                    // 获取需要调用的服务实例
                    ServiceInstance serviceInstance = response.getServer();

                    // 修改 URI 以将请求重定向到选择的服务实例
                    URI requestUrl = reconstructURI(serviceInstance, requestUri);

                    // 这里需要把lb（如有）替换成 http 或 https，为了简单起见，就替换成http
                    requestUrl = "lb".equals(requestUri.getScheme()) ? URI.create(requestUrl.toString().replaceFirst("lb", "http")) : requestUrl;

                    exchange.getAttributes().put(GATEWAY_REQUEST_URL_ATTR, requestUrl);
                    exchange.getAttributes().put(GATEWAY_LOADBALANCER_RESPONSE_ATTR, response);

                    // onStartRequest是在选择服务实例之后执行实际负载均衡请求之前执行的回调方法
                    supportedLifecycleProcessors.forEach(lifecycle -> lifecycle.onStartRequest(lbRequest, response));

                })
                .then(chain.filter(exchange))
                .doOnError(throwable -> supportedLifecycleProcessors.forEach(lifecycle -> lifecycle
                        .onComplete(new CompletionContext<ResponseData, ServiceInstance, RequestDataContext>(
                                CompletionContext.Status.FAILED, throwable, lbRequest,
                                exchange.getAttribute(GATEWAY_LOADBALANCER_RESPONSE_ATTR)))))
                .doOnSuccess(aVoid -> supportedLifecycleProcessors.forEach(lifecycle -> lifecycle
                        .onComplete(new CompletionContext<ResponseData, ServiceInstance, RequestDataContext>(
                                CompletionContext.Status.SUCCESS, lbRequest,
                                exchange.getAttribute(GATEWAY_LOADBALANCER_RESPONSE_ATTR),
                                new ResponseData(exchange.getResponse(), new RequestData(exchange.getRequest()))))));
    }

    /**
     * 选择一个可以调用的下游服务实例
     *
     * @param lbRequest
     * @param serviceId
     * @param supportedLifecycleProcessors
     * @return
     */
    private Mono<Response<ServiceInstance>> choose(Request<RequestDataContext> lbRequest, String serviceId,
                                                   Set<LoadBalancerLifecycle> supportedLifecycleProcessors) {
        // 这里用的一个小技巧是这个 ReactorServiceInstanceLoadBalancer 空接口继承自 ReactorLoadBalancer<ServiceInstance>，这样返回值就可以直接写成 ReactorLoadBalancer<ServiceInstance> 了
        ReactorLoadBalancer<ServiceInstance> loadBalancer = this.clientFactory.getInstance(serviceId,
                ReactorServiceInstanceLoadBalancer.class);
        if (loadBalancer == null) {
            throw new NotFoundException("No loadbalancer available for " + serviceId);
        }

        // 在执行真正的请求是调用负载均衡器生命周期的onStart方法
        supportedLifecycleProcessors.forEach(lifecycle -> lifecycle.onStart(lbRequest));

        // 开始调用绑定的负载均衡器来选择一个可以调用的实例
        return loadBalancer.choose(lbRequest);
    }

    @Override
    public int getOrder() {
        return 20000;
    }
}
