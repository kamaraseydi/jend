package com.seydi.jend.controller;

import com.seydi.jend.dto.request.CreateAnnonceRequest;
import com.seydi.jend.dto.request.UpdateAnnonceRequest;
import com.seydi.jend.dto.response.AnnonceResponse;
import com.seydi.jend.entity.EtatAnnonce;
import com.seydi.jend.entity.StatutAnnonce;
import com.seydi.jend.service.AnnonceService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import com.seydi.jend.dto.response.PageResponse;
import org.springframework.web.bind.annotation.RequestParam;

import java.math.BigDecimal;

@RestController
@RequestMapping("/api/annonces")
@RequiredArgsConstructor
public class AnnonceController {

    private final AnnonceService annonceService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public AnnonceResponse create(
            @Valid @RequestBody CreateAnnonceRequest request) {

        return annonceService.create(request);
    }

    @GetMapping("/{id}")
    public AnnonceResponse findById(@PathVariable Long id) {
        return annonceService.findById(id);
    }

    @GetMapping
    public PageResponse<AnnonceResponse> findAll(
            @RequestParam(required = false) String search,
            @RequestParam(required = false) String city,
            @RequestParam(required = false) Long category,
            @RequestParam(required = false) BigDecimal minPrice,
            @RequestParam(required = false) BigDecimal maxPrice,
            @RequestParam(required = false) EtatAnnonce etat,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {

        return annonceService.findAll(
                search,
                city,
                category,
                minPrice,
                maxPrice,
                etat,
                page,
                size
        );
    }

    @GetMapping("/me")
    public PageResponse<AnnonceResponse> findMyAnnonces(
            @RequestParam(required = false) StatutAnnonce statut,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        return annonceService.findMyAnnonces(
                statut,
                page,
                size
        );
    }

    @PutMapping("/{id}")
    public AnnonceResponse update(
            @PathVariable Long id,
            @Valid @RequestBody UpdateAnnonceRequest request
    ) {
        return annonceService.update(id, request);
    }

    @PatchMapping("/{id}/publish")
    public AnnonceResponse publish(@PathVariable Long id) {
        return annonceService.publish(id);
    }

    @PatchMapping("/{id}/pause")
    public AnnonceResponse pause(@PathVariable Long id) {
        return annonceService.pause(id);
    }

    @PatchMapping("/{id}/sold")
    public AnnonceResponse sold(@PathVariable Long id) {
        return annonceService.sold(id);
    }


}