
package com.seydi.jend.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;

@Schema(description = "Données nécessaires à l'ajout d'une image à une annonce")
public record AddAnnonceImageRequest(

        @Schema(
                description = "URL de l'image",
                example = "https://example.com/images/telephone.jpg",
                maxLength = 1000,
                requiredMode = Schema.RequiredMode.REQUIRED
        )
        @NotBlank(message = "L'URL de l'image est obligatoire")
        @Size(max = 1000, message = "L'URL ne doit pas dépasser 1000 caractères")
        String url,

        @Schema(
                description = "Position de l'image dans la liste, de 1 à 8",
                example = "1",
                minimum = "1",
                maximum = "8",
                requiredMode = Schema.RequiredMode.REQUIRED
        )
        @NotNull(message = "L'ordre est obligatoire")
        @Min(value = 1, message = "L'ordre doit être au minimum 1")
        @Max(value = 8, message = "L'ordre doit être au maximum 8")
        Integer ordre
) {}
