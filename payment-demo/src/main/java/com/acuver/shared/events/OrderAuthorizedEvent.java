package com.acuver.shared.events;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
@Builder
public class OrderAuthorizedEvent extends BaseEvent {
    private String customerId;
    private BigDecimal authorizedAmount;
    private String paymentMethod;
    private String authorizationCode;

}