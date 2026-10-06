package com.seydi.jend.controller;

import com.seydi.jend.dto.request.CreateAnnonceRequest;
import com.seydi.jend.dto.response.AnnonceResponse;
import com.seydi.jend.entity.EtatAnnonce;
import com.seydi.jend.entity.StatutAnnonce;
import com.seydi.jend.service.AnnonceService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import com.seydi.jend.dto.request.CreateAnnonceRequest;
import com.seydi.jend.dto.request.UpdateAnnonceRequest;
import com.seydi.jend.dto.response.PageResponse;
import com.seydi.jend.controller.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.math.BigDecimal;

import static org.mockito.ArgumentMatchers.any;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
@WebMvcTest(AnnonceController.class)
@AutoConfigureMockMvc(addFilters = false)
class AnnonceControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private AnnonceService annonceService;

    @Test
    void shouldPublishAnnonce() throws Exception {

        AnnonceResponse response = new AnnonceResponse(
                1L,
                "iPhone 15",
                "Téléphone en excellent état",
                new BigDecimal("450000"),
                EtatAnnonce.TRES_BON_ETAT,
                StatutAnnonce.PUBLIEE,
                "Dakar",
                "Almadies",
                10L,
                "Seydi",
                2L,
                "Téléphones",
                null,
                null
        );

        when(annonceService.publish(1L))
                .thenReturn(response);

        mockMvc.perform(
                        patch("/api/annonces/1/publish")
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.titre").value("iPhone 15"))
                .andExpect(jsonPath("$.statut").value("PUBLIEE"));

        verify(annonceService).publish(1L);
    }

    @Test
    void shouldPauseAnnonce() throws Exception {

        AnnonceResponse response = new AnnonceResponse(
                1L,
                "iPhone 15",
                "Téléphone en excellent état",
                new BigDecimal("450000"),
                EtatAnnonce.TRES_BON_ETAT,
                StatutAnnonce.SUSPENDUE,
                "Dakar",
                "Almadies",
                10L,
                "Seydi",
                2L,
                "Téléphones",
                null,
                null
        );

        when(annonceService.pause(1L))
                .thenReturn(response);

        mockMvc.perform(
                        patch("/api/annonces/1/pause")
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.statut").value("SUSPENDUE"));

        verify(annonceService).pause(1L);
    }

    @Test
    void shouldMarkAnnonceAsSold() throws Exception {

        AnnonceResponse response = new AnnonceResponse(
                1L,
                "iPhone 15",
                "Téléphone en excellent état",
                new BigDecimal("450000"),
                EtatAnnonce.TRES_BON_ETAT,
                StatutAnnonce.VENDUE,
                "Dakar",
                "Almadies",
                10L,
                "Seydi",
                2L,
                "Téléphones",
                null,
                null
        );

        when(annonceService.sold(1L))
                .thenReturn(response);

        mockMvc.perform(
                        patch("/api/annonces/1/sold")
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.statut").value("VENDUE"));

        verify(annonceService).sold(1L);
    }

    @Test
    void shouldCreateAnnonce() throws Exception {

        AnnonceResponse response = new AnnonceResponse(
                1L,
                "iPhone 15",
                "Téléphone en excellent état",
                new BigDecimal("450000"),
                EtatAnnonce.TRES_BON_ETAT,
                StatutAnnonce.BROUILLON,
                "Dakar",
                "Almadies",
                10L,
                "Seydi",
                2L,
                "Téléphones",
                null,
                null
        );

        when(annonceService.create(any(CreateAnnonceRequest.class)))
                .thenReturn(response);

        String request = """
            {
                "titre": "iPhone 15",
                "description": "Téléphone en excellent état",
                "prix": 450000,
                "etat": "TRES_BON_ETAT",
                "ville": "Dakar",
                "quartier": "Almadies",
                "categoryId": 2
            }
            """;

        mockMvc.perform(
                        post("/api/annonces")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(request)
                )
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.titre").value("iPhone 15"))
                .andExpect(jsonPath("$.statut").value("BROUILLON"));

        verify(annonceService).create(any(CreateAnnonceRequest.class));
    }

    @Test
    void shouldFindAnnonceById() throws Exception {

        AnnonceResponse response = new AnnonceResponse(
                1L,
                "iPhone 15",
                "Téléphone en excellent état",
                new BigDecimal("450000"),
                EtatAnnonce.TRES_BON_ETAT,
                StatutAnnonce.PUBLIEE,
                "Dakar",
                "Almadies",
                10L,
                "Seydi",
                2L,
                "Téléphones",
                null,
                null
        );

        when(annonceService.findById(1L))
                .thenReturn(response);

        mockMvc.perform(
                        get("/api/annonces/1")
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.titre").value("iPhone 15"))
                .andExpect(jsonPath("$.statut").value("PUBLIEE"));

        verify(annonceService).findById(1L);
    }

    @Test
    void shouldFindAllAnnonces() throws Exception {

        PageResponse<AnnonceResponse> response = new PageResponse<>(
                java.util.List.of(),
                0,
                20,
                0,
                0
        );

        when(annonceService.findAll(
                "iphone",
                "Dakar",
                2L,
                new BigDecimal("100000"),
                new BigDecimal("500000"),
                EtatAnnonce.TRES_BON_ETAT,
                0,
                20
        )).thenReturn(response);

        mockMvc.perform(
                        get("/api/annonces")
                                .param("search", "iphone")
                                .param("city", "Dakar")
                                .param("category", "2")
                                .param("minPrice", "100000")
                                .param("maxPrice", "500000")
                                .param("etat", "TRES_BON_ETAT")
                                .param("page", "0")
                                .param("size", "20")
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.page").value(0))
                .andExpect(jsonPath("$.size").value(20))
                .andExpect(jsonPath("$.totalElements").value(0))
                .andExpect(jsonPath("$.content").isArray());

        verify(annonceService).findAll(
                "iphone",
                "Dakar",
                2L,
                new BigDecimal("100000"),
                new BigDecimal("500000"),
                EtatAnnonce.TRES_BON_ETAT,
                0,
                20
        );
    }

    @Test
    void shouldFindMyAnnonces() throws Exception {

        PageResponse<AnnonceResponse> response = new PageResponse<>(
                java.util.List.of(),
                0,
                20,
                0,
                0
        );

        when(annonceService.findMyAnnonces(
                StatutAnnonce.PUBLIEE,
                0,
                20
        )).thenReturn(response);

        mockMvc.perform(
                        get("/api/annonces/me")
                                .param("statut", "PUBLIEE")
                                .param("page", "0")
                                .param("size", "20")
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.page").value(0))
                .andExpect(jsonPath("$.size").value(20))
                .andExpect(jsonPath("$.content").isArray());

        verify(annonceService).findMyAnnonces(
                StatutAnnonce.PUBLIEE,
                0,
                20
        );
    }

    @Test
    void shouldUpdateAnnonce() throws Exception {

        AnnonceResponse response = new AnnonceResponse(
                1L,
                "iPhone 15 Pro",
                "Téléphone comme neuf",
                new BigDecimal("500000"),
                EtatAnnonce.COMME_NEUF,
                StatutAnnonce.BROUILLON,
                "Dakar",
                "Almadies",
                10L,
                "Seydi",
                2L,
                "Téléphones",
                null,
                null
        );

        when(annonceService.update(
                eq(1L),
                any(UpdateAnnonceRequest.class)
        )).thenReturn(response);

        String request = """
            {
                "titre": "iPhone 15 Pro",
                "description": "Téléphone comme neuf",
                "prix": 500000,
                "etat": "COMME_NEUF",
                "ville": "Dakar",
                "quartier": "Almadies",
                "categoryId": 2
            }
            """;

        mockMvc.perform(
                        put("/api/annonces/1")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(request)
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.titre").value("iPhone 15 Pro"))
                .andExpect(jsonPath("$.prix").value(500000))
                .andExpect(jsonPath("$.statut").value("BROUILLON"));

        verify(annonceService).update(
                eq(1L),
                any(UpdateAnnonceRequest.class)
        );
    }

    @Test
    void shouldRejectInvalidCreateAnnonceRequest() throws Exception {

        String request = """
            {
                "titre": "",
                "description": "",
                "prix": -100,
                "etat": null,
                "ville": "",
                "categoryId": null
            }
            """;

        mockMvc.perform(
                        post("/api/annonces")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(request)
                )
                .andExpect(status().isBadRequest());

        verify(annonceService, org.mockito.Mockito.never())
                .create(any(CreateAnnonceRequest.class));
    }

    @Test
    void shouldRejectInvalidUpdateAnnonceRequest() throws Exception {

        String request = """
            {
                "titre": "",
                "description": "",
                "prix": -100,
                "etat": null,
                "ville": "",
                "categoryId": null
            }
            """;

        mockMvc.perform(
                        put("/api/annonces/1")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(request)
                )
                .andExpect(status().isBadRequest());

        verify(annonceService, org.mockito.Mockito.never())
                .update(
                        eq(1L),
                        any(UpdateAnnonceRequest.class)
                );
    }

    @Test
    void shouldDeleteAnnonce() throws Exception {

        doNothing().when(annonceService).delete(10L);

        mockMvc.perform(delete("/api/annonces/10"))
                .andExpect(status().isNoContent());

        verify(annonceService).delete(10L);
    }


}