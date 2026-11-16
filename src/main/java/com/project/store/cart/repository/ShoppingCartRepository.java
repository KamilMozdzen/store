package com.project.store.cart.repository;

import com.project.store.cart.entity.ShoppingCart;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface ShoppingCartRepository  extends JpaRepository<ShoppingCart, Long> {

    @EntityGraph(attributePaths = {"items", "items.product"})

    @Query("""
            SELECT cart
            FROM ShoppingCart cart
            WHERE LOWER(cart.user.email) = LOWER(:email)
            """)
    Optional<ShoppingCart> findDetailedByUserEmail(@Param("email") String email);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @EntityGraph(attributePaths = {
            "items", "items.product"
    })

    @Query("""
            SELECT cart
            FROM ShoppingCart cart
            WHERE LOWER(cart.user.email) = LOWER(:email)
            """)

    Optional<ShoppingCart> findDetailedByUserEmailForUpdate(@Param("email") String email);

}
