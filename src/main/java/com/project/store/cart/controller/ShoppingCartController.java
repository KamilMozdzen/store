package com.project.store.cart.controller;

import com.project.store.cart.dto.CartItemAddRequest;
import com.project.store.cart.dto.CartItemQuantityRequest;
import com.project.store.cart.dto.CartResponse;
import com.project.store.cart.service.ShoppingCartService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;


@RestController
@RequestMapping("/api/cart")
public class ShoppingCartController {

    private final ShoppingCartService cartService;

    public ShoppingCartController(ShoppingCartService cartService) {
        this.cartService = cartService;
    }

    @GetMapping
    public CartResponse getCart(Authentication authentication) {
        return cartService.getCart(
                authentication.getName()
        );
    }

    @PostMapping("/items")
    public CartResponse addItem(
            @Valid @RequestBody CartItemAddRequest request,
            Authentication authentication
    ){
        return cartService.addItem(
                authentication.getName(),
                request
        );
    }

    @PutMapping("/items/{productId}")
    public CartResponse updateQuantity(
            @PathVariable Long productId,
            @Valid @RequestBody CartItemQuantityRequest request,
            Authentication authentication
    ){
        return cartService.updateQuantity(
                authentication.getName(),
                productId,
                request
        );
    }

    @DeleteMapping("/items/{productId}")
    public CartResponse removeItem(
            @PathVariable Long productId,
            Authentication authentication
    ){
        return cartService.removeItem(
                authentication.getName(),
                productId
        );
    }

    @DeleteMapping
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void clear(Authentication authentication){
        cartService.clear(authentication.getName());
    }
}
