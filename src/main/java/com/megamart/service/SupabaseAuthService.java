package com.megamart.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import org.springframework.web.server.ResponseStatusException;

import java.util.Map;

@Service
public class SupabaseAuthService {
    private final String supabaseUrl;
    private final String publishableKey;
    private final RestClient restClient;

    public SupabaseAuthService(@Value("${app.supabase.url:}") String supabaseUrl,
                               @Value("${app.supabase.publishable-key:}") String publishableKey,
                               RestClient.Builder builder) {
        this.supabaseUrl = supabaseUrl == null ? "" : supabaseUrl.replaceAll("/+$", "");
        this.publishableKey = publishableKey == null ? "" : publishableKey;
        this.restClient = builder.build();
    }

    public PublicConfig publicConfig() {
        if (supabaseUrl.isBlank() || publishableKey.isBlank()) {
            return new PublicConfig("", "");
        }
        return new PublicConfig(supabaseUrl, publishableKey);
    }

    @SuppressWarnings("unchecked")
    public CustomerIdentity verifyBearerToken(String authorization) {
        if (supabaseUrl.isBlank() || publishableKey.isBlank()) {
            throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE, "Customer sign-in is not configured.");
        }
        if (authorization == null || !authorization.startsWith("Bearer ") || authorization.length() < 16) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Sign in with Google to continue.");
        }
        try {
            Map<String, Object> user = restClient.get()
                    .uri(supabaseUrl + "/auth/v1/user")
                    .header("apikey", publishableKey)
                    .header(HttpHeaders.AUTHORIZATION, authorization)
                    .retrieve()
                    .body(Map.class);
            if (user == null || user.get("id") == null) {
                throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Your sign-in session is invalid.");
            }
            Map<String, Object> metadata = user.get("user_metadata") instanceof Map<?, ?> map
                    ? (Map<String, Object>) map : Map.of();
            return new CustomerIdentity(String.valueOf(user.get("id")),
                    user.get("email") == null ? "" : String.valueOf(user.get("email")),
                    metadata.get("full_name") == null ? "" : String.valueOf(metadata.get("full_name")));
        } catch (ResponseStatusException exception) {
            throw exception;
        } catch (HttpClientErrorException exception) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Your sign-in session could not be verified.");
        } catch (RestClientException exception) {
            throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE,
                    "Sign-in verification is temporarily unavailable. Please try again.");
        }
    }

    public record PublicConfig(String supabaseUrl, String publishableKey) { }
    public record CustomerIdentity(String userId, String email, String fullName) { }
}
