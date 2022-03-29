# spring-cloud-gateway-mini

## 背景（Background）
为了帮助广大Spring Cloud Gateway爱好者更方便的学习SCG，了解spring-webflux和reactor-netty的使用，特意精简了SCG，保留了最核心的代码实现（只有2千多行代码），所以此项目叫做 **spring-cloud-gateway-mini**。<br >
In order to help Spring Cloud Gateway enthusiasts learn SCG more conveniently and understand the use of spring-webflux and reactor-netty, SCG is deliberately simplified and the core code implementation is retained（just over 2,000 lines of code）, so this project is called **spring-cloud-gateway-mini**.

## 使用方法（Instruction Manual）
### 1.配置注册中心
由于我们pom.xml中依赖的是Consul，所以我们默认是将Consul作为注册中心，如果想尝试使用其他注册中心，那么需要**替换**如下的Maven依赖：<br >
Since we depend on Consul in our pom.xml, we use Consul as the registry by default. If you want to try other registries, you need to **replace** the following Maven dependencies:
```xml
        <dependency>
            <groupId>org.springframework.cloud</groupId>
            <artifactId>spring-cloud-starter-consul-all</artifactId>
            <version>3.1.0</version>
        </dependency>
```
如果我们用的是Consul，那么还需要修改 **resource/application.yml** 文件中的配置，配置成正确的Consul地址，相关配置如下：<br >
If we are using Consul, we also need to modify the configuration in the **resource/application.yml** file to configure the correct Consul address. The relevant configuration is as follows:
```yaml
spring:
  config:
    import: optional:consul:localhost:8500
```
上面的配置表明我们在本地就有个Consul服务。<br >
The above configuration indicates that we have a Consul service locally.
### 2.配置路由
其实mini并没有实现SCG配置路由的能力，所以这块其实是需要在mini中编码实现，例如，在 **RouteConfig** 中的如下代码代码演示了如何创建一个通过负载均衡到达名为 *spring-cloud-openfeign-provider* 服务的路由：<br >
In fact, mini does not have the ability to configure SCG routes, so it needs to be coded in mini. For example, the following code in **RouteConfig** demonstrates how to create a route to the service named *spring-cloud-openfeign-provider* through load balancing:
```java
    @Bean
    public Route myThirdRoute() {
        try {
            return new Route("3", new URI("lb://spring-cloud-openfeign-provider"), new HashMap<>());
        } catch (URISyntaxException e) {
            throw new IllegalStateException(e);
        }
    }
```
可以看出所有在mini中的路由定义是通过代码来实现的。<br >
It can be seen that all routing definitions in mini are implemented through code.
### 3.配置谓词
和路由一样，mini也没有实现SCG配置谓词的能力，所以这块其实是需要在mini中编码实现，例如，在 **PredicateConfig** 中的如下代码代码演示了如何创建一个匹配查询参数中包含 *love* 的谓词：<br >
Like routing, mini does not have the ability to implement SCG configuration predicates, so it actually needs to be coded and implemented in mini. For example, the following code in **PredicateConfig** demonstrates how to create a predicate that matches the *love* contained in the query parameters:
```java
    @Bean
    public Predicate myThirdPredicate(@Qualifier("myThirdRoute")final Route thirdRoute) {
        return new Predicate() {
            @Override
            public boolean match(ServerWebExchange exchange) {
                return exchange.getRequest().getQueryParams().containsKey("love");
            }

            @Override
            public Route getRoute() {
                return thirdRoute;
            }

            @Override
            public int getOrder() {
                return 2;
            }
        };
    }
```
我们可以注意到，上述的谓词和 *第2步* 创建的路由是绑定的，mini遵循了SCG的设计，一个谓词只能绑定到一个路由（但不同的谓词可以绑定到同一个路由）。<br >
We can notice that the above predicates and the route created in *step 2* are bound. Mini follows the design of SCG. A predicate can only be bound to one route (but different predicates can be bound to the same route).
### 4.运行mini
直接运行 **SpringCloudGatewayMiniApplication** 即可。<br >
Just run **SpringCloudGatewayMiniApplication** directly.

## 特殊说明（Special Instructions）
1. 不用怀疑，spring-cloud-gateway-mini 中绝大部分代码来自 spring-cloud-gateway，请允许我擅自修改了部分类名。(No doubt, most of the code in spring-cloud-gateway-mini comes from spring-cloud-gateway, please allow me to modify some class names without authorization.)
2. spring-cloud-gateway-mini 只能用于学习，请勿用于生产或商业用途。（spring-cloud-gateway-mini can only be used for learning, not for production or commercial use）
3. spring-cloud-gateway-mini 中包含了 spring-cloud-gateway 的核心实现，如果有你认为的"核心实现"未在mini中体现，请联系作者或自行补充，毕竟每个人对spring-cloud-gateway理解会有一定程度的偏差。（spring-cloud-gateway-mini contains the core implementation of spring-cloud-gateway. If there is a "core implementation" that you think is not reflected in the mini, please contact the author or add it yourself. After all, everyone is not interested in spring-cloud-gateway. There will be a certain degree of deviation in understanding）
4. 很遗憾，如有侵权行为，请第一时间联系作者：835576511@qq.com、dubbocommitter@gmail.com。（Unfortunately, if there is any infringement, please contact the author as soon as possible: 835576511@qq.com, dubbocommitter@gmail.com）