package com.seydi.jend.dto.request;

import jakarta.validation.constraints.NotNull;

public record UpdateProfessionalStatusRequest(

        @NotNull(message = "Le statut professionnel est obligatoire")
        Boolean estProfessionnel

) {}