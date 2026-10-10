
package com.seydi.jend.controller;

import com.seydi.jend.dto.request.CreateAnnonceRequest;
import com.seydi.jend.dto.request.UpdateAnnonceRequest;
import com.seydi.jend.dto.response.AnnonceResponse;
import com.seydi.jend.dto.response.PageResponse;
import com.seydi.jend.entity.EtatAnnonce;
import com.seydi.jend.entity.StatutAnnonce;
import com.seydi.jend.entity.TriAnnonce;
import com.seydi.jend.service.AnnonceService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;

@RestController
@RequestMapping("/api/annonces")
@RequiredArgsConstructor
public class AnnonceController {

    private final AnnonceService annonceService;

    // POST /api/annonces
    @Operation(
            summary = "Créer une annonce",
            description = "Crée une annonce pour l'utilisateur authentifié.",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Annonce créée avec succès"),
            @ApiResponse(responseCode = "400", description = "Données invalides"),
            @ApiResponse(responseCode = "401", description = "Authentification requise"),
            @ApiResponse(responseCode = "403", description = "Accès interdit")
    })
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public AnnonceResponse create(
            @Valid @RequestBody CreateAnnonceRequest request) {

        return annonceService.create(request);
    }

    // GET /api/annonces/{id}
    @Operation(
            summary = "Récupérer une annonce",
            description = "Retourne les détails d'une annonce à partir de son identifiant."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Annonce trouvée"),
            @ApiResponse(responseCode = "404", description = "Annonce introuvable")
    })
    @GetMapping("/{id}")
    public AnnonceResponse findById(@PathVariable Long id) {
        return annonceService.findById(id);
    }

    // GET /api/annonces
    @Operation(
            summary = "Rechercher et lister les annonces",
            description = "Permet de filtrer, trier et paginer les annonces. " +
                    "Les filtres sont facultatifs."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Annonces récupérées avec succès"),
            @ApiResponse(responseCode = "400", description = "Paramètres de recherche invalides")
    })
    @GetMapping
    public PageResponse<AnnonceResponse> findAll(
            @RequestParam(required = false) String search,
            @RequestParam(required = false) String city,
            @RequestParam(required = false) Long category,
            @RequestParam(required = false) BigDecimal minPrice,
            @RequestParam(required = false) BigDecimal maxPrice,
            @RequestParam(required = false) EtatAnnonce etat,
            @RequestParam(defaultValue = "RECENT") TriAnnonce sort,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        return annonceService.findAll(
                search,
                city,
                category,
                minPrice,
                maxPrice,
                etat,
                sort,
                page,
                size
        );
    }

    // GET /api/annonces/me
    @Operation(
            summary = "Lister mes annonces",
            description = "Retourne les annonces de l'utilisateur authentifié, " +
                    "avec un filtre facultatif sur leur statut.",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Annonces personnelles récupérées"),
            @ApiResponse(responseCode = "401", description = "Authentification requise")
    })
    @GetMapping("/me")
    public PageResponse<AnnonceResponse> findMyAnnonces(
            @RequestParam(required = false) StatutAnnonce statut,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        return annonceService.findMyAnnonces(statut, page, size);
    }

    // PUT /api/annonces/{id}
    @Operation(
            summary = "Modifier une annonce",
            description = "Modifie une annonce selon les règles d'autorisation de l'application.",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Annonce modifiée avec succès"),
            @ApiResponse(responseCode = "400", description = "Données invalides"),
            @ApiResponse(responseCode = "401", description = "Authentification requise"),
            @ApiResponse(responseCode = "403", description = "Modification interdite"),
            @ApiResponse(responseCode = "404", description = "Annonce introuvable")
    })
    @PutMapping("/{id}")
    public AnnonceResponse update(
            @PathVariable Long id,
            @Valid @RequestBody UpdateAnnonceRequest request) {

        return annonceService.update(id, request);
    }

    // PATCH /api/annonces/{id}/publish
    @Operation(
            summary = "Publier une annonce",
            description = "Demande la publication d'une annonce, selon les règles métier.",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Annonce publiée"),
            @ApiResponse(responseCode = "401", description = "Authentification requise"),
            @ApiResponse(responseCode = "403", description = "Publication interdite"),
            @ApiResponse(responseCode = "404", description = "Annonce introuvable")
    })
    @PatchMapping("/{id}/publish")
    public AnnonceResponse publish(@PathVariable Long id) {
        return annonceService.publish(id);
    }

    // PATCH /api/annonces/{id}/pause
    @Operation(
            summary = "Mettre une annonce en pause",
            description = "Demande la mise en pause d'une annonce.",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Annonce mise en pause"),
            @ApiResponse(responseCode = "401", description = "Authentification requise"),
            @ApiResponse(responseCode = "403", description = "Action interdite"),
            @ApiResponse(responseCode = "404", description = "Annonce introuvable")
    })
    @PatchMapping("/{id}/pause")
    public AnnonceResponse pause(@PathVariable Long id) {
        return annonceService.pause(id);
    }

    // PATCH /api/annonces/{id}/sold
    @Operation(
            summary = "Marquer une annonce comme vendue",
            description = "Demande le passage de l'annonce au statut vendu.",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Annonce marquée comme vendue"),
            @ApiResponse(responseCode = "401", description = "Authentification requise"),
            @ApiResponse(responseCode = "403", description = "Action interdite"),
            @ApiResponse(responseCode = "404", description = "Annonce introuvable")
    })
    @PatchMapping("/{id}/sold")
    public AnnonceResponse sold(@PathVariable Long id) {
        return annonceService.sold(id);
    }

    // DELETE /api/annonces/{id}
    @Operation(
            summary = "Supprimer une annonce",
            description = "Supprime une annonce selon les règles d'autorisation de l'application.",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Annonce supprimée"),
            @ApiResponse(responseCode = "401", description = "Authentification requise"),
            @ApiResponse(responseCode = "403", description = "Suppression interdite"),
            @ApiResponse(responseCode = "404", description = "Annonce introuvable")
    })
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) {
        annonceService.delete(id);
    }
}
