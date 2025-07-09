package com.acuver.payment_demo.repository;

import com.acuver.payment_demo.entity.Payment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PaymentRepository extends JpaRepository<Payment, Long> {
    
    Optional<Payment> findByOrderId(String orderId);
    
    Optional<Payment> findByPaymentId(String paymentId);
    
    boolean existsByOrderId(String orderId);
} 