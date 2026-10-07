package com.seydi.jend.service;

import com.seydi.jend.dto.request.AddAnnonceImageRequest;
import com.seydi.jend.dto.response.AnnonceImageResponse;
import com.seydi.jend.entity.Annonce;
import com.seydi.jend.entity.AnnonceImage;
import com.seydi.jend.entity.StatutAnnonce;
import com.seydi.jend.entity.UserProfile;
import com.seydi.jend.exception.AnnonceModificationInterditeException;
import com.seydi.jend.exception.ResourceNotFoundException;
import com.seydi.jend.exception.UtilisateurSuspenduException;
import com.seydi.jend.exception.CompteSupprimeException;
import com.seydi.jend.mapper.AnnonceImageMapper;
import com.seydi.jend.repository.AnnonceImageRepository;
import com.seydi.jend.repository.AnnonceRepository;
import com.seydi.jend.security.CurrentUserService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class AnnonceImageService {

    private final AnnonceImageRepository annonceImageRepository;
    private final AnnonceRepository annonceRepository;
    private final AnnonceImageMapper annonceImageMapper;
    private final CurrentUserService currentUserService;

    @Transactional
    public AnnonceImageResponse addImage(
            Long annonceId,
            AddAnnonceImageRequest request
    ) {

        UserProfile currentUser = currentUserService.getCurrentUser();

        checkAccountActive(currentUser);

        Annonce annonce = annonceRepository.findById(annonceId)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Annonce introuvable"
                        ));

        checkOwner(annonce, currentUser);

        checkAnnonceModifiable(annonce);

        if (annonceImageRepository.countByAnnonceId(annonceId) >= 8) {
            throw new AnnonceModificationInterditeException(
                    "Une annonce ne peut pas contenir plus de 8 images"
            );
        }

        if (annonceImageRepository.existsByAnnonceIdAndOrdre(
                annonceId,
                request.ordre()
        )) {
            throw new AnnonceModificationInterditeException(
                    "Cet ordre d'image est déjà utilisé"
            );
        }

        AnnonceImage image = annonceImageMapper.toEntity(request);

        image.setAnnonce(annonce);

        AnnonceImage savedImage =
                annonceImageRepository.save(image);

        return annonceImageMapper.toResponse(savedImage);
    }

    @Transactional(readOnly = true)
    public List<AnnonceImageResponse> findImages(Long annonceId) {

        Annonce annonce = annonceRepository.findById(annonceId)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Annonce introuvable"
                        ));

        if (annonce.getStatut() != StatutAnnonce.PUBLIEE
                && annonce.getStatut() != StatutAnnonce.VENDUE) {

            throw new ResourceNotFoundException(
                    "Annonce introuvable"
            );
        }

        return annonceImageRepository
                .findByAnnonceIdOrderByOrdreAsc(annonceId)
                .stream()
                .map(annonceImageMapper::toResponse)
                .toList();
    }

    @Transactional
    public void deleteImage(
            Long annonceId,
            Long imageId
    ) {

        UserProfile currentUser = currentUserService.getCurrentUser();

        checkAccountActive(currentUser);

        Annonce annonce = annonceRepository.findById(annonceId)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Annonce introuvable"
                        ));

        checkOwner(annonce, currentUser);

        checkAnnonceModifiable(annonce);

        AnnonceImage image = annonceImageRepository.findById(imageId)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Image introuvable"
                        ));

        if (!image.getAnnonce().getId().equals(annonceId)) {
            throw new AccessDeniedException(
                    "Cette image n'appartient pas à cette annonce"
            );
        }

        annonceImageRepository.delete(image);
    }

    private void checkOwner(
            Annonce annonce,
            UserProfile currentUser
    ) {

        if (!annonce.getVendeur().getId().equals(currentUser.getId())) {
            throw new AccessDeniedException(
                    "Vous n'êtes pas autorisé à modifier les images de cette annonce"
            );
        }
    }

    private void checkAnnonceModifiable(Annonce annonce) {

        if (annonce.getStatut() == StatutAnnonce.VENDUE
                || annonce.getStatut() == StatutAnnonce.SUPPRIMEE) {

            throw new AnnonceModificationInterditeException(
                    "Cette annonce ne peut plus être modifiée"
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