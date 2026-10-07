package com.seydi.jend.dto.response;

import com.seydi.jend.entity.EtatAnnonce;
import com.seydi.jend.entity.StatutAnnonce;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;

public record AnnonceResponse(
        Long id,
        String titre,
        String description,
        BigDecimal prix,
        EtatAnnonce etat,
        StatutAnnonce statut,
        String ville,
        String quartier,
        Long vendeurId,
        String vendeurNom,
        Long categoryId,
        String categoryNom,
        List<AnnonceImageResponse> images,
        OffsetDateTime createdAt,
        OffsetDateTime updatedAt
) {}