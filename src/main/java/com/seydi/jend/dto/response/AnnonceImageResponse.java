
package com.seydi.jend.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.OffsetDateTime;

@Schema(description = "Informations d'une image associée à une annonce")
public record AnnonceImageResponse(

        @Schema(description = "Identifiant de l'image", example = "5")
        Long id,

        @Schema(
                description = "URL de l'image",
                example = "https://example.com/images/telephone.jpg"
        )
        String url,

        @Schema(description = "Position de l'image dans la liste", example = "1")
        Integer ordre,

        @Schema(description = "Date d'ajout de l'image", example = "2026-10-09T10:30:00Z")
        OffsetDateTime createdAt
) {}
