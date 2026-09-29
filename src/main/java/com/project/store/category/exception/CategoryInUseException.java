package com.project.store.category.exception;

public class CategoryInUseException extends RuntimeException {
    public CategoryInUseException(Long id) {
        super("Category with id " + id + " is assigned to products");
    }
}
