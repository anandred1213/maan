package com.practise.userservice.dto;

public class OrderDto {
    private Long orderId;
    private String orderDetails;
    private Long userId;

    public OrderDto() {}

    public OrderDto(Long orderId, String orderDetails, Long userId) {
        this.orderId = orderId;
        this.orderDetails = orderDetails;
        this.userId = userId;
    }

    public Long getOrderId() {
        return orderId;
    }

    public void setOrderId(Long orderId) {
        this.orderId = orderId;
    }

    public String getOrderDetails() {
        return orderDetails;
    }

    public void setOrderDetails(String orderDetails) {
        this.orderDetails = orderDetails;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    @Override
    public String toString() {
        return "OrderDto{" +
                "orderId=" + orderId +
                ", orderDetails='" + orderDetails + '\'' +
                ", userId=" + userId +
                '}';
    }
}
