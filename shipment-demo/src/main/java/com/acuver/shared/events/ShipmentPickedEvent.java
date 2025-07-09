package com.acuver.shared.events;

import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
@Builder
public class ShipmentPickedEvent extends BaseEvent {
    private String customerId;
    private String trackingNumber;
    private String carrierName;
    private String warehouseLocation;

}