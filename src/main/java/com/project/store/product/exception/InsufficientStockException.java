package com.project.store.product.exception;

public class InsufficientStockException extends RuntimeException {
    public InsufficientStockException(Long productId,int available,int requested) {
        super("Product with id " + productId + " has only " + available + " item available, requested " + requested);
    }
}
