package com.seydi.jend.controller;

import com.seydi.jend.dto.request.UpdateProfileRequest;
import com.seydi.jend.dto.response.MyProfileResponse;
import com.seydi.jend.dto.response.UserProfileResponse;
import com.seydi.jend.entity.Role;
import com.seydi.jend.service.UserProfileService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import com.seydi.jend.dto.response.AdminUserResponse;
import org.springframework.test.web.servlet.MockMvc;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.OffsetDateTime;
import java.util.List;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;


@WebMvcTest(UserProfileController.class)
@AutoConfigureMockMvc(addFilters = false)
class UserProfileControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private UserProfileService userProfileService;

    @Test
    void shouldGetMyProfile() throws Exception {

        MyProfileResponse response = new MyProfileResponse(
                1L,
                "Seydi",
                "seydi@example.com",
                "771234567",
                "Dakar",
                false,
                false,
                Role.UTILISATEUR,
                OffsetDateTime.now(),
                OffsetDateTime.now()
        );

        when(userProfileService.getMyProfile())
                .thenReturn(response);

        mockMvc.perform(
                        get("/api/users/me")
                )
                .andExpect(status().isOk());

        verify(userProfileService)
                .getMyProfile();
    }

    @Test
    void shouldUpdateMyProfile() throws Exception {

        MyProfileResponse response = new MyProfileResponse(
                1L,
                "Nouveau Nom",
                "seydi@example.com",
                "778765432",
                "Thiès",
                false,
                false,
                Role.UTILISATEUR,
                OffsetDateTime.now(),
                OffsetDateTime.now()
        );

        when(userProfileService.updateMyProfile(any(UpdateProfileRequest.class)))
                .thenReturn(response);

        mockMvc.perform(
                        put("/api/users/me")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("""
                                        {
                                            "nom": "Nouveau Nom",
                                            "telephone": "778765432",
                                            "ville": "Thiès"
                                        }
                                        """)
                )
                .andExpect(status().isOk());

        verify(userProfileService)
                .updateMyProfile(any(UpdateProfileRequest.class));
    }

    @Test
    void shouldRejectInvalidUpdateProfileRequest() throws Exception {

        String invalidName = "A".repeat(101);

        String json = """
            {
                "nom": "%s",
                "telephone": "778765432",
                "ville": "Dakar"
            }
            """.formatted(invalidName);

        mockMvc.perform(
                        put("/api/users/me")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(json)
                )
                .andExpect(status().isBadRequest());

        verify(userProfileService, never())
                .updateMyProfile(any(UpdateProfileRequest.class));
    }

    @Test
    void shouldGetPublicProfile() throws Exception {

        UserProfileResponse response = new UserProfileResponse(
                2L,
                "Moussa",
                "770000000",
                "Dakar",
                true,
                Role.UTILISATEUR,
                OffsetDateTime.now()
        );

        when(userProfileService.getPublicProfile(2L))
                .thenReturn(response);

        mockMvc.perform(
                        get("/api/users/2")
                )
                .andExpect(status().isOk());

        verify(userProfileService)
                .getPublicProfile(2L);
    }

    @Test
    void shouldSuspendUser() throws Exception {

        doNothing()
                .when(userProfileService)
                .suspendUser(2L);

        mockMvc.perform(
                        patch("/api/users/2/suspend")
                )
                .andExpect(status().isNoContent());

        verify(userProfileService)
                .suspendUser(2L);
    }

    @Test
    void shouldReactivateUser() throws Exception {

        doNothing()
                .when(userProfileService)
                .reactivateUser(2L);

        mockMvc.perform(
                        patch("/api/users/2/reactivate")
                )
                .andExpect(status().isNoContent());

        verify(userProfileService)
                .reactivateUser(2L);
    }

    @Test
    void shouldDeleteMyAccount() throws Exception {

        doNothing().when(userProfileService).deleteMyAccount();

        mockMvc.perform(delete("/api/users/me"))
                .andExpect(status().isNoContent());

        verify(userProfileService).deleteMyAccount();
    }

    @Test
    void shouldUpdateProfessionalStatus() throws Exception {

        doNothing()
                .when(userProfileService)
                .updateProfessionalStatus(2L, true);

        mockMvc.perform(
                        patch("/api/users/2/professional")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("""
                                    {
                                        "estProfessionnel": true
                                    }
                                    """)
                )
                .andExpect(status().isNoContent());

        verify(userProfileService)
                .updateProfessionalStatus(2L, true);
    }

    @Test
    void shouldRejectInvalidProfessionalStatus() throws Exception {

        mockMvc.perform(
                        patch("/api/users/2/professional")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("""
                                    {}
                                    """)
                )
                .andExpect(status().isBadRequest());

        verify(userProfileService, never())
                .updateProfessionalStatus(anyLong(), anyBoolean());
    }

    @Test
    void shouldFindAllUsers() throws Exception {

        AdminUserResponse user1 = new AdminUserResponse(
                1L,
                "Moussa",
                "moussa@example.com",
                "770000000",
                "Dakar",
                false,
                false,
                Role.UTILISATEUR,
                OffsetDateTime.now(),
                OffsetDateTime.now()
        );

        AdminUserResponse user2 = new AdminUserResponse(
                2L,
                "Fatou",
                "fatou@example.com",
                "778888888",
                "Thiès",
                true,
                false,
                Role.UTILISATEUR,
                OffsetDateTime.now(),
                OffsetDateTime.now()
        );

        when(userProfileService.findAllUsers())
                .thenReturn(List.of(user1, user2));

        mockMvc.perform(
                        get("/api/users")
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].nom").value("Moussa"))
                .andExpect(jsonPath("$[0].email").value("moussa@example.com"))
                .andExpect(jsonPath("$[0].estProfessionnel").value(false))
                .andExpect(jsonPath("$[1].id").value(2))
                .andExpect(jsonPath("$[1].nom").value("Fatou"))
                .andExpect(jsonPath("$[1].estProfessionnel").value(true));

        verify(userProfileService)
                .findAllUsers();
    }

    @Test
    void shouldReturnEmptyUserList() throws Exception {

        when(userProfileService.findAllUsers())
                .thenReturn(List.of());

        mockMvc.perform(
                        get("/api/users")
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isEmpty());

        verify(userProfileService)
                .findAllUsers();
    }




}