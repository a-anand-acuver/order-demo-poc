package com.acuver.payment_demo.service;

import com.acuver.payment_demo.entity.Payment;
import com.acuver.payment_demo.repository.PaymentRepository;
import com.acuver.shared.enums.EventType;
import com.acuver.shared.events.AuthFailedEvent;
import com.acuver.shared.events.OrderAuthorizedEvent;
import com.acuver.shared.events.OrderChargedEvent;
import com.acuver.shared.events.OrderCreatedEvent;
import com.acuver.shared.events.ShipmentPickedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class PaymentService {
    
    private final PaymentRepository paymentRepository;
    private final KafkaTemplate<String, Object> kafkaTemplate;

    @org.springframework.beans.factory.annotation.Value("${kafka.topics.order:order-topic}")
    private String orderTopic;
    
    @Transactional
    public void processOrderCreated(OrderCreatedEvent event) {
        log.info("Processing ORDER_CREATED event for order: {}", event.getOrderId());
        
        // Check if payment already exists for this order
        if (paymentRepository.existsByOrderId(event.getOrderId())) {
            log.warn("Payment already exists for order: {}", event.getOrderId());
            return;
        }
        
        // Extract order number from orderId and check if it's even or odd
        String orderId = event.getOrderId();
        boolean isEven = isOrderNumberEven(orderId);
        
        // Create payment record
        Payment payment = Payment.builder()
                .paymentId(UUID.randomUUID().toString())
                .orderId(event.getOrderId())
                .customerId(event.getCustomerId())
                .amount(event.getTotalAmount())
                .paymentMethod("CARD") // Default payment method
                .status(Payment.PaymentStatus.PENDING)
                .build();
        
        Payment savedPayment = paymentRepository.save(payment);
        
        if (isEven) {
            // Authorize payment
            authorizePayment(savedPayment);
        } else {
            // Fail authorization
            failAuthorization(savedPayment);
        }
    }
    
    @Transactional
    public void processShipmentPicked(ShipmentPickedEvent event) {
        log.info("Processing SHIPMENT_PICKED event for order: {}", event.getOrderId());
        
        Optional<Payment> paymentOptional = paymentRepository.findByOrderId(event.getOrderId());
        
        if (paymentOptional.isEmpty()) {
            log.warn("No payment found for order: {}", event.getOrderId());
            return;
        }
        
        Payment payment = paymentOptional.get();
        
        // Only charge if payment is authorized
        if (payment.getStatus() == Payment.PaymentStatus.AUTHORIZED) {
            chargePayment(payment);
        } else {
            log.warn("Cannot charge payment for order {} - status is {}", 
                    event.getOrderId(), payment.getStatus());
        }
    }
    
    private void authorizePayment(Payment payment) {
        payment.setStatus(Payment.PaymentStatus.AUTHORIZED);
        payment.setAuthorizationCode("AUTH_" + UUID.randomUUID().toString().substring(0, 8));
        
        Payment updatedPayment = paymentRepository.save(payment);
        log.info("Payment authorized for order: {}", updatedPayment.getOrderId());
        
        // Publish ORDER_AUTHORIZED event
        publishOrderAuthorizedEvent(updatedPayment);
    }
    
    private void failAuthorization(Payment payment) {
        payment.setStatus(Payment.PaymentStatus.FAILED);
        payment.setFailureReason("Authorization failed - odd order number");
        
        Payment updatedPayment = paymentRepository.save(payment);
        log.info("Payment authorization failed for order: {}", updatedPayment.getOrderId());
        
        // Publish AUTH_FAILED event
        publishAuthFailedEvent(updatedPayment);
    }
    
    private void chargePayment(Payment payment) {
        payment.setStatus(Payment.PaymentStatus.CHARGED);
        payment.setTransactionId("TXN_" + UUID.randomUUID().toString().substring(0, 12));
        
        Payment updatedPayment = paymentRepository.save(payment);
        log.info("Payment charged for order: {}", updatedPayment.getOrderId());
        
        // Publish ORDER_CHARGED event
        publishOrderChargedEvent(updatedPayment);
    }
    
    private boolean isOrderNumberEven(String orderId) {
        // Extract numeric part from orderId (assuming format like "ORDER_123" or just "123")
        String numericPart = orderId.replaceAll("[^0-9]", "");
        
        if (numericPart.isEmpty()) {
            // If no numbers found, use orderId hashcode
            return Math.abs(orderId.hashCode()) % 2 == 0;
        }
        
        try {
            long orderNumber = Long.parseLong(numericPart);
            return orderNumber % 2 == 0;
        } catch (NumberFormatException e) {
            // Fallback to hashcode if parsing fails
            return Math.abs(orderId.hashCode()) % 2 == 0;
        }
    }
    
    private void publishOrderAuthorizedEvent(Payment payment) {
        OrderAuthorizedEvent event = OrderAuthorizedEvent.builder()
                .customerId(payment.getCustomerId())
                .authorizedAmount(payment.getAmount())
                .paymentMethod(payment.getPaymentMethod())
                .authorizationCode(payment.getAuthorizationCode())
                .build();
        
        // Set base event fields
        event.setEventType(EventType.ORDER_AUTHORIZED.toString());
        event.setOrderId(payment.getOrderId());
        event.setTimestamp(LocalDateTime.now());
        
        kafkaTemplate.send(orderTopic, event.getOrderId(), event);
        log.info("Published ORDER_AUTHORIZED event for order: {}", payment.getOrderId());
    }
    
    private void publishAuthFailedEvent(Payment payment) {
        AuthFailedEvent event = AuthFailedEvent.builder()
                .customerId(payment.getCustomerId())
                .failureReason(payment.getFailureReason())
                .errorCode("AUTH_FAILED_ODD_ORDER")
                .build();
        
        // Set base event fields
        event.setEventType(EventType.AUTH_FAILED.toString());
        event.setOrderId(payment.getOrderId());
        event.setTimestamp(LocalDateTime.now());
        
        kafkaTemplate.send(orderTopic, event.getOrderId(), event);
        log.info("Published AUTH_FAILED event for order: {}", payment.getOrderId());
    }
    
    private void publishOrderChargedEvent(Payment payment) {
        OrderChargedEvent event = OrderChargedEvent.builder()
                .customerId(payment.getCustomerId())
                .chargedAmount(payment.getAmount())
                .paymentId(payment.getPaymentId())
                .transactionId(payment.getTransactionId())
                .build();
        
        // Set base event fields
        event.setEventType(EventType.ORDER_CHARGED.toString());
        event.setOrderId(payment.getOrderId());
        event.setTimestamp(LocalDateTime.now());
        
        kafkaTemplate.send(orderTopic, event.getOrderId(), event);
        log.info("Published ORDER_CHARGED event for order: {}", payment.getOrderId());
    }
} 