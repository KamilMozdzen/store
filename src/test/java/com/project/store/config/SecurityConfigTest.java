package com.project.store.config;

import com.project.store.product.controller.ProductController;
import com.project.store.product.dto.ProductCreateRequest;
import com.project.store.product.dto.ProductResponse;
import com.project.store.product.service.ProductService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ProductController.class)
@Import(SecurityConfig.class)
class SecurityConfigTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ProductService productService;

    @MockitoBean
    private JwtDecoder jwtDecoder;

    @Test
    void allowsPublicProductReading() throws Exception {
        when(productService.findById(1L))
                .thenReturn(new ProductResponse(
                        1L,
                        "Klawiatura",
                        "Mechaniczna",
                        new BigDecimal("199.99"),
                        5,
                        1L,
                        "Elektronika"
                ));

        mockMvc.perform(get("/api/products/{id}", 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name")
                        .value("Klawiatura"));
    }

    @Test
    void rejectsAnonymousProductCreation() throws Exception {
        mockMvc.perform(post("/api/products")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(validProductJson()))
                .andExpect(status().isUnauthorized());

        verifyNoInteractions(productService);
    }

    @Test
    void forbidsCustomerProductCreation() throws Exception {
        mockMvc.perform(post("/api/products")
                        .with(jwt().authorities(
                                new SimpleGrantedAuthority("ROLE_CUSTOMER")
                        ))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(validProductJson()))
                .andExpect(status().isForbidden());

        verifyNoInteractions(productService);
    }

    @Test
    void allowsAdminProductCreation() throws Exception {
        when(productService.create(any(ProductCreateRequest.class)))
                .thenReturn(new ProductResponse(
                        1L,
                        "Klawiatura",
                        "Mechaniczna",
                        new BigDecimal("199.99"),
                        5,
                        1L,
                        "Elektronika"
                ));

        mockMvc.perform(post("/api/products")
                        .with(jwt().authorities(
                                new SimpleGrantedAuthority("ROLE_ADMIN")
                        ))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(validProductJson()))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name")
                        .value("Klawiatura"));
    }

    private String validProductJson() {
        return """
                {
                  "name": "Klawiatura",
                  "description": "Mechaniczna",
                  "price": 199.99,
                  "stockQuantity": 5,
                  "categoryId": 1
                }
                """;
    }
}