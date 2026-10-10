
package com.seydi.jend.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

@Schema(description = "Résultats paginés d'une recherche")
public record PageResponse<T>(

        @Schema(description = "Éléments présents sur la page courante")
        List<T> content,

        @Schema(description = "Numéro de la page courante, à partir de 0", example = "0")
        int page,

        @Schema(description = "Nombre d'éléments demandés par page", example = "20")
        int size,

        @Schema(description = "Nombre total d'éléments correspondant à la recherche", example = "125")
        long totalElements,

        @Schema(description = "Nombre total de pages", example = "7")
        int totalPages
) {}
