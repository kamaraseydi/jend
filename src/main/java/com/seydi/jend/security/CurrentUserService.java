package com.seydi.jend.security;

import com.seydi.jend.entity.UserProfile;
import com.seydi.jend.repository.UserProfileRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationCredentialsNotFoundException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CurrentUserService {

    private final UserProfileRepository userProfileRepository;

    public UserProfile getCurrentUser() {

        Authentication authentication = SecurityContextHolder
                .getContext()
                .getAuthentication();

        if (authentication == null
                || !authentication.isAuthenticated()
                || !(authentication.getPrincipal() instanceof Jwt jwt)) {

            throw new AuthenticationCredentialsNotFoundException(
                    "Authentification requise"
            );
        }

        String supabaseUserId = jwt.getSubject();

        return userProfileRepository
                .findBySupabaseUserId(supabaseUserId)
                .orElseThrow(() ->
                        new AccessDeniedException(
                                "Aucun profil Jënd associé à cet utilisateur"
                        )
                );
    }
}