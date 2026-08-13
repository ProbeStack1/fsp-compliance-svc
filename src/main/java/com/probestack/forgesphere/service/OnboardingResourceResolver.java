package com.probestack.forgesphere.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.probestack.forgesphere.model.AssetType;
import com.probestack.forgesphere.model.ScanSource;
import com.probestack.forgesphere.model.SourceType;
import java.io.IOException;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;

@Component
public class OnboardingResourceResolver {

    private static final Duration REQUEST_TIMEOUT = Duration.ofSeconds(30);
    private static final String USER_EMAIL_HEADER = "X-User-Email";
    private static final String USER_ROLE_HEADER = "X-User-Role";

    private final String onboardingBaseUrl;
    private final String onboardingUserEmail;
    private final String onboardingUserRole;
    private final ObjectMapper objectMapper;
    private final HttpClient httpClient;

    public OnboardingResourceResolver(
            @Value("${compliance.onboarding.base-url:https://forgesphere.probestack.io}") String onboardingBaseUrl,
            @Value("${compliance.onboarding.user-email:system@forgesphere.probestack.io}") String onboardingUserEmail,
            @Value("${compliance.onboarding.user-role:ORG_ADMIN}") String onboardingUserRole,
            ObjectMapper objectMapper) {
        this.onboardingBaseUrl = onboardingBaseUrl.replaceAll("/$", "");
        this.onboardingUserEmail = onboardingUserEmail;
        this.onboardingUserRole = onboardingUserRole;
        this.objectMapper = objectMapper;
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(10))
                .followRedirects(HttpClient.Redirect.NORMAL)
                .build();
    }

    public ResolvedOnboardingResource resolve(AssetType assetType, String resourceId) {
        if (isBlank(resourceId)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "resourceId is required.");
        }

        JsonNode resource = fetchOnboardingResourceById(resourceId)
                .or(() -> resolveFromOnboardingList(assetType, resourceId))
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "Resource was not found in onboarding API for id: " + resourceId));

        String archiveDownloadUrl = firstRecursiveText(resource, "archiveDownloadUrl", "sourceArchivePath", "archivePath").orElse(null);
        ScanSource source = new ScanSource();
        source.setArchiveDownloadUrl(archiveDownloadUrl);

        return new ResolvedOnboardingResource(
                firstRecursiveText(resource, "projectId", "project_id", "applicationId").orElse(null),
                firstRecursiveText(resource, "projectName", "project_name", "project", "applicationName").orElse("ProbeStack"),
                firstRecursiveText(resource, "companyName", "company_name", "tenantName", "organizationName", "businessUnit", "teamName")
                        .orElse("ProbeStack"),
                firstRecursiveText(resource, "microserviceId", "resourceId", "apigeeProxyId", "kongId", "id").orElse(resourceId),
                firstRecursiveText(resource, "microserviceName", "resourceName", "proxyName", "serviceName", "applicationName",
                        "apiName", "artifactId", "name").orElse(resourceId),
                assetType,
                SourceType.BUNDLE_UPLOAD,
                source
        );
    }

    private Optional<JsonNode> fetchOnboardingResourceById(String resourceId) {
        String url = onboardingBaseUrl + "/onboarding/v1/api/onboarding/"
                + URLEncoder.encode(resourceId, StandardCharsets.UTF_8);

        try {
            HttpResponse<String> response = sendGet(url);
            if (response.statusCode() == 404) {
                return Optional.empty();
            }
            if (response.statusCode() < 200 || response.statusCode() > 299) {
                throw new ResponseStatusException(HttpStatus.BAD_GATEWAY,
                        "Onboarding API returned HTTP status " + response.statusCode());
            }
            return Optional.of(unwrapData(objectMapper.readTree(response.body())));
        } catch (IOException ex) {
            throw new ResponseStatusException(HttpStatus.BAD_GATEWAY,
                    "Unable to read onboarding API response: " + ex.getMessage());
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
            throw new ResponseStatusException(HttpStatus.BAD_GATEWAY, "Onboarding API request was interrupted.");
        } catch (IllegalArgumentException ex) {
            throw new ResponseStatusException(HttpStatus.BAD_GATEWAY, "Invalid onboarding API URL.");
        }
    }

    private Optional<JsonNode> resolveFromOnboardingList(AssetType assetType, String resourceId) {
        JsonNode response = fetchOnboardingResources(assetType);
        return onboardingRecords(response).stream()
                .filter(node -> containsMatchingId(node, resourceId))
                .findFirst()
                .or(() -> allObjects(response).stream().filter(node -> hasDirectMatchingId(node, resourceId)).findFirst());
    }

    private JsonNode fetchOnboardingResources(AssetType assetType) {
        String projectType = switch (assetType) {
            case MICROSERVICE -> "MICROSERVICE";
            case APIGEE -> "APIGEE_PROXY";
            case KONG -> "KONG";
        };
        String url = onboardingBaseUrl + "/onboarding/v1/api/onboarding?projectType="
                + URLEncoder.encode(projectType, StandardCharsets.UTF_8);

        try {
            HttpResponse<String> response = sendGet(url);
            if (response.statusCode() < 200 || response.statusCode() > 299) {
                throw new ResponseStatusException(HttpStatus.BAD_GATEWAY,
                        "Onboarding API returned HTTP status " + response.statusCode());
            }
            return objectMapper.readTree(response.body());
        } catch (IOException ex) {
            throw new ResponseStatusException(HttpStatus.BAD_GATEWAY,
                    "Unable to read onboarding API response: " + ex.getMessage());
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
            throw new ResponseStatusException(HttpStatus.BAD_GATEWAY, "Onboarding API request was interrupted.");
        } catch (IllegalArgumentException ex) {
            throw new ResponseStatusException(HttpStatus.BAD_GATEWAY, "Invalid onboarding API URL.");
        }
    }

    private HttpResponse<String> sendGet(String url) throws IOException, InterruptedException {
        HttpRequest.Builder requestBuilder = HttpRequest.newBuilder(URI.create(url))
                .timeout(REQUEST_TIMEOUT);
        if (!isBlank(onboardingUserEmail)) {
            requestBuilder.header(USER_EMAIL_HEADER, onboardingUserEmail);
        }
        if (!isBlank(onboardingUserRole)) {
            requestBuilder.header(USER_ROLE_HEADER, onboardingUserRole);
        }
        HttpRequest request = requestBuilder.GET().build();
        return httpClient.send(request, HttpResponse.BodyHandlers.ofString());
    }

    private JsonNode unwrapData(JsonNode response) {
        JsonNode data = response == null ? null : response.get("data");
        return data == null || data.isNull() ? response : data;
    }

    private boolean hasDirectMatchingId(JsonNode node, String expectedId) {
        return firstText(node, "microserviceId", "resourceId", "apigeeProxyId", "kongId", "id", "_id")
                .map(value -> value.equals(expectedId))
                .orElse(false);
    }

    private boolean containsMatchingId(JsonNode node, String expectedId) {
        if (node == null) {
            return false;
        }
        if (hasDirectMatchingId(node, expectedId)) {
            return true;
        }
        if (node.isObject()) {
            Iterator<JsonNode> values = node.elements();
            while (values.hasNext()) {
                if (containsMatchingId(values.next(), expectedId)) {
                    return true;
                }
            }
        } else if (node.isArray()) {
            for (JsonNode item : node) {
                if (containsMatchingId(item, expectedId)) {
                    return true;
                }
            }
        }
        return false;
    }

    private Optional<String> firstText(JsonNode node, String... fieldNames) {
        for (String fieldName : fieldNames) {
            JsonNode value = node.get(fieldName);
            if (value != null && value.isValueNode() && !isBlank(value.asText())) {
                return Optional.of(value.asText());
            }
        }
        return Optional.empty();
    }

    private Optional<String> firstRecursiveText(JsonNode node, String... fieldNames) {
        if (node == null) {
            return Optional.empty();
        }
        for (String fieldName : fieldNames) {
            JsonNode value = node.get(fieldName);
            if (value != null && value.isValueNode() && !isBlank(value.asText())) {
                return Optional.of(value.asText());
            }
        }
        if (node.isObject()) {
            Iterator<JsonNode> values = node.elements();
            while (values.hasNext()) {
                Optional<String> nested = firstRecursiveText(values.next(), fieldNames);
                if (nested.isPresent()) {
                    return nested;
                }
            }
        } else if (node.isArray()) {
            for (JsonNode item : node) {
                Optional<String> nested = firstRecursiveText(item, fieldNames);
                if (nested.isPresent()) {
                    return nested;
                }
            }
        }
        return Optional.empty();
    }

    private List<JsonNode> onboardingRecords(JsonNode response) {
        JsonNode data = response == null ? null : response.get("data");
        if (data != null && data.isArray()) {
            List<JsonNode> records = new ArrayList<>();
            data.forEach(records::add);
            return records;
        }
        if (response != null && response.isArray()) {
            List<JsonNode> records = new ArrayList<>();
            response.forEach(records::add);
            return records;
        }
        return allObjects(response);
    }

    private List<JsonNode> allObjects(JsonNode node) {
        List<JsonNode> objects = new ArrayList<>();
        collectObjects(node, objects);
        return objects;
    }

    private void collectObjects(JsonNode node, List<JsonNode> objects) {
        if (node == null) {
            return;
        }
        if (node.isObject()) {
            objects.add(node);
            node.elements().forEachRemaining(child -> collectObjects(child, objects));
        } else if (node.isArray()) {
            node.forEach(child -> collectObjects(child, objects));
        }
    }

    private boolean isBlank(String value) {
        return value == null || value.isBlank();
    }

    public record ResolvedOnboardingResource(
            String projectId,
            String projectName,
            String companyName,
            String resourceId,
            String resourceName,
            AssetType assetType,
            SourceType sourceType,
            ScanSource source) {
    }
}
