package com.seydi.jend.dto.response;

import java.time.OffsetDateTime;

public record FavoriteResponse(
        Long annonceId,
        OffsetDateTime createdAt
) {}