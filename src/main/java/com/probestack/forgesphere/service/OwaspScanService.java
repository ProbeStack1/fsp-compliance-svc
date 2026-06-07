package com.probestack.forgesphere.service;

import com.probestack.forgesphere.document.OwaspScanDocument;
import com.probestack.forgesphere.model.AssetType;
import com.probestack.forgesphere.model.OwaspScanDetailsResponse;
import com.probestack.forgesphere.model.RunOwaspCheckRequest;
import com.probestack.forgesphere.model.RunOwaspCheckResponse;
import com.probestack.forgesphere.model.SubmitOwaspScanRequest;
import com.probestack.forgesphere.model.SubmitOwaspScanResponse;
import com.probestack.forgesphere.model.ScanStatus;
import com.probestack.forgesphere.model.ComplianceStatus;
import com.probestack.forgesphere.model.ScanResult;
import com.probestack.forgesphere.repository.OwaspScanRepository;
import com.probestack.forgesphere.service.OnboardingResourceResolver.ResolvedOnboardingResource;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
public class OwaspScanService {

    private static final DateTimeFormatter SCAN_ID_DATE_FORMAT =
            DateTimeFormatter.ofPattern("yyyyMMdd-HHmmss", Locale.ROOT).withZone(ZoneOffset.UTC);

    private final OwaspScanRepository owaspScanRepository;
    private final OwaspScanProcessor owaspScanProcessor;
    private final OnboardingResourceResolver onboardingResourceResolver;

    public OwaspScanService(OwaspScanRepository owaspScanRepository,
            OwaspScanProcessor owaspScanProcessor,
            OnboardingResourceResolver onboardingResourceResolver) {
        this.owaspScanRepository = owaspScanRepository;
        this.owaspScanProcessor = owaspScanProcessor;
        this.onboardingResourceResolver = onboardingResourceResolver;
    }

    public ResponseEntity<OwaspScanDetailsResponse> getOwaspScanById(String scanId) {
        return owaspScanRepository.findByScanId(scanId)
                .map(this::mapToOwaspScanDetailsResponse)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    public ResponseEntity<SubmitOwaspScanResponse> submitOwaspScan(SubmitOwaspScanRequest submitOwaspScanRequest) {
        OwaspScanDocument savedScan = owaspScanRepository.save(mapToDocument(submitOwaspScanRequest));
        owaspScanProcessor.processScan(savedScan.getScanId());
        return ResponseEntity.status(HttpStatus.ACCEPTED).body(mapToSubmitOwaspScanResponse(savedScan));
    }

    public ResponseEntity<RunOwaspCheckResponse> runOwaspCheck(RunOwaspCheckRequest request) {
        if (request.getAssetType() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "assetType is required.");
        }

        List<String> resourceIds = requestedResourceIds(request);
        if (resourceIds.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "At least one resource id is required. Send microserviceId, microserviceIds, or resourceIds.");
        }

        List<SubmitOwaspScanResponse> scans;
        
        // For APIGEE asset type, create direct scans without onboarding resolution
        if (AssetType.APIGEE.equals(request.getAssetType())) {
            scans = resourceIds.stream()
                    .map(resourceId -> submitDirectOwaspScan(request, resourceId))
                    .toList();
        } else {
            // For other asset types (MICROSERVICE, KONG), resolve via onboarding
            scans = resourceIds.stream()
                    .map(resourceId -> onboardingResourceResolver.resolve(request.getAssetType(), resourceId))
                    .map(resource -> submitFromResolvedResource(request, resource))
                    .toList();
        }

        RunOwaspCheckResponse response = new RunOwaspCheckResponse();
        response.setSubmittedScans(scans.size());
        response.setScans(scans);
        response.setMessage("OWASP run check submitted successfully.");
        return ResponseEntity.status(HttpStatus.ACCEPTED).body(response);
    }

    public ResponseEntity<List<OwaspScanDetailsResponse>> getRecentOwaspScans(String projectName, AssetType assetType) {
        List<OwaspScanDocument> scans;
        if (projectName != null && assetType != null) {
            scans = owaspScanRepository.findTop12ByProjectNameAndAssetTypeOrderByCreateDateDesc(projectName, assetType);
        } else if (projectName != null) {
            scans = owaspScanRepository.findTop12ByProjectNameOrderByCreateDateDesc(projectName);
        } else if (assetType != null) {
            scans = owaspScanRepository.findTop12ByAssetTypeOrderByCreateDateDesc(assetType);
        } else {
            scans = owaspScanRepository.findTop12ByOrderByCreateDateDesc();
        }

        return ResponseEntity.ok(scans.stream()
                .map(this::mapToOwaspScanDetailsResponse)
                .toList());
    }

    private OwaspScanDocument mapToDocument(SubmitOwaspScanRequest request) {
        Instant now = Instant.now();
        OwaspScanDocument document = new OwaspScanDocument();
        document.setScanId(generateScanId(now));
        document.setProjectName(request.getProjectName());
        document.setCompanyName(request.getCompanyName());
        document.setAssetId(request.getAssetId());
        document.setAssetName(request.getAssetName());
        document.setAssetType(request.getAssetType());
        document.setSourceType(request.getSourceType());
        document.setSource(request.getSource());
        document.setRules(request.getRules());
        document.setScanOptions(request.getScanOptions());
        document.setStatus(ScanStatus.SUBMITTED);
        document.setCompliance(ComplianceStatus.PENDING);
        document.setCreateDate(now);
        document.setCreatedBy(request.getRequestedBy());
        document.setUpdatedDate(now);
        document.setUpdatedBy(request.getRequestedBy());
        return document;
    }

    private SubmitOwaspScanResponse submitFromResolvedResource(RunOwaspCheckRequest request,
            ResolvedOnboardingResource resource) {
        SubmitOwaspScanRequest scanRequest = new SubmitOwaspScanRequest();
        scanRequest.setProjectName(resource.projectName());
        scanRequest.setCompanyName(resource.companyName());
        scanRequest.setAssetId(resource.resourceId());
        scanRequest.setAssetName(resource.resourceName());
        scanRequest.setAssetType(resource.assetType());
        scanRequest.setSourceType(resource.sourceType());
        scanRequest.setSource(resource.source());
        scanRequest.setRules(request.getRules());
        scanRequest.setScanOptions(request.getScanOptions());
        scanRequest.setRequestedBy(request.getRequestedBy());

        OwaspScanDocument savedScan = owaspScanRepository.save(mapToDocument(scanRequest));
        owaspScanProcessor.processScan(savedScan.getScanId());
        return mapToSubmitOwaspScanResponse(savedScan);
    }

    private SubmitOwaspScanResponse submitDirectOwaspScan(RunOwaspCheckRequest request, String resourceId) {
        SubmitOwaspScanRequest scanRequest = new SubmitOwaspScanRequest();
        scanRequest.setProjectName(request.getProjectName() != null ? request.getProjectName() : "Default");
        scanRequest.setCompanyName(request.getCompanyName() != null ? request.getCompanyName() : "Default");
        scanRequest.setAssetId(resourceId);
        scanRequest.setAssetName(resourceId);
        scanRequest.setAssetType(request.getAssetType());
        scanRequest.setSourceType(null);
        scanRequest.setSource(null);
        scanRequest.setRules(request.getRules());
        scanRequest.setScanOptions(request.getScanOptions());
        scanRequest.setRequestedBy(request.getRequestedBy());

        OwaspScanDocument savedScan = owaspScanRepository.save(mapToDocument(scanRequest));
        owaspScanProcessor.processScan(savedScan.getScanId());
        return mapToSubmitOwaspScanResponse(savedScan);
    }

    private List<String> requestedResourceIds(RunOwaspCheckRequest request) {
        Set<String> ids = new LinkedHashSet<>();
        addIfPresent(ids, request.getMicroserviceId());
        addAllIfPresent(ids, request.getMicroserviceIds());
        addAllIfPresent(ids, request.getResourceIds());
        if (request.getResources() != null) {
            request.getResources().stream()
                    .map(resource -> resource.getResourceId())
                    .filter(this::isNotBlank)
                    .forEach(ids::add);
        }
        return new ArrayList<>(ids);
    }

    private void addAllIfPresent(Set<String> values, List<String> candidates) {
        if (candidates == null) {
            return;
        }
        candidates.stream()
                .filter(this::isNotBlank)
                .forEach(values::add);
    }

    private void addIfPresent(Set<String> values, String candidate) {
        if (isNotBlank(candidate)) {
            values.add(candidate);
        }
    }

    private boolean isNotBlank(String value) {
        return value != null && !value.isBlank();
    }

    private OwaspScanDetailsResponse mapToOwaspScanDetailsResponse(OwaspScanDocument document) {
        OwaspScanDetailsResponse response = new OwaspScanDetailsResponse();
        response.setScanId(document.getScanId());
        response.setProjectName(document.getProjectName());
        response.setCompanyName(document.getCompanyName());
        response.setAssetId(document.getAssetId());
        response.setAssetName(document.getAssetName());
        response.setAssetType(document.getAssetType());
        response.setSourceType(document.getSourceType());
        response.setSource(document.getSource());
        response.setRules(document.getRules());
        response.setScanOptions(document.getScanOptions());
        response.setStatus(document.getStatus());
        response.setCompliance(document.getCompliance());
        response.setScanDate(toOffsetDateTime(document.getScanDate()));
        response.setProcessingStartDate(toOffsetDateTime(document.getProcessingStartDate()));
        response.setProcessingEndDate(toOffsetDateTime(document.getProcessingEndDate()));
        response.setScanResults(document.getScanResults());
        response.setErrorMessage(document.getErrorMessage());
        response.setCreateDate(toOffsetDateTime(document.getCreateDate()));
        response.setCreatedBy(document.getCreatedBy());
        response.setUpdatedDate(toOffsetDateTime(document.getUpdatedDate()));
        response.setUpdatedBy(document.getUpdatedBy());
        return response;
    }

    private SubmitOwaspScanResponse mapToSubmitOwaspScanResponse(OwaspScanDocument document) {
        SubmitOwaspScanResponse response = new SubmitOwaspScanResponse();
        response.setScanId(document.getScanId());
        response.setStatus(document.getStatus());
        response.setSubmittedDate(toOffsetDateTime(document.getCreateDate()));
        response.setMessage("OWASP scan request submitted successfully.");
        return response;
    }

    private String generateScanId(Instant now) {
        return "OWASP-SCAN-" + SCAN_ID_DATE_FORMAT.format(now) + "-"
                + UUID.randomUUID().toString().substring(0, 8).toUpperCase(Locale.ROOT);
    }

    private OffsetDateTime toOffsetDateTime(Instant instant) {
        return instant == null ? null : OffsetDateTime.ofInstant(instant, ZoneOffset.UTC);
    }
}
