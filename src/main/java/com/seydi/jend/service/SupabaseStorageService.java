
package com.seydi.jend.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientResponseException;
import org.springframework.web.util.UriUtils;

import java.nio.charset.StandardCharsets;
import java.util.Map;

@Service
public class SupabaseStorageService {

    private final RestClient restClient;
    private final String supabaseUrl;
    private final String bucket;

    public SupabaseStorageService(
            @Value("${supabase.storage.url}") String supabaseUrl,
            @Value("${supabase.storage.secret-key}") String secretKey,
            @Value("${supabase.storage.bucket}") String bucket
    ) {
        this.supabaseUrl = supabaseUrl.replaceAll("/+$", "");
        this.bucket = bucket;

        this.restClient = RestClient.builder()
                .baseUrl(this.supabaseUrl + "/storage/v1")
                .defaultHeader("apikey", secretKey)
                .build();
    }

    public void upload(String path, byte[] content, String contentType) {
        String encodedPath = encodePath(path);

        try {
            restClient.post()
                    .uri("/object/" + bucket + "/" + encodedPath)
                    .contentType(MediaType.parseMediaType(contentType))
                    .header("x-upsert", "false")
                    .body(content)
                    .retrieve()
                    .toBodilessEntity();

        } catch (RestClientResponseException ex) {
            throw new IllegalStateException(
                    "Échec de l'envoi de l'image vers Supabase Storage (HTTP "
                            + ex.getStatusCode().value() + ")",
                    ex
            );
        }
    }

    public String createSignedUrl(String path) {
        String encodedPath = encodePath(path);

        try {
            Map<?, ?> response = restClient.post()
                    .uri("/object/sign/" + bucket + "/" + encodedPath)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(Map.of("expiresIn", 3600))
                    .retrieve()
                    .body(Map.class);

            if (response == null || response.get("signedURL") == null) {
                throw new IllegalStateException(
                        "Supabase n'a pas renvoyé d'URL signée"
                );
            }

            String signedPath = response.get("signedURL").toString();

            if (signedPath.startsWith("https://")
                    || signedPath.startsWith("http://")) {
                return signedPath;
            }

            if (signedPath.startsWith("/storage/v1/")) {
                return supabaseUrl + signedPath;
            }

            if (signedPath.startsWith("/")) {
                return supabaseUrl + "/storage/v1" + signedPath;
            }

            return supabaseUrl + "/storage/v1/" + signedPath;

        } catch (RestClientResponseException ex) {
            throw new IllegalStateException(
                    "Impossible de générer l'URL signée (HTTP "
                            + ex.getStatusCode().value() + ")",
                    ex
            );
        }
    }

    public void delete(String path) {
        try {
            restClient.method(HttpMethod.DELETE)
                    .uri("/object/" + bucket)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(Map.of("prefixes", new String[]{path}))
                    .retrieve()
                    .toBodilessEntity();

        } catch (RestClientResponseException ex) {
            throw new IllegalStateException(
                    "Impossible de supprimer l'image de Supabase Storage (HTTP "
                            + ex.getStatusCode().value() + ")",
                    ex
            );
        }
    }

    private String encodePath(String path) {
        String[] segments = path.split("/");
        return java.util.Arrays.stream(segments)
                .map(segment ->
                        UriUtils.encodePathSegment(
                                segment,
                                StandardCharsets.UTF_8
                        )
                )
                .collect(java.util.stream.Collectors.joining("/"));
    }
}
