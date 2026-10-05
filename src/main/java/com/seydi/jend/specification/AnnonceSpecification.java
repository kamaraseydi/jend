package com.seydi.jend.specification;

import com.seydi.jend.entity.Annonce;
import com.seydi.jend.entity.EtatAnnonce;
import com.seydi.jend.entity.StatutAnnonce;
import org.springframework.data.jpa.domain.Specification;

import java.math.BigDecimal;

public final class AnnonceSpecification {

    private AnnonceSpecification() {
    }

    public static Specification<Annonce> estPubliee() {
        return (root, query, criteriaBuilder) ->
                criteriaBuilder.equal(
                        root.get("statut"),
                        StatutAnnonce.PUBLIEE
                );
    }

    public static Specification<Annonce> parVille(String ville) {
        return (root, query, criteriaBuilder) ->
                criteriaBuilder.equal(
                        criteriaBuilder.lower(root.get("ville")),
                        ville.toLowerCase()
                );
    }

    public static Specification<Annonce> parCategorie(Long categoryId) {
        return (root, query, criteriaBuilder) ->
                criteriaBuilder.equal(
                        root.get("category").get("id"),
                        categoryId
                );
    }

    public static Specification<Annonce> prixSuperieurOuEgal(BigDecimal minPrice) {
        return (root, query, criteriaBuilder) ->
                criteriaBuilder.greaterThanOrEqualTo(
                        root.get("prix"),
                        minPrice
                );
    }

    public static Specification<Annonce> prixInferieurOuEgal(BigDecimal maxPrice) {
        return (root, query, criteriaBuilder) ->
                criteriaBuilder.lessThanOrEqualTo(
                        root.get("prix"),
                        maxPrice
                );
    }

    public static Specification<Annonce> parEtat(EtatAnnonce etat) {
        return (root, query, criteriaBuilder) ->
                criteriaBuilder.equal(
                        root.get("etat"),
                        etat
                );
    }

    public static Specification<Annonce> contientTexte(String search) {

        return (root, query, criteriaBuilder) -> {

            String pattern = "%" + search.toLowerCase() + "%";

            return criteriaBuilder.or(
                    criteriaBuilder.like(
                            criteriaBuilder.lower(root.get("titre")),
                            pattern
                    ),
                    criteriaBuilder.like(
                            criteriaBuilder.lower(root.get("description")),
                            pattern
                    )
            );
        };
    }


}