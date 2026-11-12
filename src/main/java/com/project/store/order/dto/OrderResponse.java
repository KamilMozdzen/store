package com.project.store.order.dto;

import com.project.store.order.entity.OrderStatus;



import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

public record OrderResponse(
        Long id,
        String customerName,
        String customerEmail,
        ShippingAddressResponse shippingAddress,
        OrderStatus status,
        BigDecimal totalAmount,
        Instant createdAt,
        List<OrderItemResponse> items
) {
}
