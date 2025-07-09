package com.acuver.shipment_demo.repository;

import com.acuver.shipment_demo.entity.Shipment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ShipmentRepository extends JpaRepository<Shipment, Long> {
    Optional<Shipment> findByShipmentId(String shipmentId);
    Optional<Shipment> findByOrderId(String orderId);
} 