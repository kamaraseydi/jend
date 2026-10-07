package com.seydi.jend.repository;

import com.seydi.jend.entity.Annonce;
import com.seydi.jend.entity.StatutAnnonce;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.List;

public interface AnnonceRepository extends JpaRepository<Annonce, Long>, JpaSpecificationExecutor<Annonce> {

    boolean existsByCategoryId(Long categoryId);

    List<Annonce> findByVendeurIdAndStatut(
            Long vendeurId,
            StatutAnnonce statut
    );

    List<Annonce> findByVendeurId(Long vendeurId);
}