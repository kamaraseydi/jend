
package com.seydi.jend.controller;

import com.seydi.jend.dto.request.UpdateProfessionalStatusRequest;
import com.seydi.jend.dto.request.UpdateProfileRequest;
import com.seydi.jend.dto.response.AdminUserResponse;
import com.seydi.jend.dto.response.MyProfileResponse;
import com.seydi.jend.dto.response.UserProfileResponse;
import com.seydi.jend.service.UserProfileService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserProfileController {

    private final UserProfileService userProfileService;

    @Operation(
            summary = "Consulter mon profil",
            description = "Retourne le profil de l'utilisateur connecté.",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Profil récupéré"),
            @ApiResponse(responseCode = "401", description = "Authentification requise")
    })
    @GetMapping("/me")
    public MyProfileResponse getMyProfile() {
        return userProfileService.getMyProfile();
    }

    @Operation(
            summary = "Modifier mon profil",
            description = "Met à jour les informations du profil de l'utilisateur connecté.",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Profil modifié"),
            @ApiResponse(responseCode = "400", description = "Données invalides"),
            @ApiResponse(responseCode = "401", description = "Authentification requise")
    })
    @PutMapping("/me")
    public MyProfileResponse updateMyProfile(
            @Valid @RequestBody UpdateProfileRequest request) {
        return userProfileService.updateMyProfile(request);
    }

    @Operation(
            summary = "Supprimer mon compte",
            description = "Supprime le compte de l'utilisateur connecté.",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Compte supprimé"),
            @ApiResponse(responseCode = "401", description = "Authentification requise")
    })
    @DeleteMapping("/me")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteMyAccount() {
        userProfileService.deleteMyAccount();
    }

    @Operation(
            summary = "Consulter un profil public",
            description = "Retourne les informations publiques d'un utilisateur."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Profil récupéré"),
            @ApiResponse(responseCode = "404", description = "Utilisateur introuvable")
    })
    @GetMapping("/{id}")
    public UserProfileResponse getPublicProfile(@PathVariable Long id) {
        return userProfileService.getPublicProfile(id);
    }

    @Operation(
            summary = "Suspendre un utilisateur",
            description = "Suspend un compte utilisateur.",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Utilisateur suspendu"),
            @ApiResponse(responseCode = "401", description = "Authentification requise"),
            @ApiResponse(responseCode = "403", description = "Action interdite"),
            @ApiResponse(responseCode = "404", description = "Utilisateur introuvable")
    })
    @PatchMapping("/{id}/suspend")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void suspendUser(@PathVariable Long id) {
        userProfileService.suspendUser(id);
    }

    @Operation(
            summary = "Réactiver un utilisateur",
            description = "Réactive un compte utilisateur suspendu.",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Utilisateur réactivé"),
            @ApiResponse(responseCode = "401", description = "Authentification requise"),
            @ApiResponse(responseCode = "403", description = "Action interdite"),
            @ApiResponse(responseCode = "404", description = "Utilisateur introuvable")
    })
    @PatchMapping("/{id}/reactivate")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void reactivateUser(@PathVariable Long id) {
        userProfileService.reactivateUser(id);
    }

    @Operation(
            summary = "Modifier le statut professionnel",
            description = "Modifie le statut professionnel d'un utilisateur.",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Statut professionnel modifié"),
            @ApiResponse(responseCode = "400", description = "Données invalides"),
            @ApiResponse(responseCode = "401", description = "Authentification requise"),
            @ApiResponse(responseCode = "403", description = "Action interdite"),
            @ApiResponse(responseCode = "404", description = "Utilisateur introuvable")
    })
    @PatchMapping("/{id}/professional")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void updateProfessionalStatus(
            @PathVariable Long id,
            @Valid @RequestBody UpdateProfessionalStatusRequest request) {
        userProfileService.updateProfessionalStatus(
                id,
                request.estProfessionnel()
        );
    }

    @Operation(
            summary = "Lister les utilisateurs",
            description = "Retourne la liste administrative des utilisateurs.",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Utilisateurs récupérés"),
            @ApiResponse(responseCode = "401", description = "Authentification requise"),
            @ApiResponse(responseCode = "403", description = "Accès réservé aux administrateurs")
    })
    @GetMapping
    public List<AdminUserResponse> findAllUsers() {
        return userProfileService.findAllUsers();
    }
}
