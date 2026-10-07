package com.seydi.jend.dto.response;

import com.seydi.jend.entity.Role;

import java.time.OffsetDateTime;

public record AdminUserResponse(
        Long id,
        String nom,
        String email,
        String telephone,
        String ville,
        boolean estProfessionnel,
        boolean suspendu,
        Role role,
        OffsetDateTime createdAt,
        OffsetDateTime updatedAt
) {}