package com.seydi.jend.service;

import com.seydi.jend.dto.request.AddAnnonceImageRequest;
import com.seydi.jend.dto.response.AnnonceImageResponse;
import com.seydi.jend.entity.Annonce;
import com.seydi.jend.entity.AnnonceImage;
import com.seydi.jend.entity.StatutAnnonce;
import com.seydi.jend.entity.UserProfile;
import com.seydi.jend.exception.AnnonceModificationInterditeException;
import com.seydi.jend.exception.CompteSupprimeException;
import com.seydi.jend.exception.ResourceNotFoundException;
import com.seydi.jend.exception.UtilisateurSuspenduException;
import com.seydi.jend.mapper.AnnonceImageMapper;
import com.seydi.jend.repository.AnnonceImageRepository;
import com.seydi.jend.repository.AnnonceRepository;
import com.seydi.jend.security.CurrentUserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.access.AccessDeniedException;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AnnonceImageServiceTest {

    @Mock
    private AnnonceImageRepository annonceImageRepository;

    @Mock
    private AnnonceRepository annonceRepository;

    @Mock
    private AnnonceImageMapper annonceImageMapper;

    @Mock
    private CurrentUserService currentUserService;

    @InjectMocks
    private AnnonceImageService annonceImageService;

    private UserProfile user;
    private Annonce annonce;
    private AnnonceImage image;
    private AddAnnonceImageRequest request;
    private AnnonceImageResponse response;

    @BeforeEach
    void setUp() {
        user = new UserProfile();
        user.setId(1L);
        user.setNom("Seydi");
        user.setDeletedAt(null);
        user.setSuspendu(false);

        annonce = new Annonce();
        annonce.setId(100L);
        annonce.setVendeur(user);
        annonce.setStatut(StatutAnnonce.PUBLIEE);

        image = new AnnonceImage();
        image.setId(10L);
        image.setAnnonce(annonce);
        image.setUrl("https://example.com/image.jpg");
        image.setOrdre(1);
        image.setCreatedAt(OffsetDateTime.now());

        request = new AddAnnonceImageRequest(
                "https://example.com/image.jpg",
                1
        );

        response = new AnnonceImageResponse(
                10L,
                "https://example.com/image.jpg",
                1,
                image.getCreatedAt()
        );
    }

    @Test
    void addImage_shouldAddImage_whenUserIsOwner() {

        when(currentUserService.getCurrentUser())
                .thenReturn(user);

        when(annonceRepository.findById(100L))
                .thenReturn(Optional.of(annonce));

        when(annonceImageRepository.countByAnnonceId(100L))
                .thenReturn(2L);

        when(annonceImageRepository.existsByAnnonceIdAndOrdre(100L, 1))
                .thenReturn(false);

        when(annonceImageMapper.toEntity(request))
                .thenReturn(image);

        when(annonceImageRepository.save(image))
                .thenReturn(image);

        when(annonceImageMapper.toResponse(image))
                .thenReturn(response);

        AnnonceImageResponse result =
                annonceImageService.addImage(100L, request);

        assertEquals(response, result);
        assertEquals(annonce, image.getAnnonce());

        verify(annonceImageRepository).save(image);
        verify(annonceImageMapper).toResponse(image);
    }

    @Test
    void addImage_shouldThrow_whenAnnonceDoesNotExist() {

        when(currentUserService.getCurrentUser())
                .thenReturn(user);

        when(annonceRepository.findById(999L))
                .thenReturn(Optional.empty());

        assertThrows(
                ResourceNotFoundException.class,
                () -> annonceImageService.addImage(999L, request)
        );

        verifyNoInteractions(annonceImageMapper);
    }

    @Test
    void addImage_shouldThrow_whenUserIsNotOwner() {

        UserProfile otherUser = new UserProfile();
        otherUser.setId(99L);

        annonce.setVendeur(otherUser);

        when(currentUserService.getCurrentUser())
                .thenReturn(user);

        when(annonceRepository.findById(100L))
                .thenReturn(Optional.of(annonce));

        assertThrows(
                AccessDeniedException.class,
                () -> annonceImageService.addImage(100L, request)
        );

        verify(annonceImageRepository, never())
                .countByAnnonceId(anyLong());

        verifyNoInteractions(annonceImageMapper);
    }

    @Test
    void addImage_shouldThrow_whenAccountIsDeleted() {

        user.setDeletedAt(OffsetDateTime.now());

        when(currentUserService.getCurrentUser())
                .thenReturn(user);

        assertThrows(
                CompteSupprimeException.class,
                () -> annonceImageService.addImage(100L, request)
        );

        verifyNoInteractions(annonceRepository);
        verifyNoInteractions(annonceImageRepository);
        verifyNoInteractions(annonceImageMapper);
    }

    @Test
    void addImage_shouldThrow_whenUserIsSuspended() {

        user.setSuspendu(true);

        when(currentUserService.getCurrentUser())
                .thenReturn(user);

        assertThrows(
                UtilisateurSuspenduException.class,
                () -> annonceImageService.addImage(100L, request)
        );

        verifyNoInteractions(annonceRepository);
        verifyNoInteractions(annonceImageRepository);
        verifyNoInteractions(annonceImageMapper);
    }

    @Test
    void addImage_shouldThrow_whenAnnonceIsSold() {

        annonce.setStatut(StatutAnnonce.VENDUE);

        when(currentUserService.getCurrentUser())
                .thenReturn(user);

        when(annonceRepository.findById(100L))
                .thenReturn(Optional.of(annonce));

        assertThrows(
                AnnonceModificationInterditeException.class,
                () -> annonceImageService.addImage(100L, request)
        );

        verify(annonceImageRepository, never())
                .countByAnnonceId(anyLong());

        verifyNoInteractions(annonceImageMapper);
    }

    @Test
    void addImage_shouldThrow_whenAnnonceIsDeleted() {

        annonce.setStatut(StatutAnnonce.SUPPRIMEE);

        when(currentUserService.getCurrentUser())
                .thenReturn(user);

        when(annonceRepository.findById(100L))
                .thenReturn(Optional.of(annonce));

        assertThrows(
                AnnonceModificationInterditeException.class,
                () -> annonceImageService.addImage(100L, request)
        );

        verify(annonceImageRepository, never())
                .countByAnnonceId(anyLong());

        verifyNoInteractions(annonceImageMapper);
    }

    @Test
    void addImage_shouldThrow_whenAnnonceAlreadyHasEightImages() {

        when(currentUserService.getCurrentUser())
                .thenReturn(user);

        when(annonceRepository.findById(100L))
                .thenReturn(Optional.of(annonce));

        when(annonceImageRepository.countByAnnonceId(100L))
                .thenReturn(8L);

        assertThrows(
                AnnonceModificationInterditeException.class,
                () -> annonceImageService.addImage(100L, request)
        );

        verify(annonceImageRepository)
                .countByAnnonceId(100L);

        verify(annonceImageRepository, never())
                .existsByAnnonceIdAndOrdre(anyLong(), anyInt());

        verifyNoInteractions(annonceImageMapper);
    }

    @Test
    void addImage_shouldThrow_whenOrderAlreadyExists() {

        when(currentUserService.getCurrentUser())
                .thenReturn(user);

        when(annonceRepository.findById(100L))
                .thenReturn(Optional.of(annonce));

        when(annonceImageRepository.countByAnnonceId(100L))
                .thenReturn(2L);

        when(annonceImageRepository.existsByAnnonceIdAndOrdre(100L, 1))
                .thenReturn(true);

        assertThrows(
                AnnonceModificationInterditeException.class,
                () -> annonceImageService.addImage(100L, request)
        );

        verify(annonceImageRepository)
                .existsByAnnonceIdAndOrdre(100L, 1);

        verifyNoInteractions(annonceImageMapper);
    }

    @Test
    void addImage_shouldAllowDraftAnnonce() {

        annonce.setStatut(StatutAnnonce.BROUILLON);

        when(currentUserService.getCurrentUser())
                .thenReturn(user);

        when(annonceRepository.findById(100L))
                .thenReturn(Optional.of(annonce));

        when(annonceImageRepository.countByAnnonceId(100L))
                .thenReturn(0L);

        when(annonceImageRepository.existsByAnnonceIdAndOrdre(100L, 1))
                .thenReturn(false);

        when(annonceImageMapper.toEntity(request))
                .thenReturn(image);

        when(annonceImageRepository.save(image))
                .thenReturn(image);

        when(annonceImageMapper.toResponse(image))
                .thenReturn(response);

        AnnonceImageResponse result =
                annonceImageService.addImage(100L, request);

        assertEquals(response, result);
        verify(annonceImageRepository).save(image);
    }

    @Test
    void addImage_shouldAllowSuspendedAnnonce() {

        annonce.setStatut(StatutAnnonce.SUSPENDUE);

        when(currentUserService.getCurrentUser())
                .thenReturn(user);

        when(annonceRepository.findById(100L))
                .thenReturn(Optional.of(annonce));

        when(annonceImageRepository.countByAnnonceId(100L))
                .thenReturn(0L);

        when(annonceImageRepository.existsByAnnonceIdAndOrdre(100L, 1))
                .thenReturn(false);

        when(annonceImageMapper.toEntity(request))
                .thenReturn(image);

        when(annonceImageRepository.save(image))
                .thenReturn(image);

        when(annonceImageMapper.toResponse(image))
                .thenReturn(response);

        AnnonceImageResponse result =
                annonceImageService.addImage(100L, request);

        assertEquals(response, result);
        verify(annonceImageRepository).save(image);
    }

    @Test
    void findImages_shouldReturnImages_whenAnnonceIsPublished() {

        AnnonceImage secondImage = new AnnonceImage();
        secondImage.setId(11L);
        secondImage.setAnnonce(annonce);
        secondImage.setUrl("https://example.com/image2.jpg");
        secondImage.setOrdre(2);

        AnnonceImageResponse secondResponse =
                new AnnonceImageResponse(
                        11L,
                        "https://example.com/image2.jpg",
                        2,
                        secondImage.getCreatedAt()
                );

        when(annonceRepository.findById(100L))
                .thenReturn(Optional.of(annonce));

        when(annonceImageRepository.findByAnnonceIdOrderByOrdreAsc(100L))
                .thenReturn(List.of(image, secondImage));

        when(annonceImageMapper.toResponse(image))
                .thenReturn(response);

        when(annonceImageMapper.toResponse(secondImage))
                .thenReturn(secondResponse);

        List<AnnonceImageResponse> result =
                annonceImageService.findImages(100L);

        assertEquals(2, result.size());
        assertEquals(1, result.get(0).ordre());
        assertEquals(2, result.get(1).ordre());

        verify(annonceImageRepository)
                .findByAnnonceIdOrderByOrdreAsc(100L);
    }

    @Test
    void findImages_shouldReturnImages_whenAnnonceIsSold() {

        annonce.setStatut(StatutAnnonce.VENDUE);

        when(annonceRepository.findById(100L))
                .thenReturn(Optional.of(annonce));

        when(annonceImageRepository.findByAnnonceIdOrderByOrdreAsc(100L))
                .thenReturn(List.of(image));

        when(annonceImageMapper.toResponse(image))
                .thenReturn(response);

        List<AnnonceImageResponse> result =
                annonceImageService.findImages(100L);

        assertEquals(1, result.size());
        assertEquals(response, result.get(0));
    }

    @Test
    void findImages_shouldThrow_whenAnnonceDoesNotExist() {

        when(annonceRepository.findById(999L))
                .thenReturn(Optional.empty());

        assertThrows(
                ResourceNotFoundException.class,
                () -> annonceImageService.findImages(999L)
        );

        verify(annonceImageRepository, never())
                .findByAnnonceIdOrderByOrdreAsc(anyLong());
    }

    @Test
    void findImages_shouldThrow_whenAnnonceIsNotPublic() {

        annonce.setStatut(StatutAnnonce.BROUILLON);

        when(annonceRepository.findById(100L))
                .thenReturn(Optional.of(annonce));

        assertThrows(
                ResourceNotFoundException.class,
                () -> annonceImageService.findImages(100L)
        );

        verify(annonceImageRepository, never())
                .findByAnnonceIdOrderByOrdreAsc(anyLong());
    }

    @Test
    void findImages_shouldThrow_whenAnnonceIsSuspended() {

        annonce.setStatut(StatutAnnonce.SUSPENDUE);

        when(annonceRepository.findById(100L))
                .thenReturn(Optional.of(annonce));

        assertThrows(
                ResourceNotFoundException.class,
                () -> annonceImageService.findImages(100L)
        );

        verify(annonceImageRepository, never())
                .findByAnnonceIdOrderByOrdreAsc(anyLong());
    }

    @Test
    void findImages_shouldThrow_whenAnnonceIsDeleted() {

        annonce.setStatut(StatutAnnonce.SUPPRIMEE);

        when(annonceRepository.findById(100L))
                .thenReturn(Optional.of(annonce));

        assertThrows(
                ResourceNotFoundException.class,
                () -> annonceImageService.findImages(100L)
        );

        verify(annonceImageRepository, never())
                .findByAnnonceIdOrderByOrdreAsc(anyLong());
    }

    @Test
    void deleteImage_shouldDeleteImage_whenUserIsOwner() {

        when(currentUserService.getCurrentUser())
                .thenReturn(user);

        when(annonceRepository.findById(100L))
                .thenReturn(Optional.of(annonce));

        when(annonceImageRepository.findById(10L))
                .thenReturn(Optional.of(image));

        annonceImageService.deleteImage(100L, 10L);

        verify(annonceImageRepository).findById(10L);
        verify(annonceImageRepository).delete(image);
    }

    @Test
    void deleteImage_shouldThrow_whenImageDoesNotExist() {

        when(currentUserService.getCurrentUser())
                .thenReturn(user);

        when(annonceRepository.findById(100L))
                .thenReturn(Optional.of(annonce));

        when(annonceImageRepository.findById(999L))
                .thenReturn(Optional.empty());

        assertThrows(
                ResourceNotFoundException.class,
                () -> annonceImageService.deleteImage(100L, 999L)
        );

        verify(annonceImageRepository, never()).delete(any());
    }

    @Test
    void deleteImage_shouldThrow_whenImageBelongsToAnotherAnnonce() {

        Annonce otherAnnonce = new Annonce();
        otherAnnonce.setId(200L);

        image.setAnnonce(otherAnnonce);

        when(currentUserService.getCurrentUser())
                .thenReturn(user);

        when(annonceRepository.findById(100L))
                .thenReturn(Optional.of(annonce));

        when(annonceImageRepository.findById(10L))
                .thenReturn(Optional.of(image));

        assertThrows(
                AccessDeniedException.class,
                () -> annonceImageService.deleteImage(100L, 10L)
        );

        verify(annonceImageRepository, never()).delete(any());
    }

    @Test
    void deleteImage_shouldThrow_whenUserIsNotOwner() {

        UserProfile otherUser = new UserProfile();
        otherUser.setId(99L);

        annonce.setVendeur(otherUser);

        when(currentUserService.getCurrentUser())
                .thenReturn(user);

        when(annonceRepository.findById(100L))
                .thenReturn(Optional.of(annonce));

        assertThrows(
                AccessDeniedException.class,
                () -> annonceImageService.deleteImage(100L, 10L)
        );

        verify(annonceImageRepository, never()).findById(anyLong());
        verify(annonceImageRepository, never()).delete(any());
    }

    @Test
    void deleteImage_shouldThrow_whenAccountIsDeleted() {

        user.setDeletedAt(OffsetDateTime.now());

        when(currentUserService.getCurrentUser())
                .thenReturn(user);

        assertThrows(
                CompteSupprimeException.class,
                () -> annonceImageService.deleteImage(100L, 10L)
        );

        verifyNoInteractions(annonceRepository);
        verifyNoInteractions(annonceImageRepository);
    }

    @Test
    void deleteImage_shouldThrow_whenUserIsSuspended() {

        user.setSuspendu(true);

        when(currentUserService.getCurrentUser())
                .thenReturn(user);

        assertThrows(
                UtilisateurSuspenduException.class,
                () -> annonceImageService.deleteImage(100L, 10L)
        );

        verifyNoInteractions(annonceRepository);
        verifyNoInteractions(annonceImageRepository);
    }

    @Test
    void deleteImage_shouldThrow_whenAnnonceIsSold() {

        annonce.setStatut(StatutAnnonce.VENDUE);

        when(currentUserService.getCurrentUser())
                .thenReturn(user);

        when(annonceRepository.findById(100L))
                .thenReturn(Optional.of(annonce));

        assertThrows(
                AnnonceModificationInterditeException.class,
                () -> annonceImageService.deleteImage(100L, 10L)
        );

        verify(annonceImageRepository, never()).findById(anyLong());
        verify(annonceImageRepository, never()).delete(any());
    }

    @Test
    void deleteImage_shouldThrow_whenAnnonceIsDeleted() {

        annonce.setStatut(StatutAnnonce.SUPPRIMEE);

        when(currentUserService.getCurrentUser())
                .thenReturn(user);

        when(annonceRepository.findById(100L))
                .thenReturn(Optional.of(annonce));

        assertThrows(
                AnnonceModificationInterditeException.class,
                () -> annonceImageService.deleteImage(100L, 10L)
        );

        verify(annonceImageRepository, never()).findById(anyLong());
        verify(annonceImageRepository, never()).delete(any());
    }

    @Test
    void deleteImage_shouldAllowDraftAnnonce() {

        annonce.setStatut(StatutAnnonce.BROUILLON);

        when(currentUserService.getCurrentUser())
                .thenReturn(user);

        when(annonceRepository.findById(100L))
                .thenReturn(Optional.of(annonce));

        when(annonceImageRepository.findById(10L))
                .thenReturn(Optional.of(image));

        annonceImageService.deleteImage(100L, 10L);

        verify(annonceImageRepository).delete(image);
    }

    @Test
    void deleteImage_shouldAllowSuspendedAnnonce() {

        annonce.setStatut(StatutAnnonce.SUSPENDUE);

        when(currentUserService.getCurrentUser())
                .thenReturn(user);

        when(annonceRepository.findById(100L))
                .thenReturn(Optional.of(annonce));

        when(annonceImageRepository.findById(10L))
                .thenReturn(Optional.of(image));

        annonceImageService.deleteImage(100L, 10L);

        verify(annonceImageRepository).delete(image);
    }

    @Test
    void deleteImage_shouldThrow_whenAnnonceDoesNotExist() {

        when(currentUserService.getCurrentUser())
                .thenReturn(user);

        when(annonceRepository.findById(999L))
                .thenReturn(Optional.empty());

        assertThrows(
                ResourceNotFoundException.class,
                () -> annonceImageService.deleteImage(999L, 10L)
        );

        verify(annonceImageRepository, never()).findById(anyLong());
    }
}