package com.example.client;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.core.annotation.Order;
import org.springframework.web.servlet.function.RouterFunction;
import org.springframework.web.servlet.function.ServerResponse;

import static org.springframework.cloud.gateway.server.mvc.filter.BeforeFilterFunctions.rewritePath;
import static org.springframework.cloud.gateway.server.mvc.filter.BeforeFilterFunctions.uri;
import static org.springframework.cloud.gateway.server.mvc.filter.TokenRelayFilterFunctions.tokenRelay;
import static org.springframework.cloud.gateway.server.mvc.handler.GatewayRouterFunctions.route;
import static org.springframework.cloud.gateway.server.mvc.handler.HandlerFunctions.http;

@SpringBootApplication
public class ClientApplication {

    public static void main(String[] args) {
        SpringApplication.run(ClientApplication.class, args);
    }

    @Bean
    @Order(2) // NB: this must be evaluated *after* the more specific api() bean
    RouterFunction<ServerResponse> html() {
        return route("html")
                .GET("/**", http())
                .before(uri("http://localhost:8020"))
                .filter(tokenRelay())
                .build();
    }


    @Bean
    @Order(1)
    RouterFunction<ServerResponse> api() {
        return route("api")
                .GET("/api/**", http())
                .before(rewritePath("/api", "/"))
                .filter(tokenRelay())
                .before(uri("http://localhost:8080"))
                .build();
    }


}
