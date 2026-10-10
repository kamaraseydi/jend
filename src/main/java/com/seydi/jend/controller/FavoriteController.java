
package com.seydi.jend.controller;

import com.seydi.jend.dto.response.AnnonceResponse;
import com.seydi.jend.dto.response.FavoriteResponse;
import com.seydi.jend.service.FavoriteService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/favoris")
@RequiredArgsConstructor
@SecurityRequirement(name = "bearerAuth")
public class FavoriteController {

    private final FavoriteService favoriteService;

    @Operation(
            summary = "Lister mes favoris",
            description = "Retourne les annonces enregistrées dans les favoris de l'utilisateur connecté."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Favoris récupérés"),
            @ApiResponse(responseCode = "401", description = "Authentification requise")
    })
    @GetMapping
    public List<AnnonceResponse> findMyFavorites() {
        return favoriteService.findMyFavorites();
    }

    @Operation(
            summary = "Ajouter une annonce aux favoris",
            description = "Enregistre une annonce dans les favoris de l'utilisateur connecté."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Favori ajouté"),
            @ApiResponse(responseCode = "401", description = "Authentification requise"),
            @ApiResponse(responseCode = "404", description = "Annonce introuvable")
    })
    @PostMapping("/{annonceId}")
    @ResponseStatus(HttpStatus.CREATED)
    public FavoriteResponse addFavorite(
            @PathVariable Long annonceId) {
        return favoriteService.addFavorite(annonceId);
    }

    @Operation(
            summary = "Retirer une annonce des favoris",
            description = "Retire une annonce des favoris de l'utilisateur connecté."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Favori supprimé"),
            @ApiResponse(responseCode = "401", description = "Authentification requise"),
            @ApiResponse(responseCode = "404", description = "Favori introuvable")
    })
    @DeleteMapping("/{annonceId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void removeFavorite(
            @PathVariable Long annonceId) {
        favoriteService.removeFavorite(annonceId);
    }
}
