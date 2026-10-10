package com.project.store.config;

import com.project.store.common.dto.PageResponse;
import com.project.store.order.controller.OrderController;
import com.project.store.order.dto.OrderStatusUpdateRequest;
import com.project.store.order.service.OrderService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(OrderController.class)
@Import(SecurityConfig.class)
class OrderSecurityTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private OrderService orderService;

    @MockitoBean
    private JwtDecoder jwtDecoder;

    @Test
    void rejectsAnonymousOrderAccess() throws Exception {
        mockMvc.perform(get("/api/orders"))
                .andExpect(status().isUnauthorized());

        verifyNoInteractions(orderService);
    }

    @Test
    void allowsCustomerToReadOwnOrders() throws Exception {
        when(orderService.findAll(
                any(Pageable.class),
                eq("jan@example.com"),
                eq(false)
        )).thenReturn(new PageResponse<>(
                List.of(),
                0,
                20,
                0,
                0
        ));

        mockMvc.perform(get("/api/orders")
                        .with(jwt()
                                .jwt(token -> token
                                        .subject("jan@example.com"))
                                .authorities(
                                        new SimpleGrantedAuthority(
                                                "ROLE_CUSTOMER"
                                        )
                                )))
                .andExpect(status().isOk());

        verify(orderService).findAll(
                any(Pageable.class),
                eq("jan@example.com"),
                eq(false)
        );
    }

    @Test
    void forbidsCustomerFromChangingOrderStatus() throws Exception {
        mockMvc.perform(patch("/api/orders/{id}/status", 10L)
                        .with(jwt()
                                .jwt(token -> token
                                        .subject("jan@example.com"))
                                .authorities(
                                        new SimpleGrantedAuthority(
                                                "ROLE_CUSTOMER"
                                        )
                                ))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "status": "PAID"
                                }
                                """))
                .andExpect(status().isForbidden());

        verifyNoInteractions(orderService);
    }

    @Test
    void allowsAdminToChangeOrderStatus() throws Exception {
        mockMvc.perform(patch("/api/orders/{id}/status", 10L)
                        .with(jwt()
                                .jwt(token -> token
                                        .subject("admin@example.com"))
                                .authorities(
                                        new SimpleGrantedAuthority(
                                                "ROLE_ADMIN"
                                        )
                                ))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "status": "PAID"
                                }
                                """))
                .andExpect(status().isOk());

        verify(orderService).updateStatus(
                eq(10L),
                any(OrderStatusUpdateRequest.class)
        );
    }
}