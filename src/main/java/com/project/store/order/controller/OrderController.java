package com.project.store.order.controller;

import com.project.store.common.dto.PageResponse;
import com.project.store.order.dto.OrderCreateRequest;
import com.project.store.order.dto.OrderResponse;
import com.project.store.order.dto.OrderSummaryResponse;
import com.project.store.order.service.OrderService;
import jakarta.validation.Valid;
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
    public OrderResponse create(@Valid @RequestBody OrderCreateRequest request){
        return orderService.create(request);
    }

    @GetMapping
    public PageResponse<OrderSummaryResponse> findAll(@PageableDefault(size = 20, sort = "createdAt") Pageable pageable){
        return orderService.findAll(pageable);
    }

    @GetMapping("/{id}")
    public OrderResponse findById(@PathVariable Long id){
        return orderService.findById(id);
    }
}
