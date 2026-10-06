package com.seydi.jend.service;

import com.seydi.jend.dto.request.CreateCategoryRequest;
import com.seydi.jend.dto.request.UpdateCategoryRequest;
import com.seydi.jend.dto.response.CategoryResponse;
import com.seydi.jend.entity.Category;
import com.seydi.jend.entity.Role;
import com.seydi.jend.security.CurrentUserService;
import com.seydi.jend.entity.UserProfile;
import com.seydi.jend.exception.CategoryModificationInterditeException;
import com.seydi.jend.exception.ResourceNotFoundException;
import com.seydi.jend.mapper.CategoryMapper;
import com.seydi.jend.repository.AnnonceRepository;
import com.seydi.jend.repository.CategoryRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.access.AccessDeniedException;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CategoryServiceTest {

    @Mock
    private CategoryRepository categoryRepository;

    @Mock
    private AnnonceRepository annonceRepository;

    @Mock
    private CategoryMapper categoryMapper;

    @Mock
    private CurrentUserService currentUserService;

    @InjectMocks
    private CategoryService categoryService;


    // =========================================================
    // FIND ALL
    // =========================================================

    @Test
    void shouldFindAllCategories() {

        Category category1 = new Category();
        category1.setId(1L);
        category1.setNom("Téléphones");

        Category category2 = new Category();
        category2.setId(2L);
        category2.setNom("Voitures");

        CategoryResponse response1 =
                new CategoryResponse(
                        1L,
                        "Téléphones",
                        null,
                        null
                );

        CategoryResponse response2 =
                new CategoryResponse(
                        2L,
                        "Voitures",
                        null,
                        null
                );

        when(categoryRepository.findAll())
                .thenReturn(List.of(category1, category2));

        when(categoryMapper.toResponse(category1))
                .thenReturn(response1);

        when(categoryMapper.toResponse(category2))
                .thenReturn(response2);

        List<CategoryResponse> result =
                categoryService.findAll();

        assertEquals(2, result.size());
        assertEquals("Téléphones", result.get(0).nom());
        assertEquals("Voitures", result.get(1).nom());

        verify(categoryRepository).findAll();
    }


    // =========================================================
    // CREATE
    // =========================================================

    @Test
    void shouldCreateCategoryAsAdmin() {

        UserProfile admin = new UserProfile();
        admin.setId(1L);
        admin.setRole(Role.ADMIN);

        CreateCategoryRequest request =
                new CreateCategoryRequest("Téléphones");

        Category category = new Category();
        category.setNom("Téléphones");

        Category savedCategory = new Category();
        savedCategory.setId(10L);
        savedCategory.setNom("Téléphones");

        CategoryResponse response =
                new CategoryResponse(
                        10L,
                        "Téléphones",
                        null,
                        null
                );

        when(currentUserService.getCurrentUser())
                .thenReturn(admin);

        when(categoryRepository.existsByNomIgnoreCase("Téléphones"))
                .thenReturn(false);

        when(categoryMapper.toEntity(request))
                .thenReturn(category);

        when(categoryRepository.save(category))
                .thenReturn(savedCategory);

        when(categoryMapper.toResponse(savedCategory))
                .thenReturn(response);

        CategoryResponse result =
                categoryService.create(request);

        assertEquals(10L, result.id());
        assertEquals("Téléphones", result.nom());

        verify(categoryRepository)
                .existsByNomIgnoreCase("Téléphones");

        verify(categoryRepository)
                .save(category);
    }


    @Test
    void shouldRejectCategoryCreationWhenUserIsNotAdmin() {

        UserProfile user = new UserProfile();
        user.setId(1L);
        user.setRole(Role.UTILISATEUR);

        CreateCategoryRequest request =
                new CreateCategoryRequest("Téléphones");

        when(currentUserService.getCurrentUser())
                .thenReturn(user);

        assertThrows(
                AccessDeniedException.class,
                () -> categoryService.create(request)
        );

        verify(categoryRepository, never())
                .save(any());
    }


    @Test
    void shouldRejectDuplicateCategory() {

        UserProfile admin = new UserProfile();
        admin.setId(1L);
        admin.setRole(Role.ADMIN);

        CreateCategoryRequest request =
                new CreateCategoryRequest("Téléphones");

        when(currentUserService.getCurrentUser())
                .thenReturn(admin);

        when(categoryRepository.existsByNomIgnoreCase("Téléphones"))
                .thenReturn(true);

        assertThrows(
                CategoryModificationInterditeException.class,
                () -> categoryService.create(request)
        );

        verify(categoryRepository, never())
                .save(any());
    }


    // =========================================================
    // UPDATE
    // =========================================================

    @Test
    void shouldUpdateCategoryAsAdmin() {

        UserProfile admin = new UserProfile();
        admin.setId(1L);
        admin.setRole(Role.ADMIN);

        Category category = new Category();
        category.setId(10L);
        category.setNom("Téléphones");

        UpdateCategoryRequest request =
                new UpdateCategoryRequest("Smartphones");

        CategoryResponse response =
                new CategoryResponse(
                        10L,
                        "Smartphones",
                        null,
                        null
                );

        when(currentUserService.getCurrentUser())
                .thenReturn(admin);

        when(categoryRepository.findById(10L))
                .thenReturn(Optional.of(category));

        when(categoryRepository.existsByNomIgnoreCase("Smartphones"))
                .thenReturn(false);

        when(categoryMapper.toResponse(category))
                .thenReturn(response);

        CategoryResponse result =
                categoryService.update(10L, request);

        assertEquals("Smartphones", result.nom());

        verify(categoryMapper)
                .updateEntity(category, request);
    }


    @Test
    void shouldRejectDuplicateCategoryNameOnUpdate() {

        UserProfile admin = new UserProfile();
        admin.setId(1L);
        admin.setRole(Role.ADMIN);

        Category category = new Category();
        category.setId(10L);
        category.setNom("Téléphones");

        UpdateCategoryRequest request =
                new UpdateCategoryRequest("Voitures");

        when(currentUserService.getCurrentUser())
                .thenReturn(admin);

        when(categoryRepository.findById(10L))
                .thenReturn(Optional.of(category));

        when(categoryRepository.existsByNomIgnoreCase("Voitures"))
                .thenReturn(true);

        assertThrows(
                CategoryModificationInterditeException.class,
                () -> categoryService.update(10L, request)
        );

        verify(categoryMapper, never())
                .updateEntity(any(), any());
    }


    @Test
    void shouldRejectUpdateWhenUserIsNotAdmin() {

        UserProfile user = new UserProfile();
        user.setId(1L);
        user.setRole(Role.UTILISATEUR);

        UpdateCategoryRequest request =
                new UpdateCategoryRequest("Smartphones");

        when(currentUserService.getCurrentUser())
                .thenReturn(user);

        assertThrows(
                AccessDeniedException.class,
                () -> categoryService.update(10L, request)
        );

        verify(categoryRepository, never())
                .findById(any());
    }


    @Test
    void shouldThrowWhenUpdatingUnknownCategory() {

        UserProfile admin = new UserProfile();
        admin.setRole(Role.ADMIN);

        UpdateCategoryRequest request =
                new UpdateCategoryRequest("Téléphones");

        when(currentUserService.getCurrentUser())
                .thenReturn(admin);

        when(categoryRepository.findById(99L))
                .thenReturn(Optional.empty());

        assertThrows(
                ResourceNotFoundException.class,
                () -> categoryService.update(99L, request)
        );
    }


    // =========================================================
    // DELETE
    // =========================================================

    @Test
    void shouldDeleteCategoryAsAdmin() {

        UserProfile admin = new UserProfile();
        admin.setRole(Role.ADMIN);

        Category category = new Category();
        category.setId(10L);
        category.setNom("Téléphones");

        when(currentUserService.getCurrentUser())
                .thenReturn(admin);

        when(categoryRepository.findById(10L))
                .thenReturn(Optional.of(category));

        when(annonceRepository.existsByCategoryId(10L))
                .thenReturn(false);

        categoryService.delete(10L);

        verify(categoryRepository)
                .delete(category);
    }


    @Test
    void shouldRejectDeleteWhenUserIsNotAdmin() {

        UserProfile user = new UserProfile();
        user.setRole(Role.UTILISATEUR);

        when(currentUserService.getCurrentUser())
                .thenReturn(user);

        assertThrows(
                AccessDeniedException.class,
                () -> categoryService.delete(10L)
        );

        verify(categoryRepository, never())
                .delete(any());
    }


    @Test
    void shouldThrowWhenDeletingUnknownCategory() {

        UserProfile admin = new UserProfile();
        admin.setRole(Role.ADMIN);

        when(currentUserService.getCurrentUser())
                .thenReturn(admin);

        when(categoryRepository.findById(99L))
                .thenReturn(Optional.empty());

        assertThrows(
                ResourceNotFoundException.class,
                () -> categoryService.delete(99L)
        );

        verify(categoryRepository, never())
                .delete(any());
    }


    @Test
    void shouldRejectDeleteWhenCategoryIsUsedByAnnonces() {

        UserProfile admin = new UserProfile();
        admin.setRole(Role.ADMIN);

        Category category = new Category();
        category.setId(10L);

        when(currentUserService.getCurrentUser())
                .thenReturn(admin);

        when(categoryRepository.findById(10L))
                .thenReturn(Optional.of(category));

        when(annonceRepository.existsByCategoryId(10L))
                .thenReturn(true);

        assertThrows(
                CategoryModificationInterditeException.class,
                () -> categoryService.delete(10L)
        );

        verify(categoryRepository, never())
                .delete(any());
    }
}