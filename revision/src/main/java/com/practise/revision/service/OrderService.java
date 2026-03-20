package com.practise.revision.service;

import com.practise.revision.dto.OrderDto;
import java.util.List;

public interface OrderService {
    List<OrderDto> getUserOrders(Long userId);
}
