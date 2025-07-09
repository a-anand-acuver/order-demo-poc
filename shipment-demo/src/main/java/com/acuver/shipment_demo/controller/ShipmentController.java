package com.acuver.shipment_demo.controller;

import com.acuver.shipment_demo.entity.Shipment;
import com.acuver.shipment_demo.service.ShipmentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/shipments")
@RequiredArgsConstructor
@Slf4j
public class ShipmentController {

    private final ShipmentService shipmentService;

    @PostMapping("/process")
    public ResponseEntity<?> processShipment(@RequestParam String shipmentId) {
        try {
            Shipment updated = shipmentService.markPicked(shipmentId);
            return ResponseEntity.ok(updated);
        } catch(Exception e) {
            log.error("Error processing shipment", e);
            return ResponseEntity.badRequest().body(java.util.Map.of("error", e.getMessage()));
        }
    }

    @GetMapping
    public java.util.List<Shipment> getAllShipments() {
        return shipmentService.getAll();
    }
} 