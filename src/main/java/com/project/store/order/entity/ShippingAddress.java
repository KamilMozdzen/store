package com.project.store.order.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Embeddable
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ShippingAddress {

    @Column(name = "shipping_recipient", length = 120)
    private String recipient;

    @Column(name = "shipping_street", length = 160)
    private String street;

    @Column(name = "shipping_city", length = 100)
    private String city;

    @Column(name = "shipping_postal_code", length = 20)
    private String postalCode;

    @Column(name = "shipping_country_code", length = 2)
    private String countryCode;

    public ShippingAddress(
            String recipient,
            String street,
            String city,
            String postalCode,
            String countryCode
    ){
        this.recipient = recipient;
        this.street = street;
        this.city = city;
        this.postalCode = postalCode;
        this.countryCode = countryCode;
    }
}
