package com.seydi.jend.dto.request;

import com.seydi.jend.entity.EtatAnnonce;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

public record CreateAnnonceRequest(

        @NotBlank(message = "Le titre est obligatoire")
        @Size(max = 150, message = "Le titre ne doit pas dépasser 150 caractères")
        String titre,

        @NotBlank(message = "La description est obligatoire")
        @Size(max = 5000, message = "La description ne doit pas dépasser 5000 caractères")
        String description,

        @NotNull(message = "Le prix est obligatoire")
        @DecimalMin(value = "0.0", message = "Le prix doit être supérieur ou égal à 0")
        BigDecimal prix,

        @NotNull(message = "L'état de l'annonce est obligatoire")
        EtatAnnonce etat,

        @NotBlank(message = "La ville est obligatoire")
        @Size(max = 100, message = "La ville ne doit pas dépasser 100 caractères")
        String ville,

        @Size(max = 100, message = "Le quartier ne doit pas dépasser 100 caractères")
        String quartier,

        @NotNull(message = "La catégorie est obligatoire")
        Long categoryId
) { }