package com.seydi.jend.repository;

import com.seydi.jend.entity.AnnonceImage;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AnnonceImageRepository extends JpaRepository<AnnonceImage, Long> {

    long countByAnnonceId(Long annonceId);

    boolean existsByAnnonceIdAndOrdre(Long annonceId, Integer ordre);

    List<AnnonceImage> findByAnnonceIdOrderByOrdreAsc(Long annonceId);
}