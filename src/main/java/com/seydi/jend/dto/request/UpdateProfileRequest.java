package com.seydi.jend.dto.request;

import jakarta.validation.constraints.Size;

public record UpdateProfileRequest(

        @Size(
                max = 100,
                message = "Le nom ne doit pas dépasser 100 caractères"
        )
        String nom,

        @Size(
                max = 30,
                message = "Le téléphone ne doit pas dépasser 30 caractères"
        )
        String telephone,

        @Size(
                max = 100,
                message = "La ville ne doit pas dépasser 100 caractères"
        )
        String ville
) {
}