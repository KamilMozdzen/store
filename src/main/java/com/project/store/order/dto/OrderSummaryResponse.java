package com.project.store.order.dto;

import com.project.store.order.entity.OrderStatus;

import java.math.BigDecimal;
import java.time.Instant;

public record OrderSummaryResponse(
        Long id,
        String customerName,
        String customerEmail,
        OrderStatus status,
        BigDecimal totalAmount,
        Instant createdAt
) {
}
