package com.apigateway.api_gateway.controller;

import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.timelimiter.annotation.TimeLimiter;
import org.springframework.cloud.client.circuitbreaker.CircuitBreakerFactory;
import org.springframework.cloud.client.circuitbreaker.ReactiveCircuitBreaker;
import org.springframework.cloud.client.circuitbreaker.ReactiveCircuitBreakerFactory;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;


@RestController
public class GatewayUserController {
    private final WebClient webClient;



    public GatewayUserController(WebClient.Builder builder) {
        this.webClient = builder.build();
    }



    @CircuitBreaker(name = "userServiceCB", fallbackMethod = "userServiceFallback")
    @TimeLimiter(name = "userServiceCB")
    @GetMapping("/{id}")
    public Mono<String> getUser(@PathVariable Integer id) {
        return webClient.get()
                .uri("http://USER-SERVICE/users/{id}", id)
                .retrieve()
                .bodyToMono(String.class);
    }

    public Mono<String> userServiceFallback(Integer id, Throwable t) {
        return Mono.just("user service is temporarily unavailable");
    }


}
