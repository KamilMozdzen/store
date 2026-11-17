package com.project.store.cart.exception;

public class EmptyCartException extends RuntimeException {
    public EmptyCartException() {
        super("Cart must contain at least one item");
    }
}
