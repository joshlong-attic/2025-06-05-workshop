package com.example.client;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.web.servlet.function.RouterFunction;
import org.springframework.web.servlet.function.ServerResponse;

import static org.springframework.cloud.gateway.server.mvc.filter.BeforeFilterFunctions.rewritePath;
import static org.springframework.cloud.gateway.server.mvc.filter.TokenRelayFilterFunctions.tokenRelay;
import static org.springframework.cloud.gateway.server.mvc.handler.GatewayRouterFunctions.route;
import static org.springframework.cloud.gateway.server.mvc.handler.HandlerFunctions.http;

@SpringBootApplication
public class ClientApplication {

    public static void main(String[] args) {
        SpringApplication.run(ClientApplication.class, args);
    }

    @Bean
    RouterFunction<ServerResponse> httpRoutes() {
        return route()
                .filter(tokenRelay())
                .before(rewritePath("/api", "/"))
                .GET("/api/**", http("http://localhost:8080/"))
                .GET("/**", http("http://localhost:8020/"))
                .build();
    }

//    @Bean
//    RouterFunction<ServerResponse> api() {
//        return route("api")
//                .GET("/api/**", http("http://localhost:8080"))
////                .before(uri("http://localhost:8080"))
//                .before(rewritePath("/api","/"))
//                .filter(tokenRelay())
//                .build();
//    }
//
//    @Bean
//    RouterFunction<ServerResponse> html() {
//        return route("html")
//                .GET("/**", http("http://localhost:8020"))
////                .before(uri("http://localhost:8020"))
//                .filter(tokenRelay())
//                .build();
//    }
//
}
