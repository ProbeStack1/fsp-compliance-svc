package com.probestack.forgesphere.service;

import com.probestack.forgesphere.document.ComplianceScanDocument;
import com.probestack.forgesphere.model.ComplianceScanDetailsResponse;
import com.probestack.forgesphere.model.RunComplianceCheckRequest;
import com.probestack.forgesphere.model.RunComplianceCheckResponse;
import com.probestack.forgesphere.model.RunComplianceResourceRequest;
import com.probestack.forgesphere.model.SubmitComplianceScanRequest;
import com.probestack.forgesphere.model.SubmitComplianceScanResponse;
import com.probestack.forgesphere.repository.ComplianceScanRepository;
import com.probestack.forgesphere.service.OnboardingResourceResolver.ResolvedOnboardingResource;
import com.probestack.forgesphere.model.AssetType;
import com.probestack.forgesphere.model.ComplianceStatus;
import com.probestack.forgesphere.model.ScanStatus;

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
public class ComplianceScanService {

    private static final DateTimeFormatter SCAN_ID_DATE_FORMAT =
            DateTimeFormatter.ofPattern("yyyyMMdd-HHmmss", Locale.ROOT).withZone(ZoneOffset.UTC);

    private final ComplianceScanRepository complianceScanRepository;
    private final ComplianceScanProcessor complianceScanProcessor;
    private final OnboardingResourceResolver onboardingResourceResolver;
    private final AuditLogService auditLogService;

    public ComplianceScanService(ComplianceScanRepository complianceScanRepository,
            ComplianceScanProcessor complianceScanProcessor,
            OnboardingResourceResolver onboardingResourceResolver,
            AuditLogService auditLogService) {
        this.complianceScanRepository = complianceScanRepository;
        this.complianceScanProcessor = complianceScanProcessor;
        this.onboardingResourceResolver = onboardingResourceResolver;
        this.auditLogService = auditLogService;
    }

    public ResponseEntity<ComplianceScanDetailsResponse> getComplianceScanById(String scanId) {
        return complianceScanRepository.findByScanId(scanId)
                .map(this::mapToComplianceScanDetailsResponse)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    public ResponseEntity<SubmitComplianceScanResponse> submitComplianceScan(
            SubmitComplianceScanRequest submitComplianceScanRequest) {
        ComplianceScanDocument savedScan = complianceScanRepository.save(mapToDocument(submitComplianceScanRequest));
        auditLogService.logComplianceScanCreate(savedScan);
        complianceScanProcessor.processScan(savedScan.getScanId());
        return ResponseEntity.status(HttpStatus.ACCEPTED).body(mapToSubmitComplianceScanResponse(savedScan));
    }

    public ResponseEntity<RunComplianceCheckResponse> runComplianceCheck(RunComplianceCheckRequest request) {
        if (request.getAssetType() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "assetType is required.");
        }

        List<String> resourceIds = requestedResourceIds(request);
        if (resourceIds.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "At least one resource id is required. Send microserviceId, microserviceIds, or resourceIds.");
        }

        List<SubmitComplianceScanResponse> scans;
        // APIGEE proxies do not need to go through the onboarding resolver:
        // we resolve their source straight from the Apigee wrapper export URL.
        // This mirrors the OWASP service's APIGEE branch.
        if (AssetType.APIGEE.equals(request.getAssetType())) {
            scans = resourceIds.stream()
                    .map(resourceId -> submitDirectComplianceScan(request, resourceId))
                    .toList();
        } else {
            scans = resourceIds.stream()
                    .map(resourceId -> onboardingResourceResolver.resolve(request.getAssetType(), resourceId))
                    .map(resource -> submitFromResolvedResource(request, resource))
                    .toList();
        }

        RunComplianceCheckResponse response = new RunComplianceCheckResponse();
        response.setSubmittedScans(scans.size());
        response.setScans(scans);
        response.setMessage("Compliance run check submitted successfully.");
        return ResponseEntity.status(HttpStatus.ACCEPTED).body(response);
    }

    public ResponseEntity<List<ComplianceScanDetailsResponse>> getRecentComplianceScans(
            String projectName, AssetType assetType) {
        List<ComplianceScanDocument> scans;
        if (projectName != null && assetType != null) {
            scans = complianceScanRepository.findTop12ByProjectNameAndAssetTypeOrderByCreateDateDesc(projectName, assetType);
        } else if (projectName != null) {
            scans = complianceScanRepository.findTop12ByProjectNameOrderByCreateDateDesc(projectName);
        } else if (assetType != null) {
            scans = complianceScanRepository.findTop12ByAssetTypeOrderByCreateDateDesc(assetType);
        } else {
            scans = complianceScanRepository.findTop12ByOrderByCreateDateDesc();
        }

        return ResponseEntity.ok(scans.stream()
                .map(this::mapToComplianceScanDetailsResponse)
                .toList());
    }

    private ComplianceScanDocument mapToDocument(SubmitComplianceScanRequest request) {
        Instant now = Instant.now();
        ComplianceScanDocument document = new ComplianceScanDocument();
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

    private SubmitComplianceScanResponse submitFromResolvedResource(RunComplianceCheckRequest request,
            ResolvedOnboardingResource resource) {
        SubmitComplianceScanRequest scanRequest = new SubmitComplianceScanRequest();
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

        ComplianceScanDocument savedScan = complianceScanRepository.save(mapToDocument(scanRequest));
        auditLogService.logComplianceScanCreate(savedScan);
        complianceScanProcessor.processScan(savedScan.getScanId());
        return mapToSubmitComplianceScanResponse(savedScan);
    }

    /**
     * APIGEE-specific submit path that bypasses the onboarding resolver and
     * builds a {@link ScanSource} pointing straight at the Apigee proxy export
     * URL. Matches the OWASP service's APIGEE branch.
     */
    private SubmitComplianceScanResponse submitDirectComplianceScan(RunComplianceCheckRequest request, String resourceId) {
        SubmitComplianceScanRequest scanRequest = new SubmitComplianceScanRequest();
        scanRequest.setProjectName(request.getProjectName() != null ? request.getProjectName() : "Default");
        scanRequest.setCompanyName(request.getCompanyName() != null ? request.getCompanyName() : "Default");
        scanRequest.setAssetId(resourceId);
        scanRequest.setAssetName(resourceId);
        scanRequest.setAssetType(request.getAssetType());

        com.probestack.forgesphere.model.ScanSource source = new com.probestack.forgesphere.model.ScanSource();
        String envOrg = System.getenv("FORGESPHERE_DEFAULT_APIGEE_ORG");
        String org = (envOrg != null && !envOrg.isBlank()) ? envOrg : "gen-ai-poc-onboarding";
        source.setArchiveDownloadUrl(
                "https://forgesphere.probestack.io/apigee-wrapper/organizations/"
                        + org + "/apis/" + resourceId + "/revisions/latest/export");
        source.setBundleName(resourceId + ".zip");
        scanRequest.setSourceType(com.probestack.forgesphere.model.SourceType.BUNDLE_UPLOAD);
        scanRequest.setSource(source);
        scanRequest.setRules(request.getRules());
        scanRequest.setScanOptions(request.getScanOptions());
        scanRequest.setRequestedBy(request.getRequestedBy());

        ComplianceScanDocument savedScan = complianceScanRepository.save(mapToDocument(scanRequest));
        complianceScanProcessor.processScan(savedScan.getScanId());
        return mapToSubmitComplianceScanResponse(savedScan);
    }


    private List<String> requestedResourceIds(RunComplianceCheckRequest request) {
        Set<String> ids = new LinkedHashSet<>();
        addIfPresent(ids, request.getMicroserviceId());
        addAllIfPresent(ids, request.getMicroserviceIds());
        addAllIfPresent(ids, request.getResourceIds());
        if (request.getResources() != null) {
            request.getResources().stream()
                    .map(RunComplianceResourceRequest::getResourceId)
                    .filter(resourceId -> !isBlank(resourceId))
                    .forEach(ids::add);
        }
        return new ArrayList<>(ids);
    }

    private void addAllIfPresent(Set<String> values, List<String> candidates) {
        if (candidates == null) {
            return;
        }
        candidates.stream()
                .filter(candidate -> !isBlank(candidate))
                .forEach(values::add);
    }

    private void addIfPresent(Set<String> values, String candidate) {
        if (!isBlank(candidate)) {
            values.add(candidate);
        }
    }

    private ComplianceScanDetailsResponse mapToComplianceScanDetailsResponse(ComplianceScanDocument document) {
        ComplianceScanDetailsResponse response = new ComplianceScanDetailsResponse();
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

    private SubmitComplianceScanResponse mapToSubmitComplianceScanResponse(ComplianceScanDocument document) {
        SubmitComplianceScanResponse response = new SubmitComplianceScanResponse();
        response.setScanId(document.getScanId());
        response.setStatus(document.getStatus());
        response.setMessage("Compliance scan request submitted successfully.");
        response.setSubmittedDate(toOffsetDateTime(document.getCreateDate()));
        return response;
    }

    private String generateScanId(Instant now) {
        return "SCAN-" + SCAN_ID_DATE_FORMAT.format(now) + "-"
                + UUID.randomUUID().toString().substring(0, 8).toUpperCase(Locale.ROOT);
    }

    private OffsetDateTime toOffsetDateTime(Instant instant) {
        return instant == null ? null : OffsetDateTime.ofInstant(instant, ZoneOffset.UTC);
    }

    private boolean isBlank(String value) {
        return value == null || value.isBlank();
    }
}
