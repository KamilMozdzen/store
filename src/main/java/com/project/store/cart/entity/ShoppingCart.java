package com.project.store.cart.entity;

import com.project.store.cart.exception.CartItemNotFoundException;
import com.project.store.product.entity.Product;
import com.project.store.user.entity.AppUser;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
import jakarta.persistence.OrderBy;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "shopping_carts")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ShoppingCart {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private AppUser user;

    private Instant createdAt;

    private Instant updatedAt;

    @OneToMany(
            mappedBy = "cart",
            cascade = CascadeType.ALL,
            orphanRemoval = true
    )
    @OrderBy("id ASC")
    private List<CartItem> items = new ArrayList<>();

    public ShoppingCart(AppUser user) {
        this.user = user;
        this.createdAt = Instant.now();
        this.updatedAt = this.createdAt;
    }

    public void addProduct(Product product, int quantity) {
        if (quantity < 1) {
            throw new IllegalArgumentException(
                    "Quantity must be greater than zero"
            );
        }

        CartItem existingItem = items.stream()
                .filter(item -> item.hasProduct(product.getId()))
                .findFirst()
                .orElse(null);

        if (existingItem == null) {
            items.add(new CartItem(this, product, quantity));
        } else {
            existingItem.increaseQuantity(quantity);
        }

        touch();
    }

    public void updateQuantity(Long productId, int quantity) {
        CartItem item = findItem(productId);
        item.updateQuantity(quantity);
        touch();
    }

    public void removeProduct(Long productId) {
        boolean removed = items.removeIf(
                item -> item.hasProduct(productId)
        );

        if (!removed) {
            throw new CartItemNotFoundException(productId);
        }

        touch();
    }

    public void clear() {
        items.clear();
        touch();
    }

    private CartItem findItem(Long productId) {
        return items.stream()
                .filter(item -> item.hasProduct(productId))
                .findFirst()
                .orElseThrow(() -> new CartItemNotFoundException(
                        productId
                ));
    }

    private void touch() {
        updatedAt = Instant.now();
    }
}