package com.seydi.jend.service;

import com.seydi.jend.dto.request.CreateAnnonceRequest;
import com.seydi.jend.dto.response.AnnonceResponse;
import com.seydi.jend.entity.*;
import com.seydi.jend.exception.*;
import com.seydi.jend.mapper.AnnonceMapper;
import com.seydi.jend.repository.AnnonceRepository;
import com.seydi.jend.repository.CategoryRepository;
import com.seydi.jend.security.CurrentUserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.seydi.jend.dto.response.PageResponse;
import com.seydi.jend.specification.AnnonceSpecification;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;

import java.math.BigDecimal;
import java.util.List;
import java.time.OffsetDateTime;

@Service
@RequiredArgsConstructor
public class AnnonceService {

    private final AnnonceRepository annonceRepository;
    private final CategoryRepository categoryRepository;
    private final CurrentUserService currentUserService;
    private final AnnonceMapper annonceMapper;

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
                        ));

        Annonce annonce = annonceMapper.toEntity(request);

        annonce.setVendeur(currentUser);
        annonce.setCategory(category);
        annonce.setStatut(StatutAnnonce.BROUILLON);

        OffsetDateTime now = OffsetDateTime.now();

        annonce.setCreatedAt(now);
        annonce.setUpdatedAt(now);

        Annonce savedAnnonce = annonceRepository.save(annonce);

        return annonceMapper.toResponse(savedAnnonce);
    }

    @Transactional(readOnly = true)
    public AnnonceResponse findById(Long id) {

        Annonce annonce = annonceRepository.findById(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Annonce introuvable"));

        if (annonce.getStatut() != StatutAnnonce.PUBLIEE) {
            throw new ResourceNotFoundException("Annonce introuvable");
        }

        return annonceMapper.toResponse(annonce);
    }

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

        List<AnnonceResponse> content =
                annonces.getContent()
                        .stream()
                        .map(annonceMapper::toResponse)
                        .toList();

        return new PageResponse<>(
                content,
                annonces.getNumber(),
                annonces.getSize(),
                annonces.getTotalElements(),
                annonces.getTotalPages()
        );
    }

}