package com.practice.gateway;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
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
                .build();
    }
}
