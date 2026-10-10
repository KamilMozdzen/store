package com.project.store.order.controller;

import com.project.store.order.dto.OrderStatusUpdateRequest;
import com.project.store.common.dto.PageResponse;
import com.project.store.order.dto.OrderCreateRequest;
import com.project.store.order.dto.OrderResponse;
import com.project.store.order.dto.OrderSummaryResponse;
import com.project.store.order.service.OrderService;
import jakarta.validation.Valid;
import org.springframework.security.core.Authentication;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/orders")
public class OrderController {

    private final OrderService orderService;

    public OrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public OrderResponse create(
            @Valid @RequestBody OrderCreateRequest request,
            Authentication authentication
    ) {
        return orderService.create(
                request,
                authentication.getName()
        );
    }

    @GetMapping
    public PageResponse<OrderSummaryResponse> findAll(
            @PageableDefault(size = 20, sort = "createdAt")
            Pageable pageable,
            Authentication authentication
    ) {
        return orderService.findAll(
                pageable,
                authentication.getName(),
                isAdmin(authentication)
        );
    }

    @GetMapping("/{id}")
    public OrderResponse findById(
            @PathVariable Long id,
            Authentication authentication
    ) {
        return orderService.findById(
                id,
                authentication.getName(),
                isAdmin(authentication)
        );
    }
    @PatchMapping("/{id}/status")
    public OrderResponse updateStatus(
            @PathVariable Long id,
            @Valid @RequestBody OrderStatusUpdateRequest request
    ){
        return orderService.updateStatus(id, request);
    }
    private boolean isAdmin(Authentication authentication) {
        return authentication.getAuthorities().stream()
                .anyMatch(authority ->
                        authority.getAuthority().equals("ROLE_ADMIN")
                );
    }
}
