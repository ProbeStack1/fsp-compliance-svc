package com.probestack.forgesphere.api;

import com.probestack.forgesphere.document.LintScanDocument;
import com.probestack.forgesphere.model.AssetType;
import com.probestack.forgesphere.model.ComplianceStatus;
import com.probestack.forgesphere.model.ScanKind;
import com.probestack.forgesphere.model.ScanResult;
import com.probestack.forgesphere.model.ScanResultStatus;
import com.probestack.forgesphere.model.ScanStatus;
import com.probestack.forgesphere.repository.LintScanRepository;
import com.probestack.forgesphere.service.ComplianceThresholdService;
import com.probestack.forgesphere.service.ResourceExemptionService;
import com.probestack.forgesphere.service.ScanHistoryQueryService;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

/**
 * Linting run records.
 *
 * The lint service itself is stateless — it validates a bundle and answers immediately, storing
 * nothing — so linting results only ever lived in the browser and were lost on refresh, while
 * compliance and OWASP results survived. These routes give a client somewhere to record a
 * completed run, and return it in the same shape as the other two histories.
 *
 * <pre>
 *   POST /governance/v1/lint-scans                 record a completed run
 *   GET  /governance/v1/lint-scans/history         paged, newest first
 *   GET  /governance/v1/lint-scans/{scanId}        one run, with its per-rule results
 * </pre>
 *
 * Entirely additive. No existing route, model or collection is touched.
 */
@RestController
@RequestMapping("${openapi.probestackComplianceService.base-path:}")
public class LintScanController {

    private static final Logger log = LoggerFactory.getLogger(LintScanController.class);

    private final LintScanRepository lintScanRepository;
    private final ComplianceThresholdService thresholdService;
    private final ResourceExemptionService exemptionService;
    private final ScanHistoryQueryService scanHistoryQueryService;
    private final ObjectMapper objectMapper;

    @Autowired
    public LintScanController(LintScanRepository lintScanRepository,
            ComplianceThresholdService thresholdService,
            ResourceExemptionService exemptionService,
            ScanHistoryQueryService scanHistoryQueryService,
            ObjectMapper objectMapper) {
        this.lintScanRepository = lintScanRepository;
        this.thresholdService = thresholdService;
        this.exemptionService = exemptionService;
        this.scanHistoryQueryService = scanHistoryQueryService;
        this.objectMapper = objectMapper;
    }

    @PostMapping(value = "/governance/v1/lint-scans",
            consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Map<String, Object>> record(@RequestBody Map<String, Object> request) {
        String assetName = text(request.get("assetName"));
        if (assetName == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "assetName is required.");
        }
        AssetType assetType = parseAssetType(request.get("assetType"));
        List<ScanResult> results = parseResults(request.get("scanResults"));

        String scanId = text(request.get("scanId"));
        Instant now = Instant.now();
        // Re-recording the same scanId updates it rather than failing on the unique index: a
        // client retrying after a dropped response should not be punished for it.
        LintScanDocument doc = scanId == null
                ? new LintScanDocument()
                : lintScanRepository.findByScanId(scanId).orElseGet(LintScanDocument::new);

        if (doc.getScanId() == null) {
            doc.setScanId(scanId != null ? scanId : "LINT-" + now.toEpochMilli() + "-" + Math.abs(assetName.hashCode()));
            doc.setCreateDate(now);
            doc.setCreatedBy(text(request.get("createdBy")));
        }
        doc.setProjectName(text(request.get("projectName")));
        doc.setCompanyName(text(request.get("companyName")));
        doc.setAssetId(text(request.get("assetId")));
        doc.setAssetName(assetName);
        doc.setAssetType(assetType);
        doc.setScanResults(results);
        doc.setErrorMessage(text(request.get("errorMessage")));
        doc.setStatus(doc.getErrorMessage() != null ? ScanStatus.ERROR : ScanStatus.COMPLETED);
        // Same rule the compliance processor applies: any failure means non-compliant. The
        // cut-off, if one is configured, produces its own separate verdict at read time.
        doc.setCompliance(verdictOf(results, doc.getErrorMessage() != null));
        doc.setScanDate(now);
        doc.setUpdatedDate(now);
        doc.setUpdatedBy(text(request.get("createdBy")));

        LintScanDocument saved = lintScanRepository.save(doc);
        log.info("lint scan recorded scanId={} asset={} results={}", saved.getScanId(), assetName, results.size());
        return ResponseEntity.ok(detail(saved));
    }

    @GetMapping(value = "/governance/v1/lint-scans/history", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Map<String, Object>> history(
            @RequestParam(value = "assetType", required = false) AssetType assetType,
            @RequestParam(value = "projectName", required = false) String projectName,
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "size", defaultValue = "20") int size) {
        // Matched, sorted and paged by Mongo, with the per-rule counts computed there — this used
        // to read the whole collection per request. See ScanHistoryQueryService.
        ScanHistoryQueryService.HistoryPage result = scanHistoryQueryService.page(
                "governance_lint_scans", projectName, assetType, null, page, size);

        List<Map<String, Object>> items = new ArrayList<>();
        for (Map<String, Object> m : result.items()) {
            AssetType at = m.get("assetType") == null ? null : parseAssetType(m.get("assetType"));
            thresholdService.decorate(m, ScanKind.LINTING, at,
                    ((Number) m.get("passed")).intValue(), ((Number) m.get("totalResults")).intValue());
            exemptionService.decorate(m, ScanKind.LINTING, at, (String) m.get("assetName"));
            items.add(m);
        }

        int applied = result.size();
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("type", "LINTING");
        body.put("page", page);
        // Echoes what was applied, not what was asked for: the query service caps the page size.
        body.put("size", applied);
        body.put("total", result.total());
        body.put("totalPages", (int) Math.ceil((double) result.total() / (double) applied));
        body.put("items", items);
        return ResponseEntity.ok(body);
    }

    @GetMapping(value = "/governance/v1/lint-scans/{scanId}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Map<String, Object>> get(@PathVariable("scanId") String scanId) {
        return lintScanRepository.findByScanId(scanId)
                .map(d -> ResponseEntity.ok(detail(d)))
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "No lint scan with id " + scanId + "."));
    }

    /* ─────────────────────────────── Helpers ──────────────────────────────── */

    /** Same summary shape the compliance and OWASP histories return, so clients treat all three alike. */
    private Map<String, Object> summary(LintScanDocument d) {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("scanId", d.getScanId());
        m.put("projectName", d.getProjectName());
        m.put("assetType", d.getAssetType());
        m.put("assetName", d.getAssetName());
        m.put("status", d.getStatus());
        m.put("compliance", d.getCompliance());
        m.put("totalResults", d.getScanResults() == null ? 0 : d.getScanResults().size());
        m.put("passed", countByStatus(d.getScanResults(), ScanResultStatus.PASSED));
        m.put("failed", countByStatus(d.getScanResults(), ScanResultStatus.FAILED));
        m.put("createDate", toOffset(d.getCreateDate()));
        m.put("createdBy", d.getCreatedBy());
        thresholdService.decorate(m, ScanKind.LINTING, d.getAssetType(), d.getScanResults());
        return exemptionService.decorate(m, ScanKind.LINTING, d.getAssetType(), d.getAssetName());
    }

    private Map<String, Object> detail(LintScanDocument d) {
        Map<String, Object> m = summary(d);
        m.put("scanResults", d.getScanResults());
        m.put("errorMessage", d.getErrorMessage());
        m.put("scanDate", toOffset(d.getScanDate()));
        return m;
    }

    private ComplianceStatus verdictOf(List<ScanResult> results, boolean failedRun) {
        if (failedRun) return ComplianceStatus.NON_COMPLIANT;
        if (results == null || results.isEmpty()) return ComplianceStatus.PENDING;
        boolean anyFailed = results.stream().anyMatch(r -> ScanResultStatus.FAILED.equals(r.getResult()));
        if (anyFailed) return ComplianceStatus.NON_COMPLIANT;
        boolean allPassed = results.stream().allMatch(r -> ScanResultStatus.PASSED.equals(r.getResult()));
        return allPassed ? ComplianceStatus.COMPLIANT : ComplianceStatus.PARTIAL;
    }

    private long countByStatus(List<ScanResult> results, ScanResultStatus status) {
        if (results == null) return 0L;
        return results.stream().filter(r -> r != null && status.equals(r.getResult())).count();
    }

    private OffsetDateTime toOffset(Instant i) {
        return i == null ? null : OffsetDateTime.ofInstant(i, ZoneOffset.UTC);
    }

    private String text(Object o) {
        if (o == null) return null;
        String s = String.valueOf(o).trim();
        return s.isEmpty() ? null : s;
    }

    private AssetType parseAssetType(Object raw) {
        if (raw == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "assetType is required.");
        }
        try {
            return AssetType.fromValue(String.valueOf(raw));
        } catch (IllegalArgumentException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "assetType must be one of MICROSERVICE, APIGEE, KONG.");
        }
    }

    @SuppressWarnings("unchecked")
    private List<ScanResult> parseResults(Object raw) {
        if (raw == null) return List.of();
        if (!(raw instanceof List<?> list)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "scanResults must be an array.");
        }
        List<ScanResult> out = new ArrayList<>();
        for (Object item : list) {
            try {
                out.add(objectMapper.convertValue(item, ScanResult.class));
            } catch (IllegalArgumentException e) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                        "scanResults contains an entry that is not a scan result: " + e.getMessage());
            }
        }
        return out;
    }
}
