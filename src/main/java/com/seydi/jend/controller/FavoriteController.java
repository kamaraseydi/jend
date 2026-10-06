package com.seydi.jend.controller;

import com.seydi.jend.dto.response.AnnonceResponse;
import com.seydi.jend.dto.response.FavoriteResponse;
import com.seydi.jend.service.FavoriteService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/favoris")
@RequiredArgsConstructor
public class FavoriteController {

    private final FavoriteService favoriteService;

    @GetMapping
    public List<AnnonceResponse> findMyFavorites() {
        return favoriteService.findMyFavorites();
    }

    @PostMapping("/{annonceId}")
    @ResponseStatus(HttpStatus.CREATED)
    public FavoriteResponse addFavorite(
            @PathVariable Long annonceId
    ) {
        return favoriteService.addFavorite(annonceId);
    }

    @DeleteMapping("/{annonceId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void removeFavorite(
            @PathVariable Long annonceId
    ) {
        favoriteService.removeFavorite(annonceId);
    }
}