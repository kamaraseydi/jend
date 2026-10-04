package com.seydi.jend.controller;

import com.seydi.jend.dto.request.CreateAnnonceRequest;
import com.seydi.jend.dto.response.AnnonceResponse;
import com.seydi.jend.entity.EtatAnnonce;
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
}