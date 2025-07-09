package com.acuver.shared.events;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

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
    
    public ShipmentPickedEvent(String orderId, String customerId, String trackingNumber, 
                             String carrierName, String warehouseLocation) {
        super("SHIPMENT_PICKED", orderId, LocalDateTime.now());
        this.customerId = customerId;
        this.trackingNumber = trackingNumber;
        this.carrierName = carrierName;
        this.warehouseLocation = warehouseLocation;
    }
} 