package com.project.store.order.dto;

public record ShippingAddressResponse(
        String recipient,
        String street,
        String city,
        String postalCode,
        String countryCode
) {
}
