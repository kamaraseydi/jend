package com.seydi.jend.mapper;

import com.seydi.jend.dto.request.CreateCategoryRequest;
import com.seydi.jend.dto.request.UpdateCategoryRequest;
import com.seydi.jend.dto.response.CategoryResponse;
import com.seydi.jend.entity.Category;
import org.springframework.stereotype.Component;

@Component
public class CategoryMapper {

    public Category toEntity(CreateCategoryRequest request) {

        Category category = new Category();
        category.setNom(request.nom());

        return category;
    }

    public void updateEntity(
            Category category,
            UpdateCategoryRequest request
    ) {
        category.setNom(request.nom());
    }

    public CategoryResponse toResponse(Category category) {

        return new CategoryResponse(
                category.getId(),
                category.getNom(),
                category.getCreatedAt(),
                category.getUpdatedAt()
        );
    }
}