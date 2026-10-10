
package com.seydi.jend.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

@Schema(description = "Données nécessaires à la modification du statut professionnel")
public record UpdateProfessionalStatusRequest(

        @Schema(
                description = "Indique si l'utilisateur est un professionnel",
                example = "true",
                requiredMode = Schema.RequiredMode.REQUIRED
        )
        @NotNull(message = "Le statut professionnel est obligatoire")
        Boolean estProfessionnel
) {}
