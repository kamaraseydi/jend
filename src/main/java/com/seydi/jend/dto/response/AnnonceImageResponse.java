package com.seydi.jend.dto.response;

import java.time.OffsetDateTime;

public record AnnonceImageResponse(
        Long id,
        String url,
        Integer ordre,
        OffsetDateTime createdAt
) {}