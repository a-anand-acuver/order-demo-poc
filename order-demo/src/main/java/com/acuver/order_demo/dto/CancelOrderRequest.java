package com.acuver.order_demo.dto;

import lombok.Data;

@Data
public class CancelOrderRequest {
    private String orderId;
    private String reason;
} 