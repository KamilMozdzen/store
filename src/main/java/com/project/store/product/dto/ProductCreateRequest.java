package com.project.store.product.dto;

import jakarta.validation.constraints.*;

import java.math.BigDecimal;

public record ProductCreateRequest(
        @NotBlank @Size(max = 120) String name,
        String description,
        @NotNull @DecimalMin("0.01") BigDecimal price,
        @Min(0) int stockQuantity,
        @NotNull Long categoryId
) {
}
