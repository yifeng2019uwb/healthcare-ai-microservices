package com.healthcare.client;

import com.healthcare.exception.InternalException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.Map;

/**
 * Client for integrating with Supabase Auth API
 */
@Component
public class SupabaseAuthClient {

    private final WebClient webClient;
    private final String supabaseUrl;
    private final String supabaseKey;

    public SupabaseAuthClient(
            @Value("${supabase.url}") String supabaseUrl,
            @Value("${supabase.anon.key}") String supabaseKey,
            WebClient.Builder webClientBuilder) {
        this.supabaseUrl = supabaseUrl;
        this.supabaseKey = supabaseKey;
        this.webClient = webClientBuilder
                .baseUrl(supabaseUrl + "/auth/v1")
                .defaultHeader("apikey", supabaseKey)
                .defaultHeader("Content-Type", "application/json")
                .build();
    }

    /**
     * Create a new user account in Supabase Auth
     *
     * @param email user email
     * @param password user password
     * @return Supabase user response with user ID
     */
    public Mono<SupabaseUserResponse> createUser(String email, String password) {
        Map<String, Object> request = Map.of(
                "email", email,
                "password", password,
                "email_confirm", false // Skip email confirmation for MVP
        );

        return webClient.post()
                .uri("/signup")
                .bodyValue(request)
                .retrieve()
                .onStatus(status -> status.is4xxClientError(),
                    response -> response.bodyToMono(String.class)
                            .map(body -> new InternalException("Supabase Auth error: " + body)))
                .onStatus(status -> status.is5xxServerError(),
                    response -> response.bodyToMono(String.class)
                            .map(body -> new InternalException("Supabase Auth server error: " + body)))
                .bodyToMono(SupabaseUserResponse.class);
    }

    /**
     * Response structure from Supabase Auth API
     */
    public static class SupabaseUserResponse {
        private SupabaseUser user;
        private String access_token;
        private String refresh_token;

        // Getters and Setters
        public SupabaseUser getUser() {
            return user;
        }

        public void setUser(SupabaseUser user) {
            this.user = user;
        }

        public String getAccess_token() {
            return access_token;
        }

        public void setAccess_token(String access_token) {
            this.access_token = access_token;
        }

        public String getRefresh_token() {
            return refresh_token;
        }

        public void setRefresh_token(String refresh_token) {
            this.refresh_token = refresh_token;
        }
    }

    /**
     * Supabase user structure
     */
    public static class SupabaseUser {
        private String id;
        private String email;
        private String created_at;

        // Getters and Setters
        public String getId() {
            return id;
        }

        public void setId(String id) {
            this.id = id;
        }

        public String getEmail() {
            return email;
        }

        public void setEmail(String email) {
            this.email = email;
        }

        public String getCreated_at() {
            return created_at;
        }

        public void setCreated_at(String created_at) {
            this.created_at = created_at;
        }
    }
}

