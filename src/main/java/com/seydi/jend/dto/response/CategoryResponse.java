package com.seydi.jend.dto.response;

import java.time.OffsetDateTime;

public record CategoryResponse(
        Long id,
        String nom,
        OffsetDateTime createdAt,
        OffsetDateTime updatedAt
) {}
