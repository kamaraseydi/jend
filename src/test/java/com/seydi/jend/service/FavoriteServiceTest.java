package com.seydi.jend.service;

import com.seydi.jend.dto.response.AnnonceResponse;
import com.seydi.jend.dto.response.FavoriteResponse;
import com.seydi.jend.entity.Annonce;
import com.seydi.jend.entity.Favorite;
import com.seydi.jend.entity.Role;
import com.seydi.jend.entity.StatutAnnonce;
import com.seydi.jend.entity.UserProfile;
import com.seydi.jend.exception.FavoriteModificationInterditeException;
import com.seydi.jend.exception.ResourceNotFoundException;
import com.seydi.jend.mapper.AnnonceMapper;
import com.seydi.jend.repository.AnnonceRepository;
import com.seydi.jend.repository.FavoriteRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.access.AccessDeniedException;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import com.seydi.jend.security.CurrentUserService;

@ExtendWith(MockitoExtension.class)
class FavoriteServiceTest {

    @Mock
    private FavoriteRepository favoriteRepository;

    @Mock
    private AnnonceRepository annonceRepository;

    @Mock
    private CurrentUserService currentUserService;

    @Mock
    private AnnonceMapper annonceMapper;

    @InjectMocks
    private FavoriteService favoriteService;


    // =========================================================
    // ADD FAVORITE
    // =========================================================

    @Test
    void shouldAddFavorite() {

        UserProfile user = new UserProfile();
        user.setId(1L);
        user.setRole(Role.UTILISATEUR);

        Annonce annonce = new Annonce();
        annonce.setId(10L);
        annonce.setStatut(StatutAnnonce.PUBLIEE);

        UserProfile vendeur = new UserProfile();
        vendeur.setId(2L);

        annonce.setVendeur(vendeur);

        Favorite favorite = new Favorite();
        favorite.setAnnonce(annonce);
        favorite.setUser(user);

        OffsetDateTime createdAt = OffsetDateTime.now();
        favorite.setCreatedAt(createdAt);

        when(currentUserService.getCurrentUser())
                .thenReturn(user);

        when(annonceRepository.findById(10L))
                .thenReturn(Optional.of(annonce));

        when(favoriteRepository.existsByUserIdAndAnnonceId(1L, 10L))
                .thenReturn(false);

        when(favoriteRepository.save(any(Favorite.class)))
                .thenReturn(favorite);

        FavoriteResponse result =
                favoriteService.addFavorite(10L);

        assertEquals(10L, result.annonceId());
        assertEquals(createdAt, result.createdAt());

        verify(favoriteRepository)
                .save(any(Favorite.class));
    }


    @Test
    void shouldRejectFavoriteWhenAnnonceDoesNotExist() {

        UserProfile user = new UserProfile();
        user.setId(1L);

        when(currentUserService.getCurrentUser())
                .thenReturn(user);

        when(annonceRepository.findById(99L))
                .thenReturn(Optional.empty());

        assertThrows(
                ResourceNotFoundException.class,
                () -> favoriteService.addFavorite(99L)
        );

        verify(favoriteRepository, never())
                .save(any());
    }


    @Test
    void shouldRejectFavoriteWhenAnnonceIsNotPublished() {

        UserProfile user = new UserProfile();
        user.setId(1L);

        Annonce annonce = new Annonce();
        annonce.setId(10L);
        annonce.setStatut(StatutAnnonce.SUSPENDUE);

        when(currentUserService.getCurrentUser())
                .thenReturn(user);

        when(annonceRepository.findById(10L))
                .thenReturn(Optional.of(annonce));

        assertThrows(
                ResourceNotFoundException.class,
                () -> favoriteService.addFavorite(10L)
        );

        verify(favoriteRepository, never())
                .save(any());
    }


    @Test
    void shouldRejectFavoriteWhenUserIsOwner() {

        UserProfile user = new UserProfile();
        user.setId(1L);

        Annonce annonce = new Annonce();
        annonce.setId(10L);
        annonce.setStatut(StatutAnnonce.PUBLIEE);
        annonce.setVendeur(user);

        when(currentUserService.getCurrentUser())
                .thenReturn(user);

        when(annonceRepository.findById(10L))
                .thenReturn(Optional.of(annonce));

        assertThrows(
                AccessDeniedException.class,
                () -> favoriteService.addFavorite(10L)
        );

        verify(favoriteRepository, never())
                .save(any());
    }


    @Test
    void shouldRejectDuplicateFavorite() {

        UserProfile user = new UserProfile();
        user.setId(1L);

        UserProfile vendeur = new UserProfile();
        vendeur.setId(2L);

        Annonce annonce = new Annonce();
        annonce.setId(10L);
        annonce.setStatut(StatutAnnonce.PUBLIEE);
        annonce.setVendeur(vendeur);

        when(currentUserService.getCurrentUser())
                .thenReturn(user);

        when(annonceRepository.findById(10L))
                .thenReturn(Optional.of(annonce));

        when(favoriteRepository.existsByUserIdAndAnnonceId(1L, 10L))
                .thenReturn(true);

        assertThrows(
                FavoriteModificationInterditeException.class,
                () -> favoriteService.addFavorite(10L)
        );

        verify(favoriteRepository, never())
                .save(any());
    }


    // =========================================================
    // REMOVE FAVORITE
    // =========================================================

    @Test
    void shouldRemoveFavorite() {

        UserProfile user = new UserProfile();
        user.setId(1L);

        Favorite favorite = new Favorite();

        when(currentUserService.getCurrentUser())
                .thenReturn(user);

        when(favoriteRepository.findByUserIdAndAnnonceId(1L, 10L))
                .thenReturn(Optional.of(favorite));

        favoriteService.removeFavorite(10L);

        verify(favoriteRepository)
                .delete(favorite);
    }


    @Test
    void shouldRejectRemoveWhenFavoriteDoesNotExist() {

        UserProfile user = new UserProfile();
        user.setId(1L);

        when(currentUserService.getCurrentUser())
                .thenReturn(user);

        when(favoriteRepository.findByUserIdAndAnnonceId(1L, 99L))
                .thenReturn(Optional.empty());

        assertThrows(
                ResourceNotFoundException.class,
                () -> favoriteService.removeFavorite(99L)
        );

        verify(favoriteRepository, never())
                .delete(any());
    }


    // =========================================================
    // FIND MY FAVORITES
    // =========================================================

    @Test
    void shouldFindMyFavorites() {

        UserProfile user = new UserProfile();
        user.setId(1L);

        UserProfile vendeur = new UserProfile();
        vendeur.setId(2L);

        Annonce annonce1 = new Annonce();
        annonce1.setId(10L);
        annonce1.setStatut(StatutAnnonce.PUBLIEE);
        annonce1.setVendeur(vendeur);

        Annonce annonce2 = new Annonce();
        annonce2.setId(20L);
        annonce2.setStatut(StatutAnnonce.PUBLIEE);
        annonce2.setVendeur(vendeur);

        Favorite favorite1 = new Favorite();
        favorite1.setAnnonce(annonce1);

        Favorite favorite2 = new Favorite();
        favorite2.setAnnonce(annonce2);

        AnnonceResponse response1 =
                new AnnonceResponse(
                        10L,
                        "iPhone 15",
                        "Très bon état",
                        BigDecimal.valueOf(650000),
                        com.seydi.jend.entity.EtatAnnonce.TRES_BON_ETAT,
                        StatutAnnonce.PUBLIEE,
                        "Dakar",
                        "Almadies",
                        2L,
                        "Vendeur 1",
                        1L,
                        "Téléphones",
                        null,
                        null
                );

        AnnonceResponse response2 =
                new AnnonceResponse(
                        20L,
                        "Samsung S24",
                        "Comme neuf",
                        BigDecimal.valueOf(450000),
                        com.seydi.jend.entity.EtatAnnonce.COMME_NEUF,
                        StatutAnnonce.PUBLIEE,
                        "Dakar",
                        "Mermoz",
                        2L,
                        "Vendeur 1",
                        1L,
                        "Téléphones",
                        null,
                        null
                );

        when(currentUserService.getCurrentUser())
                .thenReturn(user);

        when(favoriteRepository.findByUserId(1L))
                .thenReturn(List.of(favorite1, favorite2));

        when(annonceMapper.toResponse(annonce1))
                .thenReturn(response1);

        when(annonceMapper.toResponse(annonce2))
                .thenReturn(response2);

        List<AnnonceResponse> result =
                favoriteService.findMyFavorites();

        assertEquals(2, result.size());
        assertEquals(10L, result.get(0).id());
        assertEquals(20L, result.get(1).id());

        verify(favoriteRepository)
                .findByUserId(1L);
    }


    @Test
    void shouldNotReturnNonPublishedFavorites() {

        UserProfile user = new UserProfile();
        user.setId(1L);

        UserProfile vendeur = new UserProfile();
        vendeur.setId(2L);

        Annonce publishedAnnonce = new Annonce();
        publishedAnnonce.setId(10L);
        publishedAnnonce.setStatut(StatutAnnonce.PUBLIEE);
        publishedAnnonce.setVendeur(vendeur);

        Annonce suspendedAnnonce = new Annonce();
        suspendedAnnonce.setId(20L);
        suspendedAnnonce.setStatut(StatutAnnonce.SUSPENDUE);
        suspendedAnnonce.setVendeur(vendeur);

        Favorite favorite1 = new Favorite();
        favorite1.setAnnonce(publishedAnnonce);

        Favorite favorite2 = new Favorite();
        favorite2.setAnnonce(suspendedAnnonce);

        AnnonceResponse response =
                new AnnonceResponse(
                        10L,
                        "iPhone 15",
                        "Très bon état",
                        BigDecimal.valueOf(650000),
                        com.seydi.jend.entity.EtatAnnonce.TRES_BON_ETAT,
                        StatutAnnonce.PUBLIEE,
                        "Dakar",
                        "Almadies",
                        2L,
                        "Vendeur",
                        1L,
                        "Téléphones",
                        null,
                        null
                );

        when(currentUserService.getCurrentUser())
                .thenReturn(user);

        when(favoriteRepository.findByUserId(1L))
                .thenReturn(List.of(favorite1, favorite2));

        when(annonceMapper.toResponse(publishedAnnonce))
                .thenReturn(response);

        List<AnnonceResponse> result =
                favoriteService.findMyFavorites();

        assertEquals(1, result.size());
        assertEquals(10L, result.get(0).id());

        verify(annonceMapper)
                .toResponse(publishedAnnonce);

        verify(annonceMapper, never())
                .toResponse(suspendedAnnonce);
    }
}