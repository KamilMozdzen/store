package com.project.store.product.controller;


import com.project.store.product.dto.ProductCreateRequest;
import com.project.store.product.dto.ProductResponse;
import com.project.store.product.dto.ProductUpdateRequest;
import com.project.store.product.service.ProductService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import com.project.store.common.dto.PageResponse;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;

@RestController
@RequestMapping("/api/products")
public class ProductController {

    private final ProductService productService;

    public ProductController(ProductService productService) {
        this.productService = productService;
    }

    @GetMapping
    public PageResponse<ProductResponse> findAll(
            @RequestParam(required = false) String query,
            @RequestParam(required = false) Long categoryId,
            @PageableDefault(size = 12, sort = "name") Pageable pageable
    ){
        return productService.findAll(query, categoryId, pageable);
    }

    @PostMapping
    @SecurityRequirement(name = "bearerAuth")
    @ResponseStatus(HttpStatus.CREATED)
    public ProductResponse create(@Valid @RequestBody ProductCreateRequest request){
        return productService.create(request);
    }
    @GetMapping("/{id}")
    public ProductResponse findById(@PathVariable Long id){
        return productService.findById(id);
    }

    @PutMapping("/{id}")
    @SecurityRequirement(name = "bearerAuth")
    public ProductResponse update(@PathVariable Long id, @Valid @RequestBody ProductUpdateRequest request){
        return productService.update(id, request);
    }

    @DeleteMapping("/{id}")
    @SecurityRequirement(name = "bearerAuth")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id){
        productService.delete(id);
    }
}
