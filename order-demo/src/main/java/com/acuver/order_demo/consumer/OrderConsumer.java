package com.acuver.order_demo.consumer;

import com.acuver.order_demo.service.OrderService;
import com.acuver.shared.events.CreateOrderRequestEvent;
import com.acuver.shared.events.CancelOrderRequestEvent;
import com.acuver.shared.events.OrderChargedEvent;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class OrderConsumer {

    private final OrderService orderService;
    private final ObjectMapper objectMapper;

    @KafkaListener(topics = "order-topic", groupId = "order-service-group")
    public void handleMessage(String message) {
        log.info("Received message: {}", message);
        try {
            JsonNode root = objectMapper.readTree(message);
            if (root.has("eventType")) {
                String evType = root.get("eventType").asText();
                switch (evType) {
                    case "CREATE_ORDER_REQUEST":
                        handleCreateOrder(message);
                        break;
                    case "CANCEL_ORDER_REQUEST":
                        handleCancelOrder(message);
                        break;
                    case "ORDER_CHARGED":
                        handleOrderCharged(message);
                        break;
                    default:
                        log.debug("Ignoring event type {}", evType);
                }
            }
        } catch(Exception e) { log.error("Error processing Kafka message",e);} }

    private void handleCreateOrder(String message) throws JsonProcessingException {
        CreateOrderRequestEvent event = objectMapper.readValue(message, CreateOrderRequestEvent.class);
        log.info("Processing CREATE_ORDER_REQUEST for order: {}", event.getOrderId());
        orderService.createOrder(event);
    }

    private void handleCancelOrder(String message) throws JsonProcessingException {
        CancelOrderRequestEvent event = objectMapper.readValue(message, CancelOrderRequestEvent.class);
        log.info("Processing CANCEL_ORDER_REQUEST for order: {}", event.getOrderId());
        orderService.cancelOrder(event);
    }

    private void handleOrderCharged(String message) throws JsonProcessingException {
        OrderChargedEvent event = objectMapper.readValue(message, OrderChargedEvent.class);
        log.info("Processing ORDER_CHARGED event for order: {}", event.getOrderId());
        orderService.updatePaymentStatus(event);
    }
} 