package com.project.store.category.exception;

public class CategoryNotFoundException extends RuntimeException {
    public CategoryNotFoundException(Long id)
    {
        super("Category with id " + id + " was not found");
    }
}
