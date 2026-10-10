
package com.seydi.jend.dto.response;

import com.seydi.jend.entity.Role;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.OffsetDateTime;

@Schema(description = "Informations publiques d'un utilisateur")
public record UserProfileResponse(

        @Schema(description = "Identifiant de l'utilisateur", example = "12")
        Long id,

        @Schema(description = "Nom de l'utilisateur", example = "Seydina Kamara")
        String nom,

        @Schema(description = "Numéro de téléphone", example = "+221771234567")
        String telephone,

        @Schema(description = "Ville de résidence", example = "Dakar")
        String ville,

        @Schema(description = "Indique si l'utilisateur est professionnel", example = "true")
        boolean estProfessionnel,

        @Schema(description = "Rôle de l'utilisateur", example = "UTILISATEUR")
        Role role,

        @Schema(description = "Date de création du compte", example = "2026-10-09T10:30:00Z")
        OffsetDateTime createdAt
) {}
