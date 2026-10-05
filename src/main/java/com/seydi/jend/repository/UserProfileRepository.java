package com.seydi.jend.repository;

import com.seydi.jend.entity.UserProfile;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserProfileRepository extends JpaRepository<UserProfile, Long> {
    Optional<UserProfile> findBySupabaseUserId(String supabaseUserId);
}