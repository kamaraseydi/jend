package com.seydi.jend.service;

import com.seydi.jend.dto.request.CreateAnnonceRequest;
import com.seydi.jend.dto.request.UpdateAnnonceRequest;
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
import org.springframework.security.access.AccessDeniedException;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyList;
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

    @Mock
    private FavoriteRepository favoriteRepository;

    @Mock
    private AnnonceImageRepository annonceImageRepository;

    @Mock
    private AnnonceImageMapper annonceImageMapper;

    @InjectMocks
    private AnnonceService annonceService;

    private final TriAnnonce sort = TriAnnonce.RECENT;

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
                List.of(),
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

        when(annonceImageRepository.findByAnnonceIdOrderByOrdreAsc(annonce.getId()))


                .thenReturn(List.of());



        when(annonceMapper.toResponse(eq(annonce), anyList()))
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
        verify(annonceMapper).toResponse(eq(annonce), anyList());
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
                List.of(),
                OffsetDateTime.now(),
                OffsetDateTime.now()
        );

        when(annonceRepository.findById(1L))
                .thenReturn(Optional.of(annonce));

        when(annonceImageRepository.findByAnnonceIdOrderByOrdreAsc(annonce.getId()))


                .thenReturn(List.of());



        when(annonceMapper.toResponse(eq(annonce), anyList()))
                .thenReturn(response);

        AnnonceResponse result = annonceService.findById(1L);

        assertNotNull(result);
        assertEquals(1L, result.id());
        assertEquals(StatutAnnonce.PUBLIEE, result.statut());

        verify(annonceRepository).findById(1L);
        verify(annonceMapper).toResponse(eq(annonce), anyList());
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
                List.of(),
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

        when(annonceImageRepository.findByAnnonceIdOrderByOrdreAsc(annonce.getId()))


                .thenReturn(List.of());



        when(annonceMapper.toResponse(eq(annonce), anyList()))
                .thenReturn(response);

        PageResponse<AnnonceResponse> result =
                annonceService.findAll(null,
                        "Dakar",
                        null,
                        null,
                        null,
                        null,
                        sort, 0,
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
                List.of(),
                OffsetDateTime.now(),
                OffsetDateTime.now()
        );

        when(annonceRepository.findAll(
                any(Specification.class),
                any(Pageable.class)
        )).thenReturn(page);

        when(annonceImageRepository.findByAnnonceIdOrderByOrdreAsc(annonce.getId()))


                .thenReturn(List.of());



        when(annonceMapper.toResponse(eq(annonce), anyList()))
                .thenReturn(response);

        PageResponse<AnnonceResponse> result =
                annonceService.findAll(null,
                        null,
                        2L,
                        null,
                        null,
                        null,
                        sort, 0,
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
                List.of(),
                OffsetDateTime.now(),
                OffsetDateTime.now()
        );

        when(annonceRepository.findAll(
                any(Specification.class),
                any(Pageable.class)
        )).thenReturn(page);

        when(annonceImageRepository.findByAnnonceIdOrderByOrdreAsc(annonce.getId()))


                .thenReturn(List.of());



        when(annonceMapper.toResponse(eq(annonce), anyList()))
                .thenReturn(response);

        PageResponse<AnnonceResponse> result =
                annonceService.findAll(
                        null,
                        null,
                        null,
                        new BigDecimal("100000"),
                        null,
                        null,
                        sort, 0,
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
                List.of(),
                OffsetDateTime.now(),
                OffsetDateTime.now()
        );

        when(annonceRepository.findAll(
                any(Specification.class),
                any(Pageable.class)
        )).thenReturn(page);

        when(annonceImageRepository.findByAnnonceIdOrderByOrdreAsc(annonce.getId()))


                .thenReturn(List.of());



        when(annonceMapper.toResponse(eq(annonce), anyList()))
                .thenReturn(response);

        PageResponse<AnnonceResponse> result =
                annonceService.findAll(
                        null,
                        null,
                        null,
                        null,
                        new BigDecimal("300000"),
                        null,
                        sort, 0,
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
                List.of(),
                OffsetDateTime.now(),
                OffsetDateTime.now()
        );

        when(annonceRepository.findAll(
                any(Specification.class),
                any(Pageable.class)
        )).thenReturn(page);

        when(annonceImageRepository.findByAnnonceIdOrderByOrdreAsc(annonce.getId()))


                .thenReturn(List.of());



        when(annonceMapper.toResponse(eq(annonce), anyList()))
                .thenReturn(response);

        PageResponse<AnnonceResponse> result =
                annonceService.findAll(
                        null,
                        null,
                        null,
                        null,
                        null,
                        EtatAnnonce.COMME_NEUF,
                        sort, 0,
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
                List.of(),
                OffsetDateTime.now(),
                OffsetDateTime.now()
        );

        when(annonceRepository.findAll(
                any(Specification.class),
                any(Pageable.class)
        )).thenReturn(page);

        when(annonceImageRepository.findByAnnonceIdOrderByOrdreAsc(annonce.getId()))


                .thenReturn(List.of());



        when(annonceMapper.toResponse(eq(annonce), anyList()))
                .thenReturn(response);

        PageResponse<AnnonceResponse> result =
                annonceService.findAll(
                        "iphone",
                        null,
                        null,
                        null,
                        null,
                        null,
                        sort, 0,
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
                        sort, 0,
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
                        sort, 0,
                        51
                )
        );

        verifyNoInteractions(annonceRepository);
    }

    @Test
    void shouldReturnCurrentUserAnnonces() {

        UserProfile currentUser = new UserProfile();
        currentUser.setId(1L);

        Annonce annonce = new Annonce();
        annonce.setId(100L);
        annonce.setVendeur(currentUser);
        annonce.setStatut(StatutAnnonce.BROUILLON);

        Page<Annonce> page = new PageImpl<>(
                List.of(annonce),
                PageRequest.of(0, 20),
                1
        );

        AnnonceResponse response = new AnnonceResponse(
                100L,
                "iPhone 15",
                "Excellent état",
                new BigDecimal("350000"),
                EtatAnnonce.COMME_NEUF,
                StatutAnnonce.BROUILLON,
                "Dakar",
                "Almadies",
                1L,
                "Seydi",
                2L,
                "Téléphones",
                List.of(),
                OffsetDateTime.now(),
                OffsetDateTime.now()
        );

        when(currentUserService.getCurrentUser())
                .thenReturn(currentUser);

        when(annonceRepository.findAll(
                any(Specification.class),
                any(Pageable.class)
        )).thenReturn(page);

        when(annonceImageRepository.findByAnnonceIdOrderByOrdreAsc(annonce.getId()))


                .thenReturn(List.of());



        when(annonceMapper.toResponse(eq(annonce), anyList()))
                .thenReturn(response);

        PageResponse<AnnonceResponse> result =
                annonceService.findMyAnnonces(null,0, 20);

        assertEquals(1, result.content().size());
        assertEquals(100L, result.content().get(0).id());
        assertEquals(
                StatutAnnonce.BROUILLON,
                result.content().get(0).statut()
        );

        verify(currentUserService).getCurrentUser();

        verify(annonceRepository).findAll(
                any(Specification.class),
                any(Pageable.class)
        );
    }

    @Test
    void shouldRejectInvalidPageSizeForMyAnnonces() {

        assertThrows(
                IllegalArgumentException.class,
                () -> annonceService.findMyAnnonces(null,0, 51)
        );

        verifyNoInteractions(currentUserService);
        verifyNoInteractions(annonceRepository);
    }

    @Test
    void shouldReturnCurrentUserAnnoncesByStatus() {

        UserProfile currentUser = new UserProfile();
        currentUser.setId(1L);

        Annonce annonce = new Annonce();
        annonce.setId(100L);
        annonce.setVendeur(currentUser);
        annonce.setStatut(StatutAnnonce.BROUILLON);

        Page<Annonce> page = new PageImpl<>(
                List.of(annonce),
                PageRequest.of(0, 20),
                1
        );

        AnnonceResponse response = new AnnonceResponse(
                100L,
                "iPhone 15",
                "Excellent état",
                new BigDecimal("350000"),
                EtatAnnonce.COMME_NEUF,
                StatutAnnonce.BROUILLON,
                "Dakar",
                "Almadies",
                1L,
                "Seydi",
                2L,
                "Téléphones",
                List.of(),
                OffsetDateTime.now(),
                OffsetDateTime.now()
        );

        when(currentUserService.getCurrentUser())
                .thenReturn(currentUser);

        when(annonceRepository.findAll(
                any(Specification.class),
                any(Pageable.class)
        )).thenReturn(page);

        when(annonceImageRepository.findByAnnonceIdOrderByOrdreAsc(annonce.getId()))


                .thenReturn(List.of());



        when(annonceMapper.toResponse(eq(annonce), anyList()))
                .thenReturn(response);

        PageResponse<AnnonceResponse> result =
                annonceService.findMyAnnonces(
                        StatutAnnonce.BROUILLON,
                        0,
                        20
                );

        assertEquals(1, result.content().size());

        assertEquals(
                StatutAnnonce.BROUILLON,
                result.content().get(0).statut()
        );

        verify(currentUserService).getCurrentUser();

        verify(annonceRepository).findAll(
                any(Specification.class),
                any(Pageable.class)
        );
    }

    @Test
    void update_shouldUpdateAnnonce_whenUserIsOwner() {

        Annonce annonce = new Annonce();

        annonce.setId(100L);
        annonce.setTitre("Ancien titre");
        annonce.setDescription("Ancienne description");
        annonce.setPrix(new BigDecimal("400000"));
        annonce.setEtat(EtatAnnonce.BON_ETAT);
        annonce.setStatut(StatutAnnonce.PUBLIEE);
        annonce.setVille("Dakar");
        annonce.setQuartier("Almadies");
        annonce.setVendeur(user);
        annonce.setCategory(category);

        UpdateAnnonceRequest request = new UpdateAnnonceRequest(
                "iPhone 15 Pro",
                "Excellent état",
                new BigDecimal("500000"),
                EtatAnnonce.COMME_NEUF,
                "Dakar",
                "Plateau",
                10L
        );

        AnnonceResponse response = new AnnonceResponse(
                100L,
                "iPhone 15 Pro",
                "Excellent état",
                new BigDecimal("500000"),
                EtatAnnonce.COMME_NEUF,
                StatutAnnonce.PUBLIEE,
                "Dakar",
                "Plateau",
                1L,
                "Seydi",
                10L,
                "Téléphones",
                List.of(),
                OffsetDateTime.now(),
                OffsetDateTime.now()
        );

        when(currentUserService.getCurrentUser()).thenReturn(user);
        when(annonceRepository.findById(100L))
                .thenReturn(Optional.of(annonce));
        when(categoryRepository.findById(10L))
                .thenReturn(Optional.of(category));

        doAnswer(invocation -> {
            Annonce a = invocation.getArgument(0);

            a.setTitre(request.titre());
            a.setDescription(request.description());
            a.setPrix(request.prix());
            a.setEtat(request.etat());
            a.setVille(request.ville());
            a.setQuartier(request.quartier());

            return null;
        }).when(annonceMapper).updateEntity(annonce, request);

        when(annonceRepository.save(annonce))
                .thenReturn(annonce);

        when(annonceImageRepository.findByAnnonceIdOrderByOrdreAsc(annonce.getId()))


                .thenReturn(List.of());



        when(annonceMapper.toResponse(eq(annonce), anyList()))
                .thenReturn(response);

        AnnonceResponse result =
                annonceService.update(100L, request);

        assertEquals(response, result);

        assertEquals("iPhone 15 Pro", annonce.getTitre());
        assertEquals("Excellent état", annonce.getDescription());
        assertEquals(new BigDecimal("500000"), annonce.getPrix());
        assertEquals(EtatAnnonce.COMME_NEUF, annonce.getEtat());
        assertEquals("Dakar", annonce.getVille());
        assertEquals("Plateau", annonce.getQuartier());

        // Le statut ne doit surtout pas changer
        assertEquals(StatutAnnonce.PUBLIEE, annonce.getStatut());

        verify(annonceRepository).findById(100L);
        verify(categoryRepository).findById(10L);
        verify(annonceMapper).updateEntity(annonce, request);
        verify(annonceRepository).save(annonce);
        verify(annonceMapper).toResponse(eq(annonce), anyList());
    }

    @Test
    void update_shouldThrowException_whenAnnonceDoesNotExist() {

        UpdateAnnonceRequest request = new UpdateAnnonceRequest(
                "iPhone 15 Pro",
                "Excellent état",
                new BigDecimal("500000"),
                EtatAnnonce.COMME_NEUF,
                "Dakar",
                "Plateau",
                10L
        );

        when(currentUserService.getCurrentUser()).thenReturn(user);

        when(annonceRepository.findById(999L))
                .thenReturn(Optional.empty());

        assertThrows(
                ResourceNotFoundException.class,
                () -> annonceService.update(999L, request)
        );

        verify(annonceRepository).findById(999L);

        verify(categoryRepository, never()).findById(anyLong());
        verify(annonceRepository, never()).save(any());
        verify(annonceMapper, never()).updateEntity(any(), any());
    }

    @Test
    void update_shouldThrowAccessDenied_whenUserIsNotOwner() {

        UserProfile otherUser = new UserProfile();
        otherUser.setId(99L);

        Annonce annonce = new Annonce();
        annonce.setId(100L);
        annonce.setVendeur(otherUser);
        annonce.setStatut(StatutAnnonce.PUBLIEE);

        UpdateAnnonceRequest request = new UpdateAnnonceRequest(
                "iPhone 15 Pro",
                "Excellent état",
                new BigDecimal("500000"),
                EtatAnnonce.COMME_NEUF,
                "Dakar",
                "Plateau",
                10L
        );

        when(currentUserService.getCurrentUser()).thenReturn(user);

        when(annonceRepository.findById(100L))
                .thenReturn(Optional.of(annonce));

        assertThrows(
                AccessDeniedException.class,
                () -> annonceService.update(100L, request)
        );

        verify(categoryRepository, never()).findById(anyLong());
        verify(annonceRepository, never()).save(any());
        verify(annonceMapper, never()).updateEntity(any(), any());
    }

    @Test
    void update_shouldThrowException_whenUserIsSuspended() {

        user.setSuspendu(true);

        Annonce annonce = new Annonce();
        annonce.setId(100L);
        annonce.setVendeur(user);
        annonce.setStatut(StatutAnnonce.PUBLIEE);

        UpdateAnnonceRequest request = new UpdateAnnonceRequest(
                "iPhone 15 Pro",
                "Excellent état",
                new BigDecimal("500000"),
                EtatAnnonce.COMME_NEUF,
                "Dakar",
                "Plateau",
                10L
        );

        when(currentUserService.getCurrentUser()).thenReturn(user);

        when(annonceRepository.findById(100L))
                .thenReturn(Optional.of(annonce));

        assertThrows(
                UtilisateurSuspenduException.class,
                () -> annonceService.update(100L, request)
        );

        verify(categoryRepository, never()).findById(anyLong());
        verify(annonceRepository, never()).save(any());
    }

    @Test
    void update_shouldThrowException_whenUserAccountIsDeleted() {

        user.setDeletedAt(OffsetDateTime.now());

        Annonce annonce = new Annonce();
        annonce.setId(100L);
        annonce.setVendeur(user);
        annonce.setStatut(StatutAnnonce.PUBLIEE);

        UpdateAnnonceRequest request = new UpdateAnnonceRequest(
                "iPhone 15 Pro",
                "Excellent état",
                new BigDecimal("500000"),
                EtatAnnonce.COMME_NEUF,
                "Dakar",
                "Plateau",
                10L
        );

        when(currentUserService.getCurrentUser()).thenReturn(user);

        when(annonceRepository.findById(100L))
                .thenReturn(Optional.of(annonce));

        assertThrows(
                CompteSupprimeException.class,
                () -> annonceService.update(100L, request)
        );

        verify(categoryRepository, never()).findById(anyLong());
        verify(annonceRepository, never()).save(any());
    }

    @Test
    void update_shouldThrowException_whenAnnonceIsSold() {

        Annonce annonce = new Annonce();
        annonce.setId(100L);
        annonce.setVendeur(user);
        annonce.setStatut(StatutAnnonce.VENDUE);

        UpdateAnnonceRequest request = new UpdateAnnonceRequest(
                "iPhone 15 Pro",
                "Excellent état",
                new BigDecimal("500000"),
                EtatAnnonce.COMME_NEUF,
                "Dakar",
                "Plateau",
                10L
        );

        when(currentUserService.getCurrentUser()).thenReturn(user);

        when(annonceRepository.findById(100L))
                .thenReturn(Optional.of(annonce));

        assertThrows(
                AnnonceModificationInterditeException.class,
                () -> annonceService.update(100L, request)
        );

        verify(categoryRepository, never()).findById(anyLong());
        verify(annonceRepository, never()).save(any());
        verify(annonceMapper, never()).updateEntity(any(), any());
    }

    @Test
    void update_shouldThrowException_whenAnnonceIsDeleted() {

        Annonce annonce = new Annonce();
        annonce.setId(100L);
        annonce.setVendeur(user);
        annonce.setStatut(StatutAnnonce.SUPPRIMEE);

        UpdateAnnonceRequest request = new UpdateAnnonceRequest(
                "iPhone 15 Pro",
                "Excellent état",
                new BigDecimal("500000"),
                EtatAnnonce.COMME_NEUF,
                "Dakar",
                "Plateau",
                10L
        );

        when(currentUserService.getCurrentUser()).thenReturn(user);

        when(annonceRepository.findById(100L))
                .thenReturn(Optional.of(annonce));

        assertThrows(
                AnnonceModificationInterditeException.class,
                () -> annonceService.update(100L, request)
        );

        verify(categoryRepository, never()).findById(anyLong());
        verify(annonceRepository, never()).save(any());
    }

    @Test
    void update_shouldThrowException_whenCategoryDoesNotExist() {

        Annonce annonce = new Annonce();
        annonce.setId(100L);
        annonce.setVendeur(user);
        annonce.setStatut(StatutAnnonce.PUBLIEE);

        UpdateAnnonceRequest request = new UpdateAnnonceRequest(
                "iPhone 15 Pro",
                "Excellent état",
                new BigDecimal("500000"),
                EtatAnnonce.COMME_NEUF,
                "Dakar",
                "Plateau",
                999L
        );

        when(currentUserService.getCurrentUser()).thenReturn(user);

        when(annonceRepository.findById(100L))
                .thenReturn(Optional.of(annonce));

        when(categoryRepository.findById(999L))
                .thenReturn(Optional.empty());

        assertThrows(
                ResourceNotFoundException.class,
                () -> annonceService.update(100L, request)
        );

        verify(categoryRepository).findById(999L);

        verify(annonceRepository, never()).save(any());
        verify(annonceMapper, never()).updateEntity(any(), any());
    }

    @Test
    void update_shouldAllowModification_whenAnnonceIsSuspended() {

        Annonce annonce = new Annonce();

        annonce.setId(100L);
        annonce.setTitre("Ancien titre");
        annonce.setDescription("Ancienne description");
        annonce.setPrix(new BigDecimal("400000"));
        annonce.setEtat(EtatAnnonce.BON_ETAT);
        annonce.setStatut(StatutAnnonce.SUSPENDUE);
        annonce.setVille("Dakar");
        annonce.setQuartier("Almadies");
        annonce.setVendeur(user);
        annonce.setCategory(category);

        UpdateAnnonceRequest request = new UpdateAnnonceRequest(
                "iPhone 15 Pro",
                "Excellent état",
                new BigDecimal("500000"),
                EtatAnnonce.COMME_NEUF,
                "Dakar",
                "Plateau",
                10L
        );

        AnnonceResponse response = new AnnonceResponse(
                100L,
                "iPhone 15 Pro",
                "Excellent état",
                new BigDecimal("500000"),
                EtatAnnonce.COMME_NEUF,
                StatutAnnonce.SUSPENDUE,
                "Dakar",
                "Plateau",
                1L,
                "Seydi",
                10L,
                "Téléphones",
                List.of(),
                OffsetDateTime.now(),
                OffsetDateTime.now()
        );

        when(currentUserService.getCurrentUser())
                .thenReturn(user);

        when(annonceRepository.findById(100L))
                .thenReturn(Optional.of(annonce));

        when(categoryRepository.findById(10L))
                .thenReturn(Optional.of(category));

        doAnswer(invocation -> {
            Annonce a = invocation.getArgument(0);

            a.setTitre(request.titre());
            a.setDescription(request.description());
            a.setPrix(request.prix());
            a.setEtat(request.etat());
            a.setVille(request.ville());
            a.setQuartier(request.quartier());

            return null;
        }).when(annonceMapper).updateEntity(annonce, request);

        when(annonceRepository.save(annonce))
                .thenReturn(annonce);

        when(annonceImageRepository.findByAnnonceIdOrderByOrdreAsc(annonce.getId()))


                .thenReturn(List.of());



        when(annonceMapper.toResponse(eq(annonce), anyList()))
                .thenReturn(response);

        AnnonceResponse result =
                annonceService.update(100L, request);

        assertEquals(response, result);

        assertEquals("iPhone 15 Pro", annonce.getTitre());
        assertEquals("Excellent état", annonce.getDescription());
        assertEquals(new BigDecimal("500000"), annonce.getPrix());
        assertEquals(EtatAnnonce.COMME_NEUF, annonce.getEtat());
        assertEquals("Plateau", annonce.getQuartier());

        // Une modification de contenu ne change pas le statut
        assertEquals(StatutAnnonce.SUSPENDUE, annonce.getStatut());

        verify(annonceRepository).findById(100L);
        verify(categoryRepository).findById(10L);
        verify(annonceMapper).updateEntity(annonce, request);
        verify(annonceRepository).save(annonce);
        verify(annonceMapper).toResponse(eq(annonce), anyList());
    }

    @Test
    void publish_shouldPublishAnnonce_whenAnnonceIsDraft() {

        Annonce annonce = new Annonce();

        annonce.setId(100L);
        annonce.setStatut(StatutAnnonce.BROUILLON);
        annonce.setVendeur(user);
        annonce.setCategory(category);

        AnnonceResponse response = new AnnonceResponse(
                100L,
                "iPhone 15",
                "Excellent état",
                new BigDecimal("450000"),
                EtatAnnonce.COMME_NEUF,
                StatutAnnonce.PUBLIEE,
                "Dakar",
                "Almadies",
                1L,
                "Seydi",
                10L,
                "Téléphones",
                List.of(),
                OffsetDateTime.now(),
                OffsetDateTime.now()
        );

        when(currentUserService.getCurrentUser())
                .thenReturn(user);

        when(annonceRepository.findById(100L))
                .thenReturn(Optional.of(annonce));

        when(annonceRepository.save(annonce))
                .thenReturn(annonce);

        when(annonceImageRepository.findByAnnonceIdOrderByOrdreAsc(annonce.getId()))


                .thenReturn(List.of());



        when(annonceMapper.toResponse(eq(annonce), anyList()))
                .thenReturn(response);

        AnnonceResponse result =
                annonceService.publish(100L);

        assertEquals(response, result);

        // La transition métier doit avoir eu lieu
        assertEquals(
                StatutAnnonce.PUBLIEE,
                annonce.getStatut()
        );

        verify(currentUserService).getCurrentUser();
        verify(annonceRepository).findById(100L);
        verify(annonceRepository).save(annonce);
        verify(annonceMapper).toResponse(eq(annonce), anyList());
    }

    @Test
    void publish_shouldRepublishAnnonce_whenAnnonceIsSuspended() {

        Annonce annonce = new Annonce();

        annonce.setId(100L);
        annonce.setStatut(StatutAnnonce.SUSPENDUE);
        annonce.setVendeur(user);
        annonce.setCategory(category);

        AnnonceResponse response = new AnnonceResponse(
                100L,
                "iPhone 15",
                "Excellent état",
                new BigDecimal("450000"),
                EtatAnnonce.COMME_NEUF,
                StatutAnnonce.PUBLIEE,
                "Dakar",
                "Almadies",
                1L,
                "Seydi",
                10L,
                "Téléphones",
                List.of(),
                OffsetDateTime.now(),
                OffsetDateTime.now()
        );

        when(currentUserService.getCurrentUser())
                .thenReturn(user);

        when(annonceRepository.findById(100L))
                .thenReturn(Optional.of(annonce));

        when(annonceRepository.save(annonce))
                .thenReturn(annonce);

        when(annonceImageRepository.findByAnnonceIdOrderByOrdreAsc(annonce.getId()))


                .thenReturn(List.of());



        when(annonceMapper.toResponse(eq(annonce), anyList()))
                .thenReturn(response);

        AnnonceResponse result =
                annonceService.publish(100L);

        assertEquals(response, result);

        assertEquals(
                StatutAnnonce.PUBLIEE,
                annonce.getStatut()
        );

        verify(annonceRepository).save(annonce);
        verify(annonceMapper).toResponse(eq(annonce), anyList());
    }

    @Test
    void publish_shouldThrowException_whenAnnonceDoesNotExist() {

        when(currentUserService.getCurrentUser())
                .thenReturn(user);

        when(annonceRepository.findById(999L))
                .thenReturn(Optional.empty());

        assertThrows(
                ResourceNotFoundException.class,
                () -> annonceService.publish(999L)
        );

        verify(annonceRepository).findById(999L);

        verify(annonceRepository, never()).save(any());
        verify(annonceMapper, never()).toResponse(any(), anyList());
    }

    @Test
    void publish_shouldThrowAccessDenied_whenUserIsNotOwner() {

        UserProfile otherUser = new UserProfile();
        otherUser.setId(99L);

        Annonce annonce = new Annonce();

        annonce.setId(100L);
        annonce.setStatut(StatutAnnonce.BROUILLON);
        annonce.setVendeur(otherUser);

        when(currentUserService.getCurrentUser())
                .thenReturn(user);

        when(annonceRepository.findById(100L))
                .thenReturn(Optional.of(annonce));

        assertThrows(
                AccessDeniedException.class,
                () -> annonceService.publish(100L)
        );

        verify(annonceRepository, never()).save(any());
        verify(annonceMapper, never()).toResponse(any(), anyList());
    }

    @Test
    void publish_shouldThrowException_whenUserIsSuspended() {

        user.setSuspendu(true);

        Annonce annonce = new Annonce();

        annonce.setId(100L);
        annonce.setStatut(StatutAnnonce.BROUILLON);
        annonce.setVendeur(user);

        when(currentUserService.getCurrentUser())
                .thenReturn(user);

        when(annonceRepository.findById(100L))
                .thenReturn(Optional.of(annonce));

        assertThrows(
                UtilisateurSuspenduException.class,
                () -> annonceService.publish(100L)
        );

        verify(annonceRepository, never()).save(any());
        verify(annonceMapper, never()).toResponse(any(), anyList());
    }

    @Test
    void publish_shouldThrowException_whenUserAccountIsDeleted() {

        user.setDeletedAt(OffsetDateTime.now());

        Annonce annonce = new Annonce();

        annonce.setId(100L);
        annonce.setStatut(StatutAnnonce.BROUILLON);
        annonce.setVendeur(user);

        when(currentUserService.getCurrentUser())
                .thenReturn(user);

        when(annonceRepository.findById(100L))
                .thenReturn(Optional.of(annonce));

        assertThrows(
                CompteSupprimeException.class,
                () -> annonceService.publish(100L)
        );

        verify(annonceRepository, never()).save(any());
        verify(annonceMapper, never()).toResponse(any(), anyList());
    }

    @Test
    void publish_shouldRejectAnnonceAlreadyPublished() {

        Annonce annonce = new Annonce();

        annonce.setId(100L);
        annonce.setStatut(StatutAnnonce.PUBLIEE);
        annonce.setVendeur(user);

        when(currentUserService.getCurrentUser())
                .thenReturn(user);

        when(annonceRepository.findById(100L))
                .thenReturn(Optional.of(annonce));

        assertThrows(
                InvalidAnnonceStatusTransitionException.class,
                () -> annonceService.publish(100L)
        );

        assertEquals(
                StatutAnnonce.PUBLIEE,
                annonce.getStatut()
        );

        verify(annonceRepository, never()).save(any());
        verify(annonceMapper, never()).toResponse(any(), anyList());
    }

    @Test
    void publish_shouldRejectSoldAnnonce() {

        Annonce annonce = new Annonce();

        annonce.setId(100L);
        annonce.setStatut(StatutAnnonce.VENDUE);
        annonce.setVendeur(user);

        when(currentUserService.getCurrentUser())
                .thenReturn(user);

        when(annonceRepository.findById(100L))
                .thenReturn(Optional.of(annonce));

        assertThrows(
                InvalidAnnonceStatusTransitionException.class,
                () -> annonceService.publish(100L)
        );

        verify(annonceRepository, never()).save(any());
        verify(annonceMapper, never()).toResponse(any(), anyList());
    }

    @Test
    void publish_shouldRejectDeletedAnnonce() {

        Annonce annonce = new Annonce();

        annonce.setId(100L);
        annonce.setStatut(StatutAnnonce.SUPPRIMEE);
        annonce.setVendeur(user);

        when(currentUserService.getCurrentUser())
                .thenReturn(user);

        when(annonceRepository.findById(100L))
                .thenReturn(Optional.of(annonce));

        assertThrows(
                InvalidAnnonceStatusTransitionException.class,
                () -> annonceService.publish(100L)
        );

        verify(annonceRepository, never()).save(any());
        verify(annonceMapper, never()).toResponse(any(), anyList());
    }

    @Test
    void pause_shouldSuspendAnnonce_whenAnnonceIsPublished() {

        Annonce annonce = new Annonce();

        annonce.setId(100L);
        annonce.setStatut(StatutAnnonce.PUBLIEE);
        annonce.setVendeur(user);
        annonce.setCategory(category);

        AnnonceResponse response = new AnnonceResponse(
                100L,
                "iPhone 15",
                "Excellent état",
                new BigDecimal("450000"),
                EtatAnnonce.COMME_NEUF,
                StatutAnnonce.SUSPENDUE,
                "Dakar",
                "Almadies",
                1L,
                "Seydi",
                10L,
                "Téléphones",
                List.of(),
                OffsetDateTime.now(),
                OffsetDateTime.now()
        );

        when(currentUserService.getCurrentUser())
                .thenReturn(user);

        when(annonceRepository.findById(100L))
                .thenReturn(Optional.of(annonce));

        when(annonceRepository.save(annonce))
                .thenReturn(annonce);

        when(annonceImageRepository.findByAnnonceIdOrderByOrdreAsc(annonce.getId()))


                .thenReturn(List.of());



        when(annonceMapper.toResponse(eq(annonce), anyList()))
                .thenReturn(response);

        AnnonceResponse result =
                annonceService.pause(100L);

        assertEquals(response, result);

        assertEquals(
                StatutAnnonce.SUSPENDUE,
                annonce.getStatut()
        );

        verify(currentUserService).getCurrentUser();
        verify(annonceRepository).findById(100L);
        verify(annonceRepository).save(annonce);
        verify(annonceMapper).toResponse(eq(annonce), anyList());
    }

    @Test
    void pause_shouldThrowException_whenAnnonceDoesNotExist() {

        when(currentUserService.getCurrentUser())
                .thenReturn(user);

        when(annonceRepository.findById(999L))
                .thenReturn(Optional.empty());

        assertThrows(
                ResourceNotFoundException.class,
                () -> annonceService.pause(999L)
        );

        verify(annonceRepository).findById(999L);

        verify(annonceRepository, never()).save(any());
        verify(annonceMapper, never()).toResponse(any(), anyList());
    }

    @Test
    void pause_shouldThrowAccessDenied_whenUserIsNotOwner() {

        UserProfile otherUser = new UserProfile();
        otherUser.setId(99L);

        Annonce annonce = new Annonce();

        annonce.setId(100L);
        annonce.setStatut(StatutAnnonce.PUBLIEE);
        annonce.setVendeur(otherUser);

        when(currentUserService.getCurrentUser())
                .thenReturn(user);

        when(annonceRepository.findById(100L))
                .thenReturn(Optional.of(annonce));

        assertThrows(
                AccessDeniedException.class,
                () -> annonceService.pause(100L)
        );

        verify(annonceRepository, never()).save(any());
        verify(annonceMapper, never()).toResponse(any(), anyList());
    }

    @Test
    void pause_shouldThrowException_whenUserIsSuspended() {

        user.setSuspendu(true);

        Annonce annonce = new Annonce();

        annonce.setId(100L);
        annonce.setStatut(StatutAnnonce.PUBLIEE);
        annonce.setVendeur(user);

        when(currentUserService.getCurrentUser())
                .thenReturn(user);

        when(annonceRepository.findById(100L))
                .thenReturn(Optional.of(annonce));

        assertThrows(
                UtilisateurSuspenduException.class,
                () -> annonceService.pause(100L)
        );

        verify(annonceRepository, never()).save(any());
        verify(annonceMapper, never()).toResponse(any(), anyList());
    }

    @Test
    void pause_shouldThrowException_whenUserAccountIsDeleted() {

        user.setDeletedAt(OffsetDateTime.now());

        Annonce annonce = new Annonce();

        annonce.setId(100L);
        annonce.setStatut(StatutAnnonce.PUBLIEE);
        annonce.setVendeur(user);

        when(currentUserService.getCurrentUser())
                .thenReturn(user);

        when(annonceRepository.findById(100L))
                .thenReturn(Optional.of(annonce));

        assertThrows(
                CompteSupprimeException.class,
                () -> annonceService.pause(100L)
        );

        verify(annonceRepository, never()).save(any());
        verify(annonceMapper, never()).toResponse(any(), anyList());
    }

    @Test
    void pause_shouldRejectDraftAnnonce() {

        Annonce annonce = new Annonce();

        annonce.setId(100L);
        annonce.setStatut(StatutAnnonce.BROUILLON);
        annonce.setVendeur(user);

        when(currentUserService.getCurrentUser())
                .thenReturn(user);

        when(annonceRepository.findById(100L))
                .thenReturn(Optional.of(annonce));

        assertThrows(
                InvalidAnnonceStatusTransitionException.class,
                () -> annonceService.pause(100L)
        );

        verify(annonceRepository, never()).save(any());
    }

    @Test
    void pause_shouldRejectAlreadySuspendedAnnonce() {

        Annonce annonce = new Annonce();

        annonce.setId(100L);
        annonce.setStatut(StatutAnnonce.SUSPENDUE);
        annonce.setVendeur(user);

        when(currentUserService.getCurrentUser())
                .thenReturn(user);

        when(annonceRepository.findById(100L))
                .thenReturn(Optional.of(annonce));

        assertThrows(
                InvalidAnnonceStatusTransitionException.class,
                () -> annonceService.pause(100L)
        );

        verify(annonceRepository, never()).save(any());
    }

    @Test
    void pause_shouldRejectSoldAnnonce() {

        Annonce annonce = new Annonce();

        annonce.setId(100L);
        annonce.setStatut(StatutAnnonce.VENDUE);
        annonce.setVendeur(user);

        when(currentUserService.getCurrentUser())
                .thenReturn(user);

        when(annonceRepository.findById(100L))
                .thenReturn(Optional.of(annonce));

        assertThrows(
                InvalidAnnonceStatusTransitionException.class,
                () -> annonceService.pause(100L)
        );

        verify(annonceRepository, never()).save(any());
    }

    @Test
    void sold_shouldMarkAnnonceAsSold_whenAnnonceIsPublished() {

        Annonce annonce = new Annonce();

        annonce.setId(100L);
        annonce.setStatut(StatutAnnonce.PUBLIEE);
        annonce.setVendeur(user);
        annonce.setCategory(category);

        AnnonceResponse response = new AnnonceResponse(
                100L,
                "iPhone 15",
                "Excellent état",
                new BigDecimal("450000"),
                EtatAnnonce.COMME_NEUF,
                StatutAnnonce.VENDUE,
                "Dakar",
                "Almadies",
                1L,
                "Seydi",
                10L,
                "Téléphones",
                List.of(),
                OffsetDateTime.now(),
                OffsetDateTime.now()
        );

        when(currentUserService.getCurrentUser())
                .thenReturn(user);

        when(annonceRepository.findById(100L))
                .thenReturn(Optional.of(annonce));

        when(annonceRepository.save(annonce))
                .thenReturn(annonce);

        when(annonceImageRepository.findByAnnonceIdOrderByOrdreAsc(annonce.getId()))


                .thenReturn(List.of());



        when(annonceMapper.toResponse(eq(annonce), anyList()))
                .thenReturn(response);

        AnnonceResponse result =
                annonceService.sold(100L);

        assertEquals(response, result);

        assertEquals(
                StatutAnnonce.VENDUE,
                annonce.getStatut()
        );

        verify(currentUserService).getCurrentUser();
        verify(annonceRepository).findById(100L);
        verify(annonceRepository).save(annonce);
        verify(annonceMapper).toResponse(eq(annonce), anyList());
    }

    @Test
    void sold_shouldThrowException_whenAnnonceDoesNotExist() {

        when(currentUserService.getCurrentUser())
                .thenReturn(user);

        when(annonceRepository.findById(999L))
                .thenReturn(Optional.empty());

        assertThrows(
                ResourceNotFoundException.class,
                () -> annonceService.sold(999L)
        );

        verify(annonceRepository).findById(999L);
        verify(annonceRepository, never()).save(any());
        verify(annonceMapper, never()).toResponse(any(), anyList());
    }

    @Test
    void sold_shouldThrowAccessDenied_whenUserIsNotOwner() {

        UserProfile otherUser = new UserProfile();
        otherUser.setId(99L);

        Annonce annonce = new Annonce();

        annonce.setId(100L);
        annonce.setStatut(StatutAnnonce.PUBLIEE);
        annonce.setVendeur(otherUser);

        when(currentUserService.getCurrentUser())
                .thenReturn(user);

        when(annonceRepository.findById(100L))
                .thenReturn(Optional.of(annonce));

        assertThrows(
                AccessDeniedException.class,
                () -> annonceService.sold(100L)
        );

        verify(annonceRepository, never()).save(any());
        verify(annonceMapper, never()).toResponse(any(), anyList());
    }

    @Test
    void sold_shouldThrowException_whenUserIsSuspended() {

        user.setSuspendu(true);

        Annonce annonce = new Annonce();

        annonce.setId(100L);
        annonce.setStatut(StatutAnnonce.PUBLIEE);
        annonce.setVendeur(user);

        when(currentUserService.getCurrentUser())
                .thenReturn(user);

        when(annonceRepository.findById(100L))
                .thenReturn(Optional.of(annonce));

        assertThrows(
                UtilisateurSuspenduException.class,
                () -> annonceService.sold(100L)
        );

        verify(annonceRepository, never()).save(any());
        verify(annonceMapper, never()).toResponse(any(), anyList());
    }

    @Test
    void sold_shouldThrowException_whenUserAccountIsDeleted() {

        user.setDeletedAt(OffsetDateTime.now());

        Annonce annonce = new Annonce();

        annonce.setId(100L);
        annonce.setStatut(StatutAnnonce.PUBLIEE);
        annonce.setVendeur(user);

        when(currentUserService.getCurrentUser())
                .thenReturn(user);

        when(annonceRepository.findById(100L))
                .thenReturn(Optional.of(annonce));

        assertThrows(
                CompteSupprimeException.class,
                () -> annonceService.sold(100L)
        );

        verify(annonceRepository, never()).save(any());
        verify(annonceMapper, never()).toResponse(any(), anyList());
    }

    @Test
    void sold_shouldRejectDraftAnnonce() {

        Annonce annonce = new Annonce();

        annonce.setId(100L);
        annonce.setStatut(StatutAnnonce.BROUILLON);
        annonce.setVendeur(user);

        when(currentUserService.getCurrentUser())
                .thenReturn(user);

        when(annonceRepository.findById(100L))
                .thenReturn(Optional.of(annonce));

        assertThrows(
                InvalidAnnonceStatusTransitionException.class,
                () -> annonceService.sold(100L)
        );

        verify(annonceRepository, never()).save(any());
    }

    @Test
    void sold_shouldRejectSuspendedAnnonce() {

        Annonce annonce = new Annonce();

        annonce.setId(100L);
        annonce.setStatut(StatutAnnonce.SUSPENDUE);
        annonce.setVendeur(user);

        when(currentUserService.getCurrentUser())
                .thenReturn(user);

        when(annonceRepository.findById(100L))
                .thenReturn(Optional.of(annonce));

        assertThrows(
                InvalidAnnonceStatusTransitionException.class,
                () -> annonceService.sold(100L)
        );

        verify(annonceRepository, never()).save(any());
    }

    @Test
    void sold_shouldRejectAlreadySoldAnnonce() {

        Annonce annonce = new Annonce();

        annonce.setId(100L);
        annonce.setStatut(StatutAnnonce.VENDUE);
        annonce.setVendeur(user);

        when(currentUserService.getCurrentUser())
                .thenReturn(user);

        when(annonceRepository.findById(100L))
                .thenReturn(Optional.of(annonce));

        assertThrows(
                InvalidAnnonceStatusTransitionException.class,
                () -> annonceService.sold(100L)
        );

        verify(annonceRepository, never()).save(any());
    }

    @Test
    void sold_shouldRejectDeletedAnnonce() {

        Annonce annonce = new Annonce();

        annonce.setId(100L);
        annonce.setStatut(StatutAnnonce.SUPPRIMEE);
        annonce.setVendeur(user);

        when(currentUserService.getCurrentUser())
                .thenReturn(user);

        when(annonceRepository.findById(100L))
                .thenReturn(Optional.of(annonce));

        assertThrows(
                InvalidAnnonceStatusTransitionException.class,
                () -> annonceService.sold(100L)
        );

        verify(annonceRepository, never()).save(any());
    }

    @Test
    void shouldDeleteAnnonce() {

        UserProfile user = new UserProfile();
        user.setId(1L);
        user.setSuspendu(false);

        Annonce annonce = new Annonce();
        annonce.setId(10L);
        annonce.setVendeur(user);
        annonce.setStatut(StatutAnnonce.PUBLIEE);

        when(currentUserService.getCurrentUser()).thenReturn(user);
        when(annonceRepository.findById(10L)).thenReturn(Optional.of(annonce));

        annonceService.delete(10L);

        assertEquals(
                StatutAnnonce.SUPPRIMEE,
                annonce.getStatut()
        );

        verify(favoriteRepository)
                .deleteByAnnonceId(10L);
    }

    @Test
    void shouldThrowWhenDeletingUnknownAnnonce() {

        UserProfile user = new UserProfile();
        user.setId(1L);

        when(currentUserService.getCurrentUser()).thenReturn(user);
        when(annonceRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(
                ResourceNotFoundException.class,
                () -> annonceService.delete(99L)
        );
    }

    @Test
    void shouldRejectDeleteWhenUserIsNotOwner() {

        UserProfile currentUser = new UserProfile();
        currentUser.setId(1L);

        UserProfile owner = new UserProfile();
        owner.setId(2L);

        Annonce annonce = new Annonce();
        annonce.setId(10L);
        annonce.setVendeur(owner);
        annonce.setStatut(StatutAnnonce.PUBLIEE);

        when(currentUserService.getCurrentUser()).thenReturn(currentUser);
        when(annonceRepository.findById(10L)).thenReturn(Optional.of(annonce));

        assertThrows(
                AccessDeniedException.class,
                () -> annonceService.delete(10L)
        );
    }

    @Test
    void shouldRejectDeleteWhenUserIsSuspended() {

        UserProfile user = new UserProfile();
        user.setId(1L);
        user.setSuspendu(true);

        Annonce annonce = new Annonce();
        annonce.setId(10L);
        annonce.setVendeur(user);
        annonce.setStatut(StatutAnnonce.PUBLIEE);

        when(currentUserService.getCurrentUser()).thenReturn(user);
        when(annonceRepository.findById(10L)).thenReturn(Optional.of(annonce));

        assertThrows(
                UtilisateurSuspenduException.class,
                () -> annonceService.delete(10L)
        );
    }

    @Test
    void shouldRejectDeleteWhenAccountIsDeleted() {

        UserProfile user = new UserProfile();
        user.setId(1L);
        user.setSuspendu(false);
        user.setDeletedAt(OffsetDateTime.now());

        Annonce annonce = new Annonce();
        annonce.setId(10L);
        annonce.setVendeur(user);
        annonce.setStatut(StatutAnnonce.PUBLIEE);

        when(currentUserService.getCurrentUser()).thenReturn(user);
        when(annonceRepository.findById(10L)).thenReturn(Optional.of(annonce));

        assertThrows(
                CompteSupprimeException.class,
                () -> annonceService.delete(10L)
        );
    }

    @Test
    void shouldRejectAlreadyDeletedAnnonce() {

        UserProfile user = new UserProfile();
        user.setId(1L);
        user.setSuspendu(false);

        Annonce annonce = new Annonce();
        annonce.setId(10L);
        annonce.setVendeur(user);
        annonce.setStatut(StatutAnnonce.SUPPRIMEE);

        when(currentUserService.getCurrentUser()).thenReturn(user);
        when(annonceRepository.findById(10L)).thenReturn(Optional.of(annonce));

        assertThrows(
                AnnonceModificationInterditeException.class,
                () -> annonceService.delete(10L)
        );
    }



}
