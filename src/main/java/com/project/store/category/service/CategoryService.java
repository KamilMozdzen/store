package com.project.store.category.service;

import com.project.store.category.dto.CategoryCreateRequest;
import com.project.store.category.dto.CategoryResponse;
import com.project.store.category.entity.Category;
import com.project.store.category.exception.CategoryAlreadyExistsException;
import com.project.store.category.repository.CategoryRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CategoryService {
    private final CategoryRepository categoryRepository;

    public CategoryService(CategoryRepository categoryRepository) {
        this.categoryRepository = categoryRepository;
    }

    public List<CategoryResponse> findAll(){
        return categoryRepository.findAll().stream()
                .map(this::toResponse)
                .toList();
    }
    public CategoryResponse create(CategoryCreateRequest request){
        String name = request.name().trim();

        if(categoryRepository.existsByNameIgnoreCase(name)){
            throw new CategoryAlreadyExistsException(name);
        }
        Category savedCategory = categoryRepository.save(new Category(name));
        return toResponse(savedCategory);
    }
    private CategoryResponse toResponse(Category category) {
        return new CategoryResponse(category.getId(),category.getName());
    }
}
