package com.acuver.payment_demo.consumer;

import com.acuver.payment_demo.service.PaymentService;
import com.acuver.shared.events.OrderCreatedEvent;
import com.acuver.shared.events.ShipmentPickedEvent;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class PaymentConsumer {
    
    private final PaymentService paymentService;
    private final ObjectMapper objectMapper;
    
    @KafkaListener(topics = "order-topic", groupId = "payment-service-group")
    public void handleOrderEvent(String message) {
        log.info("Received event: {}", message);
        try {
            var node = objectMapper.readTree(message);
            if (node.has("eventType")) {
                String type = node.get("eventType").asText();
                switch (type) {
                    case "ORDER_CREATED":
                        handleOrderCreated(message);
                        break;
                    case "SHIPMENT_PICKED":
                        handleShipmentPicked(message);
                        break;
                    default:
                        // discard
                }
            }
        } catch (Exception e) {
            log.error("Error processing message", e);
        }
    }
    
    private void handleOrderCreated(String message) throws JsonProcessingException {
        OrderCreatedEvent event = objectMapper.readValue(message, OrderCreatedEvent.class);
        log.info("Processing ORDER_CREATED event for order: {}", event.getOrderId());
        paymentService.processOrderCreated(event);
    }
    
    private void handleShipmentPicked(String message) throws JsonProcessingException {
        ShipmentPickedEvent event = objectMapper.readValue(message, ShipmentPickedEvent.class);
        log.info("Processing SHIPMENT_PICKED event for order: {}", event.getOrderId());
        paymentService.processShipmentPicked(event);
    }
} 