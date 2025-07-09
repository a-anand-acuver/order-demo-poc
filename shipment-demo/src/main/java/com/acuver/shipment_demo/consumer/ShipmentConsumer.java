package com.acuver.shipment_demo.consumer;

import com.acuver.shared.events.OrderAuthorizedEvent;
import com.acuver.shared.events.ShipmentPickedEvent; // not used maybe
import com.acuver.shipment_demo.service.ShipmentService;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class ShipmentConsumer {

    private final ShipmentService shipmentService;
    private final ObjectMapper objectMapper;

    @KafkaListener(topics = "order-topic", groupId = "shipment-service-group")
    public void onMessage(String message) {
        try {
            JsonNode node = objectMapper.readTree(message);
            if (node.has("eventType")) {
                String type = node.get("eventType").asText();
                switch (type) {
                    case "ORDER_AUTHORIZED" -> {
                        OrderAuthorizedEvent ev = objectMapper.treeToValue(node, OrderAuthorizedEvent.class);
                        shipmentService.handleOrderAuthorized(ev);
                    }
                    default -> {}
                }
            }
        } catch (Exception e) {
            log.error("Error processing kafka message", e);
        }
    }
} 