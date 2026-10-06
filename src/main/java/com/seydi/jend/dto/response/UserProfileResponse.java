package com.seydi.jend.dto.response;

import com.seydi.jend.entity.Role;

import java.time.OffsetDateTime;

public record UserProfileResponse(
        Long id,
        String nom,
        String telephone,
        String ville,
        boolean estProfessionnel,
        Role role,
        OffsetDateTime createdAt
) {
}