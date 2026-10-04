package com.project.store.user.dto;

import com.project.store.user.entity.UserRole;

import java.time.Instant;

public record UserResponse(
        Long id,
        String email,
        String firstName,
        String lastName,
        UserRole role,
        Instant createdAt
) {
}
