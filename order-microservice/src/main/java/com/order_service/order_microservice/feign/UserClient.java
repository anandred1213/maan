package com.order_service.order_microservice.feign;

import com.order_service.order_microservice.dto.UserResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

//@FeignClient(name = "revision")
public interface UserClient {

    @GetMapping("/users/{id}")
    UserResponse getUserResponse(@PathVariable("id") int id);
}
