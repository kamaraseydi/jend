package com.seydi.jend.security;

import com.seydi.jend.entity.UserProfile;
import com.seydi.jend.repository.UserProfileRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CurrentUserService {

    private final UserProfileRepository userProfileRepository;

    public UserProfile getCurrentUser() {

        Jwt jwt = (Jwt) SecurityContextHolder
                .getContext()
                .getAuthentication()
                .getPrincipal();

        String supabaseUserId = jwt.getSubject();

        return userProfileRepository
                .findBySupabaseUserId(supabaseUserId)
                .orElseThrow(() ->
                        new IllegalStateException(
                                "Utilisateur Jënd introuvable"
                        )
                );
    }
}