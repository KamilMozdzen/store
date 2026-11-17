package com.project.store.cart.dto;

import com.project.store.order.dto.ShippingAddressRequest;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;

public record CartCheckoutRequest(
        @NotNull(message = "Shipping address is required")
        @Valid
        ShippingAddressRequest shippingAddress
) {
}
