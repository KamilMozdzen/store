package com.project.store.category.exception;

public class CategoryAlreadyExistsException extends RuntimeException {
    public CategoryAlreadyExistsException(String name) {

        super("Category with name " + name + " already exists");
    }
}
