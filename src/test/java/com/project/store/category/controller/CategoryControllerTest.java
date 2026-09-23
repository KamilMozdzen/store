package com.project.store.category.controller;

import com.project.store.category.dto.CategoryCreateRequest;
import com.project.store.category.dto.CategoryResponse;
import com.project.store.category.exception.CategoryAlreadyExistsException;
import com.project.store.category.service.CategoryService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;



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
                .andExpect(status().isBadRequest());
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
}
