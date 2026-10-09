package com.seydi.jend.security;

import com.seydi.jend.controller.AnnonceController;
import com.seydi.jend.controller.UserProfileController;
import com.seydi.jend.service.AnnonceService;
import com.seydi.jend.service.UserProfileService;

import org.junit.jupiter.api.Test;
import com.seydi.jend.security.CorsConfig;

import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.options;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = {
        AnnonceController.class,
        UserProfileController.class
})
@Import({SecurityConfig.class, CorsConfig.class})
class SecurityConfigTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private AnnonceService annonceService;

    @MockitoBean
    private UserProfileService userProfileService;

    @MockitoBean
    private JwtDecoder jwtDecoder;

    @Test
    void shouldRejectAnonymousRequestToMyAnnonces() throws Exception {
        mockMvc.perform(get("/api/annonces/me"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void shouldRejectAnonymousRequestToMyProfile() throws Exception {
        mockMvc.perform(get("/api/users/me"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void shouldRejectAnonymousRequestToUsersList() throws Exception {
        mockMvc.perform(get("/api/users"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void shouldAllowAnonymousRequestToPublicAnnonces() throws Exception {
        mockMvc.perform(get("/api/annonces")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }

    @Test
    void shouldAllowAnonymousRequestToPublicAnnonceDetails()
            throws Exception {
        mockMvc.perform(get("/api/annonces/123")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }

    @Test
    void shouldAllowAnonymousRequestToPublicUserProfile()
            throws Exception {
        mockMvc.perform(get("/api/users/123")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }

    @Test
    void shouldAllowCorsPreflightFromConfiguredFrontend()
            throws Exception {

        mockMvc.perform(options("/api/annonces")
                        .header(
                                "Origin",
                                "http://localhost:5173"
                        )
                        .header(
                                "Access-Control-Request-Method",
                                "GET"
                        )
                        .header(
                                "Access-Control-Request-Headers",
                                "Authorization, Content-Type"
                        ))
                .andExpect(status().isOk())
                .andExpect(header().string(
                        "Access-Control-Allow-Origin",
                        "http://localhost:5173"
                ))
                .andExpect(header().string(
                        "Access-Control-Allow-Methods",
                        containsString("GET")
                ));
    }
}