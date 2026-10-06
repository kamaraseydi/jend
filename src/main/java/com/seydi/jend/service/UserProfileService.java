package com.seydi.jend.service;

import com.seydi.jend.dto.request.UpdateProfileRequest;
import com.seydi.jend.dto.response.MyProfileResponse;
import com.seydi.jend.dto.response.UserProfileResponse;
import com.seydi.jend.entity.Role;
import com.seydi.jend.repository.AnnonceRepository;
import com.seydi.jend.security.CurrentUserService;
import com.seydi.jend.entity.UserProfile;
import com.seydi.jend.exception.CompteSupprimeException;
import com.seydi.jend.exception.ResourceNotFoundException;
import com.seydi.jend.exception.UtilisateurSuspenduException;
import com.seydi.jend.mapper.UserProfileMapper;
import com.seydi.jend.repository.UserProfileRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.seydi.jend.entity.Annonce;
import com.seydi.jend.entity.StatutAnnonce;
import com.seydi.jend.repository.AnnonceRepository;

import java.util.List;
import java.time.OffsetDateTime;

@Service
@RequiredArgsConstructor
public class UserProfileService {

    private final UserProfileRepository userProfileRepository;
    private final UserProfileMapper userProfileMapper;
    private final CurrentUserService currentUserService;
    private final AnnonceRepository annonceRepository;

    @Transactional(readOnly = true)
    public MyProfileResponse getMyProfile() {

        UserProfile currentUser = currentUserService.getCurrentUser();

        checkAccountActive(currentUser);

        return userProfileMapper.toMyProfileResponse(currentUser);
    }

    @Transactional
    public MyProfileResponse updateMyProfile(
            UpdateProfileRequest request
    ) {

        UserProfile currentUser = currentUserService.getCurrentUser();

        checkAccountActive(currentUser);

        userProfileMapper.updateEntity(currentUser, request);

        currentUser.setUpdatedAt(OffsetDateTime.now());

        return userProfileMapper.toMyProfileResponse(currentUser);
    }

    @Transactional(readOnly = true)
    public UserProfileResponse getPublicProfile(Long id) {

        UserProfile user = userProfileRepository.findById(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Utilisateur introuvable"
                        ));

        if (user.getDeletedAt() != null) {
            throw new ResourceNotFoundException(
                    "Utilisateur introuvable"
            );
        }

        return userProfileMapper.toPublicResponse(user);
    }

    @Transactional
    public void suspendUser(Long id) {

        UserProfile admin = currentUserService.getCurrentUser();

        checkAdmin(admin);

        UserProfile user = userProfileRepository.findById(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Utilisateur introuvable"
                        ));

        if (user.getDeletedAt() != null) {
            throw new ResourceNotFoundException(
                    "Utilisateur introuvable"
            );
        }

        if (user.getId().equals(admin.getId())) {
            throw new AccessDeniedException(
                    "Un administrateur ne peut pas se suspendre lui-même"
            );
        }

        user.setSuspendu(true);
        user.setUpdatedAt(OffsetDateTime.now());

        List<Annonce> annoncesPubliees =
                annonceRepository.findByVendeurIdAndStatut(
                        user.getId(),
                        StatutAnnonce.PUBLIEE
                );

        for (Annonce annonce : annoncesPubliees) {
            annonce.setStatut(StatutAnnonce.SUSPENDUE);
            annonce.setUpdatedAt(OffsetDateTime.now());
        }
    }

    @Transactional
    public void reactivateUser(Long id) {

        UserProfile admin = currentUserService.getCurrentUser();

        checkAdmin(admin);

        UserProfile user = userProfileRepository.findById(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Utilisateur introuvable"
                        ));

        if (user.getDeletedAt() != null) {
            throw new ResourceNotFoundException(
                    "Utilisateur introuvable"
            );
        }

        user.setSuspendu(false);
        user.setUpdatedAt(OffsetDateTime.now());
    }

    private void checkAdmin(UserProfile user) {

        if (user.getRole() != Role.ADMIN) {
            throw new AccessDeniedException(
                    "Seul un administrateur peut effectuer cette action"
            );
        }
    }

    private void checkAccountActive(UserProfile user) {

        if (user.getDeletedAt() != null) {
            throw new CompteSupprimeException(
                    "Votre compte a été supprimé"
            );
        }

        if (user.isSuspendu()) {
            throw new UtilisateurSuspenduException(
                    "Votre compte est suspendu"
            );
        }
    }
}