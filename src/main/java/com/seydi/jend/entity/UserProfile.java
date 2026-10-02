package com.seydi.jend.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import java.util.ArrayList;
import java.util.List;

import java.time.OffsetDateTime;

@Entity
@Table(name = "user_profile")
@Getter
@Setter
public class UserProfile {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "supabase_user_id", nullable = false, unique = true)
    private String supabaseUserId;

    @Column(nullable = false, length = 100)
    private String nom;

    @Column(nullable = false, unique = true, length = 255)
    private String email;

    @Column(length = 30)
    private String telephone;

    @Column(length = 100)
    private String ville;

    @Column(name = "est_professionnel", nullable = false)
    private boolean estProfessionnel = false;

    @Column(nullable = false)
    private boolean suspendu = false;

    @Column(name = "deleted_at")
    private OffsetDateTime deletedAt;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private Role role;

    @Column(name = "created_at", nullable = false)
    private OffsetDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private OffsetDateTime updatedAt;

    @OneToMany(mappedBy = "vendeur")
    private List<Annonce> annonces = new ArrayList<>();

    @OneToMany(mappedBy = "user")
    private List<Favorite> favoris = new ArrayList<>();
}