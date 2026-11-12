package com.project.store.order.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record ShippingAddressRequest(
        @NotBlank(message = "Recipient is required")
        @Size(max = 120, message = "Recipient must not exceed 120 characters")
        String recipient,

        @NotBlank(message = "Street is required")
        @Size(max = 160, message = "Street must not exceed 160 characters")
        String street,

        @NotBlank(message = "City is required")
        @Size(max = 100, message = "City must not exceed 100 characters")
        String city,

        @NotBlank(message = "Postal code is required")
        @Size(max = 20, message = "Postal code must not exceed 20 characters")
        String postalCode,

        @NotBlank(message = "Country code is required")
        @Pattern(
                regexp = "^[A-Za-z]{2}$",
                message = "Country code must contain 2 letters"
        )
        String countryCode

) {
}
