package com.acuver.order_demo.controller;

import com.acuver.order_demo.entity.Order;
import com.acuver.order_demo.enums.OrderStatus;
import com.acuver.order_demo.service.OrderService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import com.acuver.order_demo.dto.UpdateStatusRequest;

@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
@Slf4j
public class OrderController {
    
    private final OrderService orderService;
    
    @PutMapping("/{orderId}/status")
    public ResponseEntity<Map<String, Object>> updateOrderStatus(
            @PathVariable String orderId,
            @RequestBody UpdateStatusRequest request) {
        
        try {
            String newStatus = request.getStatus();
            if (newStatus == null || newStatus.trim().isEmpty()) {
                return ResponseEntity.badRequest()
                        .body(Map.of("error", "Status is required"));
            }
            
            OrderStatus orderStatus;
            try {
                orderStatus = OrderStatus.valueOf(newStatus.toUpperCase());
            } catch (IllegalArgumentException e) {
                return ResponseEntity.badRequest()
                        .body(Map.of("error", "Invalid status value. Valid values are: " + 
                                java.util.Arrays.toString(OrderStatus.values())));
            }
            
            orderService.updateOrderStatus(orderId, orderStatus);
            
            return ResponseEntity.ok(Map.of(
                    "message", "Order status updated successfully",
                    "orderId", orderId,
                    "newStatus", orderStatus.toString()
            ));
            
        } catch (RuntimeException e) {
            log.error("Error updating order status for order: {}", orderId, e);
            return ResponseEntity.badRequest()
                    .body(Map.of("error", e.getMessage()));
        }
    }
    
    @GetMapping("/{orderId}")
    public ResponseEntity<Order> getOrder(@PathVariable String orderId) {
        try {
            Order order = orderService.getOrder(orderId);
            return ResponseEntity.ok(order);
        } catch (RuntimeException e) {
            log.error("Error retrieving order: {}", orderId, e);
            return ResponseEntity.notFound().build();
        }
    }
} 