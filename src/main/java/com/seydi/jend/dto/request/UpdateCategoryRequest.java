
package com.seydi.jend.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Schema(description = "Données nécessaires à la modification d'une catégorie")
public record UpdateCategoryRequest(

        @Schema(
                description = "Nouveau nom de la catégorie",
                example = "Informatique",
                maxLength = 100,
                requiredMode = Schema.RequiredMode.REQUIRED
        )
        @NotBlank(message = "Le nom est obligatoire")
        @Size(max = 100, message = "Le nom ne doit pas dépasser 100 caractères")
        String nom
) {}
