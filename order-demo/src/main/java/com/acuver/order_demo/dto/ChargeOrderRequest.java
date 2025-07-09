package com.acuver.order_demo.dto;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class ChargeOrderRequest {
    private String orderId;
    private String customerId; // optional override
    private BigDecimal amount;
    private String paymentId; // optional
    private String transactionId; // optional
} 