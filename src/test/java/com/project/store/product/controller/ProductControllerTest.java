package com.project.store.product.controller;

import com.project.store.category.exception.CategoryNotFoundException;
import com.project.store.common.dto.PageResponse;
import com.project.store.product.dto.ProductCreateRequest;
import com.project.store.product.dto.ProductResponse;
import com.project.store.product.dto.ProductUpdateRequest;
import com.project.store.product.exception.ProductNotFoundException;
import com.project.store.product.service.ProductService;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.List;


import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ProductController.class)
public class ProductControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ProductService productService;

    @Test
    void create() throws Exception {
        when(productService.create(any(ProductCreateRequest.class)))
                .thenReturn(new ProductResponse(
                        1L, "Klawiatura" , "Mechaniczna",
                        new BigDecimal("199.99"),5,1L,"Elektronika"
                ));
        mockMvc.perform(post("/api/products")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "name": "Klawiatura",
                                  "description": "Mechaniczna",
                                  "price": 199.99,
                                  "stockQuantity": 5,
                                  "categoryId": 1
                                }
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("Klawiatura"));
    }

    @Test
    void rejectsBlankName() throws Exception {
        mockMvc.perform(post("/api/products")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                          {
                            "name":"",
                            "price": 199.99,
                            "stockQuantity": 5
                          }
                          """))
                .andExpect(status().isBadRequest());
        verifyNoInteractions(productService);
    }
    @Test
    void returnsProductById() throws Exception {
        when(productService.findById(1L))
                .thenReturn(new ProductResponse(
                        1L,
                        "Klawiatura",
                        "mechaniczna",
                        new BigDecimal("199.99"),
                        5,1L,"Elektronika"
                ));
        mockMvc.perform(get("/api/products/{id}",1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("Klawiatura"))
                .andExpect(jsonPath("$.price").value(199.99))
                .andExpect(jsonPath("$.categoryId").value(1))
                .andExpect(jsonPath("$.categoryName").value("Elektronika"));
    }

    @Test
    void returnsNotFoundWhenProductDoesNotExist() throws Exception {
        when(productService.findById(9999999L))
                .thenThrow(new ProductNotFoundException(9999999L));

        mockMvc.perform(get("/api/products/{id}",9999999L))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.title").value("Product Not Found"))
                .andExpect(jsonPath("$.detail")
                .value("Product with id 9999999 was not found"));
    }

    @Test
    void updateProduct() throws Exception {
        when(productService.update(eq(1L), any(ProductUpdateRequest.class)))
                .thenReturn(new ProductResponse(
                        1L,
                        "Klawiatura Pro",
                        "Mechaniczna RGB",
                        new BigDecimal("249.99"),
                        8,1L,"Elektronika"
                ));
        mockMvc.perform(put("/api/products/{id}",1L)
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {
                        "name": "Klawiatura Pro",
                        "description": "Mechaniczna RGB",
                        "price": 249.99,
                        "stockQuantity": 8,
                        "categoryId": 1
                        }
                        """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("Klawiatura Pro"))
                .andExpect(jsonPath("$.price").value(249.99))
                .andExpect(jsonPath("$.stockQuantity").value(8))
                .andExpect(jsonPath("$.categoryId").value(1))
                .andExpect(jsonPath("$.categoryName").value("Elektronika"));
    }

    @Test
    void rejectsInvalidProductUpdate() throws Exception {
        mockMvc.perform(put("/api/products/{id}",1L)
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {
                        "name": "",
                        "price": 0,
                        "stockQuantity": -1
                        }
                        """))
                .andExpect(status().isBadRequest());
        verifyNoInteractions(productService);
    }
    @Test
    void returnsNotFoundWhenUpdatingMissingProduct() throws Exception {
        when(productService.update(eq(9999999L), any(ProductUpdateRequest.class)))
                .thenThrow(new ProductNotFoundException(9999999L));

        mockMvc.perform(put("/api/products/{id}",9999999L)
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {
                        "name": "Klawiatura Pro",
                        "description": "Mechaniczna RGB",
                        "price": 249.99,
                        "stockQuantity": 8,
                        "categoryId": 1
                        }
                        """))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.title").value("Product Not Found"))
                .andExpect(jsonPath("$.detail")
                        .value("Product with id 9999999 was not found"));


    }
    @Test
    void deleteProduct() throws Exception {
        mockMvc.perform(delete("/api/products/{id}",1L))
                .andExpect(status().isNoContent());
        verify(productService).delete(1L);
    }

    @Test
    void returnsNotFoundWhenDeletingMissingProduct() throws Exception {
        doThrow(new ProductNotFoundException(9999999L))
                .when(productService).delete(9999999L);

        mockMvc.perform(delete("/api/products/{id}",9999999L))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.title").value("Product Not Found"))
                .andExpect(jsonPath("$.detail").value("Product with id 9999999 was not found"));
    }
    @Test
    void returnsNotFoundWhenCreatingProductWithMissingCategory() throws Exception {
        when(productService.create(any(ProductCreateRequest.class)))
                .thenThrow(new CategoryNotFoundException(9999999L));

        mockMvc.perform(post("/api/products")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {
                         "name": "Klawiatura",
                         "description": "Mechaniczna RGB",
                         "price": 199.99,
                         "stockQuantity": 5,
                         "categoryId": 9999999
                        }
                        """))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.title").value("Category Not Found"))
                .andExpect(jsonPath("$.detail").value("Category with id 9999999 was not found"));
    }
    @Test
    void returnsPagedProducts() throws Exception {
        ProductResponse productResponse = new ProductResponse(
                1L,
                "Klawiatura",
                "Mechaniczna",
                new BigDecimal("199.99"),
                5,
                2L,
                "Elektronika"
        );

        when(productService.findAll(isNull(), any(Pageable.class)))
                .thenReturn(new PageResponse<>(
                        List.of(productResponse),
                        0,
                        12,
                        1,
                        1
                ));

        mockMvc.perform(get("/api/products"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].id").value(1))
                .andExpect(jsonPath("$.content[0].name").value("Klawiatura"))
                .andExpect(jsonPath("$.content[0].categoryName").value("Elektronika"))
                .andExpect(jsonPath("$.page").value(0))
                .andExpect(jsonPath("$.size").value(12))
                .andExpect(jsonPath("$.totalElements").value(1))
                .andExpect(jsonPath("$.totalPages").value(1));
    }

    @Test
    void filtersProductsByCategory() throws Exception {
        when(productService.findAll(eq(2L), any(Pageable.class)))
                .thenReturn(new PageResponse<>(
                    List.of(),
                    0,
                    10,
                    0,
                    0
                ));
        mockMvc.perform(get("/api/products")
                .param("categoryId", "2")
                .param("page", "0")
                .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isEmpty())
                .andExpect(jsonPath("$.page").value(0))
                .andExpect(jsonPath("$.size").value(10))
                .andExpect(jsonPath("$.totalElements").value(0));
        verify(productService).findAll(eq(2L),any(Pageable.class));
    }
}
