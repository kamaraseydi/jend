package com.seydi.jend.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import java.util.ArrayList;
import java.util.List;
import java.math.BigDecimal;
import java.time.OffsetDateTime;

@Entity
@Table(name = "annonce")
@Getter
@Setter
public class Annonce {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 150)
    private String titre;

    @Column(nullable = false, length = 5000)
    private String description;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal prix;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private EtatAnnonce etat;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private StatutAnnonce statut;

    @Column(nullable = false, length = 100)
    private String ville;

    @Column(length = 100)
    private String quartier;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "vendeur_id", nullable = false)
    private UserProfile vendeur;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "category_id", nullable = false)
    private Category category;

    @Column(name = "created_at", nullable = false)
    private OffsetDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private OffsetDateTime updatedAt;

    @OneToMany(
            mappedBy = "annonce",
            cascade = CascadeType.ALL,
            orphanRemoval = true
    )
    private List<AnnonceImage> images = new ArrayList<>();

    @OneToMany(mappedBy = "annonce")
    private List<Favorite> favoris = new ArrayList<>();
}