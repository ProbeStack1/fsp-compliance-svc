package com.probestack.forgesphere.scanner.microservice;

import java.util.Locale;
import java.util.Optional;
import org.springframework.stereotype.Component;

@Component
class AccessTokenResolver {

    Optional<String> resolve(String accessTokenRef) {
        if (accessTokenRef == null || accessTokenRef.isBlank()) {
            return Optional.empty();
        }

        String key = accessTokenRef.startsWith("env:")
                ? accessTokenRef.substring("env:".length())
                : accessTokenRef;
        String sanitizedKey = key.toUpperCase(Locale.ROOT).replaceAll("[^A-Z0-9_]", "_");

        return lookup(key)
                .or(() -> lookup(sanitizedKey))
                .or(() -> lookup("COMPLIANCE_SCAN_TOKEN_" + sanitizedKey));
    }

    private Optional<String> lookup(String key) {
        String value = System.getenv(key);
        return value == null || value.isBlank() ? Optional.empty() : Optional.of(value);
    }
}
