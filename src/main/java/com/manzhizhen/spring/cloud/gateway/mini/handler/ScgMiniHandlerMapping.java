package com.manzhizhen.spring.cloud.gateway.mini.handler;

import static com.manzhizhen.spring.cloud.gateway.mini.constant.ScgMiniConstant.GATEWAY_ROUTE_ATTR;
import com.manzhizhen.spring.cloud.gateway.mini.predicate.Predicate;
import com.manzhizhen.spring.cloud.gateway.mini.route.Route;
import java.util.List;
import javax.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.handler.AbstractHandlerMapping;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * 该类可以说是SCG自定义类的总入口，由Spring WebFlux 的 DispatcherHandler 来调用，而 DispatcherHandler 内部的处理流程如下：
 * 1.Each HandlerMapping is asked to find a matching handler, and the first match is used.
 * 2.If a handler is found, it is run through an appropriate HandlerAdapter, which exposes the return value from the execution as HandlerResult.
 * 3. The HandlerResult is given to an appropriate HandlerResultHandler to complete processing by writing to the response directly or by using a view to render.
 *
 * from RoutePredicateHandlerMapping
 *
 * @author: yizhenqiang
 * @date: 2022/3/25 11:11 下午
 */
@Component
public class ScgMiniHandlerMapping extends AbstractHandlerMapping {

    @Autowired
    private ScgMiniWebHandler webHandler;

    @Autowired
    private List<Predicate> predicates;

    private Flux<Predicate> predicateFlux;

    @PostConstruct
    public void init() {
        predicateFlux = Flux.fromIterable(predicates);
    }

    @Override
    protected Mono<?> getHandlerInternal(ServerWebExchange exchange) {
        return lookupRouteByPredicate(exchange)
                .flatMap(route -> {
                    exchange.getAttributes().put(GATEWAY_ROUTE_ATTR, route);
                    return Mono.just(webHandler);
                })
                .switchIfEmpty(Mono.empty().then(Mono.fromRunnable(() ->
                        logger.info("No RouteDefinition found for [" + getExchangeDesc(exchange) + "]")
                )));
    }

    /**
     * 路由匹配
     * 谓词一旦多起来，这个方法很容易成为性能瓶颈
     *
     * @param exchange
     * @return
     */
    protected Mono<Route> lookupRouteByPredicate(ServerWebExchange exchange) {
        return predicateFlux
                .concatMap(predicate -> Mono.just(predicate).filterWhen(pre -> Mono.just(pre.match(exchange))))
                .next()
                .flatMap(predicate -> Mono.just(predicate.getRoute()));
    }

    private String getExchangeDesc(ServerWebExchange exchange) {
        StringBuilder out = new StringBuilder();
        out.append("Exchange: ");
        out.append(exchange.getRequest().getMethod());
        out.append(" ");
        out.append(exchange.getRequest().getURI());
        return out.toString();
    }

    @Override
    public int getOrder() {
        return super.getOrder() - 100;
    }

}
