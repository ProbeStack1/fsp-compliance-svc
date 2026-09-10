package com.probestack.forgesphere.api;

import com.probestack.forgesphere.config.AuthenticatedCaller;
import com.probestack.forgesphere.document.ComplianceThresholdDocument;
import com.probestack.forgesphere.model.AssetType;
import com.probestack.forgesphere.model.ScanKind;
import com.probestack.forgesphere.service.ComplianceThresholdService;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

/**
 * Pass-rate cut-offs.
 *
 * Entirely new surface — no existing route, model or response is modified. A pair with no row
 * here behaves exactly as this service always has, so teams that do not use cut-offs need change
 * nothing and will see nothing change.
 *
 * <pre>
 *   GET    /governance/v1/compliance-thresholds?assetType=APIGEE
 *   GET    /governance/v1/compliance-thresholds/{scanKind}?assetType=APIGEE
 *   PUT    /governance/v1/compliance-thresholds/{scanKind}   { assetType, threshold, enabled, updatedBy }
 *   DELETE /governance/v1/compliance-thresholds/{scanKind}?assetType=APIGEE
 * </pre>
 */
@RestController
@RequestMapping("${openapi.probestackComplianceService.base-path:}")
public class ComplianceThresholdController {

    private static final Logger log = LoggerFactory.getLogger(ComplianceThresholdController.class);

    private final ComplianceThresholdService thresholdService;

    @Autowired
    public ComplianceThresholdController(ComplianceThresholdService thresholdService) {
        this.thresholdService = thresholdService;
    }

    @GetMapping(value = "/governance/v1/compliance-thresholds", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Map<String, Object>> list(
            @RequestParam(value = "assetType", required = false) AssetType assetType) {
        List<ComplianceThresholdDocument> found = thresholdService.findByAssetType(assetType);
        List<Map<String, Object>> items = new ArrayList<>();
        for (ComplianceThresholdDocument doc : found) {
            items.add(toPayload(doc));
        }
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("total", items.size());
        body.put("items", items);
        return ResponseEntity.ok(body);
    }

    @GetMapping(value = "/governance/v1/compliance-thresholds/{scanKind}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Map<String, Object>> get(
            @PathVariable("scanKind") String scanKind,
            @RequestParam(value = "assetType") AssetType assetType) {
        ScanKind kind = parseKind(scanKind);
        return thresholdService.find(kind, assetType)
                .map(doc -> ResponseEntity.ok(toPayload(doc)))
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "No cut-off configured for " + kind + " / " + assetType + "."));
    }

    @PutMapping(value = "/governance/v1/compliance-thresholds/{scanKind}",
            consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Map<String, Object>> upsert(
            @PathVariable("scanKind") String scanKind,
            @RequestBody Map<String, Object> request) {
        ScanKind kind = parseKind(scanKind);
        AssetType assetType = parseAssetType(request.get("assetType"));
        int threshold = parseThreshold(request.get("threshold"));
        // Absent means on: a caller writing a cut-off intends to use it. Send false to stage one.
        boolean enabled = request.get("enabled") == null || Boolean.parseBoolean(String.valueOf(request.get("enabled")));
        // The verified token's own email claim always wins when there is one — a client-supplied
        // updatedBy field can't be trusted for "who made this change" on a compliance threshold.
        String actor = AuthenticatedCaller.email()
                .orElseGet(() -> request.get("updatedBy") == null ? null : String.valueOf(request.get("updatedBy")));

        try {
            ComplianceThresholdDocument saved = thresholdService.upsert(kind, assetType, threshold, enabled, actor);
            return ResponseEntity.ok(toPayload(saved));
        } catch (IllegalArgumentException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getMessage());
        }
    }

    @DeleteMapping(value = "/governance/v1/compliance-thresholds/{scanKind}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Map<String, Object>> delete(
            @PathVariable("scanKind") String scanKind,
            @RequestParam(value = "assetType") AssetType assetType) {
        ScanKind kind = parseKind(scanKind);
        boolean removed = thresholdService.delete(kind, assetType);
        if (!removed) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND,
                    "No cut-off configured for " + kind + " / " + assetType + ".");
        }
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("scanKind", kind.getValue());
        body.put("assetType", assetType);
        body.put("deleted", true);
        log.info("cut-off deleted kind={} asset={}", kind, assetType);
        return ResponseEntity.ok(body);
    }

    /* ─────────────────────────────── Helpers ──────────────────────────────── */

    private Map<String, Object> toPayload(ComplianceThresholdDocument doc) {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("scanKind", doc.getScanKind() == null ? null : doc.getScanKind().getValue());
        m.put("assetType", doc.getAssetType());
        m.put("threshold", doc.getThreshold());
        m.put("enabled", doc.isEnabled());
        m.put("createDate", toOffset(doc.getCreateDate()));
        m.put("createdBy", doc.getCreatedBy());
        m.put("updatedDate", toOffset(doc.getUpdatedDate()));
        m.put("updatedBy", doc.getUpdatedBy());
        return m;
    }

    private OffsetDateTime toOffset(java.time.Instant i) {
        return i == null ? null : OffsetDateTime.ofInstant(i, ZoneOffset.UTC);
    }

    private ScanKind parseKind(String raw) {
        try {
            return ScanKind.fromValue(raw);
        } catch (IllegalArgumentException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "scanKind must be one of COMPLIANCE, OWASP, LINTING.");
        }
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

    private int parseThreshold(Object raw) {
        if (raw == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "threshold is required.");
        }
        try {
            return (int) Math.round(Double.parseDouble(String.valueOf(raw)));
        } catch (NumberFormatException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "threshold must be a number between 0 and 100.");
        }
    }
}
