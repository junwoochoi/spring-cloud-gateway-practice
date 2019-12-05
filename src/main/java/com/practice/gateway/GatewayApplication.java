package com.practice.gateway;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.gateway.filter.ratelimit.KeyResolver;
import org.springframework.cloud.gateway.filter.ratelimit.RedisRateLimiter;
import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

import java.util.Objects;

@SpringBootApplication
@RestController
public class GatewayApplication {

    public static void main(String[] args) {
        SpringApplication.run(GatewayApplication.class, args);
    }

    @Bean
    public RouteLocator myRouteLocator(RouteLocatorBuilder builder) {
        return builder.routes()
                .route("path", r -> r.path("/headers")
                        .filters(f -> f.addRequestHeader("X-Foo", "Bar"))
                        .uri("http://httpbin.org")
                )
                .route("host", r -> r.host("{sub}.myhost.org")
                        .filters(f -> f.addRequestHeader("X-Foo", "Bar-{sub}"))
                        .uri("http://httpbin.org")
                )
                .route("hystrix", r -> r.host("**.hystrix.org")
                        .filters(f -> f.hystrix(
                                c -> c.setName("mycircuit")
                                        .setFallbackUri("forward:/myfallback")))
                        .uri("http://httpbin.org")

                )
                .route("websockets", r -> r.path("/echo")
                        .uri("ws://localhost:9000")
                )
                .route("limited", r -> r.host("**.limited.org")
                        .filters(f -> f.requestRateLimiter()
                                .rateLimiter(RedisRateLimiter.class, rl ->
                                        rl.setBurstCapacity(1).setReplenishRate(1)).and())
                        .uri("http://httpbin.org")
                )
                .build();
    }

    @Bean
    public KeyResolver keyResolver(){
        return exchange -> {
            String token = exchange.getRequest().getHeaders().getFirst("X-Token");
            if (Objects.nonNull(token)) {
                return Mono.just(token);
            }
            return Mono.empty();
        };
    }

    @GetMapping("/myfallback")
    public String myfallback() {
        return "Hello? You Failed!!";
    }
}
