package com.seydi.jend.service;

import com.seydi.jend.dto.request.CreateAnnonceRequest;
import com.seydi.jend.dto.request.UpdateAnnonceRequest;
import com.seydi.jend.dto.response.AnnonceImageResponse;
import com.seydi.jend.dto.response.AnnonceResponse;
import com.seydi.jend.dto.response.PageResponse;
import com.seydi.jend.entity.*;
import com.seydi.jend.exception.*;
import com.seydi.jend.mapper.AnnonceImageMapper;
import com.seydi.jend.mapper.AnnonceMapper;
import com.seydi.jend.repository.AnnonceImageRepository;
import com.seydi.jend.repository.AnnonceRepository;
import com.seydi.jend.repository.CategoryRepository;
import com.seydi.jend.repository.FavoriteRepository;
import com.seydi.jend.security.CurrentUserService;
import com.seydi.jend.specification.AnnonceSpecification;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AnnonceService {

    private final AnnonceRepository annonceRepository;
    private final CategoryRepository categoryRepository;
    private final CurrentUserService currentUserService;
    private final AnnonceMapper annonceMapper;
    private final FavoriteRepository favoriteRepository;
    private final AnnonceImageRepository annonceImageRepository;
    private final AnnonceImageMapper annonceImageMapper;


    // =========================================================
    // CREATE
    // =========================================================

    @Transactional
    public AnnonceResponse create(CreateAnnonceRequest request) {

        UserProfile currentUser = currentUserService.getCurrentUser();

        if (currentUser.isSuspendu()) {
            throw new UtilisateurSuspenduException(
                    "Votre compte est suspendu"
            );
        }

        if (currentUser.getDeletedAt() != null) {
            throw new CompteSupprimeException(
                    "Votre compte a été supprimé"
            );
        }

        Category category = categoryRepository.findById(request.categoryId())
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Catégorie introuvable"
                        )
                );

        Annonce annonce = annonceMapper.toEntity(request);

        annonce.setVendeur(currentUser);
        annonce.setCategory(category);
        annonce.setStatut(StatutAnnonce.BROUILLON);

        OffsetDateTime now = OffsetDateTime.now();

        annonce.setCreatedAt(now);
        annonce.setUpdatedAt(now);

        Annonce savedAnnonce = annonceRepository.save(annonce);

        return annonceMapper.toResponse(
                savedAnnonce,
                getImageResponses(savedAnnonce.getId())
        );
    }


    // =========================================================
    // FIND BY ID
    // =========================================================

    @Transactional(readOnly = true)
    public AnnonceResponse findById(Long id) {

        Annonce annonce = annonceRepository.findById(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Annonce introuvable"
                        )
                );

        /*
         * Une annonce publiée ou vendue reste visible publiquement.
         *
         * BROUILLON    -> invisible
         * PUBLIEE      -> visible
         * SUSPENDUE    -> invisible
         * VENDUE       -> visible
         * SUPPRIMEE    -> invisible
         */
        if (annonce.getStatut() != StatutAnnonce.PUBLIEE
                && annonce.getStatut() != StatutAnnonce.VENDUE) {

            throw new ResourceNotFoundException(
                    "Annonce introuvable"
            );
        }

        return annonceMapper.toResponse(
                annonce,
                getImageResponses(annonce.getId())
        );
    }


    // =========================================================
    // IMAGES
    // =========================================================

    private List<AnnonceImageResponse> getImageResponses(Long annonceId) {

        return annonceImageRepository
                .findByAnnonceIdOrderByOrdreAsc(annonceId)
                .stream()
                .map(annonceImageMapper::toResponse)
                .toList();
    }


    // =========================================================
    // FIND ALL - PUBLIC
    // =========================================================

    @Transactional(readOnly = true)
    public PageResponse<AnnonceResponse> findAll(
            String search,
            String city,
            Long categoryId,
            BigDecimal minPrice,
            BigDecimal maxPrice,
            EtatAnnonce etat,
            int page,
            int size
    ) {

        if (page < 0) {
            throw new IllegalArgumentException(
                    "La page doit être supérieure ou égale à 0"
            );
        }

        if (size < 1 || size > 50) {
            throw new IllegalArgumentException(
                    "La taille doit être comprise entre 1 et 50"
            );
        }

        if (minPrice != null
                && maxPrice != null
                && minPrice.compareTo(maxPrice) > 0) {

            throw new IllegalArgumentException(
                    "Le prix minimum ne peut pas être supérieur au prix maximum"
            );
        }

        Specification<Annonce> specification =
                AnnonceSpecification.estPubliee();

        if (search != null && !search.isBlank()) {
            specification = specification.and(
                    AnnonceSpecification.contientTexte(search.trim())
            );
        }

        if (city != null && !city.isBlank()) {
            specification = specification.and(
                    AnnonceSpecification.parVille(city.trim())
            );
        }

        if (categoryId != null) {
            specification = specification.and(
                    AnnonceSpecification.parCategorie(categoryId)
            );
        }

        if (minPrice != null) {
            specification = specification.and(
                    AnnonceSpecification.prixSuperieurOuEgal(minPrice)
            );
        }

        if (maxPrice != null) {
            specification = specification.and(
                    AnnonceSpecification.prixInferieurOuEgal(maxPrice)
            );
        }

        if (etat != null) {
            specification = specification.and(
                    AnnonceSpecification.parEtat(etat)
            );
        }

        Pageable pageable = PageRequest.of(
                page,
                size,
                Sort.by(
                        Sort.Direction.DESC,
                        "createdAt"
                )
        );

        Page<Annonce> annonces =
                annonceRepository.findAll(
                        specification,
                        pageable
                );

        List<AnnonceResponse> content = annonces.getContent()
                .stream()
                .map(annonce -> annonceMapper.toResponse(
                        annonce,
                        getImageResponses(annonce.getId())
                ))
                .toList();

        return new PageResponse<>(
                content,
                annonces.getNumber(),
                annonces.getSize(),
                annonces.getTotalElements(),
                annonces.getTotalPages()
        );
    }


    // =========================================================
    // FIND MY ANNONCES
    // =========================================================

    @Transactional(readOnly = true)
    public PageResponse<AnnonceResponse> findMyAnnonces(
            StatutAnnonce statut,
            int page,
            int size
    ) {

        if (page < 0) {
            throw new IllegalArgumentException(
                    "La page doit être supérieure ou égale à 0"
            );
        }

        if (size < 1 || size > 50) {
            throw new IllegalArgumentException(
                    "La taille doit être comprise entre 1 et 50"
            );
        }

        UserProfile currentUser =
                currentUserService.getCurrentUser();

        Specification<Annonce> specification =
                AnnonceSpecification.parVendeur(
                        currentUser.getId()
                );

        if (statut != null) {
            specification = specification.and(
                    AnnonceSpecification.parStatut(statut)
            );
        }

        Pageable pageable = PageRequest.of(
                page,
                size,
                Sort.by(
                        Sort.Direction.DESC,
                        "createdAt"
                )
        );

        Page<Annonce> annonces =
                annonceRepository.findAll(
                        specification,
                        pageable
                );

        List<AnnonceResponse> content = annonces.getContent()
                .stream()
                .map(annonce -> annonceMapper.toResponse(
                        annonce,
                        getImageResponses(annonce.getId())
                ))
                .toList();

        return new PageResponse<>(
                content,
                annonces.getNumber(),
                annonces.getSize(),
                annonces.getTotalElements(),
                annonces.getTotalPages()
        );
    }


    // =========================================================
    // UPDATE
    // =========================================================

    @Transactional
    public AnnonceResponse update(
            Long id,
            UpdateAnnonceRequest request
    ) {

        UserProfile currentUser =
                currentUserService.getCurrentUser();

        Annonce annonce = annonceRepository.findById(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Annonce introuvable"
                        )
                );

        if (annonce.getStatut() == StatutAnnonce.VENDUE
                || annonce.getStatut() == StatutAnnonce.SUPPRIMEE) {

            throw new AnnonceModificationInterditeException(
                    "Cette annonce ne peut plus être modifiée"
            );
        }

        if (!annonce.getVendeur().getId()
                .equals(currentUser.getId())) {

            throw new AccessDeniedException(
                    "Vous n'êtes pas autorisé à modifier cette annonce"
            );
        }

        if (currentUser.isSuspendu()) {
            throw new UtilisateurSuspenduException(
                    "Votre compte est suspendu"
            );
        }

        if (currentUser.getDeletedAt() != null) {
            throw new CompteSupprimeException(
                    "Votre compte a été supprimé"
            );
        }

        Category category =
                categoryRepository.findById(request.categoryId())
                        .orElseThrow(() ->
                                new ResourceNotFoundException(
                                        "Catégorie introuvable"
                                )
                        );

        annonceMapper.updateEntity(
                annonce,
                request
        );

        annonce.setCategory(category);
        annonce.setUpdatedAt(OffsetDateTime.now());

        Annonce updatedAnnonce =
                annonceRepository.save(annonce);

        return annonceMapper.toResponse(
                updatedAnnonce,
                getImageResponses(updatedAnnonce.getId())
        );
    }


    // =========================================================
    // PUBLISH
    // =========================================================

    @Transactional
    public AnnonceResponse publish(Long id) {

        UserProfile currentUser =
                currentUserService.getCurrentUser();

        Annonce annonce = annonceRepository.findById(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Annonce introuvable"
                        )
                );

        if (!annonce.getVendeur().getId()
                .equals(currentUser.getId())) {

            throw new AccessDeniedException(
                    "Vous n'êtes pas autorisé à publier cette annonce"
            );
        }

        if (currentUser.isSuspendu()) {
            throw new UtilisateurSuspenduException(
                    "Votre compte est suspendu"
            );
        }

        if (currentUser.getDeletedAt() != null) {
            throw new CompteSupprimeException(
                    "Votre compte a été supprimé"
            );
        }

        if (annonce.getStatut() != StatutAnnonce.BROUILLON
                && annonce.getStatut() != StatutAnnonce.SUSPENDUE) {

            throw new InvalidAnnonceStatusTransitionException(
                    "Cette annonce ne peut pas être publiée depuis son statut actuel"
            );
        }

        annonce.setStatut(StatutAnnonce.PUBLIEE);
        annonce.setUpdatedAt(OffsetDateTime.now());

        Annonce publishedAnnonce =
                annonceRepository.save(annonce);

        return annonceMapper.toResponse(
                publishedAnnonce,
                getImageResponses(publishedAnnonce.getId())
        );
    }


    // =========================================================
    // PAUSE
    // =========================================================

    @Transactional
    public AnnonceResponse pause(Long id) {

        UserProfile currentUser =
                currentUserService.getCurrentUser();

        Annonce annonce = annonceRepository.findById(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Annonce introuvable"
                        )
                );

        if (!annonce.getVendeur().getId()
                .equals(currentUser.getId())) {

            throw new AccessDeniedException(
                    "Vous n'êtes pas autorisé à suspendre cette annonce"
            );
        }

        if (currentUser.isSuspendu()) {
            throw new UtilisateurSuspenduException(
                    "Votre compte est suspendu"
            );
        }

        if (currentUser.getDeletedAt() != null) {
            throw new CompteSupprimeException(
                    "Votre compte a été supprimé"
            );
        }

        if (annonce.getStatut() != StatutAnnonce.PUBLIEE) {

            throw new InvalidAnnonceStatusTransitionException(
                    "Cette annonce ne peut pas être suspendue depuis son statut actuel"
            );
        }

        annonce.setStatut(StatutAnnonce.SUSPENDUE);
        annonce.setUpdatedAt(OffsetDateTime.now());

        Annonce pausedAnnonce =
                annonceRepository.save(annonce);

        return annonceMapper.toResponse(
                pausedAnnonce,
                getImageResponses(pausedAnnonce.getId())
        );
    }


    // =========================================================
    // SOLD
    // =========================================================

    @Transactional
    public AnnonceResponse sold(Long id) {

        UserProfile currentUser =
                currentUserService.getCurrentUser();

        Annonce annonce = annonceRepository.findById(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Annonce introuvable"
                        )
                );

        if (!annonce.getVendeur().getId()
                .equals(currentUser.getId())) {

            throw new AccessDeniedException(
                    "Vous n'êtes pas autorisé à marquer cette annonce comme vendue"
            );
        }

        if (currentUser.isSuspendu()) {
            throw new UtilisateurSuspenduException(
                    "Votre compte est suspendu"
            );
        }

        if (currentUser.getDeletedAt() != null) {
            throw new CompteSupprimeException(
                    "Votre compte a été supprimé"
            );
        }

        if (annonce.getStatut() != StatutAnnonce.PUBLIEE) {

            throw new InvalidAnnonceStatusTransitionException(
                    "Cette annonce ne peut pas être marquée comme vendue depuis son statut actuel"
            );
        }

        annonce.setStatut(StatutAnnonce.VENDUE);
        annonce.setUpdatedAt(OffsetDateTime.now());

        Annonce soldAnnonce =
                annonceRepository.save(annonce);

        return annonceMapper.toResponse(
                soldAnnonce,
                getImageResponses(soldAnnonce.getId())
        );
    }


    // =========================================================
    // DELETE
    // =========================================================

    @Transactional
    public void delete(Long id) {

        UserProfile currentUser =
                currentUserService.getCurrentUser();

        Annonce annonce = annonceRepository.findById(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Annonce introuvable"
                        )
                );

        if (!annonce.getVendeur().getId()
                .equals(currentUser.getId())) {

            throw new AccessDeniedException(
                    "Vous n'êtes pas autorisé à supprimer cette annonce"
            );
        }

        if (currentUser.isSuspendu()) {
            throw new UtilisateurSuspenduException(
                    "Votre compte est suspendu"
            );
        }

        if (currentUser.getDeletedAt() != null) {
            throw new CompteSupprimeException(
                    "Votre compte a été supprimé"
            );
        }

        if (annonce.getStatut() == StatutAnnonce.SUPPRIMEE) {

            throw new AnnonceModificationInterditeException(
                    "Cette annonce est déjà supprimée"
            );
        }

        // Les favoris doivent être supprimés explicitement
        // car la suppression de l'annonce est logique.
        favoriteRepository.deleteByAnnonceId(id);

        annonce.setStatut(StatutAnnonce.SUPPRIMEE);
        annonce.setUpdatedAt(OffsetDateTime.now());
    }
}