package com.seydi.jend.service;

import com.seydi.jend.dto.request.CreateAnnonceRequest;
import com.seydi.jend.dto.response.AnnonceResponse;
import com.seydi.jend.dto.response.PageResponse;
import com.seydi.jend.entity.*;
import com.seydi.jend.exception.CompteSupprimeException;
import com.seydi.jend.exception.ResourceNotFoundException;
import com.seydi.jend.exception.UtilisateurSuspenduException;
import com.seydi.jend.mapper.AnnonceMapper;
import com.seydi.jend.repository.AnnonceRepository;
import com.seydi.jend.repository.CategoryRepository;
import com.seydi.jend.security.CurrentUserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AnnonceServiceTest {

    @Mock
    private AnnonceRepository annonceRepository;

    @Mock
    private CategoryRepository categoryRepository;

    @Mock
    private CurrentUserService currentUserService;

    @Mock
    private AnnonceMapper annonceMapper;

    @InjectMocks
    private AnnonceService annonceService;

    private UserProfile user;
    private Category category;
    private CreateAnnonceRequest request;

    @BeforeEach
    void setUp() {

        user = new UserProfile();
        user.setId(1L);
        user.setNom("Seydi");
        user.setEmail("seydi@example.com");
        user.setSupabaseUserId("supabase-user-123");
        user.setRole(Role.UTILISATEUR);
        user.setSuspendu(false);
        user.setDeletedAt(null);

        category = new Category();
        category.setId(10L);
        category.setNom("Téléphones");

        request = new CreateAnnonceRequest(
                "iPhone 15",
                "iPhone 15 en excellent état",
                new BigDecimal("450000"),
                EtatAnnonce.COMME_NEUF,
                "Dakar",
                "Almadies",
                10L
        );
    }

    @Test
    void create_shouldCreateAnnonceSuccessfully() {

        Annonce annonce = new Annonce();

        AnnonceResponse expectedResponse = new AnnonceResponse(
                100L,
                "iPhone 15",
                "iPhone 15 en excellent état",
                new BigDecimal("450000"),
                EtatAnnonce.COMME_NEUF,
                StatutAnnonce.BROUILLON,
                "Dakar",
                "Almadies",
                1L,
                "Seydi",
                10L,
                "Téléphones",
                OffsetDateTime.now(),
                OffsetDateTime.now()
        );

        when(currentUserService.getCurrentUser())
                .thenReturn(user);

        when(categoryRepository.findById(10L))
                .thenReturn(Optional.of(category));

        when(annonceMapper.toEntity(request))
                .thenReturn(annonce);

        when(annonceRepository.save(annonce))
                .thenReturn(annonce);

        when(annonceMapper.toResponse(annonce))
                .thenReturn(expectedResponse);

        AnnonceResponse response = annonceService.create(request);

        assertNotNull(response);
        assertEquals(100L, response.id());
        assertEquals("iPhone 15", response.titre());
        assertEquals(StatutAnnonce.BROUILLON, response.statut());

        assertSame(user, annonce.getVendeur());
        assertSame(category, annonce.getCategory());
        assertEquals(StatutAnnonce.BROUILLON, annonce.getStatut());

        verify(currentUserService).getCurrentUser();
        verify(categoryRepository).findById(10L);
        verify(annonceMapper).toEntity(request);
        verify(annonceRepository).save(annonce);
        verify(annonceMapper).toResponse(annonce);
    }

    @Test
    void create_shouldRejectSuspendedUser() {

        user.setSuspendu(true);

        when(currentUserService.getCurrentUser())
                .thenReturn(user);

        assertThrows(
                UtilisateurSuspenduException.class,
                () -> annonceService.create(request)
        );

        verifyNoInteractions(categoryRepository);
        verifyNoInteractions(annonceRepository);
        verifyNoInteractions(annonceMapper);
    }

    @Test
    void create_shouldRejectDeletedAccount() {

        user.setDeletedAt(OffsetDateTime.now());

        when(currentUserService.getCurrentUser())
                .thenReturn(user);

        assertThrows(
                CompteSupprimeException.class,
                () -> annonceService.create(request)
        );

        verifyNoInteractions(categoryRepository);
        verifyNoInteractions(annonceRepository);
        verifyNoInteractions(annonceMapper);
    }

    @Test
    void create_shouldRejectUnknownCategory() {

        when(currentUserService.getCurrentUser())
                .thenReturn(user);

        when(categoryRepository.findById(10L))
                .thenReturn(Optional.empty());

        assertThrows(
                ResourceNotFoundException.class,
                () -> annonceService.create(request)
        );

        verify(categoryRepository).findById(10L);
        verifyNoInteractions(annonceRepository);
        verifyNoInteractions(annonceMapper);
    }

    @Test
    void shouldReturnPublishedAnnonce() {

        Annonce annonce = new Annonce();
        annonce.setId(1L);
        annonce.setStatut(StatutAnnonce.PUBLIEE);

        AnnonceResponse response = new AnnonceResponse(
                1L,
                "iPhone 15",
                "Très bon état",
                new BigDecimal("350000"),
                EtatAnnonce.TRES_BON_ETAT,
                StatutAnnonce.PUBLIEE,
                "Dakar",
                "Almadies",
                10L,
                "Seydi",
                2L,
                "Téléphones",
                OffsetDateTime.now(),
                OffsetDateTime.now()
        );

        when(annonceRepository.findById(1L))
                .thenReturn(Optional.of(annonce));

        when(annonceMapper.toResponse(annonce))
                .thenReturn(response);

        AnnonceResponse result = annonceService.findById(1L);

        assertNotNull(result);
        assertEquals(1L, result.id());
        assertEquals(StatutAnnonce.PUBLIEE, result.statut());

        verify(annonceRepository).findById(1L);
        verify(annonceMapper).toResponse(annonce);
    }

    @Test
    void shouldThrowExceptionWhenAnnonceDoesNotExist() {

        when(annonceRepository.findById(99L))
                .thenReturn(Optional.empty());

        assertThrows(
                ResourceNotFoundException.class,
                () -> annonceService.findById(99L)
        );

        verify(annonceRepository).findById(99L);
        verifyNoInteractions(annonceMapper);
    }

    @Test
    void shouldNotReturnDraftAnnonce() {

        Annonce annonce = new Annonce();
        annonce.setId(1L);
        annonce.setStatut(StatutAnnonce.BROUILLON);

        when(annonceRepository.findById(1L))
                .thenReturn(Optional.of(annonce));

        assertThrows(
                ResourceNotFoundException.class,
                () -> annonceService.findById(1L)
        );

        verify(annonceRepository).findById(1L);
        verifyNoInteractions(annonceMapper);
    }

    @Test
    void shouldReturnPublishedAnnoncesByCity() {

        Annonce annonce = new Annonce();
        annonce.setId(1L);
        annonce.setStatut(StatutAnnonce.PUBLIEE);
        annonce.setVille("Dakar");

        AnnonceResponse response = new AnnonceResponse(
                1L,
                "iPhone 15",
                "Très bon état",
                new BigDecimal("350000"),
                EtatAnnonce.TRES_BON_ETAT,
                StatutAnnonce.PUBLIEE,
                "Dakar",
                "Almadies",
                10L,
                "Seydi",
                2L,
                "Téléphones",
                OffsetDateTime.now(),
                OffsetDateTime.now()
        );

        Page<Annonce> page = new PageImpl<>(
                List.of(annonce),
                PageRequest.of(0, 20),
                1
        );

        when(annonceRepository.findAll(
                any(Specification.class),
                any(Pageable.class)
        )).thenReturn(page);

        when(annonceMapper.toResponse(annonce))
                .thenReturn(response);

        PageResponse<AnnonceResponse> result =
                annonceService.findAll(null,
                        "Dakar",
                        null,
                        null,
                        null,
                        null,
                        0,
                        20);

        assertEquals(1, result.content().size());
        assertEquals("Dakar", result.content().get(0).ville());
        assertEquals(1, result.totalElements());
    }

    @Test
    void shouldReturnPublishedAnnoncesByCategory() {

        Annonce annonce = new Annonce();
        annonce.setId(1L);
        annonce.setStatut(StatutAnnonce.PUBLIEE);

        Category category = new Category();
        category.setId(2L);
        category.setNom("Téléphones");

        annonce.setCategory(category);

        Page<Annonce> page = new PageImpl<>(
                List.of(annonce),
                PageRequest.of(0, 20),
                1
        );

        AnnonceResponse response = new AnnonceResponse(
                1L,
                "iPhone 15",
                "Très bon état",
                new BigDecimal("350000"),
                EtatAnnonce.TRES_BON_ETAT,
                StatutAnnonce.PUBLIEE,
                "Dakar",
                "Almadies",
                10L,
                "Seydi",
                2L,
                "Téléphones",
                OffsetDateTime.now(),
                OffsetDateTime.now()
        );

        when(annonceRepository.findAll(
                any(Specification.class),
                any(Pageable.class)
        )).thenReturn(page);

        when(annonceMapper.toResponse(annonce))
                .thenReturn(response);

        PageResponse<AnnonceResponse> result =
                annonceService.findAll(null,
                        null,
                        2L,
                        null,
                        null,
                        null,
                        0,
                        20);

        assertEquals(1, result.content().size());
        assertEquals(2L, result.content().get(0).categoryId());
    }

    @Test
    void shouldReturnPublishedAnnoncesAboveMinimumPrice() {

        Annonce annonce = new Annonce();
        annonce.setId(1L);
        annonce.setStatut(StatutAnnonce.PUBLIEE);
        annonce.setPrix(new BigDecimal("350000"));

        Page<Annonce> page = new PageImpl<>(
                List.of(annonce),
                PageRequest.of(0, 20),
                1
        );

        AnnonceResponse response = new AnnonceResponse(
                1L,
                "iPhone 15",
                "Très bon état",
                new BigDecimal("350000"),
                EtatAnnonce.TRES_BON_ETAT,
                StatutAnnonce.PUBLIEE,
                "Dakar",
                "Almadies",
                10L,
                "Seydi",
                2L,
                "Téléphones",
                OffsetDateTime.now(),
                OffsetDateTime.now()
        );

        when(annonceRepository.findAll(
                any(Specification.class),
                any(Pageable.class)
        )).thenReturn(page);

        when(annonceMapper.toResponse(annonce))
                .thenReturn(response);

        PageResponse<AnnonceResponse> result =
                annonceService.findAll(
                        null,
                        null,
                        null,
                        new BigDecimal("100000"),
                        null,
                        null,
                        0,
                        20
                );

        assertEquals(1, result.content().size());
        assertEquals(
                new BigDecimal("350000"),
                result.content().get(0).prix()
        );
    }

    @Test
    void shouldReturnPublishedAnnoncesBelowMaximumPrice() {
        Annonce annonce = new Annonce();
        annonce.setId(1L);
        annonce.setStatut(StatutAnnonce.PUBLIEE);
        annonce.setPrix(new BigDecimal("200000"));

        Page<Annonce> page = new PageImpl<>(
                List.of(annonce),
                PageRequest.of(0, 20),
                1
        );

        AnnonceResponse response = new AnnonceResponse(
                1L,
                "Samsung",
                "Bon état",
                new BigDecimal("200000"),
                EtatAnnonce.BON_ETAT,
                StatutAnnonce.PUBLIEE,
                "Dakar",
                "Plateau",
                10L,
                "Seydi",
                2L,
                "Téléphones",
                OffsetDateTime.now(),
                OffsetDateTime.now()
        );

        when(annonceRepository.findAll(
                any(Specification.class),
                any(Pageable.class)
        )).thenReturn(page);

        when(annonceMapper.toResponse(annonce))
                .thenReturn(response);

        PageResponse<AnnonceResponse> result =
                annonceService.findAll(
                        null,
                        null,
                        null,
                        null,
                        new BigDecimal("300000"),
                        null,
                        0,
                        20
                );

        assertEquals(1, result.content().size());
        assertEquals(
                new BigDecimal("200000"),
                result.content().get(0).prix()
        );
    }

    @Test
    void shouldReturnPublishedAnnoncesByEtat() {

        Annonce annonce = new Annonce();
        annonce.setId(1L);
        annonce.setStatut(StatutAnnonce.PUBLIEE);
        annonce.setEtat(EtatAnnonce.COMME_NEUF);

        Page<Annonce> page = new PageImpl<>(
                List.of(annonce),
                PageRequest.of(0, 20),
                1
        );

        AnnonceResponse response = new AnnonceResponse(
                1L,
                "iPhone 15",
                "Excellent état",
                new BigDecimal("350000"),
                EtatAnnonce.COMME_NEUF,
                StatutAnnonce.PUBLIEE,
                "Dakar",
                "Almadies",
                10L,
                "Seydi",
                2L,
                "Téléphones",
                OffsetDateTime.now(),
                OffsetDateTime.now()
        );

        when(annonceRepository.findAll(
                any(Specification.class),
                any(Pageable.class)
        )).thenReturn(page);

        when(annonceMapper.toResponse(annonce))
                .thenReturn(response);

        PageResponse<AnnonceResponse> result =
                annonceService.findAll(
                        null,
                        null,
                        null,
                        null,
                        null,
                        EtatAnnonce.COMME_NEUF,
                        0,
                        20
                );

        assertEquals(1, result.content().size());
        assertEquals(
                EtatAnnonce.COMME_NEUF,
                result.content().get(0).etat()
        );
    }

    @Test
    void shouldReturnPublishedAnnoncesMatchingSearch() {

        Annonce annonce = new Annonce();
        annonce.setId(1L);
        annonce.setStatut(StatutAnnonce.PUBLIEE);

        Page<Annonce> page = new PageImpl<>(
                List.of(annonce),
                PageRequest.of(0, 20),
                1
        );

        AnnonceResponse response = new AnnonceResponse(
                1L,
                "iPhone 15",
                "Téléphone Apple en excellent état",
                new BigDecimal("350000"),
                EtatAnnonce.COMME_NEUF,
                StatutAnnonce.PUBLIEE,
                "Dakar",
                "Almadies",
                10L,
                "Seydi",
                2L,
                "Téléphones",
                OffsetDateTime.now(),
                OffsetDateTime.now()
        );

        when(annonceRepository.findAll(
                any(Specification.class),
                any(Pageable.class)
        )).thenReturn(page);

        when(annonceMapper.toResponse(annonce))
                .thenReturn(response);

        PageResponse<AnnonceResponse> result =
                annonceService.findAll(
                        "iphone",
                        null,
                        null,
                        null,
                        null,
                        null,
                        0,
                        20
                );

        assertEquals(1, result.content().size());
        assertEquals(
                "iPhone 15",
                result.content().get(0).titre()
        );
    }

    @Test
    void shouldRejectInvalidPriceRange() {

        assertThrows(
                IllegalArgumentException.class,
                () -> annonceService.findAll(
                        null,
                        null,
                        null,
                        new BigDecimal("500000"),
                        new BigDecimal("100000"),
                        null,
                        0,
                        20
                )
        );

        verifyNoInteractions(annonceRepository);
    }

    @Test
    void shouldRejectInvalidPageSize() {

        assertThrows(
                IllegalArgumentException.class,
                () -> annonceService.findAll(
                        null,
                        null,
                        null,
                        null,
                        null,
                        null,
                        0,
                        51
                )
        );

        verifyNoInteractions(annonceRepository);
    }



}