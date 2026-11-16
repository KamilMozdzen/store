package com.project.store.cart.controller;

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
}