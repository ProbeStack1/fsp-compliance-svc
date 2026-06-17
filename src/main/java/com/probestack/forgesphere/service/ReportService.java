package com.probestack.forgesphere.service;

import com.probestack.forgesphere.document.ComplianceScanDocument;
import com.probestack.forgesphere.document.OwaspScanDocument;
import com.probestack.forgesphere.model.CombinedReportResponse;
import com.probestack.forgesphere.model.ComplianceScanDetailsResponse;
import com.probestack.forgesphere.model.EmailReportRequest;
import com.probestack.forgesphere.model.OwaspScanDetailsResponse;
import com.probestack.forgesphere.model.ReportRequest;
import com.probestack.forgesphere.repository.ComplianceScanRepository;
import com.probestack.forgesphere.repository.OwaspScanRepository;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Objects;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
public class ReportService {

    private final ComplianceScanRepository complianceScanRepository;
    private final OwaspScanRepository owaspScanRepository;
    private final MailService mailService;

    public ReportService(ComplianceScanRepository complianceScanRepository,
            OwaspScanRepository owaspScanRepository,
            MailService mailService) {
        this.complianceScanRepository = complianceScanRepository;
        this.owaspScanRepository = owaspScanRepository;
        this.mailService = mailService;
    }

    public ComplianceScanDetailsResponse getComplianceReport(String scanId) {
        return complianceScanRepository.findByScanId(scanId)
                .map(this::mapToComplianceScanDetailsResponse)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Compliance scan not found: " + scanId));
    }

    public OwaspScanDetailsResponse getOwaspReport(String scanId) {
        return owaspScanRepository.findByScanId(scanId)
                .map(this::mapToOwaspScanDetailsResponse)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "OWASP scan not found: " + scanId));
    }

    public CombinedReportResponse getCombinedReport(String complianceScanId, String owaspScanId) {
        ComplianceScanDetailsResponse compliance = getComplianceReport(complianceScanId);
        OwaspScanDetailsResponse owasp = getOwaspReport(owaspScanId);

        CombinedReportResponse response = new CombinedReportResponse();
        response.setProjectName(compliance.getProjectName() != null ? compliance.getProjectName() : owasp.getProjectName());
        response.setAssetType(compliance.getAssetType() != null ? compliance.getAssetType() : owasp.getAssetType());
        response.setComplianceScanId(complianceScanId);
        response.setOwaspScanId(owaspScanId);
        response.setComplianceStatus(compliance.getCompliance());
        response.setOwaspStatus(owasp.getCompliance());
        double complianceRate = calculateSuccessRate(compliance.getScanResults());
        double owaspRate = calculateSuccessRate(owasp.getScanResults());
        response.setComplianceSuccessRate(complianceRate);
        response.setOwaspSuccessRate(owaspRate);
        response.setCombinedSuccessRate((complianceRate + owaspRate) / 2.0);
        response.setGeneratedAt(OffsetDateTime.now(ZoneOffset.UTC));
        response.setDetails(List.of(
                "Compliance scan " + complianceScanId + " success rate: " + formatPercent(complianceRate),
                "OWASP scan " + owaspScanId + " success rate: " + formatPercent(owaspRate),
                "Combined success rate: " + formatPercent(response.getCombinedSuccessRate())
        ));
        return response;
    }

    public MailService.SendOutcome sendReport(EmailReportRequest request) {
        String reportBody = buildReportBody(request);
        return mailService.sendReport(request, reportBody);
    }

    /** Public method used by the preview endpoint to surface the exact
     *  text that would be sent in an email body \u2014 without actually sending. */
    public String previewReportBody(EmailReportRequest request) {
        return buildReportBody(request);
    }

    private String buildReportBody(EmailReportRequest request) {
        String reportType = request.getReportType();
        if (reportType == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "reportType is required.");
        }
        switch (reportType.toUpperCase()) {
            case "COMPLIANCE":
                ComplianceScanDetailsResponse compliance = getComplianceReport(request.getScanId());
                return formatComplianceReport(compliance);
            case "OWASP":
                OwaspScanDetailsResponse owasp = getOwaspReport(request.getScanId());
                return formatOwaspReport(owasp);
            case "COMBINED":
                if (request.getComplianceScanId() == null || request.getOwaspScanId() == null) {
                    throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                            "Both complianceScanId and owaspScanId are required for a combined report.");
                }
                CombinedReportResponse combined = getCombinedReport(request.getComplianceScanId(), request.getOwaspScanId());
                return formatCombinedReport(combined);
            default:
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                        "Unsupported reportType: " + request.getReportType());
        }
    }

    private String formatComplianceReport(ComplianceScanDetailsResponse compliance) {
        StringBuilder b = new StringBuilder();
        b.append("ForgeSphere \u2014 Compliance scan report\n");
        b.append("=====================================\n\n");
        b.append("Scan Id     : ").append(compliance.getScanId()).append('\n');
        b.append("Project     : ").append(compliance.getProjectName()).append('\n');
        b.append("Asset       : ").append(compliance.getAssetName())
          .append(" (").append(compliance.getAssetType()).append(")\n");
        b.append("Status      : ").append(compliance.getStatus()).append('\n');
        b.append("Compliance  : ").append(compliance.getCompliance()).append('\n');
        b.append("Requested by: ").append(compliance.getCreatedBy()).append('\n');
        b.append('\n');
        appendResultBreakdown(b, compliance.getScanResults());
        b.append('\n').append("\u2014 Sent automatically by ForgeSphere.").append('\n');
        return b.toString();
    }

    private String formatOwaspReport(OwaspScanDetailsResponse owasp) {
        StringBuilder b = new StringBuilder();
        b.append("ForgeSphere \u2014 OWASP Top 10 scan report\n");
        b.append("======================================\n\n");
        b.append("Scan Id     : ").append(owasp.getScanId()).append('\n');
        b.append("Project     : ").append(owasp.getProjectName()).append('\n');
        b.append("Asset       : ").append(owasp.getAssetName())
          .append(" (").append(owasp.getAssetType()).append(")\n");
        b.append("Status      : ").append(owasp.getStatus()).append('\n');
        b.append("Compliance  : ").append(owasp.getCompliance()).append('\n');
        b.append("Requested by: ").append(owasp.getCreatedBy()).append('\n');
        b.append('\n');
        appendResultBreakdown(b, owasp.getScanResults());
        b.append('\n').append("\u2014 Sent automatically by ForgeSphere.").append('\n');
        return b.toString();
    }

    private void appendResultBreakdown(StringBuilder b, List<?> results) {
        if (results == null || results.isEmpty()) {
            b.append("No rule results were produced for this scan.\n");
            return;
        }
        long passed = 0, failed = 0, skipped = 0;
        for (Object o : results) {
            if (!(o instanceof com.probestack.forgesphere.model.ScanResult)) continue;
            com.probestack.forgesphere.model.ScanResult sr = (com.probestack.forgesphere.model.ScanResult) o;
            Object st = sr.getResult();
            if (com.probestack.forgesphere.model.ScanResultStatus.PASSED.equals(st)) passed++;
            else if (com.probestack.forgesphere.model.ScanResultStatus.FAILED.equals(st)) failed++;
            else skipped++;
        }
        b.append(String.format("Summary     : %d passed \u00b7 %d failed \u00b7 %d skipped (total %d)%n%n",
                passed, failed, skipped, results.size()));
        b.append("Per-rule breakdown:\n");
        b.append("-------------------\n");
        int i = 1;
        for (Object o : results) {
            if (!(o instanceof com.probestack.forgesphere.model.ScanResult)) continue;
            com.probestack.forgesphere.model.ScanResult sr = (com.probestack.forgesphere.model.ScanResult) o;
            b.append(String.format("%2d. [%s] %s  (severity=%s)%n",
                    i++, safeStr(sr.getResult()), safeStr(sr.getRuleName()), safeStr(sr.getSeverity())));
            b.append("    ruleId  : ").append(safeStr(sr.getRuleId())).append('\n');
            if (sr.getMessage() != null && !sr.getMessage().isBlank()) {
                b.append("    reason  : ").append(sr.getMessage()).append('\n');
            }
        }
    }

    private String safeStr(Object o) { return o == null ? "" : o.toString(); }

    private String formatCombinedReport(CombinedReportResponse combined) {
        return "Combined Compliance Report:\n" +
                "Project: " + combined.getProjectName() + "\n" +
                "Compliance Scan: " + combined.getComplianceScanId() + "\n" +
                "OWASP Scan: " + combined.getOwaspScanId() + "\n" +
                "Compliance Status: " + combined.getComplianceStatus() + "\n" +
                "OWASP Status: " + combined.getOwaspStatus() + "\n" +
                "Compliance Success Rate: " + formatPercent(combined.getComplianceSuccessRate()) + "\n" +
                "OWASP Success Rate: " + formatPercent(combined.getOwaspSuccessRate()) + "\n" +
                "Combined Success Rate: " + formatPercent(combined.getCombinedSuccessRate());
    }

    private String summarizeResults(List<?> results) {
        if (results == null || results.isEmpty()) {
            return "No scan results available.";
        }
        return String.format("%d entries", results.size());
    }

    private double calculateSuccessRate(List<?> scanResults) {
        if (scanResults == null || scanResults.isEmpty()) {
            return 0.0;
        }
        long passing = scanResults.stream()
                .filter(result -> result instanceof com.probestack.forgesphere.model.ScanResult
                        && com.probestack.forgesphere.model.ScanResultStatus.PASSED
                                .equals(((com.probestack.forgesphere.model.ScanResult) result).getResult()))
                .count();
        return ((double) passing / scanResults.size()) * 100.0;
    }

    private String formatPercent(double value) {
        return String.format("%.1f%%", value);
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

    private OffsetDateTime toOffsetDateTime(java.time.Instant instant) {
        return instant == null ? null : OffsetDateTime.ofInstant(instant, ZoneOffset.UTC);
    }
}
