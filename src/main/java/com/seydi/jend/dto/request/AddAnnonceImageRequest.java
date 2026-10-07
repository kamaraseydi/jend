package com.seydi.jend.dto.request;

import jakarta.validation.constraints.*;

public record AddAnnonceImageRequest(

        @NotBlank(message = "L'URL de l'image est obligatoire")
        @Size(max = 1000, message = "L'URL ne doit pas dépasser 1000 caractères")
        String url,

        @NotNull(message = "L'ordre est obligatoire")
        @Min(value = 1, message = "L'ordre doit être au minimum 1")
        @Max(value = 8, message = "L'ordre doit être au maximum 8")
        Integer ordre
) {}
