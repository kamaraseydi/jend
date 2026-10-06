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
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.OffsetDateTime;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

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
}