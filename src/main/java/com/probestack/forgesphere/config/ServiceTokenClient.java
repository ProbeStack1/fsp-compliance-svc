package com.probestack.forgesphere.config;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.Instant;
import java.util.Base64;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * Obtains a short-lived {@code probestack_service_access} token from ps-token-issuer-svc so this
 * service can authenticate itself into a peer backend (fsp-onboarding-svc) instead of presenting a
 * spoofable {@code X-User-Email: system@…} header. Cached until ~30s before expiry.
 * <p>
 * Disabled by default ({@code forge.service-token.enabled=false}) — the caller falls back to its
 * prior header behaviour until the issuer client is provisioned and the receiver enables
 * service-token validation.
 */
@Component
public class ServiceTokenClient {

    private static final Logger log = LoggerFactory.getLogger(ServiceTokenClient.class);
    private static final Duration REFRESH_SKEW = Duration.ofSeconds(30);

    private final boolean enabled;
    private final String tokenUrl;
    private final String clientId;
    private final String clientSecret;
    private final String audience;
    private final String organizationId;
    private final String scope;
    private final ObjectMapper objectMapper;
    private final HttpClient httpClient;

    private volatile String cachedToken;
    private volatile Instant cachedExpiry = Instant.EPOCH;

    public ServiceTokenClient(
            @Value("${forge.service-token.enabled:false}") boolean enabled,
            @Value("${forge.service-token.token-url:https://probestack.io/token-issuer-api/api/v1/service-tokens}") String tokenUrl,
            @Value("${forge.service-token.client-id:}") String clientId,
            @Value("${forge.service-token.client-secret:}") String clientSecret,
            @Value("${forge.service-token.audience:probestack-api}") String audience,
            @Value("${forge.service-token.organization-id:}") String organizationId,
            @Value("${forge.service-token.scope:onboarding:applications:read}") String scope,
            ObjectMapper objectMapper) {
        this.enabled = enabled;
        this.tokenUrl = tokenUrl;
        this.clientId = clientId;
        this.clientSecret = clientSecret;
        this.audience = audience;
        this.organizationId = organizationId;
        this.scope = scope;
        this.objectMapper = objectMapper;
        this.httpClient = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(5)).build();
    }

    public boolean isEnabled() {
        return enabled
                && hasText(tokenUrl) && hasText(clientId) && hasText(clientSecret) && hasText(organizationId);
    }

    /** A currently-valid service access token, minting a fresh one only when the cache is stale. */
    public synchronized String getToken() {
        if (cachedToken != null && Instant.now().isBefore(cachedExpiry.minus(REFRESH_SKEW))) {
            return cachedToken;
        }
        String form = "grant_type=client_credentials"
                + "&audience=" + enc(audience)
                + "&organization_id=" + enc(organizationId)
                + "&scope=" + enc(scope);
        String basic = Base64.getEncoder().encodeToString(
                (clientId + ":" + clientSecret).getBytes(StandardCharsets.UTF_8));
        HttpRequest request = HttpRequest.newBuilder(URI.create(tokenUrl))
                .timeout(Duration.ofSeconds(10))
                .header("Authorization", "Basic " + basic)
                .header("Content-Type", "application/x-www-form-urlencoded")
                .POST(HttpRequest.BodyPublishers.ofString(form))
                .build();
        try {
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() != 200) {
                throw new IllegalStateException("Token issuer returned HTTP " + response.statusCode() + ": " + response.body());
            }
            JsonNode body = objectMapper.readTree(response.body());
            String token = body.path("access_token").asText(null);
            if (token == null || token.isBlank()) {
                throw new IllegalStateException("Token issuer returned no access_token");
            }
            long expiresIn = body.path("expires_in").asLong(300L);
            cachedToken = token;
            cachedExpiry = Instant.now().plusSeconds(expiresIn);
            log.info("Obtained service access token for client={} org={} scope=[{}] ttl={}s",
                    clientId, organizationId, scope, expiresIn);
            return cachedToken;
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("Interrupted while obtaining a service access token", ex);
        } catch (Exception ex) {
            throw new IllegalStateException("Unable to obtain a service access token: " + ex.getMessage(), ex);
        }
    }

    /** Drop the cached token so the next getToken() re-mints (call once after a 401). */
    public synchronized void invalidate() {
        cachedToken = null;
        cachedExpiry = Instant.EPOCH;
    }

    private static String enc(String value) {
        return URLEncoder.encode(value == null ? "" : value, StandardCharsets.UTF_8);
    }

    private static boolean hasText(String value) {
        return value != null && !value.isBlank();
    }
}
