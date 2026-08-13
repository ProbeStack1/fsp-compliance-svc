package com.probestack.forgesphere.service;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.probestack.forgesphere.model.AssetType;
import com.sun.net.httpserver.HttpServer;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.atomic.AtomicReference;
import org.junit.jupiter.api.Test;

class OnboardingResourceResolverTest {

    @Test
    void sendsConfiguredIdentityHeadersWhenResolvingResource() throws Exception {
        AtomicReference<String> receivedEmail = new AtomicReference<>();
        AtomicReference<String> receivedRole = new AtomicReference<>();
        HttpServer server = HttpServer.create(new InetSocketAddress(0), 0);
        server.createContext("/", exchange -> {
            receivedEmail.set(exchange.getRequestHeaders().getFirst("X-User-Email"));
            receivedRole.set(exchange.getRequestHeaders().getFirst("X-User-Role"));
            byte[] response = ("{\"data\":{"
                    + "\"id\":\"6a7d9d5ba6ecca344572b551\","
                    + "\"applicationId\":\"payment-app\","
                    + "\"apiName\":\"Payment Processing Service\","
                    + "\"codeGenResults\":[{\"archiveDownloadUrl\":\"https://example.test/source.zip\"}]"
                    + "}}").getBytes(StandardCharsets.UTF_8);
            exchange.getResponseHeaders().add("Content-Type", "application/json");
            exchange.sendResponseHeaders(200, response.length);
            exchange.getResponseBody().write(response);
            exchange.close();
        });
        server.start();

        try {
            OnboardingResourceResolver resolver = new OnboardingResourceResolver(
                    "http://localhost:" + server.getAddress().getPort(),
                    "system@forgesphere.probestack.io",
                    "ORG_ADMIN",
                    new ObjectMapper());

            OnboardingResourceResolver.ResolvedOnboardingResource resource =
                    resolver.resolve(AssetType.MICROSERVICE, "6a7d9d5ba6ecca344572b551");

            assertEquals("system@forgesphere.probestack.io", receivedEmail.get());
            assertEquals("ORG_ADMIN", receivedRole.get());
            assertEquals("Payment Processing Service", resource.resourceName());
            assertEquals("https://example.test/source.zip", resource.source().getArchiveDownloadUrl());
        } finally {
            server.stop(0);
        }
    }
}
