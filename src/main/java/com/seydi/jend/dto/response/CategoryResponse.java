
package com.seydi.jend.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.OffsetDateTime;

@Schema(description = "Informations d'une catégorie d'annonces")
public record CategoryResponse(

        @Schema(description = "Identifiant de la catégorie", example = "2")
        Long id,

        @Schema(description = "Nom de la catégorie", example = "Téléphones")
        String nom,

        @Schema(description = "Date de création", example = "2026-10-09T10:30:00Z")
        OffsetDateTime createdAt,

        @Schema(description = "Date de dernière modification", example = "2026-10-09T12:00:00Z")
        OffsetDateTime updatedAt
) {}
