package com.probestack.forgesphere.api;

import com.probestack.forgesphere.model.CombinedReportResponse;
import com.probestack.forgesphere.model.ComplianceScanDetailsResponse;
import com.probestack.forgesphere.model.EmailReportRequest;
import com.probestack.forgesphere.model.OwaspScanDetailsResponse;
import com.probestack.forgesphere.service.ReportService;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
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

    @PostMapping(value = "/governance/v1/reports/email", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.TEXT_PLAIN_VALUE)
    public ResponseEntity<String> sendReportEmail(@Valid @RequestBody EmailReportRequest request) {
        log.info("Processing sendReportEmail request");
        boolean delivered = reportService.sendReport(request);
        String message = delivered ? "Report email queued successfully." : "Mail sender is not configured. Report was not delivered.";
        return ResponseEntity.accepted().body(message);
    }
}
