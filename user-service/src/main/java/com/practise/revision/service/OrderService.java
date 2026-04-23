package com.practise.userservice.service;

import com.practise.userservice.dto.OrderDto;
import java.util.List;

public interface OrderService {
    List<OrderDto> getUserOrders(Long userId);
}
