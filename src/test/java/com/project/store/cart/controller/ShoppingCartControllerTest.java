package com.project.store.cart.controller;

import com.project.store.cart.exception.EmptyCartException;
import com.project.store.cart.dto.CartCheckoutRequest;
import com.project.store.order.dto.OrderResponse;
import com.project.store.order.dto.ShippingAddressResponse;
import com.project.store.order.entity.OrderStatus;
import com.project.store.order.service.OrderService;
import com.project.store.cart.dto.CartItemAddRequest;
import com.project.store.cart.dto.CartItemQuantityRequest;
import com.project.store.cart.dto.CartItemResponse;
import com.project.store.cart.dto.CartResponse;
import com.project.store.cart.service.ShoppingCartService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Collections;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ShoppingCartController.class)
@AutoConfigureMockMvc(addFilters = false)
class ShoppingCartControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ShoppingCartService cartService;

    @MockitoBean
    private OrderService orderService;

    private final Authentication authenticatedUser =
            new UsernamePasswordAuthenticationToken(
                    "jan@example.com",
                    null,
                    Collections.emptyList()
            );

    @Test
    void returnsCart() throws Exception {
        when(cartService.getCart("jan@example.com"))
                .thenReturn(cartResponse());

        mockMvc.perform(get("/api/cart")
                        .principal(authenticatedUser))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items[0].productId")
                        .value(1))
                .andExpect(jsonPath("$.items[0].quantity")
                        .value(2))
                .andExpect(jsonPath("$.totalAmount")
                        .value(399.98));
    }

    @Test
    void addsItem() throws Exception {
        when(cartService.addItem(
                eq("jan@example.com"),
                any(CartItemAddRequest.class)
        )).thenReturn(cartResponse());

        mockMvc.perform(post("/api/cart/items")
                        .principal(authenticatedUser)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "productId": 1,
                                  "quantity": 2
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items[0].productName")
                        .value("Klawiatura"))
                .andExpect(jsonPath("$.totalAmount")
                        .value(399.98));
    }

    @Test
    void rejectsInvalidItem() throws Exception {
        mockMvc.perform(post("/api/cart/items")
                        .principal(authenticatedUser)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "productId": 0,
                                  "quantity": 0
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.title")
                        .value("Validation Failed"))
                .andExpect(jsonPath("$.errors.productId")
                        .value("Product id must be positive"))
                .andExpect(jsonPath("$.errors.quantity")
                        .value("Quantity must be at least 1"));

        verifyNoInteractions(cartService);
    }

    @Test
    void updatesItemQuantity() throws Exception {
        when(cartService.updateQuantity(
                eq("jan@example.com"),
                eq(1L),
                any(CartItemQuantityRequest.class)
        )).thenReturn(cartResponse());

        mockMvc.perform(put("/api/cart/items/{productId}", 1L)
                        .principal(authenticatedUser)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "quantity": 2
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items[0].quantity")
                        .value(2));
    }

    @Test
    void removesItem() throws Exception {
        when(cartService.removeItem(
                "jan@example.com",
                1L
        )).thenReturn(new CartResponse(
                10L,
                List.of(),
                BigDecimal.ZERO,
                Instant.parse("2026-11-16T10:00:00Z")
        ));

        mockMvc.perform(delete("/api/cart/items/{productId}", 1L)
                        .principal(authenticatedUser))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items").isEmpty())
                .andExpect(jsonPath("$.totalAmount").value(0));
    }

    @Test
    void clearsCart() throws Exception {
        mockMvc.perform(delete("/api/cart")
                        .principal(authenticatedUser))
                .andExpect(status().isNoContent());

        verify(cartService).clear("jan@example.com");
    }

    private CartResponse cartResponse() {
        CartItemResponse item = new CartItemResponse(
                1L,
                "Klawiatura",
                new BigDecimal("199.99"),
                2,
                new BigDecimal("399.98"),
                10
        );

        return new CartResponse(
                10L,
                List.of(item),
                new BigDecimal("399.98"),
                Instant.parse("2026-11-16T10:00:00Z")
        );
    }
    @Test
    void checksOutCart() throws Exception {
        ShippingAddressResponse address =
                new ShippingAddressResponse(
                        "Jan Kowalski",
                        "Długa 10",
                        "Kraków",
                        "30-001",
                        "PL"
                );

        when(orderService.checkoutCart(
                any(CartCheckoutRequest.class),
                eq("jan@example.com")
        )).thenReturn(new OrderResponse(
                20L,
                "Jan Kowalski",
                "jan@example.com",
                address,
                OrderStatus.NEW,
                new BigDecimal("399.98"),
                Instant.parse("2026-11-17T10:00:00Z"),
                List.of()
        ));

        mockMvc.perform(post("/api/cart/checkout")
                        .principal(authenticatedUser)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                            {
                              "shippingAddress": {
                                "recipient": "Jan Kowalski",
                                "street": "Długa 10",
                                "city": "Kraków",
                                "postalCode": "30-001",
                                "countryCode": "PL"
                              }
                            }
                            """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(20))
                .andExpect(jsonPath("$.status").value("NEW"))
                .andExpect(jsonPath("$.totalAmount")
                        .value(399.98))
                .andExpect(jsonPath(
                        "$.shippingAddress.city"
                ).value("Kraków"));
    }
    @Test
    void rejectsCheckoutWithoutShippingAddress()
            throws Exception {
        mockMvc.perform(post("/api/cart/checkout")
                        .principal(authenticatedUser)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                            {
                            }
                            """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.title")
                        .value("Validation Failed"))
                .andExpect(jsonPath(
                        "$.errors.shippingAddress"
                ).value("Shipping address is required"));

        verifyNoInteractions(orderService);
    }
    @Test
    void returnsConflictWhenCheckingOutEmptyCart()
            throws Exception {
        when(orderService.checkoutCart(
                any(CartCheckoutRequest.class),
                eq("jan@example.com")
        )).thenThrow(new EmptyCartException());

        mockMvc.perform(post("/api/cart/checkout")
                        .principal(authenticatedUser)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                            {
                              "shippingAddress": {
                                "recipient": "Jan Kowalski",
                                "street": "Długa 10",
                                "city": "Kraków",
                                "postalCode": "30-001",
                                "countryCode": "PL"
                              }
                            }
                            """))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.title")
                        .value("Cart Is Empty"))
                .andExpect(jsonPath("$.detail")
                        .value(
                                "Cart must contain at least one item"
                        ));
    }
}