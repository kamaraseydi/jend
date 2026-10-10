
package com.seydi.jend.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Size;

@Schema(description = "Données nécessaires à la modification du profil utilisateur")
public record UpdateProfileRequest(

        @Schema(
                description = "Nouveau nom de l'utilisateur",
                example = "Seydina Kamara",
                maxLength = 100
        )
        @Size(
                max = 100,
                message = "Le nom ne doit pas dépasser 100 caractères"
        )
        String nom,

        @Schema(
                description = "Numéro de téléphone de l'utilisateur",
                example = "+221771234567",
                maxLength = 30
        )
        @Size(
                max = 30,
                message = "Le téléphone ne doit pas dépasser 30 caractères"
        )
        String telephone,

        @Schema(
                description = "Ville de résidence de l'utilisateur",
                example = "Dakar",
                maxLength = 100
        )
        @Size(
                max = 100,
                message = "La ville ne doit pas dépasser 100 caractères"
        )
        String ville
) {}
