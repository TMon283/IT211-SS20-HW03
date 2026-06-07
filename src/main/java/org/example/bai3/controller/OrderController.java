package org.example.bai3.controller;

import lombok.RequiredArgsConstructor;
import org.example.bai3.dto.OrderSummaryDTO;
import org.example.bai3.service.OrderService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/inventory/orders")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;

    /** GET /api/inventory/orders/my-summary — protected endpoint */
    @GetMapping("/my-summary")
    public ResponseEntity<OrderSummaryDTO> getMySummary() {
        return ResponseEntity.ok(orderService.getMySummary());
    }
}
