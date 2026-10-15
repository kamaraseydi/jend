
package com.seydi.jend.service;

import com.seydi.jend.dto.response.AnnonceImageResponse;
import com.seydi.jend.entity.Annonce;
import com.seydi.jend.entity.AnnonceImage;
import com.seydi.jend.entity.StatutAnnonce;
import com.seydi.jend.entity.UserProfile;
import com.seydi.jend.exception.AnnonceModificationInterditeException;
import com.seydi.jend.exception.CompteSupprimeException;
import com.seydi.jend.exception.ResourceNotFoundException;
import com.seydi.jend.exception.UtilisateurSuspenduException;
import com.seydi.jend.repository.AnnonceImageRepository;
import com.seydi.jend.repository.AnnonceRepository;
import com.seydi.jend.security.CurrentUserService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AnnonceImageService {

    private static final long MAX_FILE_SIZE = 5L * 1024 * 1024;

    private final AnnonceImageRepository annonceImageRepository;
    private final AnnonceRepository annonceRepository;
    private final CurrentUserService currentUserService;
    private final SupabaseStorageService storageService;

    @Transactional
    public AnnonceImageResponse addImage(
            Long annonceId,
            MultipartFile file,
            Integer ordre
    ) {
        UserProfile currentUser = currentUserService.getCurrentUser();
        checkAccountActive(currentUser);

        Annonce annonce = annonceRepository.findById(annonceId)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Annonce introuvable"));

        checkOwner(annonce, currentUser);
        checkAnnonceModifiable(annonce);
        validateFile(file);
        validateOrder(ordre);

        if (annonceImageRepository.countByAnnonceId(annonceId) >= 8) {
            throw new AnnonceModificationInterditeException(
                    "Une annonce ne peut pas contenir plus de 8 images");
        }

        if (annonceImageRepository.existsByAnnonceIdAndOrdre(annonceId, ordre)) {
            throw new AnnonceModificationInterditeException(
                    "Cet ordre d'image est déjà utilisé");
        }

        String contentType = file.getContentType();
        String extension = extensionFor(contentType);
        String path = "annonces/" + annonceId + "/" + UUID.randomUUID()
                + "." + extension;

        try {
            storageService.upload(path, file.getBytes(), contentType);
        } catch (IOException ex) {
            throw new IllegalStateException(
                    "Impossible de lire le fichier envoyé", ex);
        }

        try {
            AnnonceImage image = new AnnonceImage();
            image.setAnnonce(annonce);
            image.setUrl(path);
            image.setOrdre(ordre);
            image.setCreatedAt(OffsetDateTime.now());

            AnnonceImage saved = annonceImageRepository.saveAndFlush(image);

            String signedUrl = storageService.createSignedUrl(path);
            return toResponse(saved, signedUrl);

        } catch (RuntimeException ex) {
            try {
                storageService.delete(path);
            } catch (RuntimeException cleanupException) {
                ex.addSuppressed(cleanupException);
            }
            throw ex;
        }
    }

    @Transactional(readOnly = true)
    public List<AnnonceImageResponse> findImages(Long annonceId) {
        Annonce annonce = annonceRepository.findById(annonceId)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Annonce introuvable"));

        if (annonce.getStatut() != StatutAnnonce.PUBLIEE
                && annonce.getStatut() != StatutAnnonce.VENDUE) {
            throw new ResourceNotFoundException("Annonce introuvable");
        }

        return annonceImageRepository
                .findByAnnonceIdOrderByOrdreAsc(annonceId)
                .stream()
                .map(image -> toResponse(
                        image,
                        storageService.createSignedUrl(image.getUrl())))
                .toList();
    }

    @Transactional
    public void deleteImage(Long annonceId, Long imageId) {
        UserProfile currentUser = currentUserService.getCurrentUser();
        checkAccountActive(currentUser);

        Annonce annonce = annonceRepository.findById(annonceId)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Annonce introuvable"));

        checkOwner(annonce, currentUser);
        checkAnnonceModifiable(annonce);

        AnnonceImage image = annonceImageRepository.findById(imageId)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Image introuvable"));

        if (!image.getAnnonce().getId().equals(annonceId)) {
            throw new AccessDeniedException(
                    "Cette image n'appartient pas à cette annonce");
        }

        // Supprimer d'abord le fichier distant.
        storageService.delete(image.getUrl());
        annonceImageRepository.delete(image);
    }

    private void validateFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("Le fichier image est obligatoire");
        }

        if (file.getSize() > MAX_FILE_SIZE) {
            throw new IllegalArgumentException(
                    "L'image ne doit pas dépasser 5 Mo");
        }

        extensionFor(file.getContentType());
    }

    private void validateOrder(Integer ordre) {
        if (ordre == null || ordre < 1 || ordre > 8) {
            throw new IllegalArgumentException(
                    "L'ordre doit être compris entre 1 et 8");
        }
    }

    private String extensionFor(String contentType) {
        if ("image/jpeg".equalsIgnoreCase(contentType)) {
            return "jpg";
        }
        if ("image/png".equalsIgnoreCase(contentType)) {
            return "png";
        }
        if ("image/webp".equalsIgnoreCase(contentType)) {
            return "webp";
        }

        throw new IllegalArgumentException(
                "Format refusé : seuls JPEG, PNG et WebP sont autorisés");
    }

    private AnnonceImageResponse toResponse(
            AnnonceImage image,
            String signedUrl
    ) {
        return new AnnonceImageResponse(
                image.getId(),
                signedUrl,
                image.getOrdre(),
                image.getCreatedAt()
        );
    }

    private void checkOwner(Annonce annonce, UserProfile currentUser) {
        if (!annonce.getVendeur().getId().equals(currentUser.getId())) {
            throw new AccessDeniedException(
                    "Vous n'êtes pas autorisé à modifier les images de cette annonce");
        }
    }

    private void checkAnnonceModifiable(Annonce annonce) {
        if (annonce.getStatut() == StatutAnnonce.VENDUE
                || annonce.getStatut() == StatutAnnonce.SUPPRIMEE) {
            throw new AnnonceModificationInterditeException(
                    "Cette annonce ne peut plus être modifiée");
        }
    }

    private void checkAccountActive(UserProfile user) {
        if (user.getDeletedAt() != null) {
            throw new CompteSupprimeException(
                    "Votre compte a été supprimé");
        }

        if (user.isSuspendu()) {
            throw new UtilisateurSuspenduException(
                    "Votre compte est suspendu");
        }
    }
}
