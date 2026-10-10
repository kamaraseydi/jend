
package com.seydi.jend.dto.request;

import com.seydi.jend.entity.EtatAnnonce;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

@Schema(description = "Données nécessaires à la modification d'une annonce")
public record UpdateAnnonceRequest(

        @Schema(
                description = "Nouveau titre de l'annonce",
                example = "Samsung Galaxy S24 Ultra",
                maxLength = 150,
                requiredMode = Schema.RequiredMode.REQUIRED
        )
        @NotBlank(message = "Le titre est obligatoire")
        @Size(max = 150, message = "Le titre ne doit pas dépasser 150 caractères")
        String titre,

        @Schema(
                description = "Nouvelle description de l'annonce",
                example = "Téléphone en excellent état, vendu avec ses accessoires.",
                maxLength = 5000,
                requiredMode = Schema.RequiredMode.REQUIRED
        )
        @NotBlank(message = "La description est obligatoire")
        @Size(max = 5000, message = "La description ne doit pas dépasser 5000 caractères")
        String description,

        @Schema(
                description = "Nouveau prix de vente",
                example = "230000.00",
                minimum = "0",
                requiredMode = Schema.RequiredMode.REQUIRED
        )
        @NotNull(message = "Le prix est obligatoire")
        @DecimalMin(value = "0.0", message = "Le prix doit être supérieur ou égal à 0")
        BigDecimal prix,

        @Schema(
                description = "Nouvel état de l'article",
                example = "TRES_BON_ETAT",
                requiredMode = Schema.RequiredMode.REQUIRED
        )
        @NotNull(message = "L'état de l'annonce est obligatoire")
        EtatAnnonce etat,

        @Schema(
                description = "Ville de l'article",
                example = "Dakar",
                maxLength = 100,
                requiredMode = Schema.RequiredMode.REQUIRED
        )
        @NotBlank(message = "La ville est obligatoire")
        @Size(max = 100, message = "La ville ne doit pas dépasser 100 caractères")
        String ville,

        @Schema(
                description = "Quartier de l'article",
                example = "Mermoz",
                maxLength = 100
        )
        @Size(max = 100, message = "Le quartier ne doit pas dépasser 100 caractères")
        String quartier,

        @Schema(
                description = "Identifiant de la catégorie",
                example = "2",
                minimum = "1",
                requiredMode = Schema.RequiredMode.REQUIRED
        )
        @NotNull(message = "La catégorie est obligatoire")
        Long categoryId
) {}
