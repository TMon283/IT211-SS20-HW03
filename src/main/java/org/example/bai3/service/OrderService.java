package org.example.bai3.service;

import lombok.RequiredArgsConstructor;
import org.example.bai3.dto.OrderSummaryDTO;
import org.example.bai3.entity.AppUser;
import org.example.bai3.entity.OrderStatus;
import org.example.bai3.entity.PurchaseOrder;
import org.example.bai3.repository.AppUserRepository;
import org.example.bai3.repository.PurchaseOrderRepository;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class OrderService {

    private final PurchaseOrderRepository purchaseOrderRepository;
    private final AppUserRepository appUserRepository;

    /**
     * Builds an order summary for the currently authenticated user.
     * <p>
     * Constraints satisfied:
     * <ul>
     *   <li>No arrays, no for/while loops in this layer.</li>
     *   <li>All filtering, mapping, aggregation done via Java Stream API.</li>
     * </ul>
     */
    @Transactional(readOnly = true)
    public OrderSummaryDTO getMySummary() {

        // ① Identify current user from SecurityContext
        String username = SecurityContextHolder.getContext()
                .getAuthentication().getName();

        AppUser user = appUserRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found: " + username));

        // ② Raw fetch: all orders + their items from DB (one query via JOIN FETCH)
        List<PurchaseOrder> allOrders =
                purchaseOrderRepository.findAllByUserIdWithItems(user.getId());

        // ③-a  filter() — keep only COMPLETED orders
        List<PurchaseOrder> completedOrders = allOrders.stream()
                .filter(order -> OrderStatus.COMPLETED.equals(order.getStatus()))
                .toList();

        // ③-b  Total spent:
        //       flatMap  → stream of all OrderItems from completed orders
        //       mapToDouble → quantity × unit_price per item
        //       sum()    → grand total
        double totalSpent = completedOrders.stream()
                .flatMap(order -> order.getItems().stream())
                .mapToDouble(item ->
                        item.getQuantity() * item.getUnitPrice().doubleValue())
                .sum();

        // ③-c  Distinct product names purchased:
        //       flatMap  → stream of all OrderItems from completed orders
        //       map      → product name
        //       distinct → eliminate duplicates
        //       toList   → collect
        List<String> purchasedItems = completedOrders.stream()
                .flatMap(order -> order.getItems().stream())
                .map(item -> item.getProduct().getName())
                .distinct()
                .toList();

        // ④ Return DTO
        return OrderSummaryDTO.builder()
                .totalSpent(totalSpent)
                .purchasedItems(purchasedItems)
                .build();
    }
}
