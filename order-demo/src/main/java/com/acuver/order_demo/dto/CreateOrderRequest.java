package com.acuver.order_demo.dto;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class CreateOrderRequest {
    private String orderId; // optional
    private String customerId;
    private String productId;
    private Integer quantity;
    private BigDecimal price;
    private BigDecimal totalAmount;
} 