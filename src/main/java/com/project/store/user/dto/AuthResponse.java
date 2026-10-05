package com.project.store.user.dto;

public record AuthResponse(
        String accessToken,
        String tokenType,
        long expiresIn
) {
}
