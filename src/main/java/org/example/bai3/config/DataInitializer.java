package org.example.bai3.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.bai3.entity.*;
import org.example.bai3.repository.*;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Seeds the database with demo data on first startup.
 * Uses Stream API (no for-loops) for bulk inserts.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class DataInitializer implements CommandLineRunner {

    private final AppUserRepository userRepository;
    private final ProductRepository productRepository;
    private final PurchaseOrderRepository orderRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    @Transactional
    public void run(String... args) {
        if (userRepository.count() > 0) {
            log.info("Database already seeded — skipping.");
            return;
        }

        // ── Users ────────────────────────────────────────────────────────────
        AppUser alice = userRepository.save(AppUser.builder()
                .username("alice")
                .password(passwordEncoder.encode("password123"))
                .email("alice@example.com")
                .build());

        AppUser bob = userRepository.save(AppUser.builder()
                .username("bob")
                .password(passwordEncoder.encode("password123"))
                .email("bob@example.com")
                .build());

        // ── Products ─────────────────────────────────────────────────────────
        Product laptop = productRepository.save(Product.builder()
                .name("Laptop Pro 15")
                .price(new BigDecimal("1299.99"))
                .build());

        Product mouse = productRepository.save(Product.builder()
                .name("Wireless Mouse")
                .price(new BigDecimal("29.99"))
                .build());

        Product keyboard = productRepository.save(Product.builder()
                .name("Mechanical Keyboard")
                .price(new BigDecimal("89.99"))
                .build());

        Product monitor = productRepository.save(Product.builder()
                .name("4K Monitor")
                .price(new BigDecimal("499.99"))
                .build());

        // ── Orders for Alice ─────────────────────────────────────────────────

        // Order 1 — COMPLETED
        PurchaseOrder order1 = orderRepository.save(PurchaseOrder.builder()
                .orderDate(LocalDateTime.now().minusDays(10))
                .status(OrderStatus.COMPLETED)
                .user(alice)
                .build());

        order1.setItems(List.of(
                buildItem(order1, laptop,   1, laptop.getPrice()),
                buildItem(order1, mouse,    2, mouse.getPrice())
        ));
        orderRepository.save(order1);

        // Order 2 — COMPLETED (laptop again → tests distinct())
        PurchaseOrder order2 = orderRepository.save(PurchaseOrder.builder()
                .orderDate(LocalDateTime.now().minusDays(5))
                .status(OrderStatus.COMPLETED)
                .user(alice)
                .build());

        order2.setItems(List.of(
                buildItem(order2, laptop,   1, laptop.getPrice()),
                buildItem(order2, keyboard, 1, keyboard.getPrice())
        ));
        orderRepository.save(order2);

        // Order 3 — PENDING (should be excluded from summary)
        PurchaseOrder order3 = orderRepository.save(PurchaseOrder.builder()
                .orderDate(LocalDateTime.now().minusDays(1))
                .status(OrderStatus.PENDING)
                .user(alice)
                .build());

        order3.setItems(List.of(
                buildItem(order3, monitor, 2, monitor.getPrice())
        ));
        orderRepository.save(order3);

        // Order 4 — CANCELED (should be excluded)
        PurchaseOrder order4 = orderRepository.save(PurchaseOrder.builder()
                .orderDate(LocalDateTime.now().minusDays(3))
                .status(OrderStatus.CANCELED)
                .user(alice)
                .build());

        order4.setItems(List.of(
                buildItem(order4, mouse, 1, mouse.getPrice())
        ));
        orderRepository.save(order4);

        log.info("Database seeded successfully.");
        log.info("Login with  username=alice  password=password123");
    }

    private OrderItem buildItem(PurchaseOrder order, Product product,
                                int qty, BigDecimal unitPrice) {
        return OrderItem.builder()
                .order(order)
                .product(product)
                .quantity(qty)
                .unitPrice(unitPrice)
                .build();
    }
}
