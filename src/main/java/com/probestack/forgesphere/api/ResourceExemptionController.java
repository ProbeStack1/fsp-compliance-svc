package com.probestack.forgesphere.api;

import com.probestack.forgesphere.config.AuthenticatedCaller;
import com.probestack.forgesphere.document.ResourceExemptionDocument;
import com.probestack.forgesphere.model.AssetType;
import com.probestack.forgesphere.model.ScanKind;
import com.probestack.forgesphere.service.ResourceExemptionService;
import java.time.Instant;
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
 * Asset exemptions.
 *
 * Entirely new surface — no existing route, model or response is modified. An asset with no row
 * here behaves exactly as this service always has, so teams that do not use exemptions need change
 * nothing and will see nothing change.
 *
 * <pre>
 *   GET    /governance/v1/resource-exemptions?assetType=APIGEE[&amp;scanKind=COMPLIANCE]
 *   PUT    /governance/v1/resource-exemptions/{scanKind}   { assetType, assetName, reason, updatedBy }
 *   DELETE /governance/v1/resource-exemptions/{scanKind}?assetType=APIGEE&amp;assetName=my-proxy
 * </pre>
 */
@RestController
@RequestMapping("${openapi.probestackComplianceService.base-path:}")
public class ResourceExemptionController {

    private static final Logger log = LoggerFactory.getLogger(ResourceExemptionController.class);

    private final ResourceExemptionService exemptionService;

    @Autowired
    public ResourceExemptionController(ResourceExemptionService exemptionService) {
        this.exemptionService = exemptionService;
    }

    @GetMapping(value = "/governance/v1/resource-exemptions", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Map<String, Object>> list(
            @RequestParam(value = "assetType", required = false) AssetType assetType,
            @RequestParam(value = "scanKind", required = false) String scanKind) {
        ScanKind kind = scanKind == null ? null : parseKind(scanKind);
        List<ResourceExemptionDocument> found = exemptionService.find(kind, assetType);
        List<Map<String, Object>> items = new ArrayList<>();
        for (ResourceExemptionDocument doc : found) {
            items.add(toPayload(doc));
        }
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("total", items.size());
        body.put("items", items);
        return ResponseEntity.ok(body);
    }

    @PutMapping(value = "/governance/v1/resource-exemptions/{scanKind}",
            consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Map<String, Object>> exempt(
            @PathVariable("scanKind") String scanKind,
            @RequestBody Map<String, Object> request) {
        ScanKind kind = parseKind(scanKind);
        AssetType assetType = parseAssetType(request.get("assetType"));
        String assetName = text(request.get("assetName"));
        if (assetName == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "assetName is required.");
        }
        String reason = text(request.get("reason"));
        // The verified token's own email claim always wins when there is one — a client-supplied
        // updatedBy field can't be trusted for "who approved this security exemption".
        String actor = AuthenticatedCaller.email().orElseGet(() -> text(request.get("updatedBy")));

        try {
            ResourceExemptionDocument saved = exemptionService.exempt(kind, assetType, assetName, reason, actor);
            return ResponseEntity.ok(toPayload(saved));
        } catch (IllegalArgumentException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getMessage());
        }
    }

    @DeleteMapping(value = "/governance/v1/resource-exemptions/{scanKind}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Map<String, Object>> lift(
            @PathVariable("scanKind") String scanKind,
            @RequestParam(value = "assetType") AssetType assetType,
            @RequestParam(value = "assetName") String assetName) {
        ScanKind kind = parseKind(scanKind);
        boolean removed = exemptionService.lift(kind, assetType, assetName);
        if (!removed) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND,
                    "No exemption for " + assetName + " under " + kind + " / " + assetType + ".");
        }
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("scanKind", kind.getValue());
        body.put("assetType", assetType);
        body.put("assetName", assetName);
        body.put("deleted", true);
        log.info("exemption deleted kind={} asset={} name={}", kind, assetType, assetName);
        return ResponseEntity.ok(body);
    }

    /* ─────────────────────────────── Helpers ──────────────────────────────── */

    private Map<String, Object> toPayload(ResourceExemptionDocument doc) {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("scanKind", doc.getScanKind() == null ? null : doc.getScanKind().getValue());
        m.put("assetType", doc.getAssetType());
        m.put("assetName", doc.getAssetName());
        m.put("reason", doc.getReason());
        m.put("createDate", toOffset(doc.getCreateDate()));
        m.put("createdBy", doc.getCreatedBy());
        m.put("updatedDate", toOffset(doc.getUpdatedDate()));
        m.put("updatedBy", doc.getUpdatedBy());
        return m;
    }

    private OffsetDateTime toOffset(Instant i) {
        return i == null ? null : OffsetDateTime.ofInstant(i, ZoneOffset.UTC);
    }

    private String text(Object o) {
        if (o == null) {
            return null;
        }
        String s = String.valueOf(o).trim();
        return s.isEmpty() ? null : s;
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
}
