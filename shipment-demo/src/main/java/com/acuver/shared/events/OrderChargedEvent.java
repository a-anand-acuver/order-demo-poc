package com.acuver.shared.events;

import lombok.*;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
@Builder
public class OrderChargedEvent extends BaseEvent {
    private String customerId;
    private BigDecimal chargedAmount;
    private String paymentId;
    private String transactionId;

}