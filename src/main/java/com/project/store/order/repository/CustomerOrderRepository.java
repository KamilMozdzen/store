package com.project.store.order.repository;

import com.project.store.order.entity.CustomerOrder;

import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
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

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @EntityGraph(attributePaths = {"items", "items.product"})
    @Query("""
                SELECT customerOrder
                FROM CustomerOrder  customerOrder
                WHERE customerOrder.id = :id
                """)
    Optional<CustomerOrder> findDetailedByIdForUpdate(@Param("id") Long id);

    Page<CustomerOrder> findAllByUserEmailIgnoreCase(
            String email,
            Pageable pageable
    );
    @EntityGraph(attributePaths = {"items", "items.product"})
    @Query("""
        SELECT customerOrder
        FROM CustomerOrder customerOrder
        WHERE customerOrder.id = :id
          AND LOWER(customerOrder.user.email) = LOWER(:email)
        """)
    Optional<CustomerOrder> findDetailedByIdAndUserEmail(
            @Param("id") Long id,
            @Param("email") String email
    );
}
