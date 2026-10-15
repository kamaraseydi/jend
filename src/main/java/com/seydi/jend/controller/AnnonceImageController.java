
package com.seydi.jend.controller;

import com.seydi.jend.dto.response.AnnonceImageResponse;
import com.seydi.jend.service.AnnonceImageService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/annonces/{annonceId}/images")
@RequiredArgsConstructor
public class AnnonceImageController {

    private final AnnonceImageService annonceImageService;

    @Operation(
            summary = "Ajouter une image à une annonce",
            description = "Téléverse une image vers le stockage sécurisé.",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Image ajoutée"),
            @ApiResponse(responseCode = "400", description = "Fichier ou ordre invalide"),
            @ApiResponse(responseCode = "401", description = "Authentification requise"),
            @ApiResponse(responseCode = "403", description = "Action interdite"),
            @ApiResponse(responseCode = "404", description = "Annonce introuvable")
    })
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @ResponseStatus(HttpStatus.CREATED)
    public AnnonceImageResponse addImage(
            @PathVariable Long annonceId,
            @RequestParam("file") MultipartFile file,
            @RequestParam("ordre") @Min(1) @Max(8) Integer ordre
    ) {
        return annonceImageService.addImage(annonceId, file, ordre);
    }

    @Operation(
            summary = "Lister les images d'une annonce",
            description = "Retourne les images des annonces publiées ou vendues."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Images récupérées"),
            @ApiResponse(responseCode = "404", description = "Annonce introuvable")
    })
    @GetMapping
    public List<AnnonceImageResponse> findImages(
            @PathVariable Long annonceId
    ) {
        return annonceImageService.findImages(annonceId);
    }

    @Operation(
            summary = "Supprimer une image",
            description = "Supprime une image du stockage et de l'annonce.",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Image supprimée"),
            @ApiResponse(responseCode = "401", description = "Authentification requise"),
            @ApiResponse(responseCode = "403", description = "Suppression interdite"),
            @ApiResponse(responseCode = "404", description = "Image ou annonce introuvable")
    })
    @DeleteMapping("/{imageId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteImage(
            @PathVariable Long annonceId,
            @PathVariable Long imageId
    ) {
        annonceImageService.deleteImage(annonceId, imageId);
    }
}
