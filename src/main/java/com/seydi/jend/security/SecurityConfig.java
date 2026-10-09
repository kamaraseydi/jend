package com.seydi.jend.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    SecurityFilterChain securityFilterChain(HttpSecurity http)
            throws Exception {

        http
                .csrf(csrf -> csrf.disable())

                .sessionManagement(session ->
                        session.sessionCreationPolicy(
                                SessionCreationPolicy.STATELESS
                        )
                )

                .authorizeHttpRequests(auth -> auth

                        // Mes annonces : authentification obligatoire
                        .requestMatchers(
                                HttpMethod.GET, "/api/annonces/me"
                        ).authenticated()

                        // Consultation publique des annonces
                        .requestMatchers(
                                HttpMethod.GET, "/api/annonces",
                                "/api/annonces/*"
                        ).permitAll()

                        // Mon profil : authentification obligatoire
                        .requestMatchers(
                                "/api/users/me"
                        ).authenticated()

                        // Liste des utilisateurs : authentification obligatoire
                        .requestMatchers(
                                HttpMethod.GET, "/api/users"
                        ).authenticated()

                        // Profil public d'un utilisateur
                        .requestMatchers(
                                HttpMethod.GET, "/api/users/*"
                        ).permitAll()

                        // Consultation publique des catégories
                        .requestMatchers(
                                HttpMethod.GET, "/api/categories",
                                "/api/categories/*"
                        ).permitAll()

                        // Toutes les autres routes sont protégées
                        .anyRequest().authenticated()

                )

                .oauth2ResourceServer(oauth2 ->
                        oauth2.jwt(jwt -> {})
                );

        return http.build();
    }
}
