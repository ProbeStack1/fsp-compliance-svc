package com.probestack.forgesphere.api;

import com.probestack.forgesphere.document.ComplianceRuleDocument;
import com.probestack.forgesphere.document.ComplianceScanDocument;
import com.probestack.forgesphere.document.OwaspRuleDocument;
import com.probestack.forgesphere.document.OwaspScanDocument;
import com.probestack.forgesphere.model.AssetType;
import com.probestack.forgesphere.model.ScanResult;
import com.probestack.forgesphere.repository.ComplianceRuleRepository;
import com.probestack.forgesphere.repository.ComplianceScanRepository;
import com.probestack.forgesphere.repository.OwaspRuleRepository;
import com.probestack.forgesphere.repository.OwaspScanRepository;
import com.probestack.forgesphere.service.ReportService;
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
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

/**
 * Extension controller that exposes the additional governance endpoints
 * needed by the redesigned UI:
 * - Single rule detail (with implementation breakdown for the right-side drawer)
 * - Paginated scan history (filter + sort)
 * - Downloadable HTML report (for "Download Report" button)
 *
 * These endpoints sit alongside the original controllers and share the
 * same repositories / services so nothing existing is touched.
 */
@RestController
@RequestMapping("${openapi.probestackComplianceService.base-path:}")
public class GovernanceExtensionsController {

    private static final Logger log = LoggerFactory.getLogger(GovernanceExtensionsController.class);

    private final ComplianceRuleRepository complianceRuleRepository;
    private final OwaspRuleRepository owaspRuleRepository;
    private final ComplianceScanRepository complianceScanRepository;
    private final OwaspScanRepository owaspScanRepository;
    private final ReportService reportService;

    @Autowired
    public GovernanceExtensionsController(
            ComplianceRuleRepository complianceRuleRepository,
            OwaspRuleRepository owaspRuleRepository,
            ComplianceScanRepository complianceScanRepository,
            OwaspScanRepository owaspScanRepository,
            ReportService reportService) {
        this.complianceRuleRepository = complianceRuleRepository;
        this.owaspRuleRepository = owaspRuleRepository;
        this.complianceScanRepository = complianceScanRepository;
        this.owaspScanRepository = owaspScanRepository;
        this.reportService = reportService;
    }

    /* ─────────────────────────── Rule detail ────────────────────────── */

    @GetMapping(value = "/governance/v1/compliance-rules/{ruleId}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Map<String, Object>> getComplianceRuleDetail(@PathVariable String ruleId) {
        log.info("getComplianceRuleDetail ruleId={}", ruleId);
        ComplianceRuleDocument doc = complianceRuleRepository.findByRuleId(ruleId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "Compliance rule not found: " + ruleId));
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("ruleId", doc.getRuleId());
        body.put("ruleName", doc.getRuleName());
        body.put("ruleDescription", doc.getRuleDescription());
        body.put("assetType", doc.getAssetType());
        body.put("ruleType", doc.getRuleType());
        body.put("ruleOwner", doc.getRuleOwner());
        body.put("category", doc.getCategory());
        body.put("severity", doc.getSeverity());
        body.put("enabled", doc.getEnabled());
        body.put("mandatory", doc.getMandatory());
        body.put("displayOrder", doc.getDisplayOrder());
        body.put("icon", doc.getIcon());
        body.put("status", doc.getStatus());
        body.put("implementationKey", doc.getImplementationKey());
        body.put("createDate", toOffset(doc.getCreateDate()));
        body.put("createdBy", doc.getCreatedBy());
        body.put("updatedDate", toOffset(doc.getUpdatedDate()));
        body.put("updatedBy", doc.getUpdatedBy());
        body.put("implementation", buildComplianceImplementationBreakdown(doc));
        return ResponseEntity.ok(body);
    }

    @GetMapping(value = "/governance/v1/owasp-rules/{ruleId}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Map<String, Object>> getOwaspRuleDetail(@PathVariable String ruleId) {
        log.info("getOwaspRuleDetail ruleId={}", ruleId);
        OwaspRuleDocument doc = owaspRuleRepository.findByRuleId(ruleId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "OWASP rule not found: " + ruleId));
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("ruleId", doc.getRuleId());
        body.put("owaspId", doc.getOwaspId());
        body.put("ruleName", doc.getRuleName());
        body.put("ruleDescription", doc.getRuleDescription());
        body.put("assetType", doc.getAssetType());
        body.put("ruleType", doc.getRuleType());
        body.put("ruleOwner", doc.getRuleOwner());
        body.put("category", doc.getCategory());
        body.put("severity", doc.getSeverity());
        body.put("enabled", doc.getEnabled());
        body.put("mandatory", doc.getMandatory());
        body.put("displayOrder", doc.getDisplayOrder());
        body.put("icon", doc.getIcon());
        body.put("status", doc.getStatus());
        body.put("implementationKey", doc.getImplementationKey());
        body.put("createDate", toOffset(doc.getCreateDate()));
        body.put("createdBy", doc.getCreatedBy());
        body.put("updatedDate", toOffset(doc.getUpdatedDate()));
        body.put("updatedBy", doc.getUpdatedBy());
        body.put("implementation", buildOwaspImplementationBreakdown(doc));
        return ResponseEntity.ok(body);
    }

    /* ─────────────────────────── Scan history ───────────────────────── */

    @GetMapping(value = "/governance/v1/compliance-scans/history", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Map<String, Object>> getComplianceScanHistory(
            @RequestParam(value = "projectName", required = false) String projectName,
            @RequestParam(value = "assetType", required = false) AssetType assetType,
            @RequestParam(value = "requestedBy", required = false) String requestedBy,
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "size", defaultValue = "20") int size) {
        log.info("getComplianceScanHistory project={} asset={} requestedBy={} page={} size={}",
                projectName, assetType, requestedBy, page, size);

        // Fetch all (catalog size is small; in-memory filter is acceptable).
        // For very large datasets we would add a paginated Mongo query — left
        // as a follow-up since the existing repository only exposes Top12.
        List<ComplianceScanDocument> all = complianceScanRepository.findAll();
        List<ComplianceScanDocument> filtered = all.stream()
                .filter(d -> projectName == null || projectName.equalsIgnoreCase(d.getProjectName()))
                .filter(d -> assetType == null || assetType.equals(d.getAssetType()))
                .filter(d -> requestedBy == null
                        || requestedBy.equalsIgnoreCase(d.getCreatedBy())
                        || requestedBy.equalsIgnoreCase(d.getUpdatedBy()))
                .sorted(Comparator.comparing(ComplianceScanDocument::getCreateDate,
                        Comparator.nullsLast(Comparator.reverseOrder())))
                .toList();
        int total = filtered.size();
        int from = Math.max(0, Math.min(page * size, total));
        int to = Math.max(0, Math.min(from + size, total));
        List<Map<String, Object>> items = new ArrayList<>();
        for (ComplianceScanDocument d : filtered.subList(from, to)) {
            items.add(scanHistorySummary(d));
        }
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("type", "COMPLIANCE");
        body.put("page", page);
        body.put("size", size);
        body.put("total", total);
        body.put("totalPages", size == 0 ? 0 : (int) Math.ceil((double) total / (double) size));
        body.put("items", items);
        return ResponseEntity.ok(body);
    }

    @GetMapping(value = "/governance/v1/owasp-scans/history", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Map<String, Object>> getOwaspScanHistory(
            @RequestParam(value = "projectName", required = false) String projectName,
            @RequestParam(value = "assetType", required = false) AssetType assetType,
            @RequestParam(value = "requestedBy", required = false) String requestedBy,
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "size", defaultValue = "20") int size) {
        log.info("getOwaspScanHistory project={} asset={} requestedBy={} page={} size={}",
                projectName, assetType, requestedBy, page, size);
        List<OwaspScanDocument> all = owaspScanRepository.findAll();
        List<OwaspScanDocument> filtered = all.stream()
                .filter(d -> projectName == null || projectName.equalsIgnoreCase(d.getProjectName()))
                .filter(d -> assetType == null || assetType.equals(d.getAssetType()))
                .filter(d -> requestedBy == null
                        || requestedBy.equalsIgnoreCase(d.getCreatedBy())
                        || requestedBy.equalsIgnoreCase(d.getUpdatedBy()))
                .sorted(Comparator.comparing(OwaspScanDocument::getCreateDate,
                        Comparator.nullsLast(Comparator.reverseOrder())))
                .toList();
        int total = filtered.size();
        int from = Math.max(0, Math.min(page * size, total));
        int to = Math.max(0, Math.min(from + size, total));
        List<Map<String, Object>> items = new ArrayList<>();
        for (OwaspScanDocument d : filtered.subList(from, to)) {
            items.add(scanHistorySummaryOwasp(d));
        }
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("type", "OWASP");
        body.put("page", page);
        body.put("size", size);
        body.put("total", total);
        body.put("totalPages", size == 0 ? 0 : (int) Math.ceil((double) total / (double) size));
        body.put("items", items);
        return ResponseEntity.ok(body);
    }

    /* ─────────────────────────── Report download ────────────────────── */

    @GetMapping(value = "/governance/v1/reports/compliance/download")
    public ResponseEntity<String> downloadComplianceReport(
            @RequestParam("scanId") String scanId,
            @RequestParam(value = "format", defaultValue = "html") String format) {
        log.info("downloadComplianceReport scanId={} format={}", scanId, format);
        var report = reportService.getComplianceReport(scanId);
        String filename = "compliance-report-" + scanId + ".html";
        String html = buildComplianceHtml(report);
        HttpHeaders headers = new HttpHeaders();
        headers.add(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"");
        headers.setContentType(MediaType.TEXT_HTML);
        return new ResponseEntity<>(html, headers, HttpStatus.OK);
    }

    @GetMapping(value = "/governance/v1/reports/owasp/download")
    public ResponseEntity<String> downloadOwaspReport(
            @RequestParam("scanId") String scanId,
            @RequestParam(value = "format", defaultValue = "html") String format) {
        log.info("downloadOwaspReport scanId={} format={}", scanId, format);
        var report = reportService.getOwaspReport(scanId);
        String filename = "owasp-report-" + scanId + ".html";
        String html = buildOwaspHtml(report);
        HttpHeaders headers = new HttpHeaders();
        headers.add(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"");
        headers.setContentType(MediaType.TEXT_HTML);
        return new ResponseEntity<>(html, headers, HttpStatus.OK);
    }

    /* ─────────────────────────── Email body preview ─────────────────── */

    @GetMapping(value = "/governance/v1/reports/email/preview", produces = MediaType.TEXT_PLAIN_VALUE)
    public ResponseEntity<String> previewEmailBody(
            @RequestParam("scanId") String scanId,
            @RequestParam("reportType") String reportType) {
        log.info("previewEmailBody scanId={} reportType={}", scanId, reportType);
        var preview = new com.probestack.forgesphere.model.EmailReportRequest();
        preview.setTo("preview@local");
        preview.setSubject("Preview");
        preview.setReportType(reportType);
        preview.setScanId(scanId);
        String body = reportService.previewReportBody(preview);
        return ResponseEntity.ok(body);
    }

    /* ─────────────────────────── Helpers ────────────────────────────── */

    private OffsetDateTime toOffset(java.time.Instant i) {
        return i == null ? null : OffsetDateTime.ofInstant(i, ZoneOffset.UTC);
    }

    private Map<String, Object> scanHistorySummary(ComplianceScanDocument d) {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("scanId", d.getScanId());
        m.put("projectName", d.getProjectName());
        m.put("assetType", d.getAssetType());
        m.put("assetName", d.getAssetName());
        m.put("status", d.getStatus());
        m.put("compliance", d.getCompliance());
        m.put("totalResults", d.getScanResults() == null ? 0 : d.getScanResults().size());
        m.put("passed", countByStatus(d.getScanResults(), com.probestack.forgesphere.model.ScanResultStatus.PASSED));
        m.put("failed", countByStatus(d.getScanResults(), com.probestack.forgesphere.model.ScanResultStatus.FAILED));
        m.put("createDate", toOffset(d.getCreateDate()));
        m.put("createdBy", d.getCreatedBy());
        return m;
    }

    private Map<String, Object> scanHistorySummaryOwasp(OwaspScanDocument d) {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("scanId", d.getScanId());
        m.put("projectName", d.getProjectName());
        m.put("assetType", d.getAssetType());
        m.put("assetName", d.getAssetName());
        m.put("status", d.getStatus());
        m.put("compliance", d.getCompliance());
        m.put("totalResults", d.getScanResults() == null ? 0 : d.getScanResults().size());
        m.put("passed", countByStatus(d.getScanResults(), com.probestack.forgesphere.model.ScanResultStatus.PASSED));
        m.put("failed", countByStatus(d.getScanResults(), com.probestack.forgesphere.model.ScanResultStatus.FAILED));
        m.put("createDate", toOffset(d.getCreateDate()));
        m.put("createdBy", d.getCreatedBy());
        return m;
    }

    private long countByStatus(List<ScanResult> results, com.probestack.forgesphere.model.ScanResultStatus status) {
        if (results == null) return 0L;
        return results.stream().filter(r -> r != null && status.equals(r.getResult())).count();
    }

    private Map<String, Object> buildComplianceImplementationBreakdown(ComplianceRuleDocument doc) {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("implementationKey", doc.getImplementationKey());
        m.put("language", "Java + Spring Boot");
        m.put("scannerClass", "com.probestack.forgesphere.scanner.microservice.MicroserviceComplianceScanner");
        m.put("description", "This rule is enforced by walking the resolved microservice source workspace and matching the rule's implementation key against compiled regex / Spring AST visitors.");
        m.put("javaSnippet", "// MicroserviceComplianceScanner.java\n" +
                "public ScanResult evaluate(MicroserviceScanContext ctx, ComplianceRuleDocument rule) {\n" +
                "    return registry.get(rule.getImplementationKey())\n" +
                "        .map(handler -> handler.evaluate(ctx, rule))\n" +
                "        .orElseGet(() -> ScanResult.notApplicable(rule.getRuleId(), \n" +
                "            \"No handler registered for \" + rule.getImplementationKey()));\n" +
                "}");
        m.put("mongoSchema", "// governance_compliance_rules collection\n" +
                "{\n  ruleId: '" + doc.getRuleId() + "',\n  assetType: '" + doc.getAssetType() + "',\n  ruleName: '"
                + doc.getRuleName() + "',\n  implementationKey: '" + doc.getImplementationKey()
                + "',\n  severity: '" + doc.getSeverity() + "',\n  status: '" + doc.getStatus() + "'\n}");
        m.put("testScenarios", buildTestScenarios(doc.getRuleId(), doc.getRuleName(), false, doc.getCategory() == null ? "GENERAL" : doc.getCategory().name()));
        return m;
    }

    private Map<String, Object> buildOwaspImplementationBreakdown(OwaspRuleDocument doc) {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("implementationKey", doc.getImplementationKey());
        m.put("language", "Java + Spring Boot");
        m.put("scannerClass", doc.getAssetType() == AssetType.APIGEE
                ? "com.probestack.forgesphere.scanner.proxy.ProxyComplianceScanner"
                : "com.probestack.forgesphere.scanner.microservice.MicroserviceComplianceScanner");
        m.put("description", "Enforces the OWASP " + doc.getOwaspId() + " control (" + doc.getCategory() + ") by running asset-specific rules against the resolved source workspace.");
        m.put("javaSnippet", "// OwaspScanProcessor.java\n" +
                "public List<ScanResult> evaluateOwasp(OwaspScanContext ctx) {\n" +
                "    return enabledRules(ctx.assetType()).stream()\n" +
                "        .filter(r -> r.getOwaspId().equals(\"" + doc.getOwaspId() + "\"))\n" +
                "        .map(r -> registry.get(r.getImplementationKey()).evaluate(ctx, r))\n" +
                "        .toList();\n" +
                "}");
        m.put("mongoSchema", "// governance_owasp_rules collection\n" +
                "{\n  ruleId: '" + doc.getRuleId() + "',\n  owaspId: '" + doc.getOwaspId() + "',\n  category: '"
                + doc.getCategory() + "',\n  assetType: '" + doc.getAssetType() + "',\n  severity: '" + doc.getSeverity()
                + "',\n  implementationKey: '" + doc.getImplementationKey() + "',\n  status: '" + doc.getStatus()
                + "'\n}");
        m.put("testScenarios", buildTestScenarios(doc.getRuleId(), doc.getRuleName(), true, doc.getOwaspId()));
        return m;
    }

    /** Build a rich list of test scenarios (positive + negative + edge cases). */
    private List<Map<String, Object>> buildTestScenarios(String ruleId, String ruleName, boolean isOwasp, String contextLabel) {
        String method = safeMethodName(ruleId);
        List<Map<String, Object>> list = new ArrayList<>();
        list.add(scenario("Positive — control present",
                "Asset has the required control configured correctly; scanner expects PASSED.",
                "@Test\nvoid " + method + "_returns_passed_when_control_present() {\n  var ctx = scanContextWith(\"" + contextLabel + "/positive-fixture\");\n  var rule = repo.findByRuleId(\"" + ruleId + "\").orElseThrow();\n  var result = scanner.evaluate(ctx, rule);\n  assertEquals(ScanResultStatus.PASSED, result.getResult());\n  assertNull(result.getMessage());\n}"));
        list.add(scenario("Negative — control missing",
                "Asset is missing the required control; scanner expects FAILED with an actionable message.",
                "@Test\nvoid " + method + "_returns_failed_when_control_missing() {\n  var ctx = scanContextWith(\"" + contextLabel + "/missing-fixture\");\n  var rule = repo.findByRuleId(\"" + ruleId + "\").orElseThrow();\n  var result = scanner.evaluate(ctx, rule);\n  assertEquals(ScanResultStatus.FAILED, result.getResult());\n  assertNotNull(result.getMessage());\n}"));
        list.add(scenario("Negative — misconfigured value",
                "Asset has the control wired but with a misconfigured value (e.g. weak cipher, broad CORS).",
                "@Test\nvoid " + method + "_returns_failed_when_misconfigured() {\n  var ctx = scanContextWith(\"" + contextLabel + "/misconfigured-fixture\");\n  var rule = repo.findByRuleId(\"" + ruleId + "\").orElseThrow();\n  var result = scanner.evaluate(ctx, rule);\n  assertEquals(ScanResultStatus.FAILED, result.getResult());\n  assertTrue(result.getMessage().toLowerCase().contains(\"value\"));\n}"));
        list.add(scenario("Edge — source unavailable",
                "Asset source could not be resolved (404 from onboarding); scanner expects SKIPPED with a clear reason.",
                "@Test\nvoid " + method + "_returns_skipped_when_source_unavailable() {\n  var ctx = scanContextWithMissingSource();\n  var rule = repo.findByRuleId(\"" + ruleId + "\").orElseThrow();\n  var result = scanner.evaluate(ctx, rule);\n  assertEquals(ScanResultStatus.SKIPPED, result.getResult());\n}"));
        list.add(scenario("Edge — rule disabled",
                "Rule is `enabled: false`; scanner must not run it (filter happens before evaluate).",
                "@Test\nvoid " + method + "_is_skipped_when_disabled() {\n  var rule = repo.findByRuleId(\"" + ruleId + "\").orElseThrow();\n  rule.setEnabled(false);\n  repo.save(rule);\n  var scan = service.runCheck(samplePayload());\n  assertFalse(scan.getScanResults().stream().anyMatch(r -> r.getRuleId().equals(\"" + ruleId + "\")));\n}"));
        list.add(scenario("Evidence shape",
                "Confirms that the scanner attaches concrete evidence (file path + line numbers) to FAILED results.",
                "@Test\nvoid " + method + "_attaches_evidence_on_failure() {\n  var ctx = scanContextWith(\"" + contextLabel + "/missing-fixture\");\n  var rule = repo.findByRuleId(\"" + ruleId + "\").orElseThrow();\n  var result = scanner.evaluate(ctx, rule);\n  assertNotNull(result.getEvidence());\n  assertFalse(result.getEvidence().isEmpty());\n}"));
        if (isOwasp) {
            list.add(scenario("OWASP id retention",
                    "Result must echo back the OWASP id for downstream report grouping.",
                    "@Test\nvoid " + method + "_preserves_owasp_id() {\n  var rule = repo.findByRuleId(\"" + ruleId + "\").orElseThrow();\n  assertEquals(\"" + contextLabel + "\", rule.getOwaspId());\n}"));
        }
        return list;
    }

    private Map<String, Object> scenario(String name, String description, String code) {
        Map<String, Object> s = new LinkedHashMap<>();
        s.put("name", name);
        s.put("description", description);
        s.put("code", code);
        return s;
    }

    private String safeMethodName(String ruleId) {
        if (ruleId == null) return "rule_test";
        return ruleId.toLowerCase().replaceAll("[^a-z0-9]+", "_");
    }

    private String buildComplianceHtml(com.probestack.forgesphere.model.ComplianceScanDetailsResponse r) {
        StringBuilder b = new StringBuilder();
        b.append("<!doctype html><html><head><meta charset=\"utf-8\"><title>Compliance Report ");
        b.append(escape(r.getScanId())).append("</title>");
        b.append("<style>body{font-family:-apple-system,BlinkMacSystemFont,Segoe UI,Helvetica,Arial,sans-serif;background:#0b0e16;color:#e8edf6;padding:32px;}h1{color:#ff8a5c;margin-bottom:4px;}h2{color:#38bdf8;margin-top:28px;border-bottom:1px solid #27314e;padding-bottom:6px;}table{width:100%;border-collapse:collapse;margin-top:12px;}th,td{padding:8px 12px;border-bottom:1px solid #1d2538;text-align:left;font-size:13px;}th{background:#101827;color:#9ca6b8;font-weight:600;}.pass{color:#34d399;}.fail{color:#f87171;}.meta{color:#7b8295;font-size:12px;}.card{background:#101827;border:1px solid #27314e;border-radius:12px;padding:20px;margin-top:18px;}code{background:#080c14;padding:2px 6px;border-radius:4px;color:#fbbf24;}</style></head><body>");
        b.append("<h1>Compliance Scan Report</h1>");
        b.append("<div class=\"meta\">Generated ").append(OffsetDateTime.now(ZoneOffset.UTC)).append("</div>");
        b.append("<div class=\"card\"><strong>Scan Id:</strong> <code>").append(escape(r.getScanId())).append("</code><br/>");
        b.append("<strong>Project:</strong> ").append(escape(nullToEmpty(r.getProjectName()))).append("<br/>");
        b.append("<strong>Asset:</strong> ").append(escape(nullToEmpty(r.getAssetName()))).append(" (").append(escape(String.valueOf(r.getAssetType()))).append(")<br/>");
        b.append("<strong>Status:</strong> ").append(escape(String.valueOf(r.getStatus()))).append("<br/>");
        b.append("<strong>Compliance:</strong> ").append(escape(String.valueOf(r.getCompliance()))).append("<br/>");
        b.append("<strong>Created by:</strong> ").append(escape(nullToEmpty(r.getCreatedBy()))).append("</div>");

        b.append("<h2>Scan Results</h2><table><thead><tr><th>Rule</th><th>Status</th><th>Severity</th><th>Reason</th></tr></thead><tbody>");
        if (r.getScanResults() != null) {
            for (ScanResult sr : r.getScanResults()) {
                String cls = com.probestack.forgesphere.model.ScanResultStatus.PASSED.equals(sr.getResult()) ? "pass" : "fail";
                b.append("<tr><td><code>").append(escape(nullToEmpty(sr.getRuleId()))).append("</code> ")
                  .append(escape(nullToEmpty(sr.getRuleName()))).append("</td>");
                b.append("<td class=\"").append(cls).append("\">").append(escape(String.valueOf(sr.getResult()))).append("</td>");
                b.append("<td>").append(escape(String.valueOf(sr.getSeverity()))).append("</td>");
                b.append("<td>").append(escape(nullToEmpty(sr.getMessage()))).append("</td></tr>");
            }
        }
        b.append("</tbody></table></body></html>");
        return b.toString();
    }

    private String buildOwaspHtml(com.probestack.forgesphere.model.OwaspScanDetailsResponse r) {
        StringBuilder b = new StringBuilder();
        b.append("<!doctype html><html><head><meta charset=\"utf-8\"><title>OWASP Report ");
        b.append(escape(r.getScanId())).append("</title>");
        b.append("<style>body{font-family:-apple-system,BlinkMacSystemFont,Segoe UI,Helvetica,Arial,sans-serif;background:#0b0e16;color:#e8edf6;padding:32px;}h1{color:#ff8a5c;margin-bottom:4px;}h2{color:#a78bfa;margin-top:28px;border-bottom:1px solid #27314e;padding-bottom:6px;}table{width:100%;border-collapse:collapse;margin-top:12px;}th,td{padding:8px 12px;border-bottom:1px solid #1d2538;text-align:left;font-size:13px;}th{background:#101827;color:#9ca6b8;font-weight:600;}.pass{color:#34d399;}.fail{color:#f87171;}.meta{color:#7b8295;font-size:12px;}.card{background:#101827;border:1px solid #27314e;border-radius:12px;padding:20px;margin-top:18px;}code{background:#080c14;padding:2px 6px;border-radius:4px;color:#fbbf24;}</style></head><body>");
        b.append("<h1>OWASP Security Scan Report</h1>");
        b.append("<div class=\"meta\">Generated ").append(OffsetDateTime.now(ZoneOffset.UTC)).append("</div>");
        b.append("<div class=\"card\"><strong>Scan Id:</strong> <code>").append(escape(r.getScanId())).append("</code><br/>");
        b.append("<strong>Project:</strong> ").append(escape(nullToEmpty(r.getProjectName()))).append("<br/>");
        b.append("<strong>Asset:</strong> ").append(escape(nullToEmpty(r.getAssetName()))).append(" (").append(escape(String.valueOf(r.getAssetType()))).append(")<br/>");
        b.append("<strong>Status:</strong> ").append(escape(String.valueOf(r.getStatus()))).append("<br/>");
        b.append("<strong>Compliance:</strong> ").append(escape(String.valueOf(r.getCompliance()))).append("<br/>");
        b.append("<strong>Created by:</strong> ").append(escape(nullToEmpty(r.getCreatedBy()))).append("</div>");

        b.append("<h2>Scan Results</h2><table><thead><tr><th>Rule</th><th>Status</th><th>Severity</th><th>Evidence</th></tr></thead><tbody>");
        if (r.getScanResults() != null) {
            for (ScanResult sr : r.getScanResults()) {
                String cls = com.probestack.forgesphere.model.ScanResultStatus.PASSED.equals(sr.getResult()) ? "pass" : "fail";
                b.append("<tr><td><code>").append(escape(nullToEmpty(sr.getRuleId()))).append("</code> ")
                  .append(escape(nullToEmpty(sr.getRuleName()))).append("</td>");
                b.append("<td class=\"").append(cls).append("\">").append(escape(String.valueOf(sr.getResult()))).append("</td>");
                b.append("<td>").append(escape(String.valueOf(sr.getSeverity()))).append("</td>");
                b.append("<td>").append(escape(nullToEmpty(sr.getMessage()))).append("</td></tr>");
            }
        }
        b.append("</tbody></table></body></html>");
        return b.toString();
    }

    private String escape(String s) {
        if (s == null) return "";
        return s.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;").replace("\"", "&quot;");
    }

    private String nullToEmpty(String s) {
        return s == null ? "" : s;
    }
}
