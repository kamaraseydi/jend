package com.seydi.jend.controller;

import com.seydi.jend.dto.request.AddAnnonceImageRequest;
import com.seydi.jend.dto.response.AnnonceImageResponse;
import com.seydi.jend.service.AnnonceImageService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/annonces/{annonceId}/images")
@RequiredArgsConstructor
public class AnnonceImageController {

    private final AnnonceImageService annonceImageService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public AnnonceImageResponse addImage(
            @PathVariable Long annonceId,
            @Valid @RequestBody AddAnnonceImageRequest request
    ) {
        return annonceImageService.addImage(annonceId, request);
    }

    @GetMapping
    public List<AnnonceImageResponse> findImages(
            @PathVariable Long annonceId
    ) {
        return annonceImageService.findImages(annonceId);
    }

    @DeleteMapping("/{imageId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteImage(
            @PathVariable Long annonceId,
            @PathVariable Long imageId
    ) {
        annonceImageService.deleteImage(annonceId, imageId);
    }
}