package com.project.store.category.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CategoryCreateRequest(
     @NotBlank(message = "Name is required")
     @Size(max = 80, message = "Name must not exceed 80 characters")
     String name
) {
}
