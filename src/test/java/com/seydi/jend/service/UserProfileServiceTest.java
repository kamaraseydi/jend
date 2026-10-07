package com.seydi.jend.service;

import com.seydi.jend.dto.request.UpdateProfileRequest;
import com.seydi.jend.dto.response.AdminUserResponse;
import com.seydi.jend.dto.response.MyProfileResponse;
import com.seydi.jend.dto.response.UserProfileResponse;
import com.seydi.jend.entity.Annonce;
import com.seydi.jend.entity.Role;
import com.seydi.jend.security.CurrentUserService;
import com.seydi.jend.entity.StatutAnnonce;
import com.seydi.jend.entity.UserProfile;
import com.seydi.jend.exception.CompteSupprimeException;
import com.seydi.jend.exception.ResourceNotFoundException;
import com.seydi.jend.exception.UtilisateurSuspenduException;
import com.seydi.jend.mapper.UserProfileMapper;
import com.seydi.jend.repository.AnnonceRepository;
import com.seydi.jend.repository.UserProfileRepository;
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
class UserProfileServiceTest {

    @Mock
    private UserProfileRepository userProfileRepository;

    @Mock
    private UserProfileMapper userProfileMapper;

    @Mock
    private CurrentUserService currentUserService;

    @Mock
    private AnnonceRepository annonceRepository;

    @InjectMocks
    private UserProfileService userProfileService;

    private UserProfile currentUser;

    @BeforeEach
    void setUp() {

        currentUser = new UserProfile();

        currentUser.setId(1L);
        currentUser.setNom("Seydi");
        currentUser.setEmail("seydi@example.com");
        currentUser.setTelephone("771234567");
        currentUser.setVille("Dakar");
        currentUser.setEstProfessionnel(false);
        currentUser.setSuspendu(false);
        currentUser.setRole(Role.UTILISATEUR);
        currentUser.setCreatedAt(OffsetDateTime.now());
        currentUser.setUpdatedAt(OffsetDateTime.now());
    }

    @Test
    void shouldGetMyProfile() {

        MyProfileResponse response = new MyProfileResponse(
                1L,
                "Seydi",
                "seydi@example.com",
                "771234567",
                "Dakar",
                false,
                false,
                Role.UTILISATEUR,
                currentUser.getCreatedAt(),
                currentUser.getUpdatedAt()
        );

        when(currentUserService.getCurrentUser())
                .thenReturn(currentUser);

        when(userProfileMapper.toMyProfileResponse(currentUser))
                .thenReturn(response);

        MyProfileResponse result =
                userProfileService.getMyProfile();

        assertEquals(response, result);

        verify(currentUserService).getCurrentUser();
        verify(userProfileMapper).toMyProfileResponse(currentUser);
    }

    @Test
    void shouldNotGetMyProfileWhenAccountIsSuspended() {

        currentUser.setSuspendu(true);

        when(currentUserService.getCurrentUser())
                .thenReturn(currentUser);

        assertThrows(
                UtilisateurSuspenduException.class,
                () -> userProfileService.getMyProfile()
        );

        verify(userProfileMapper, never())
                .toMyProfileResponse(any());
    }

    @Test
    void shouldNotGetMyProfileWhenAccountIsDeleted() {

        currentUser.setDeletedAt(OffsetDateTime.now());

        when(currentUserService.getCurrentUser())
                .thenReturn(currentUser);

        assertThrows(
                CompteSupprimeException.class,
                () -> userProfileService.getMyProfile()
        );

        verify(userProfileMapper, never())
                .toMyProfileResponse(any());
    }

    @Test
    void shouldUpdateMyProfile() {

        UpdateProfileRequest request =
                new UpdateProfileRequest(
                        "Nouveau Nom",
                        "778765432",
                        "Thiès"
                );

        MyProfileResponse response = new MyProfileResponse(
                1L,
                "Nouveau Nom",
                "seydi@example.com",
                "778765432",
                "Thiès",
                false,
                false,
                Role.UTILISATEUR,
                currentUser.getCreatedAt(),
                currentUser.getUpdatedAt()
        );

        when(currentUserService.getCurrentUser())
                .thenReturn(currentUser);

        when(userProfileMapper.toMyProfileResponse(currentUser))
                .thenReturn(response);

        MyProfileResponse result =
                userProfileService.updateMyProfile(request);

        verify(userProfileMapper)
                .updateEntity(currentUser, request);

        verify(userProfileMapper)
                .toMyProfileResponse(currentUser);

        assertEquals(response, result);
    }

    @Test
    void shouldGetPublicProfile() {

        UserProfile user = new UserProfile();

        user.setId(2L);
        user.setNom("Moussa");
        user.setTelephone("770000000");
        user.setVille("Dakar");
        user.setEstProfessionnel(true);
        user.setRole(Role.UTILISATEUR);
        user.setCreatedAt(OffsetDateTime.now());

        UserProfileResponse response = new UserProfileResponse(
                2L,
                "Moussa",
                "770000000",
                "Dakar",
                true,
                Role.UTILISATEUR,
                user.getCreatedAt()
        );

        when(userProfileRepository.findById(2L))
                .thenReturn(Optional.of(user));

        when(userProfileMapper.toPublicResponse(user))
                .thenReturn(response);

        UserProfileResponse result =
                userProfileService.getPublicProfile(2L);

        assertEquals(response, result);

        verify(userProfileMapper)
                .toPublicResponse(user);
    }

    @Test
    void shouldNotGetPublicProfileWhenUserDoesNotExist() {

        when(userProfileRepository.findById(99L))
                .thenReturn(Optional.empty());

        assertThrows(
                ResourceNotFoundException.class,
                () -> userProfileService.getPublicProfile(99L)
        );
    }

    @Test
    void shouldNotGetPublicProfileWhenAccountIsDeleted() {

        UserProfile user = new UserProfile();

        user.setId(2L);
        user.setDeletedAt(OffsetDateTime.now());

        when(userProfileRepository.findById(2L))
                .thenReturn(Optional.of(user));

        assertThrows(
                ResourceNotFoundException.class,
                () -> userProfileService.getPublicProfile(2L)
        );

        verify(userProfileMapper, never())
                .toPublicResponse(any());
    }

    @Test
    void shouldSuspendUserAndSuspendPublishedAnnonces() {

        UserProfile admin = new UserProfile();
        admin.setId(1L);
        admin.setRole(Role.ADMIN);
        admin.setSuspendu(false);

        UserProfile user = new UserProfile();
        user.setId(2L);
        user.setRole(Role.UTILISATEUR);
        user.setSuspendu(false);

        Annonce annonce1 = new Annonce();
        annonce1.setId(10L);
        annonce1.setStatut(StatutAnnonce.PUBLIEE);

        Annonce annonce2 = new Annonce();
        annonce2.setId(11L);
        annonce2.setStatut(StatutAnnonce.PUBLIEE);

        when(currentUserService.getCurrentUser())
                .thenReturn(admin);

        when(userProfileRepository.findById(2L))
                .thenReturn(Optional.of(user));

        when(annonceRepository.findByVendeurIdAndStatut(
                2L,
                StatutAnnonce.PUBLIEE
        )).thenReturn(List.of(annonce1, annonce2));

        userProfileService.suspendUser(2L);

        assertTrue(user.isSuspendu());

        assertEquals(
                StatutAnnonce.SUSPENDUE,
                annonce1.getStatut()
        );

        assertEquals(
                StatutAnnonce.SUSPENDUE,
                annonce2.getStatut()
        );

        verify(annonceRepository)
                .findByVendeurIdAndStatut(
                        2L,
                        StatutAnnonce.PUBLIEE
                );
    }

    @Test
    void shouldNotSuspendUserWhenCurrentUserIsNotAdmin() {

        when(currentUserService.getCurrentUser())
                .thenReturn(currentUser);

        assertThrows(
                AccessDeniedException.class,
                () -> userProfileService.suspendUser(2L)
        );

        verify(userProfileRepository, never())
                .findById(anyLong());

        verify(annonceRepository, never())
                .findByVendeurIdAndStatut(anyLong(), any());
    }

    @Test
    void shouldNotSuspendOwnAccount() {

        UserProfile admin = new UserProfile();
        admin.setId(1L);
        admin.setRole(Role.ADMIN);

        when(currentUserService.getCurrentUser())
                .thenReturn(admin);

        when(userProfileRepository.findById(1L))
                .thenReturn(Optional.of(admin));

        assertThrows(
                AccessDeniedException.class,
                () -> userProfileService.suspendUser(1L)
        );
    }

    @Test
    void shouldNotSuspendDeletedUser() {

        UserProfile admin = new UserProfile();
        admin.setId(1L);
        admin.setRole(Role.ADMIN);

        UserProfile deletedUser = new UserProfile();
        deletedUser.setId(2L);
        deletedUser.setDeletedAt(OffsetDateTime.now());

        when(currentUserService.getCurrentUser())
                .thenReturn(admin);

        when(userProfileRepository.findById(2L))
                .thenReturn(Optional.of(deletedUser));

        assertThrows(
                ResourceNotFoundException.class,
                () -> userProfileService.suspendUser(2L)
        );

        verify(annonceRepository, never())
                .findByVendeurIdAndStatut(anyLong(), any());
    }

    @Test
    void shouldReactivateUser() {

        UserProfile admin = new UserProfile();
        admin.setId(1L);
        admin.setRole(Role.ADMIN);

        UserProfile user = new UserProfile();
        user.setId(2L);
        user.setSuspendu(true);

        when(currentUserService.getCurrentUser())
                .thenReturn(admin);

        when(userProfileRepository.findById(2L))
                .thenReturn(Optional.of(user));

        userProfileService.reactivateUser(2L);

        assertFalse(user.isSuspendu());

        verify(userProfileRepository)
                .findById(2L);
    }

    @Test
    void shouldNotReactivateUserWhenCurrentUserIsNotAdmin() {

        when(currentUserService.getCurrentUser())
                .thenReturn(currentUser);

        assertThrows(
                AccessDeniedException.class,
                () -> userProfileService.reactivateUser(2L)
        );

        verify(userProfileRepository, never())
                .findById(anyLong());
    }

    @Test
    void shouldNotReactivateDeletedUser() {

        UserProfile admin = new UserProfile();
        admin.setId(1L);
        admin.setRole(Role.ADMIN);

        UserProfile deletedUser = new UserProfile();
        deletedUser.setId(2L);
        deletedUser.setDeletedAt(OffsetDateTime.now());

        when(currentUserService.getCurrentUser())
                .thenReturn(admin);

        when(userProfileRepository.findById(2L))
                .thenReturn(Optional.of(deletedUser));

        assertThrows(
                ResourceNotFoundException.class,
                () -> userProfileService.reactivateUser(2L)
        );
    }

    @Test
    void shouldDeleteMyAccountAndDeleteAllMyAnnonces() {

        Annonce annonce1 = new Annonce();
        annonce1.setId(10L);
        annonce1.setStatut(StatutAnnonce.PUBLIEE);

        Annonce annonce2 = new Annonce();
        annonce2.setId(11L);
        annonce2.setStatut(StatutAnnonce.BROUILLON);

        Annonce annonce3 = new Annonce();
        annonce3.setId(12L);
        annonce3.setStatut(StatutAnnonce.SUSPENDUE);

        when(currentUserService.getCurrentUser())
                .thenReturn(currentUser);

        when(annonceRepository.findByVendeurId(1L))
                .thenReturn(List.of(annonce1, annonce2, annonce3));

        userProfileService.deleteMyAccount();

        assertNotNull(currentUser.getDeletedAt());

        assertEquals(
                StatutAnnonce.SUPPRIMEE,
                annonce1.getStatut()
        );

        assertEquals(
                StatutAnnonce.SUPPRIMEE,
                annonce2.getStatut()
        );

        assertEquals(
                StatutAnnonce.SUPPRIMEE,
                annonce3.getStatut()
        );

        assertNotNull(annonce1.getUpdatedAt());
        assertNotNull(annonce2.getUpdatedAt());
        assertNotNull(annonce3.getUpdatedAt());

        verify(currentUserService).getCurrentUser();
        verify(annonceRepository).findByVendeurId(1L);
    }

    @Test
    void shouldDeleteMyAccountEvenWhenUserHasNoAnnonces() {

        when(currentUserService.getCurrentUser())
                .thenReturn(currentUser);

        when(annonceRepository.findByVendeurId(1L))
                .thenReturn(List.of());

        userProfileService.deleteMyAccount();

        assertNotNull(currentUser.getDeletedAt());

        verify(annonceRepository)
                .findByVendeurId(1L);
    }

    @Test
    void shouldNotDeleteAccountWhenAlreadyDeleted() {

        currentUser.setDeletedAt(OffsetDateTime.now());

        when(currentUserService.getCurrentUser())
                .thenReturn(currentUser);

        assertThrows(
                CompteSupprimeException.class,
                () -> userProfileService.deleteMyAccount()
        );

        verify(annonceRepository, never())
                .findByVendeurId(anyLong());
    }

    @Test
    void shouldNotDeleteAccountWhenUserIsSuspended() {

        currentUser.setSuspendu(true);

        when(currentUserService.getCurrentUser())
                .thenReturn(currentUser);

        assertThrows(
                UtilisateurSuspenduException.class,
                () -> userProfileService.deleteMyAccount()
        );

        verify(annonceRepository, never())
                .findByVendeurId(anyLong());
    }

    @Test
    void shouldDeleteSoldAnnonceWhenDeletingAccount() {

        Annonce annonce = new Annonce();
        annonce.setId(20L);
        annonce.setStatut(StatutAnnonce.VENDUE);

        when(currentUserService.getCurrentUser())
                .thenReturn(currentUser);

        when(annonceRepository.findByVendeurId(1L))
                .thenReturn(List.of(annonce));

        userProfileService.deleteMyAccount();

        assertEquals(
                StatutAnnonce.SUPPRIMEE,
                annonce.getStatut()
        );

        assertNotNull(currentUser.getDeletedAt());
    }

    @Test
    void shouldDeleteSuspendedAnnonceWhenDeletingAccount() {

        Annonce annonce = new Annonce();
        annonce.setId(21L);
        annonce.setStatut(StatutAnnonce.SUSPENDUE);

        when(currentUserService.getCurrentUser())
                .thenReturn(currentUser);

        when(annonceRepository.findByVendeurId(1L))
                .thenReturn(List.of(annonce));

        userProfileService.deleteMyAccount();

        assertEquals(
                StatutAnnonce.SUPPRIMEE,
                annonce.getStatut()
        );

        assertNotNull(currentUser.getDeletedAt());
    }

    @Test
    void shouldMakeUserProfessional() {

        UserProfile admin = new UserProfile();
        admin.setId(1L);
        admin.setRole(Role.ADMIN);

        UserProfile user = new UserProfile();
        user.setId(2L);
        user.setRole(Role.UTILISATEUR);
        user.setEstProfessionnel(false);

        when(currentUserService.getCurrentUser())
                .thenReturn(admin);

        when(userProfileRepository.findById(2L))
                .thenReturn(Optional.of(user));

        userProfileService.updateProfessionalStatus(2L, true);

        assertTrue(user.isEstProfessionnel());
        assertNotNull(user.getUpdatedAt());

        verify(userProfileRepository)
                .findById(2L);
    }

    @Test
    void shouldRemoveProfessionalStatus() {

        UserProfile admin = new UserProfile();
        admin.setId(1L);
        admin.setRole(Role.ADMIN);

        UserProfile user = new UserProfile();
        user.setId(2L);
        user.setRole(Role.UTILISATEUR);
        user.setEstProfessionnel(true);

        when(currentUserService.getCurrentUser())
                .thenReturn(admin);

        when(userProfileRepository.findById(2L))
                .thenReturn(Optional.of(user));

        userProfileService.updateProfessionalStatus(2L, false);

        assertFalse(user.isEstProfessionnel());
        assertNotNull(user.getUpdatedAt());
    }

    @Test
    void shouldNotUpdateProfessionalStatusWhenCurrentUserIsNotAdmin() {

        when(currentUserService.getCurrentUser())
                .thenReturn(currentUser);

        assertThrows(
                AccessDeniedException.class,
                () -> userProfileService.updateProfessionalStatus(2L, true)
        );

        verify(userProfileRepository, never())
                .findById(anyLong());
    }

    @Test
    void shouldNotUpdateProfessionalStatusWhenUserDoesNotExist() {

        UserProfile admin = new UserProfile();
        admin.setId(1L);
        admin.setRole(Role.ADMIN);

        when(currentUserService.getCurrentUser())
                .thenReturn(admin);

        when(userProfileRepository.findById(99L))
                .thenReturn(Optional.empty());

        assertThrows(
                ResourceNotFoundException.class,
                () -> userProfileService.updateProfessionalStatus(99L, true)
        );
    }

    @Test
    void shouldNotUpdateProfessionalStatusWhenUserIsDeleted() {

        UserProfile admin = new UserProfile();
        admin.setId(1L);
        admin.setRole(Role.ADMIN);

        UserProfile deletedUser = new UserProfile();
        deletedUser.setId(2L);
        deletedUser.setDeletedAt(OffsetDateTime.now());

        when(currentUserService.getCurrentUser())
                .thenReturn(admin);

        when(userProfileRepository.findById(2L))
                .thenReturn(Optional.of(deletedUser));

        assertThrows(
                ResourceNotFoundException.class,
                () -> userProfileService.updateProfessionalStatus(2L, true)
        );
    }

    @Test
    void shouldFindAllUsersAsAdmin() {

        UserProfile admin = new UserProfile();
        admin.setId(1L);
        admin.setRole(Role.ADMIN);

        UserProfile user1 = new UserProfile();
        user1.setId(2L);
        user1.setNom("Moussa");
        user1.setEmail("moussa@example.com");
        user1.setRole(Role.UTILISATEUR);
        user1.setEstProfessionnel(false);
        user1.setSuspendu(false);
        user1.setCreatedAt(OffsetDateTime.now());
        user1.setUpdatedAt(OffsetDateTime.now());

        UserProfile user2 = new UserProfile();
        user2.setId(3L);
        user2.setNom("Fatou");
        user2.setEmail("fatou@example.com");
        user2.setRole(Role.UTILISATEUR);
        user2.setEstProfessionnel(true);
        user2.setSuspendu(false);
        user2.setCreatedAt(OffsetDateTime.now());
        user2.setUpdatedAt(OffsetDateTime.now());

        AdminUserResponse response1 = new AdminUserResponse(
                2L,
                "Moussa",
                "moussa@example.com",
                null,
                null,
                false,
                false,
                Role.UTILISATEUR,
                user1.getCreatedAt(),
                user1.getUpdatedAt()
        );

        AdminUserResponse response2 = new AdminUserResponse(
                3L,
                "Fatou",
                "fatou@example.com",
                null,
                null,
                true,
                false,
                Role.UTILISATEUR,
                user2.getCreatedAt(),
                user2.getUpdatedAt()
        );

        when(currentUserService.getCurrentUser())
                .thenReturn(admin);

        when(userProfileRepository.findAll())
                .thenReturn(List.of(user1, user2));

        when(userProfileMapper.toAdminResponse(user1))
                .thenReturn(response1);

        when(userProfileMapper.toAdminResponse(user2))
                .thenReturn(response2);

        List<AdminUserResponse> result =
                userProfileService.findAllUsers();

        assertEquals(2, result.size());

        assertEquals("Moussa", result.get(0).nom());
        assertEquals("Fatou", result.get(1).nom());

        verify(userProfileRepository).findAll();
        verify(userProfileMapper).toAdminResponse(user1);
        verify(userProfileMapper).toAdminResponse(user2);
    }

    @Test
    void shouldExcludeDeletedUsers() {

        UserProfile admin = new UserProfile();
        admin.setId(1L);
        admin.setRole(Role.ADMIN);

        UserProfile activeUser = new UserProfile();
        activeUser.setId(2L);
        activeUser.setNom("Moussa");
        activeUser.setRole(Role.UTILISATEUR);

        UserProfile deletedUser = new UserProfile();
        deletedUser.setId(3L);
        deletedUser.setNom("Fatou");
        deletedUser.setRole(Role.UTILISATEUR);
        deletedUser.setDeletedAt(OffsetDateTime.now());

        AdminUserResponse response = new AdminUserResponse(
                2L,
                "Moussa",
                null,
                null,
                null,
                false,
                false,
                Role.UTILISATEUR,
                null,
                null
        );

        when(currentUserService.getCurrentUser())
                .thenReturn(admin);

        when(userProfileRepository.findAll())
                .thenReturn(List.of(activeUser, deletedUser));

        when(userProfileMapper.toAdminResponse(activeUser))
                .thenReturn(response);

        List<AdminUserResponse> result =
                userProfileService.findAllUsers();

        assertEquals(1, result.size());
        assertEquals(2L, result.get(0).id());
        assertEquals("Moussa", result.get(0).nom());

        verify(userProfileMapper)
                .toAdminResponse(activeUser);

        verify(userProfileMapper, never())
                .toAdminResponse(deletedUser);
    }

    @Test
    void shouldNotFindAllUsersWhenCurrentUserIsNotAdmin() {

        when(currentUserService.getCurrentUser())
                .thenReturn(currentUser);

        assertThrows(
                AccessDeniedException.class,
                () -> userProfileService.findAllUsers()
        );

        verify(userProfileRepository, never())
                .findAll();

        verify(userProfileMapper, never())
                .toAdminResponse(any());
    }

    @Test
    void shouldReturnEmptyListWhenThereAreNoActiveUsers() {

        UserProfile admin = new UserProfile();
        admin.setId(1L);
        admin.setRole(Role.ADMIN);

        when(currentUserService.getCurrentUser())
                .thenReturn(admin);

        when(userProfileRepository.findAll())
                .thenReturn(List.of());

        List<AdminUserResponse> result =
                userProfileService.findAllUsers();

        assertTrue(result.isEmpty());

        verify(userProfileRepository)
                .findAll();

        verify(userProfileMapper, never())
                .toAdminResponse(any());
    }


}