package org.example.bai3.repository;

import org.example.bai3.entity.PurchaseOrder;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PurchaseOrderRepository extends JpaRepository<PurchaseOrder, Long> {

    /**
     * Fetch all orders for a user, eagerly loading their items and each item's product.
     * A single JPQL JOIN FETCH avoids N+1 queries.
     */
    @Query("SELECT DISTINCT o FROM PurchaseOrder o " +
           "LEFT JOIN FETCH o.items i " +
           "LEFT JOIN FETCH i.product " +
           "WHERE o.user.id = :userId")
    List<PurchaseOrder> findAllByUserIdWithItems(@Param("userId") Long userId);
}
