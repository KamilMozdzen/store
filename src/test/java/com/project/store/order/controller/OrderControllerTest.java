package com.project.store.order.controller;

import com.project.store.order.dto.OrderStatusUpdateRequest;
import com.project.store.order.exception.InvalidOrderStatusTransitionException;
import com.project.store.common.dto.PageResponse;
import com.project.store.order.dto.OrderCreateRequest;
import com.project.store.order.dto.OrderItemResponse;
import com.project.store.order.dto.OrderResponse;
import com.project.store.order.dto.OrderSummaryResponse;
import com.project.store.order.entity.OrderStatus;
import com.project.store.order.exception.OrderNotFoundException;
import com.project.store.order.service.OrderService;
import com.project.store.product.exception.InsufficientStockException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;

import org.springframework.security.core.Authentication;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;

import java.util.Collections;

@AutoConfigureMockMvc(addFilters = false)
@WebMvcTest(OrderController.class)
public class OrderControllerTest {
    private final Authentication authenticatedUser =
            new UsernamePasswordAuthenticationToken(
                    "jan@example.com",
                    null,
                    Collections.emptyList()
            );

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private OrderService orderService;

    @Test
    void createOrder() throws Exception {
        OrderItemResponse item = new OrderItemResponse(
            1L,
            "Klawiatura",
            new BigDecimal("199.99"),
            2,
            new BigDecimal("399.98")
        );

        when(orderService.create(
                any(OrderCreateRequest.class),
                eq("jan@example.com")
        ))
                .thenReturn(new OrderResponse(
                        10L,
                        "Jan Kowalski",
                        "jan@example.com",
                        OrderStatus.NEW,
                        new BigDecimal("399.98"),
                        Instant.parse("2026-09-29T10:00:00Z"),
                        List.of(item)
                ));
        mockMvc.perform(post("/api/orders")
                .principal(authenticatedUser)
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {
                         "items":[
                         {
                           "productId": 1,
                           "quantity": 2
                           }
                           ]
                        }
                        """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(10))
                .andExpect(jsonPath("$.status").value("NEW"))
                .andExpect(jsonPath("$.totalAmount").value(399.98))
                .andExpect(jsonPath("$.items[0].productName").value("Klawiatura"))
                .andExpect(jsonPath("$.items[0].quantity").value(2));
    }
    @Test
    void rejectInvalidOrder() throws Exception {
        mockMvc.perform(post("/api/orders")
                        .principal(authenticatedUser)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                        {
                          "items": []
                        }
                        """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.title")
                        .value("Validation Failed"))
                .andExpect(jsonPath("$.errors.items")
                        .value("Order must contain at least one item"));

        verifyNoInteractions(orderService);
    }
    @Test
    void returnsConflictWhenStockIsInsufficient() throws Exception {
        when(orderService.create(
                any(OrderCreateRequest.class),
                eq("jan@example.com")
        ))
                .thenThrow(new InsufficientStockException(1L,1,2));

        mockMvc.perform(post("/api/orders")
                .principal(authenticatedUser)
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {
                         "items":[
                         {
                           "productId": 1,
                           "quantity": 2
                         }
                         ]
                        }
                        """))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.title").value("Insufficient Stock"))
                .andExpect(jsonPath("$.detail").value("Product with id 1 has only 1 item available, requested 2"
                ));
    }
    @Test
    void returnsPagedOrders() throws Exception {
        OrderSummaryResponse order = new OrderSummaryResponse(
                10L,
                "Jan Kowalski",
                "jan@example.com",
                OrderStatus.NEW,
                new BigDecimal("399.98"),
                Instant.parse("2026-09-29T10:00:00Z")
        );

        when(orderService.findAll(
                any(Pageable.class),
                eq("jan@example.com"),
                eq(false)
        ))
                .thenReturn(new PageResponse<>(
                        List.of(order),
                        0,
                        20,
                        1,
                        1
                ));

        mockMvc.perform(get("/api/orders")
                        .principal(authenticatedUser)
                        .param("page", "0")
                        .param("size", "20"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].id").value(10))
                .andExpect(jsonPath("$.content[0].status").value("NEW"))
                .andExpect(jsonPath("$.content[0].totalAmount")
                        .value(399.98))
                .andExpect(jsonPath("$.page").value(0))
                .andExpect(jsonPath("$.size").value(20))
                .andExpect(jsonPath("$.totalElements").value(1));
    }
    @Test
    void returnsOrderById() throws Exception {
        OrderItemResponse item = new OrderItemResponse(
                1L,
                "Klawiatura",
                new BigDecimal("199.99"),
                2,
                new BigDecimal("399.98")
        );

        when(orderService.findById(
                10L,
                "jan@example.com",
                false
        ))
                .thenReturn(new OrderResponse(
                        10L,
                        "Jan Kowalski",
                        "jan@example.com",
                        OrderStatus.NEW,
                        new BigDecimal("399.98"),
                        Instant.parse("2026-09-29T10:00:00Z"),
                        List.of(item)
                ));

        mockMvc.perform(get("/api/orders/{id}", 10L)
                        .principal(authenticatedUser))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(10))
                .andExpect(jsonPath("$.customerEmail")
                        .value("jan@example.com"))
                .andExpect(jsonPath("$.items[0].productId").value(1))
                .andExpect(jsonPath("$.items[0].lineTotal")
                        .value(399.98));
    }
    @Test
    void returnsNotFoundWhenOrderDoesNotExist() throws Exception {
        when(orderService.findById(
                999999L,
                "jan@example.com",
                false
        ))
                .thenThrow(new OrderNotFoundException(999999L));

        mockMvc.perform(get("/api/orders/{id}", 999999L)
                        .principal(authenticatedUser))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.title").value("Order Not Found"))
                .andExpect(jsonPath("$.detail")
                        .value("Order with id 999999 was not found"));
    }
    @Test
    void updatesOrderStatus() throws Exception {
        when(orderService.updateStatus(
                eq(10L),
                any(OrderStatusUpdateRequest.class)
        )).thenReturn(new OrderResponse(
                10L,
                "Jan Kowalski",
                "jan@example.com",
                OrderStatus.PAID,
                new BigDecimal("399.98"),
                Instant.parse("2026-09-29T10:00:00Z"),
                List.of()
        ));

        mockMvc.perform(patch("/api/orders/{id}/status", 10L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                            {
                              "status": "PAID"
                            }
                            """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(10))
                .andExpect(jsonPath("$.status").value("PAID"));
    }
    @Test
    void returnsConflictForInvalidStatusTransition() throws Exception {
        when(orderService.updateStatus(
                eq(10L),
                any(OrderStatusUpdateRequest.class)
        )).thenThrow(
                new InvalidOrderStatusTransitionException(
                        OrderStatus.SHIPPED,
                        OrderStatus.NEW
                )
        );

        mockMvc.perform(patch("/api/orders/{id}/status", 10L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                            {
                              "status": "NEW"
                            }
                            """))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.title")
                        .value("Invalid Order Status Transition"))
                .andExpect(jsonPath("$.detail")
                        .value(
                                "Cannot change order status from SHIPPED to NEW"
                        ));
    }
    @Test
    void rejectsMissingOrderStatus() throws Exception {
        mockMvc.perform(patch("/api/orders/{id}/status", 10L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                            {
                            }
                            """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.title")
                        .value("Validation Failed"))
                .andExpect(jsonPath("$.errors.status")
                        .value("Status is required"));
    }
}
