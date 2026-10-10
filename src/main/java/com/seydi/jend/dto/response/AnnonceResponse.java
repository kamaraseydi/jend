
package com.seydi.jend.dto.response;

import com.seydi.jend.entity.EtatAnnonce;
import com.seydi.jend.entity.StatutAnnonce;
import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;

@Schema(description = "Détails d'une annonce publiée ou appartenant à un utilisateur")
public record AnnonceResponse(

        @Schema(description = "Identifiant de l'annonce", example = "42")
        Long id,

        @Schema(description = "Titre de l'annonce", example = "Samsung Galaxy S24")
        String titre,

        @Schema(description = "Description détaillée de l'article", example = "Téléphone en très bon état avec ses accessoires.")
        String description,

        @Schema(description = "Prix de vente", example = "250000.00")
        BigDecimal prix,

        @Schema(description = "État de l'article", example = "TRES_BON_ETAT")
        EtatAnnonce etat,

        @Schema(description = "Statut de l'annonce", example = "PUBLIEE")
        StatutAnnonce statut,

        @Schema(description = "Ville de l'article", example = "Dakar")
        String ville,

        @Schema(description = "Quartier de l'article", example = "Parcelles Assainies")
        String quartier,

        @Schema(description = "Identifiant du vendeur", example = "7")
        Long vendeurId,

        @Schema(description = "Nom du vendeur", example = "Seydina Kamara")
        String vendeurNom,

        @Schema(description = "Identifiant de la catégorie", example = "2")
        Long categoryId,

        @Schema(description = "Nom de la catégorie", example = "Téléphones")
        String categoryNom,

        @Schema(description = "Images associées à l'annonce")
        List<AnnonceImageResponse> images,

        @Schema(description = "Date de création de l'annonce", example = "2026-10-09T10:30:00Z")
        OffsetDateTime createdAt,

        @Schema(description = "Date de dernière modification", example = "2026-10-09T12:00:00Z")
        OffsetDateTime updatedAt
) {}
