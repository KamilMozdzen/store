package com.project.store.category.controller;

import com.project.store.category.dto.CategoryCreateRequest;
import com.project.store.category.dto.CategoryResponse;
import com.project.store.category.dto.CategoryUpdateRequest;
import com.project.store.category.exception.CategoryAlreadyExistsException;
import com.project.store.category.exception.CategoryInUseException;
import com.project.store.category.exception.CategoryNotFoundException;
import com.project.store.category.service.CategoryService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;

import java.util.List;


import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


@AutoConfigureMockMvc(addFilters = false)
@WebMvcTest(CategoryController.class)
public class CategoryControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private CategoryService categoryService;

    @Test
    void returnsCategories() throws Exception {
        when(categoryService.findAll()).thenReturn(List.of(
                new CategoryResponse(1L, "Elektronika"),
                new CategoryResponse(2L, "Książki")
        ));
        mockMvc.perform(get("/api/categories"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].name").value("Elektronika"))
                .andExpect(jsonPath("$[1].name").value("Książki"));


    }
    @Test
    void createCategory() throws Exception {
        when(categoryService.create(any(CategoryCreateRequest.class)))
                .thenReturn(new CategoryResponse(1L, "Elektronika"));

        mockMvc.perform(post("/api/categories")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {
                            "name": "Elektronika"
                        }
                        """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("Elektronika"));
    }
    @Test
    void rejectBlankCategoryName() throws Exception {
        mockMvc.perform(post("/api/categories")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {
                        "name": ""
                        }
                        """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.title").value("Validation Failed"))
                .andExpect(jsonPath("$.errors.name").value("Name is required"));
        verifyNoInteractions(categoryService);
    }
    @Test
    void returnsConflictForExistingCategory() throws Exception {
        when(categoryService.create(any(CategoryCreateRequest.class)))
                .thenThrow(new CategoryAlreadyExistsException("Elektronika"));

        mockMvc.perform(post("/api/categories")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {
                         "name": "Elektronika"
                        }
                        """))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.title").value("Category Already Exists"))
                .andExpect(jsonPath("$.detail")
                        .value("Category with name Elektronika already exists"));
    }
    @Test
    void returnsCategoryById() throws Exception {
        when(categoryService.findById(1L))
                .thenReturn(new CategoryResponse(1L, "Elektronika"));

        mockMvc.perform(get("/api/categories/{id}",1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("Elektronika"));
    }

    @Test
    void returnsNotFoundWhenCategoryDoesNotExist() throws Exception {
        when(categoryService.findById(9999999L))
                .thenThrow(new CategoryNotFoundException(9999999L));

        mockMvc.perform(get("/api/categories/{id}",9999999L))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.title").value("Category Not Found"))
                .andExpect(jsonPath("$.detail").value("Category with id 9999999 was not found"));
    }

    @Test
    void updateCategory() throws Exception {
        when(categoryService.update(
                eq(1L),
                any(CategoryUpdateRequest.class)
        )).thenReturn(new CategoryResponse(1L, "Akcesoria"));

        mockMvc.perform(put("/api/categories/{id}",1L)
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {
                          "name": "Akcesoria"
                        }
                        """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("Akcesoria"));
    }

    @Test
    void returnsConflictWhenUpdatingToExistingName() throws Exception {
        when(categoryService.update(
                eq(1L),
                any(CategoryUpdateRequest.class)
        )).thenThrow(new CategoryAlreadyExistsException("Akcesoria"));

        mockMvc.perform(put("/api/categories/{id}", 1L)
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {
                         "name": "Akcesoria"
                        }
                        """))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.title").value("Category Already Exists"));
    }

    @Test
    void deletesCategory() throws Exception {
        mockMvc.perform(delete("/api/categories/{id}",1L))
                .andExpect(status().isNoContent());
        verify(categoryService).delete(1L);
    }
    @Test
    void returnsConflictWhenDeletingCategoryInUse() throws Exception {
        doThrow(new CategoryInUseException(1L))
                .when(categoryService)
                .delete(1L);

        mockMvc.perform(delete("/api/categories/{id}", 1L))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.title").value("Category In Use"))
                .andExpect(jsonPath("$.detail")
                        .value("Category with id 1 is assigned to products"));
    }

}
