package com.probestack.forgesphere.api;

import com.probestack.forgesphere.model.CombinedReportResponse;
import com.probestack.forgesphere.model.ComplianceScanDetailsResponse;
import com.probestack.forgesphere.model.EmailReportRequest;
import com.probestack.forgesphere.model.OwaspScanDetailsResponse;
import com.probestack.forgesphere.service.MailService;
import com.probestack.forgesphere.service.ReportService;
import jakarta.validation.Valid;
import java.util.LinkedHashMap;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@RequestMapping("${openapi.probestackComplianceService.base-path:}")
public class ReportController {

    private static final Logger log = LoggerFactory.getLogger(ReportController.class);

    private final ReportService reportService;

    @Autowired
    public ReportController(ReportService reportService) {
        this.reportService = reportService;
    }

    @GetMapping(value = "/governance/v1/reports/compliance", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<ComplianceScanDetailsResponse> getComplianceReport(
            @RequestParam(value = "scanId") String scanId) {
        log.info("Processing getComplianceReport request");
        return ResponseEntity.ok(reportService.getComplianceReport(scanId));
    }

    @GetMapping(value = "/governance/v1/reports/owasp", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<OwaspScanDetailsResponse> getOwaspReport(
            @RequestParam(value = "scanId") String scanId) {
        log.info("Processing getOwaspReport request");
        return ResponseEntity.ok(reportService.getOwaspReport(scanId));
    }

    @GetMapping(value = "/governance/v1/reports/combined", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<CombinedReportResponse> getCombinedReport(
            @RequestParam(value = "complianceScanId") String complianceScanId,
            @RequestParam(value = "owaspScanId") String owaspScanId) {
        log.info("Processing getCombinedReport request");
        return ResponseEntity.ok(reportService.getCombinedReport(complianceScanId, owaspScanId));
    }

    /**
     * Send a compliance/OWASP/combined report by email.
     *
     * Returns a structured JSON body so the UI can render a crisp toast:
     * <pre>{
     *   "status": "SENT" | "EMAIL_DISABLED" | "FAILED",
     *   "message": "human-readable summary"
     * }</pre>
     *
     * The HTTP status code is always 200 for {@code SENT} and {@code EMAIL_DISABLED}
     * (the operation completed — even when the transport was deliberately disabled);
     * unexpected transport failures surface as 502.
     */
    @PostMapping(value = "/governance/v1/reports/email",
            consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Map<String, Object>> sendReportEmail(@Valid @RequestBody EmailReportRequest request) {
        log.info("Processing sendReportEmail request type={} to={}", request.getReportType(), request.getTo());
        MailService.SendOutcome outcome = reportService.sendReport(request);
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("status", outcome.status().name());
        body.put("message", outcome.message());
        body.put("to", request.getTo());
        body.put("reportType", request.getReportType());

        HttpStatus http = switch (outcome.status()) {
            case SENT, EMAIL_DISABLED -> HttpStatus.OK;
            case FAILED               -> HttpStatus.BAD_GATEWAY;
        };
        return ResponseEntity.status(http).body(body);
    }
}
