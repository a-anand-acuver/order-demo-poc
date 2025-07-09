package com.acuver.shared.events;

import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
@Builder
public class ShipmentCreatedEvent extends BaseEvent {
    private String customerId;
    private String trackingNumber;
    private String carrierName;
} 