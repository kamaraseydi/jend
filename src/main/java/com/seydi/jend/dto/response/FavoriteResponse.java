
package com.seydi.jend.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.OffsetDateTime;

@Schema(description = "Informations sur un favori enregistré")
public record FavoriteResponse(

        @Schema(description = "Identifiant de l'annonce ajoutée aux favoris", example = "42")
        Long annonceId,

        @Schema(description = "Date d'ajout aux favoris", example = "2026-10-09T10:30:00Z")
        OffsetDateTime createdAt
) {}
