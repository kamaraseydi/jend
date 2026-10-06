package com.seydi.jend.controller;

import com.seydi.jend.dto.response.AnnonceResponse;
import com.seydi.jend.dto.response.FavoriteResponse;
import com.seydi.jend.service.FavoriteService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;

import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(FavoriteController.class)
@AutoConfigureMockMvc(addFilters = false)
class FavoriteControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private FavoriteService favoriteService;

    @Test
    void shouldFindMyFavorites() throws Exception {

        when(favoriteService.findMyFavorites())
                .thenReturn(List.of());

        mockMvc.perform(get("/api/favoris"))
                .andExpect(status().isOk());

        verify(favoriteService).findMyFavorites();
    }

    @Test
    void shouldAddFavorite() throws Exception {

        FavoriteResponse response = new FavoriteResponse(
                10L,
                OffsetDateTime.now()
        );

        when(favoriteService.addFavorite(10L))
                .thenReturn(response);

        mockMvc.perform(
                        post("/api/favoris/10")
                                .contentType(MediaType.APPLICATION_JSON)
                )
                .andExpect(status().isCreated());

        verify(favoriteService).addFavorite(10L);
    }

    @Test
    void shouldRemoveFavorite() throws Exception {

        doNothing()
                .when(favoriteService)
                .removeFavorite(10L);

        mockMvc.perform(
                        delete("/api/favoris/10")
                )
                .andExpect(status().isNoContent());

        verify(favoriteService).removeFavorite(10L);
    }
}