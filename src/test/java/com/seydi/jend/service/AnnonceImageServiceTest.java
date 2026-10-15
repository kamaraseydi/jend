
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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.multipart.MultipartFile;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AnnonceImageServiceTest {

    private static final Long ANNONCE_ID = 100L;
    private static final Long IMAGE_ID = 10L;
    private static final String STORAGE_PATH =
            "annonces/100/123456.jpg";
    private static final String SIGNED_URL =
            "https://example.supabase.co/signed/test.jpg?token=test";

    @Mock
    private AnnonceImageRepository annonceImageRepository;

    @Mock
    private AnnonceRepository annonceRepository;

    @Mock
    private CurrentUserService currentUserService;

    @Mock
    private SupabaseStorageService storageService;

    @InjectMocks
    private AnnonceImageService annonceImageService;

    private UserProfile user;
    private Annonce annonce;
    private AnnonceImage image;
    private MultipartFile file;

    @BeforeEach
    void setUp() {
        user = new UserProfile();
        user.setId(1L);
        user.setNom("Seydi");
        user.setDeletedAt(null);
        user.setSuspendu(false);

        annonce = new Annonce();
        annonce.setId(ANNONCE_ID);
        annonce.setVendeur(user);
        annonce.setStatut(StatutAnnonce.PUBLIEE);

        image = new AnnonceImage();
        image.setId(IMAGE_ID);
        image.setAnnonce(annonce);
        image.setUrl(STORAGE_PATH);
        image.setOrdre(1);
        image.setCreatedAt(OffsetDateTime.now());

        file = validImage();
    }

    private MultipartFile validImage() {
        return new MockMultipartFile(
                "file",
                "image.jpg",
                "image/jpeg",
                new byte[]{1, 2, 3}
        );
    }

    private void givenOwnerAndAnnonce() {
        when(currentUserService.getCurrentUser()).thenReturn(user);
        when(annonceRepository.findById(ANNONCE_ID))
                .thenReturn(Optional.of(annonce));
    }

    private void givenAvailableOrder(Integer ordre) {
        when(annonceImageRepository.countByAnnonceId(ANNONCE_ID))
                .thenReturn(0L);
        when(annonceImageRepository
                .existsByAnnonceIdAndOrdre(ANNONCE_ID, ordre))
                .thenReturn(false);
    }

    private void givenSuccessfulUpload() {
        when(annonceImageRepository.saveAndFlush(any(AnnonceImage.class)))
                .thenAnswer(invocation -> {
                    AnnonceImage saved = invocation.getArgument(0);
                    saved.setId(IMAGE_ID);
                    return saved;
                });

        when(storageService.createSignedUrl(anyString()))
                .thenReturn(SIGNED_URL);
    }

    @Test
    void addImage_shouldUploadAndSave_whenUserIsOwner() {
        givenOwnerAndAnnonce();
        givenAvailableOrder(1);
        givenSuccessfulUpload();

        AnnonceImageResponse result =
                annonceImageService.addImage(ANNONCE_ID, file, 1);

        assertEquals(IMAGE_ID, result.id());
        assertEquals(SIGNED_URL, result.url());
        assertEquals(1, result.ordre());

        verify(storageService).upload(
                startsWith("annonces/100/"),
                any(byte[].class),
                eq("image/jpeg")
        );
        verify(annonceImageRepository).saveAndFlush(any(AnnonceImage.class));
        verify(storageService).createSignedUrl(anyString());
    }

    @Test
    void addImage_shouldThrow_whenAnnonceDoesNotExist() {
        when(currentUserService.getCurrentUser()).thenReturn(user);
        when(annonceRepository.findById(999L))
                .thenReturn(Optional.empty());

        assertThrows(
                ResourceNotFoundException.class,
                () -> annonceImageService.addImage(999L, file, 1)
        );

        verifyNoInteractions(storageService);
    }

    @Test
    void addImage_shouldThrow_whenUserIsNotOwner() {
        UserProfile otherUser = new UserProfile();
        otherUser.setId(99L);
        annonce.setVendeur(otherUser);

        givenOwnerAndAnnonce();

        assertThrows(
                AccessDeniedException.class,
                () -> annonceImageService.addImage(ANNONCE_ID, file, 1)
        );

        verifyNoInteractions(storageService);
        verify(annonceImageRepository, never()).countByAnnonceId(anyLong());
    }

    @Test
    void addImage_shouldThrow_whenAccountIsDeleted() {
        user.setDeletedAt(OffsetDateTime.now());
        when(currentUserService.getCurrentUser()).thenReturn(user);

        assertThrows(
                CompteSupprimeException.class,
                () -> annonceImageService.addImage(ANNONCE_ID, file, 1)
        );

        verifyNoInteractions(annonceRepository, annonceImageRepository,
                storageService);
    }

    @Test
    void addImage_shouldThrow_whenUserIsSuspended() {
        user.setSuspendu(true);
        when(currentUserService.getCurrentUser()).thenReturn(user);

        assertThrows(
                UtilisateurSuspenduException.class,
                () -> annonceImageService.addImage(ANNONCE_ID, file, 1)
        );

        verifyNoInteractions(annonceRepository, annonceImageRepository,
                storageService);
    }

    @Test
    void addImage_shouldThrow_whenAnnonceIsSold() {
        annonce.setStatut(StatutAnnonce.VENDUE);
        givenOwnerAndAnnonce();

        assertThrows(
                AnnonceModificationInterditeException.class,
                () -> annonceImageService.addImage(ANNONCE_ID, file, 1)
        );

        verifyNoInteractions(storageService);
    }

    @Test
    void addImage_shouldThrow_whenAnnonceIsDeleted() {
        annonce.setStatut(StatutAnnonce.SUPPRIMEE);
        givenOwnerAndAnnonce();

        assertThrows(
                AnnonceModificationInterditeException.class,
                () -> annonceImageService.addImage(ANNONCE_ID, file, 1)
        );

        verifyNoInteractions(storageService);
    }

    @Test
    void addImage_shouldThrow_whenAnnonceAlreadyHasEightImages() {
        givenOwnerAndAnnonce();
        when(annonceImageRepository.countByAnnonceId(ANNONCE_ID))
                .thenReturn(8L);

        assertThrows(
                AnnonceModificationInterditeException.class,
                () -> annonceImageService.addImage(ANNONCE_ID, file, 1)
        );

        verify(annonceImageRepository, never())
                .existsByAnnonceIdAndOrdre(anyLong(), anyInt());
        verifyNoInteractions(storageService);
    }

    @Test
    void addImage_shouldThrow_whenOrderAlreadyExists() {
        givenOwnerAndAnnonce();
        when(annonceImageRepository.countByAnnonceId(ANNONCE_ID))
                .thenReturn(2L);
        when(annonceImageRepository
                .existsByAnnonceIdAndOrdre(ANNONCE_ID, 1))
                .thenReturn(true);

        assertThrows(
                AnnonceModificationInterditeException.class,
                () -> annonceImageService.addImage(ANNONCE_ID, file, 1)
        );

        verifyNoInteractions(storageService);
    }

    @Test
    void addImage_shouldAllowDraftAnnonce() {
        annonce.setStatut(StatutAnnonce.BROUILLON);
        givenOwnerAndAnnonce();
        givenAvailableOrder(1);
        givenSuccessfulUpload();

        AnnonceImageResponse result =
                annonceImageService.addImage(ANNONCE_ID, file, 1);

        assertEquals(SIGNED_URL, result.url());
        verify(storageService).upload(
                anyString(), any(byte[].class), eq("image/jpeg"));
    }

    @Test
    void addImage_shouldAllowSuspendedAnnonce() {
        annonce.setStatut(StatutAnnonce.SUSPENDUE);
        givenOwnerAndAnnonce();
        givenAvailableOrder(1);
        givenSuccessfulUpload();

        assertDoesNotThrow(
                () -> annonceImageService.addImage(ANNONCE_ID, file, 1)
        );
    }

    @Test
    void addImage_shouldRejectEmptyFile() {
        givenOwnerAndAnnonce();

        MultipartFile emptyFile = new MockMultipartFile(
                "file", "empty.jpg", "image/jpeg", new byte[0]);

        assertThrows(
                IllegalArgumentException.class,
                () -> annonceImageService.addImage(ANNONCE_ID, emptyFile, 1)
        );

        verifyNoInteractions(storageService);
    }

    @Test
    void addImage_shouldRejectUnsupportedMimeType() {
        givenOwnerAndAnnonce();

        MultipartFile invalidFile = new MockMultipartFile(
                "file", "script.txt", "text/plain", new byte[]{1, 2, 3});

        assertThrows(
                IllegalArgumentException.class,
                () -> annonceImageService.addImage(ANNONCE_ID, invalidFile, 1)
        );

        verifyNoInteractions(storageService);
    }

    @Test
    void addImage_shouldRejectOrderOutsideRange() {
        givenOwnerAndAnnonce();

        assertThrows(
                IllegalArgumentException.class,
                () -> annonceImageService.addImage(ANNONCE_ID, file, 9)
        );

        verifyNoInteractions(storageService);
    }

    @Test
    void findImages_shouldReturnSignedUrls_whenAnnonceIsPublished() {
        when(annonceRepository.findById(ANNONCE_ID))
                .thenReturn(Optional.of(annonce));
        when(annonceImageRepository.findByAnnonceIdOrderByOrdreAsc(ANNONCE_ID))
                .thenReturn(List.of(image));
        when(storageService.createSignedUrl(STORAGE_PATH))
                .thenReturn(SIGNED_URL);

        List<AnnonceImageResponse> result =
                annonceImageService.findImages(ANNONCE_ID);

        assertEquals(1, result.size());
        assertEquals(SIGNED_URL, result.get(0).url());
        assertEquals(1, result.get(0).ordre());
    }

    @Test
    void findImages_shouldReturnImages_whenAnnonceIsSold() {
        annonce.setStatut(StatutAnnonce.VENDUE);
        when(annonceRepository.findById(ANNONCE_ID))
                .thenReturn(Optional.of(annonce));
        when(annonceImageRepository.findByAnnonceIdOrderByOrdreAsc(ANNONCE_ID))
                .thenReturn(List.of(image));
        when(storageService.createSignedUrl(STORAGE_PATH))
                .thenReturn(SIGNED_URL);

        assertEquals(1, annonceImageService.findImages(ANNONCE_ID).size());
    }

    @Test
    void findImages_shouldThrow_whenAnnonceDoesNotExist() {
        when(annonceRepository.findById(999L))
                .thenReturn(Optional.empty());

        assertThrows(
                ResourceNotFoundException.class,
                () -> annonceImageService.findImages(999L)
        );

        verifyNoInteractions(storageService);
    }

    @Test
    void findImages_shouldThrow_whenAnnonceIsNotPublic() {
        annonce.setStatut(StatutAnnonce.BROUILLON);
        when(annonceRepository.findById(ANNONCE_ID))
                .thenReturn(Optional.of(annonce));

        assertThrows(
                ResourceNotFoundException.class,
                () -> annonceImageService.findImages(ANNONCE_ID)
        );

        verifyNoInteractions(storageService);
    }

    @Test
    void findImages_shouldThrow_whenAnnonceIsSuspended() {
        annonce.setStatut(StatutAnnonce.SUSPENDUE);
        when(annonceRepository.findById(ANNONCE_ID))
                .thenReturn(Optional.of(annonce));

        assertThrows(
                ResourceNotFoundException.class,
                () -> annonceImageService.findImages(ANNONCE_ID)
        );

        verifyNoInteractions(storageService);
    }

    @Test
    void findImages_shouldThrow_whenAnnonceIsDeleted() {
        annonce.setStatut(StatutAnnonce.SUPPRIMEE);
        when(annonceRepository.findById(ANNONCE_ID))
                .thenReturn(Optional.of(annonce));

        assertThrows(
                ResourceNotFoundException.class,
                () -> annonceImageService.findImages(ANNONCE_ID)
        );

        verifyNoInteractions(storageService);
    }

    @Test
    void deleteImage_shouldDeleteStorageAndDatabase_whenUserIsOwner() {
        givenOwnerAndAnnonce();
        when(annonceImageRepository.findById(IMAGE_ID))
                .thenReturn(Optional.of(image));

        annonceImageService.deleteImage(ANNONCE_ID, IMAGE_ID);

        verify(storageService).delete(STORAGE_PATH);
        verify(annonceImageRepository).delete(image);
    }

    @Test
    void deleteImage_shouldThrow_whenImageDoesNotExist() {
        givenOwnerAndAnnonce();
        when(annonceImageRepository.findById(999L))
                .thenReturn(Optional.empty());

        assertThrows(
                ResourceNotFoundException.class,
                () -> annonceImageService.deleteImage(ANNONCE_ID, 999L)
        );

        verifyNoInteractions(storageService);
    }

    @Test
    void deleteImage_shouldThrow_whenImageBelongsToAnotherAnnonce() {
        Annonce otherAnnonce = new Annonce();
        otherAnnonce.setId(200L);
        image.setAnnonce(otherAnnonce);

        givenOwnerAndAnnonce();
        when(annonceImageRepository.findById(IMAGE_ID))
                .thenReturn(Optional.of(image));

        assertThrows(
                AccessDeniedException.class,
                () -> annonceImageService.deleteImage(ANNONCE_ID, IMAGE_ID)
        );

        verifyNoInteractions(storageService);
        verify(annonceImageRepository, never()).delete(any());
    }

    @Test
    void deleteImage_shouldThrow_whenUserIsNotOwner() {
        UserProfile otherUser = new UserProfile();
        otherUser.setId(99L);
        annonce.setVendeur(otherUser);
        givenOwnerAndAnnonce();

        assertThrows(
                AccessDeniedException.class,
                () -> annonceImageService.deleteImage(ANNONCE_ID, IMAGE_ID)
        );

        verifyNoInteractions(storageService);
        verify(annonceImageRepository, never()).findById(anyLong());
    }

    @Test
    void deleteImage_shouldThrow_whenAccountIsDeleted() {
        user.setDeletedAt(OffsetDateTime.now());
        when(currentUserService.getCurrentUser()).thenReturn(user);

        assertThrows(
                CompteSupprimeException.class,
                () -> annonceImageService.deleteImage(ANNONCE_ID, IMAGE_ID)
        );

        verifyNoInteractions(annonceRepository, annonceImageRepository,
                storageService);
    }

    @Test
    void deleteImage_shouldThrow_whenUserIsSuspended() {
        user.setSuspendu(true);
        when(currentUserService.getCurrentUser()).thenReturn(user);

        assertThrows(
                UtilisateurSuspenduException.class,
                () -> annonceImageService.deleteImage(ANNONCE_ID, IMAGE_ID)
        );

        verifyNoInteractions(annonceRepository, annonceImageRepository,
                storageService);
    }

    @Test
    void deleteImage_shouldThrow_whenAnnonceIsSold() {
        annonce.setStatut(StatutAnnonce.VENDUE);
        givenOwnerAndAnnonce();

        assertThrows(
                AnnonceModificationInterditeException.class,
                () -> annonceImageService.deleteImage(ANNONCE_ID, IMAGE_ID)
        );

        verifyNoInteractions(storageService);
        verify(annonceImageRepository, never()).findById(anyLong());
    }

    @Test
    void deleteImage_shouldThrow_whenAnnonceIsDeleted() {
        annonce.setStatut(StatutAnnonce.SUPPRIMEE);
        givenOwnerAndAnnonce();

        assertThrows(
                AnnonceModificationInterditeException.class,
                () -> annonceImageService.deleteImage(ANNONCE_ID, IMAGE_ID)
        );

        verifyNoInteractions(storageService);
        verify(annonceImageRepository, never()).findById(anyLong());
    }

    @Test
    void deleteImage_shouldAllowDraftAnnonce() {
        annonce.setStatut(StatutAnnonce.BROUILLON);
        givenOwnerAndAnnonce();
        when(annonceImageRepository.findById(IMAGE_ID))
                .thenReturn(Optional.of(image));

        assertDoesNotThrow(
                () -> annonceImageService.deleteImage(ANNONCE_ID, IMAGE_ID)
        );

        verify(storageService).delete(STORAGE_PATH);
        verify(annonceImageRepository).delete(image);
    }

    @Test
    void deleteImage_shouldAllowSuspendedAnnonce() {
        annonce.setStatut(StatutAnnonce.SUSPENDUE);
        givenOwnerAndAnnonce();
        when(annonceImageRepository.findById(IMAGE_ID))
                .thenReturn(Optional.of(image));

        assertDoesNotThrow(
                () -> annonceImageService.deleteImage(ANNONCE_ID, IMAGE_ID)
        );

        verify(storageService).delete(STORAGE_PATH);
    }

    @Test
    void deleteImage_shouldThrow_whenAnnonceDoesNotExist() {
        when(currentUserService.getCurrentUser()).thenReturn(user);
        when(annonceRepository.findById(999L))
                .thenReturn(Optional.empty());

        assertThrows(
                ResourceNotFoundException.class,
                () -> annonceImageService.deleteImage(999L, IMAGE_ID)
        );

        verifyNoInteractions(storageService);
    }
}
