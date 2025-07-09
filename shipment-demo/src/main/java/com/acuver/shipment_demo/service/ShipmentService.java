package com.acuver.shipment_demo.service;

import com.acuver.shipment_demo.entity.Shipment;
import com.acuver.shipment_demo.entity.Shipment.ShipmentStatus;
import com.acuver.shipment_demo.repository.ShipmentRepository;
import com.acuver.shared.enums.EventType;
import com.acuver.shared.events.OrderAuthorizedEvent;
import com.acuver.shared.events.OrderChargedEvent;
import com.acuver.shared.events.ShipmentCreatedEvent;
import com.acuver.shared.events.ShipmentPickedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class ShipmentService {

    private final ShipmentRepository shipmentRepository;
    private final KafkaTemplate<String, Object> kafkaTemplate;
    private final RestTemplate restTemplate = new RestTemplate();

    @Value("${order.service.url:http://localhost:8080}")
    private String orderServiceUrl;

    @Value("${kafka.topics.order:order-topic}")
    private String orderTopic;

    @Transactional
    public void handleOrderAuthorized(OrderAuthorizedEvent event) {
        log.info("Create shipment for order {}", event.getOrderId());
        if (shipmentRepository.findByOrderId(event.getOrderId()).isPresent()) {
            log.info("Shipment already exists for order {}");
            return;
        }
        Shipment shipment = Shipment.builder()
                .shipmentId(UUID.randomUUID().toString())
                .orderId(event.getOrderId())
                .customerId(event.getCustomerId())
                .status(ShipmentStatus.CREATED)
                .trackingNumber("TRK_"+UUID.randomUUID().toString().substring(0,8))
                .carrierName("DHL")
                .build();
        shipmentRepository.save(shipment);
        publishShipmentCreatedEvent(shipment);
        updateOrderStatus(event.getOrderId(), "PROCESSING");
    }

    @Transactional
    public Shipment markPicked(String shipmentId) {
        Shipment shipment = shipmentRepository.findByShipmentId(shipmentId)
                .orElseThrow(() -> new RuntimeException("Shipment not found"));
        shipment.setStatus(ShipmentStatus.PICKED);
        Shipment updated = shipmentRepository.save(shipment);
        publishShipmentPickedEvent(updated);
        updateOrderStatus(updated.getOrderId(), "SHIPPED");
        return updated;
    }

    private void publishShipmentCreatedEvent(Shipment shipment) {
        ShipmentCreatedEvent ev = ShipmentCreatedEvent.builder()
                .customerId(shipment.getCustomerId())
                .trackingNumber(shipment.getTrackingNumber())
                .carrierName(shipment.getCarrierName())
                .build();
        ev.setEventType(EventType.SHIPMENT_CREATED.toString());
        ev.setOrderId(shipment.getOrderId());
        ev.setTimestamp(LocalDateTime.now());
        kafkaTemplate.send(orderTopic, shipment.getOrderId(), ev);
    }

    private void publishShipmentPickedEvent(Shipment shipment) {
        ShipmentPickedEvent ev = ShipmentPickedEvent.builder()
                .customerId(shipment.getCustomerId())
                .trackingNumber(shipment.getTrackingNumber())
                .carrierName(shipment.getCarrierName())
                .warehouseLocation("WH1")
                .build();
        ev.setEventType(EventType.SHIPMENT_PICKED.toString());
        ev.setOrderId(shipment.getOrderId());
        ev.setTimestamp(LocalDateTime.now());
        kafkaTemplate.send(orderTopic, shipment.getOrderId(), ev);
    }

    private void updateOrderStatus(String orderId, String status) {
        String url = orderServiceUrl + "/api/orders/"+orderId+"/status";
        try {
            restTemplate.put(url, java.util.Map.of("status", status));
            log.info("Updated order {} status to {}", orderId, status);
        } catch (Exception e) {
            log.error("Failed to update order status", e);
        }
    }

    public java.util.List<Shipment> getAll() {
        return shipmentRepository.findAll();
    }
} 