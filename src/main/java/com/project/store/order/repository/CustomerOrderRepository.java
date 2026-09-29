package com.project.store.order.repository;

import com.project.store.order.entity.CustomerOrder;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface CustomerOrderRepository extends JpaRepository<CustomerOrder, Long> {

    @EntityGraph(attributePaths = {"items", "items.product"})
    @Query("""
            SELECT customerOrder
            FROM CustomerOrder customerOrder
            WHERE customerOrder.id = :id
            """)
    Optional<CustomerOrder> findDetailedById(@Param("id") Long id);
}
