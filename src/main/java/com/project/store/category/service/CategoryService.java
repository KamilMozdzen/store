package com.project.store.category.service;

import com.project.store.category.dto.CategoryCreateRequest;
import com.project.store.category.dto.CategoryResponse;
import com.project.store.category.dto.CategoryUpdateRequest;
import com.project.store.category.entity.Category;
import com.project.store.category.exception.CategoryAlreadyExistsException;
import com.project.store.category.exception.CategoryInUseException;
import com.project.store.category.exception.CategoryNotFoundException;
import com.project.store.category.repository.CategoryRepository;
import com.project.store.product.repository.ProductRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Transactional(readOnly = true)
@Service
public class CategoryService {
    private final CategoryRepository categoryRepository;
    private final ProductRepository productRepository;

    public CategoryService(CategoryRepository categoryRepository, ProductRepository productRepository) {
        this.categoryRepository = categoryRepository;
        this.productRepository = productRepository;
    }

    public List<CategoryResponse> findAll(){
        return categoryRepository.findAll().stream()
                .map(this::toResponse)
                .toList();
    }
    @Transactional
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

    public CategoryResponse findById(Long id){
        return toResponse(findCategory(id));
    }
    @Transactional
    public CategoryResponse update(Long id,CategoryUpdateRequest request){
        Category category = findCategory(id);
        String name = request.name().trim();

        if(categoryRepository.existsByNameIgnoreCaseAndIdNot(name,id)){
            throw new CategoryAlreadyExistsException(name);
        }
        category.setName(name);
        Category savedCategory = categoryRepository.save(category);
        return toResponse(savedCategory);
    }
    @Transactional
    public void delete(Long id){
        Category category = findCategory(id);

        if(productRepository.existsByCategory_Id(id)){
            throw new CategoryInUseException(id);
        }
        categoryRepository.delete(category);
    }

    private Category findCategory(Long id){
        return categoryRepository.findById(id).orElseThrow(() -> new CategoryNotFoundException(id));
    }
}
