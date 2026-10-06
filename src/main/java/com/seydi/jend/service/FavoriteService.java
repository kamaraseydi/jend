package com.seydi.jend.service;

import com.seydi.jend.dto.response.AnnonceResponse;
import com.seydi.jend.dto.response.FavoriteResponse;
import com.seydi.jend.entity.Annonce;
import com.seydi.jend.exception.FavoriteModificationInterditeException;
import com.seydi.jend.security.CurrentUserService;
import com.seydi.jend.entity.Favorite;
import com.seydi.jend.entity.StatutAnnonce;
import com.seydi.jend.entity.UserProfile;
import com.seydi.jend.exception.ResourceNotFoundException;
import com.seydi.jend.mapper.AnnonceMapper;
import com.seydi.jend.repository.AnnonceRepository;
import com.seydi.jend.repository.FavoriteRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class FavoriteService {

    private final FavoriteRepository favoriteRepository;
    private final AnnonceRepository annonceRepository;
    private final CurrentUserService currentUserService;
    private final AnnonceMapper annonceMapper;

    @Transactional
    public FavoriteResponse addFavorite(Long annonceId) {

        UserProfile currentUser =
                currentUserService.getCurrentUser();

        Annonce annonce = annonceRepository.findById(annonceId)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Annonce introuvable"
                        ));

        if (annonce.getStatut() != StatutAnnonce.PUBLIEE) {
            throw new ResourceNotFoundException(
                    "Annonce introuvable"
            );
        }

        if (annonce.getVendeur().getId()
                .equals(currentUser.getId())) {

            throw new AccessDeniedException(
                    "Vous ne pouvez pas ajouter votre propre annonce aux favoris"
            );
        }

        if (favoriteRepository.existsByUserIdAndAnnonceId(
                currentUser.getId(),
                annonceId
        )) {

            throw new FavoriteModificationInterditeException(
                    "Cette annonce est déjà dans vos favoris"
            );
        }

        Favorite favorite = new Favorite();

        favorite.setUser(currentUser);
        favorite.setAnnonce(annonce);
        favorite.setCreatedAt(OffsetDateTime.now());

        Favorite savedFavorite =
                favoriteRepository.save(favorite);

        return new FavoriteResponse(
                savedFavorite.getAnnonce().getId(),
                savedFavorite.getCreatedAt()
        );
    }

    @Transactional
    public void removeFavorite(Long annonceId) {

        UserProfile currentUser =
                currentUserService.getCurrentUser();

        Favorite favorite =
                favoriteRepository.findByUserIdAndAnnonceId(
                        currentUser.getId(),
                        annonceId
                ).orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Favori introuvable"
                        ));

        favoriteRepository.delete(favorite);
    }

    @Transactional(readOnly = true)
    public List<AnnonceResponse> findMyFavorites() {

        UserProfile currentUser =
                currentUserService.getCurrentUser();

        return favoriteRepository
                .findByUserId(currentUser.getId())
                .stream()
                .map(Favorite::getAnnonce)
                .filter(annonce ->
                        annonce.getStatut() == StatutAnnonce.PUBLIEE)
                .map(annonceMapper::toResponse)
                .toList();
    }
}