package com.probestack.forgesphere.api;

import com.probestack.forgesphere.model.AssetType;
import com.probestack.forgesphere.model.OwaspScanDetailsResponse;
import com.probestack.forgesphere.model.RunOwaspCheckRequest;
import com.probestack.forgesphere.model.RunOwaspCheckResponse;
import com.probestack.forgesphere.model.SubmitOwaspScanRequest;
import com.probestack.forgesphere.model.SubmitOwaspScanResponse;
import com.probestack.forgesphere.service.OwaspScanService;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@Controller
@RequestMapping("${openapi.probestackComplianceService.base-path:}")
public class OwaspScanApiController {

    private static final Logger log = LoggerFactory.getLogger(OwaspScanApiController.class);

    private final OwaspScanService owaspScanService;

    @Autowired
    public OwaspScanApiController(OwaspScanService owaspScanService) {
        this.owaspScanService = owaspScanService;
    }

    @GetMapping(value = "/governance/v1/owasp-scans/{scanId}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<OwaspScanDetailsResponse> getOwaspScanById(@PathVariable String scanId) {
        log.info("Processing getOwaspScanById request");
        return owaspScanService.getOwaspScanById(scanId);
    }

    @PostMapping(value = "/governance/v1/owasp-scans/run-check", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<RunOwaspCheckResponse> runOwaspCheck(
            @Valid @RequestBody RunOwaspCheckRequest request) {
        log.info("Processing runOwaspCheck request");
        return owaspScanService.runOwaspCheck(request);
    }

    @GetMapping(value = "/governance/v1/owasp-scans/recent", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<List<OwaspScanDetailsResponse>> getRecentOwaspScans(
            @RequestParam(value = "projectName", required = false) String projectName,
            @RequestParam(value = "assetType", required = false) AssetType assetType) {
        log.info("Processing getRecentOwaspScans request");
        return owaspScanService.getRecentOwaspScans(projectName, assetType);
    }

    @PostMapping(value = "/governance/v1/owasp-scans/submit", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<SubmitOwaspScanResponse> submitOwaspScan(
            @Valid @RequestBody SubmitOwaspScanRequest request) {
        log.info("Processing submitOwaspScan request");
        return owaspScanService.submitOwaspScan(request);
    }
}
