package com.project.store.order.controller;

import com.project.store.order.dto.OrderCreateRequest;
import com.project.store.order.dto.OrderItemResponse;
import com.project.store.order.dto.OrderResponse;
import com.project.store.order.entity.OrderStatus;
import com.project.store.order.service.OrderService;
import com.project.store.product.exception.InsufficientStockException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(OrderController.class)
public class OrderControllerTest {

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

        when(orderService.create(any(OrderCreateRequest.class)))
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
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {
                         "customerName": "Jan Kowalski",
                         "customerEmail": "jan@example.com",
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
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {
                         "customerName": "",
                         "customerEmail": "invalid-email",
                         "items":[]
                        }
                        """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.title").value("Validation Failed"))
                .andExpect(jsonPath("$.errors.customerName").value("Customer name is required"))
                .andExpect(jsonPath("$.errors.customerEmail").value("Customer email must be valid"))
                .andExpect(jsonPath("$.errors.items").value("Order must contain at least one item"));
        verifyNoInteractions(orderService);
    }
    @Test
    void returnsConflictWhenStockIsInsufficient() throws Exception {
        when(orderService.create(any(OrderCreateRequest.class)))
                .thenThrow(new InsufficientStockException(1L,1,2));

        mockMvc.perform(post("/api/orders")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {
                         "customerName": "Jan Kowalski",
                         "customerEmail": "jan@example.com",
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
}
