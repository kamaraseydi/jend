package com.seydi.jend.repository;

import com.seydi.jend.entity.Favorite;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface FavoriteRepository extends JpaRepository<Favorite, Long> {

    boolean existsByUserIdAndAnnonceId(Long userId, Long annonceId);

    Optional<Favorite> findByUserIdAndAnnonceId(
            Long userId,
            Long annonceId
    );

    List<Favorite> findByUserId(Long userId);

    void deleteByAnnonceId(Long annonceId);
}