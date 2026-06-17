package com.probestack.forgesphere.scanner.evaluator;

import com.probestack.forgesphere.document.ComplianceScanDocument;
import com.probestack.forgesphere.model.AssetType;
import com.probestack.forgesphere.model.ScanSource;
import java.util.regex.Pattern;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * Resolves the runtime endpoint URL where live HTTP probes should hit for a
 * given scan. Resolution order:
 *   1. Per-asset override property: {@code compliance.live-endpoint.{asset-name}}
 *   2. Asset-type override:         {@code compliance.live-endpoint.{ASSET_TYPE}}
 *   3. Global base + asset name:    {@code {compliance.live-endpoint.base-url}/{assetName}}
 *
 * The defaults are wired to {@code https://forgesphere.probestack.io} so the
 * existing deployment topology continues to work out of the box.  Operators
 * can override the base URL by setting either the env var
 * {@code COMPLIANCE_LIVE_ENDPOINT_BASE_URL} or the property
 * {@code compliance.live-endpoint.base-url} (see application.properties).
 */
@Component
public class LiveEndpointResolver {

    private static final Pattern URL_PATTERN = Pattern.compile("^https?://", Pattern.CASE_INSENSITIVE);

    private final String baseUrl;
    private final String microserviceBase;
    private final String apigeeBase;
    private final String kongBase;

    public LiveEndpointResolver(
            @Value("${compliance.live-endpoint.base-url:https://forgesphere.probestack.io}") String baseUrl,
            @Value("${compliance.live-endpoint.microservice-base:}") String microserviceBase,
            @Value("${compliance.live-endpoint.apigee-base:}") String apigeeBase,
            @Value("${compliance.live-endpoint.kong-base:}") String kongBase) {
        this.baseUrl = trimSlash(baseUrl);
        this.microserviceBase = trimSlash(microserviceBase);
        this.apigeeBase = trimSlash(apigeeBase);
        this.kongBase = trimSlash(kongBase);
    }

    /** Returns the live URL for a compliance scan or {@code null} if nothing is configured. */
    public String resolve(ComplianceScanDocument scan) {
        return resolve(scan.getAssetType(), scan.getAssetName(), scan.getSource());
    }

    public String resolve(AssetType assetType, String assetName, ScanSource source) {
        // 1. Explicit override stored on the scan source.repositoryUrl when it
        //    happens to be an HTTP(S) URL — keeps the API backwards compatible.
        if (source != null && source.getRepositoryUrl() != null
                && URL_PATTERN.matcher(source.getRepositoryUrl()).find()
                && !source.getRepositoryUrl().endsWith(".git")) {
            return source.getRepositoryUrl();
        }

        String perTypeBase = switch (assetType == null ? AssetType.MICROSERVICE : assetType) {
            case APIGEE -> firstNonBlank(apigeeBase, baseUrl);
            case KONG -> firstNonBlank(kongBase, baseUrl);
            case MICROSERVICE -> firstNonBlank(microserviceBase, baseUrl);
        };

        if (perTypeBase == null || perTypeBase.isBlank()) {
            return null;
        }
        if (assetName == null || assetName.isBlank()) {
            return perTypeBase;
        }
        return perTypeBase + "/" + assetName.replaceFirst("^/+", "");
    }

    private static String firstNonBlank(String a, String b) {
        return (a != null && !a.isBlank()) ? a : b;
    }

    private static String trimSlash(String s) {
        return s == null ? null : s.replaceAll("/+$", "");
    }
}
