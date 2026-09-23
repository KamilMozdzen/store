package com.project.store.product.service;


import com.project.store.product.dto.ProductCreateRequest;
import com.project.store.product.dto.ProductResponse;
import com.project.store.product.dto.ProductUpdateRequest;
import com.project.store.product.entity.Product;
import com.project.store.product.exception.ProductNotFoundException;
import com.project.store.product.repository.ProductRepository;
import org.springframework.stereotype.Service;

import java.util.List;


@Service
public class ProductService {

    private final ProductRepository productRepository;

    public ProductService(ProductRepository productRepository) {
        this.productRepository = productRepository;
    }

    private ProductResponse toResponse(Product product) {
        return new ProductResponse(
                product.getId(),
                product.getName(),
                product.getDescription(),
                product.getPrice(),
                product.getStockQuantity()
        );
    }

    public List<ProductResponse> findAll(){
        return productRepository.findAll().stream()
                .map(this::toResponse)
                .toList();
    }

    public ProductResponse create(ProductCreateRequest request) {
        Product product = new Product(
                request.name(),
                request.description(),
                request.price(),
                request.stockQuantity()

        );
        Product savedProduct = productRepository.save(product);
        return toResponse(savedProduct);
    }
    public ProductResponse findById(Long id) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ProductNotFoundException(id));
        return toResponse(product);
    }
    public ProductResponse update(Long id, ProductUpdateRequest request) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ProductNotFoundException(id));

        product.setName(request.name());
        product.setDescription(request.description());
        product.setPrice(request.price());
        product.setStockQuantity(request.stockQuantity());

        Product savedProduct = productRepository.save(product);
        return toResponse(savedProduct);
    }
}
