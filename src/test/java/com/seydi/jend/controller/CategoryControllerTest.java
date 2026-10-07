package com.seydi.jend.controller;

import com.seydi.jend.dto.request.CreateCategoryRequest;
import com.seydi.jend.dto.request.UpdateCategoryRequest;
import com.seydi.jend.dto.response.CategoryResponse;
import com.seydi.jend.service.CategoryService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(CategoryController.class)
@AutoConfigureMockMvc(addFilters = false)
class CategoryControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private CategoryService categoryService;


    // =========================================================
    // GET /api/categories
    // =========================================================

    @Test
    void shouldReturnAllCategories() throws Exception {

        CategoryResponse category1 =
                new CategoryResponse(
                        1L,
                        "Téléphones",
                        null,
                        null
                );

        CategoryResponse category2 =
                new CategoryResponse(
                        2L,
                        "Voitures",
                        null,
                        null
                );

        when(categoryService.findAll())
                .thenReturn(List.of(category1, category2));

        mockMvc.perform(get("/api/categories"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].nom").value("Téléphones"))
                .andExpect(jsonPath("$[1].id").value(2))
                .andExpect(jsonPath("$[1].nom").value("Voitures"));

        verify(categoryService).findAll();
    }


    // =========================================================
    // POST /api/categories
    // =========================================================

    @Test
    void shouldCreateCategory() throws Exception {

        CategoryResponse response =
                new CategoryResponse(
                        1L,
                        "Téléphones",
                        null,
                        null
                );

        when(categoryService.create(any(CreateCategoryRequest.class)))
                .thenReturn(response);

        mockMvc.perform(
                        post("/api/categories")
                                .contentType("application/json")
                                .content("""
                                        {
                                            "nom": "Téléphones"
                                        }
                                        """)
                )
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.nom").value("Téléphones"));

        verify(categoryService)
                .create(any(CreateCategoryRequest.class));
    }


    // =========================================================
    // POST validation
    // =========================================================

    @Test
    void shouldRejectInvalidCreateCategory() throws Exception {

        mockMvc.perform(
                        post("/api/categories")
                                .contentType("application/json")
                                .content("""
                                        {
                                            "nom": ""
                                        }
                                        """)
                )
                .andExpect(status().isBadRequest());

        verify(categoryService, never())
                .create(any());
    }


    // =========================================================
    // PUT /api/categories/{id}
    // =========================================================

    @Test
    void shouldUpdateCategory() throws Exception {

        CategoryResponse response =
                new CategoryResponse(
                        1L,
                        "Smartphones",
                        null,
                        null
                );

        when(categoryService.update(
                eq(1L),
                any(UpdateCategoryRequest.class)
        )).thenReturn(response);

        mockMvc.perform(
                        put("/api/categories/1")
                                .contentType("application/json")
                                .content("""
                                        {
                                            "nom": "Smartphones"
                                        }
                                        """)
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.nom").value("Smartphones"));

        verify(categoryService)
                .update(eq(1L), any(UpdateCategoryRequest.class));
    }


    // =========================================================
    // PUT validation
    // =========================================================

    @Test
    void shouldRejectInvalidUpdateCategory() throws Exception {

        mockMvc.perform(
                        put("/api/categories/1")
                                .contentType("application/json")
                                .content("""
                                        {
                                            "nom": ""
                                        }
                                        """)
                )
                .andExpect(status().isBadRequest());

        verify(categoryService, never())
                .update(anyLong(), any());
    }


    // =========================================================
    // DELETE /api/categories/{id}
    // =========================================================

    @Test
    void shouldDeleteCategory() throws Exception {

        doNothing()
                .when(categoryService)
                .delete(1L);

        mockMvc.perform(
                        delete("/api/categories/1")
                )
                .andExpect(status().isNoContent());

        verify(categoryService)
                .delete(1L);
    }


}