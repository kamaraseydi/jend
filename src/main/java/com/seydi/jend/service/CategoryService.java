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
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class CategoryService {

    private final CategoryRepository categoryRepository;
    private final AnnonceRepository annonceRepository;
    private final CategoryMapper categoryMapper;
    private final CurrentUserService currentUserService;

    @Transactional(readOnly = true)
    public List<CategoryResponse> findAll() {

        return categoryRepository.findAll()
                .stream()
                .map(categoryMapper::toResponse)
                .toList();
    }

    @Transactional
    public CategoryResponse create(CreateCategoryRequest request) {

        UserProfile currentUser = currentUserService.getCurrentUser();

        checkAdmin(currentUser);

        if (categoryRepository.existsByNomIgnoreCase(request.nom())) {
            throw new CategoryModificationInterditeException(
                    "Cette catégorie existe déjà"
            );
        }

        Category category = categoryMapper.toEntity(request);

        OffsetDateTime now = OffsetDateTime.now();

        category.setCreatedAt(now);
        category.setUpdatedAt(now);

        Category savedCategory = categoryRepository.save(category);

        return categoryMapper.toResponse(savedCategory);
    }

    @Transactional
    public CategoryResponse update(
            Long id,
            UpdateCategoryRequest request
    ) {

        UserProfile currentUser = currentUserService.getCurrentUser();

        checkAdmin(currentUser);

        Category category = categoryRepository.findById(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Catégorie introuvable"
                        ));

        if (categoryRepository.existsByNomIgnoreCase(request.nom())
                && !category.getNom().equalsIgnoreCase(request.nom())) {

            throw new CategoryModificationInterditeException(
                    "Cette catégorie existe déjà"
            );
        }

        categoryMapper.updateEntity(category, request);

        category.setUpdatedAt(OffsetDateTime.now());

        return categoryMapper.toResponse(category);
    }

    @Transactional
    public void delete(Long id) {

        UserProfile currentUser = currentUserService.getCurrentUser();

        checkAdmin(currentUser);

        Category category = categoryRepository.findById(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Catégorie introuvable"
                        ));

        if (annonceRepository.existsByCategoryId(id)) {

            throw new CategoryModificationInterditeException(
                    "Impossible de supprimer une catégorie utilisée par des annonces"
            );
        }

        categoryRepository.delete(category);
    }

    private void checkAdmin(UserProfile user) {

        if (user.getRole() != Role.ADMIN) {

            throw new AccessDeniedException(
                    "Seul un administrateur peut gérer les catégories"
            );
        }
    }

}