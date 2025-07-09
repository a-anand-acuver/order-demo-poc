package com.acuver.order_demo.service;

import com.acuver.order_demo.entity.Order;
import com.acuver.order_demo.enums.OrderStatus;
import com.acuver.order_demo.enums.PaymentStatus;
import com.acuver.order_demo.repository.OrderRepository;
import com.acuver.shared.enums.EventType;
import com.acuver.shared.events.OrderCancelledEvent;
import com.acuver.shared.events.OrderChargedEvent;
import com.acuver.shared.events.OrderCreatedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class OrderService {
    
    private final OrderRepository orderRepository;
    private final KafkaTemplate<String, Object> kafkaTemplate;

    @org.springframework.beans.factory.annotation.Value("${kafka.topics.order:order-topic}")
    private String orderTopic;
    
    @Transactional
    public void createOrder(com.acuver.shared.events.CreateOrderRequestEvent event) {
        log.info("Creating order with ID: {}", event.getOrderId());
        
        if (orderRepository.existsByOrderId(event.getOrderId())) {
            log.warn("Order with ID {} already exists", event.getOrderId());
            return;
        }
        
        Order order = Order.builder()
                .orderId(event.getOrderId())
                .customerId(event.getCustomerId())
                .productId(event.getProductId())
                .quantity(event.getQuantity())
                .price(event.getPrice())
                .totalAmount(event.getTotalAmount())
                .status(OrderStatus.PENDING)
                .paymentStatus(PaymentStatus.PENDING)
                .build();
        
        Order savedOrder = orderRepository.save(order);
        log.info("Order created successfully: {}", savedOrder.getOrderId());
        
        publishOrderCreatedEvent(savedOrder);
    }
    
    @Transactional
    public void cancelOrder(com.acuver.shared.events.CancelOrderRequestEvent command) {
        log.info("Cancelling order with ID: {}", command.getOrderId());
        
        Optional<Order> orderOptional = orderRepository.findByOrderId(command.getOrderId());
        
        if (orderOptional.isEmpty()) {
            log.warn("Order with ID {} not found", command.getOrderId());
            return;
        }
        
        Order order = orderOptional.get();
        order.setStatus(OrderStatus.CANCELLED);
        order.setQuantity(0);
        
        Order updatedOrder = orderRepository.save(order);
        log.info("Order cancelled successfully: {}", updatedOrder.getOrderId());
        
        publishOrderCancelledEvent(updatedOrder, command.getReason());
    }
    
    @Transactional
    public void updatePaymentStatus(OrderChargedEvent event) {
        log.info("Updating payment status for order ID: {}", event.getOrderId());
        
        Optional<Order> orderOptional = orderRepository.findByOrderId(event.getOrderId());
        
        if (orderOptional.isEmpty()) {
            log.warn("Order with ID {} not found", event.getOrderId());
            return;
        }
        
        Order order = orderOptional.get();
        order.setPaymentStatus(PaymentStatus.SETTLED);
        
        Order updatedOrder = orderRepository.save(order);
        log.info("Payment status updated to SETTLED for order: {}", updatedOrder.getOrderId());
    }
    
    @Transactional
    public void updateOrderStatus(String orderId, OrderStatus newStatus) {
        log.info("Updating order status for order ID: {} to {}", orderId, newStatus);
        
        Optional<Order> orderOptional = orderRepository.findByOrderId(orderId);
        
        if (orderOptional.isEmpty()) {
            log.warn("Order with ID {} not found", orderId);
            throw new RuntimeException("Order not found");
        }
        
        Order order = orderOptional.get();
        order.setStatus(newStatus);
        
        Order updatedOrder = orderRepository.save(order);
        log.info("Order status updated successfully: {} to {}", updatedOrder.getOrderId(), newStatus);
    }
    
    public Order getOrder(String orderId) {
        return orderRepository.findByOrderId(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found with ID: " + orderId));
    }
    
    private void publishOrderCreatedEvent(Order order) {
        OrderCreatedEvent event = OrderCreatedEvent.builder()
                .customerId(order.getCustomerId())
                .productId(order.getProductId())
                .quantity(order.getQuantity())
                .price(order.getPrice())
                .totalAmount(order.getTotalAmount())
                .status(order.getStatus().toString())
                .paymentStatus(order.getPaymentStatus().toString())
                .build();
        
        // Set base event fields
        event.setEventType(EventType.ORDER_CREATED.toString());
        event.setOrderId(order.getOrderId());
        event.setTimestamp(LocalDateTime.now());
        
        kafkaTemplate.send(orderTopic, event.getOrderId(), event);
        log.info("Published ORDER_CREATED event for order: {}", order.getOrderId());
    }
    
    private void publishOrderCancelledEvent(Order order, String reason) {
        OrderCancelledEvent event = OrderCancelledEvent.builder()
                .customerId(order.getCustomerId())
                .reason(reason)
                .status(order.getStatus().toString())
                .build();
        
        // Set base event fields
        event.setEventType(EventType.ORDER_CANCELLED.toString());
        event.setOrderId(order.getOrderId());
        event.setTimestamp(LocalDateTime.now());
        
        kafkaTemplate.send(orderTopic, event.getOrderId(), event);
        log.info("Published ORDER_CANCELLED event for order: {}", order.getOrderId());
    }
} 