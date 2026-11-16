package com.project.store.cart.exception;

public class CartItemNotFoundException extends RuntimeException {
    public CartItemNotFoundException(Long ProductId) {
        super("Product with id " + ProductId + " is not present in cart");
    }
}
