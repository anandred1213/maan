package com.practise.userservice.service.serviceImpl;

import com.practise.userservice.dto.OrderDto;
import com.practise.userservice.service.OrderService;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.Arrays;
import java.util.List;

@Service
public class OrderServiceImpl implements OrderService {
    
    private static final Logger log = LoggerFactory.getLogger(OrderServiceImpl.class);
    private final RestTemplate restTemplate;

    public OrderServiceImpl(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    @Override
    @CircuitBreaker(name="getUserOrdersCB", fallbackMethod = "getUserOrdersFallback")
    public List<OrderDto> getUserOrders(Long userId) {
        try {
            String url = "http://order-microservice/orders/user/" + userId;
            ResponseEntity<List<OrderDto>> response = restTemplate.exchange(
                url,
                HttpMethod.GET,
                null,
                new ParameterizedTypeReference<List<OrderDto>>() {}
            );
            
            List<OrderDto> orders = response.getBody();
            if (orders != null) {
                log.info("User Orders Details - User ID: {}, Total Orders: {}", userId, orders.size());
                orders.forEach(order -> log.debug("Order: {}", order));
            }
            return orders;
        } catch (org.springframework.web.client.RestClientException e) {
            log.error("Error fetching orders for user {}", userId, e);
            throw new RuntimeException("Failed to fetch orders for user: " + userId, e);
        }
    }

    public List<OrderDto> getUserOrdersFallback(Long userId, Throwable throwable) {
        log.warn("Fallback: Unable to fetch orders for user {}. Reason: {}", userId, throwable.getMessage());
        return Arrays.asList(new OrderDto(0L, "Service unavailable - fallback response", userId));
    }
}
