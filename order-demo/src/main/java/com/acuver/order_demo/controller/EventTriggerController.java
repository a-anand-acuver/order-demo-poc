package com.acuver.order_demo.controller;

import com.acuver.shared.enums.EventType;
import com.acuver.shared.events.CancelOrderRequestEvent;
import com.acuver.shared.events.CreateOrderRequestEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/events")
@RequiredArgsConstructor
@Slf4j
public class EventTriggerController {

    private final KafkaTemplate<String, Object> kafkaTemplate;

    @Value("${kafka.topics.order:order-topic}")
    private String orderTopic;

    @PostMapping("/create-order")
    public ResponseEntity<?> triggerCreateOrder(@RequestBody com.acuver.order_demo.dto.CreateOrderRequest req) {
        try {
            String orderId = (req.getOrderId() == null || req.getOrderId().isBlank()) ? UUID.randomUUID().toString() : req.getOrderId();
            String customerId = req.getCustomerId();
            String productId  = req.getProductId();
            Integer quantity  = req.getQuantity();
            BigDecimal price  = req.getPrice();
            BigDecimal total  = req.getTotalAmount();

            CreateOrderRequestEvent event = CreateOrderRequestEvent.builder()
                    .customerId(customerId)
                    .productId(productId)
                    .quantity(quantity)
                    .price(price)
                    .totalAmount(total)
                    .build();
            event.setEventType(EventType.CREATE_ORDER_REQUEST.toString());
            event.setOrderId(orderId);
            event.setTimestamp(LocalDateTime.now());

            kafkaTemplate.send(orderTopic, orderId, event);
            log.info("Triggered CREATE_ORDER_REQUEST event for order {}", orderId);
            return ResponseEntity.ok(Map.of("status", "sent", "orderId", orderId));
        } catch (Exception e) {
            log.error("Error triggering create order", e);
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @PostMapping("/cancel-order")
    public ResponseEntity<?> triggerCancelOrder(@RequestBody com.acuver.order_demo.dto.CancelOrderRequest req) {
        try {
            String orderId = req.getOrderId();
            String reason  = (req.getReason()==null? "manual cancel": req.getReason());

            CancelOrderRequestEvent event = CancelOrderRequestEvent.builder()
                    .reason(reason)
                    .build();
            event.setEventType(EventType.CANCEL_ORDER_REQUEST.toString());
            event.setOrderId(orderId);
            event.setTimestamp(LocalDateTime.now());

            kafkaTemplate.send(orderTopic, orderId, event);
            log.info("Triggered CANCEL_ORDER_REQUEST event for order {}", orderId);
            return ResponseEntity.ok(Map.of("status", "sent", "orderId", orderId));
        } catch (Exception e) {
            log.error("Error triggering cancel order", e);
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @PostMapping("/charge-order")
    public ResponseEntity<?> triggerChargeOrder(@RequestBody com.acuver.order_demo.dto.ChargeOrderRequest req) {
        try {
            String orderId = req.getOrderId();
            java.math.BigDecimal amount = req.getAmount();
            if (amount == null) amount = java.math.BigDecimal.ZERO;

            com.acuver.shared.events.OrderChargedEvent event = com.acuver.shared.events.OrderChargedEvent.builder()
                    .customerId(req.getCustomerId())
                    .chargedAmount(amount)
                    .paymentId(req.getPaymentId() == null ? java.util.UUID.randomUUID().toString() : req.getPaymentId())
                    .transactionId(req.getTransactionId() == null ? "TXN_"+java.util.UUID.randomUUID().toString().substring(0,10) : req.getTransactionId())
                    .build();
            event.setEventType(com.acuver.shared.enums.EventType.ORDER_CHARGED.toString());
            event.setOrderId(orderId);
            event.setTimestamp(java.time.LocalDateTime.now());

            kafkaTemplate.send(orderTopic, orderId, event);
            log.info("Triggered ORDER_CHARGED event for order {}", orderId);
            return ResponseEntity.ok(java.util.Map.of("status","sent","orderId",orderId));
        } catch(Exception e) {
            log.error("Error triggering charge order", e);
            return ResponseEntity.badRequest().body(java.util.Map.of("error", e.getMessage()));
        }
    }
} 