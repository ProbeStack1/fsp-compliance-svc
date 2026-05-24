package com.probestack.forgesphere.api;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.probestack.forgesphere.model.AssetType;
import com.probestack.forgesphere.model.ComplianceScanDetailsResponse;
import com.probestack.forgesphere.model.ErrorResponse;
import com.probestack.forgesphere.model.RunComplianceCheckRequest;
import com.probestack.forgesphere.model.RunComplianceCheckResponse;
import com.probestack.forgesphere.model.SubmitComplianceScanRequest;
import com.probestack.forgesphere.model.SubmitComplianceScanResponse;
import com.probestack.forgesphere.service.ComplianceScanService;

import jakarta.validation.constraints.*;
import jakarta.validation.Valid;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import jakarta.annotation.Generated;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Generated(value = "org.openapitools.codegen.languages.SpringCodegen", date = "2026-05-13T05:38:50.494838724Z[GMT]")
@Controller
@RequestMapping("${openapi.probestackComplianceService.base-path:}")
public class ComplianceScanApiController implements ComplianceScanApi {

    private static final Logger log = LoggerFactory.getLogger(ComplianceScanApiController.class);

    private final ComplianceScanService complianceScanService;

    @Autowired()
    public ComplianceScanApiController(ComplianceScanService complianceScanService) {
        this.complianceScanService = complianceScanService;
    }

    @Override()
    public ResponseEntity<ComplianceScanDetailsResponse> getComplianceScanById(@PathVariable() String scanId) {
        log.info("Processing getComplianceScanById request");
        try {
            var response = complianceScanService.getComplianceScanById(scanId);
            log.info("getComplianceScanById completed successfully");
            return response;
        } catch (Exception e) {
            log.error("Failed to process getComplianceScanById: {}", e.getMessage(), e);
            throw e;
        }
    }

    @PostMapping(value = "/governance/v1/compliance-scans/run-check", produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<RunComplianceCheckResponse> runComplianceCheck(
            @Valid @RequestBody RunComplianceCheckRequest request) {
        log.info("Processing runComplianceCheck request");
        try {
            var response = complianceScanService.runComplianceCheck(request);
            log.info("runComplianceCheck completed successfully");
            return response;
        } catch (Exception e) {
            log.error("Failed to process runComplianceCheck: {}", e.getMessage(), e);
            throw e;
        }
    }

    @GetMapping(value = "/governance/v1/compliance-scans/recent", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<List<ComplianceScanDetailsResponse>> getRecentComplianceScans(
            @RequestParam(value = "projectName", required = false) String projectName,
            @RequestParam(value = "assetType", required = false) AssetType assetType) {
        log.info("Processing getRecentComplianceScans request");
        try {
            var response = complianceScanService.getRecentComplianceScans(projectName, assetType);
            log.info("getRecentComplianceScans completed successfully");
            return response;
        } catch (Exception e) {
            log.error("Failed to process getRecentComplianceScans: {}", e.getMessage(), e);
            throw e;
        }
    }

    @Override()
    public ResponseEntity<SubmitComplianceScanResponse> submitComplianceScan(@Valid() @RequestBody() SubmitComplianceScanRequest submitComplianceScanRequest) {
        log.info("Processing submitComplianceScan request");
        try {
            var response = complianceScanService.submitComplianceScan(submitComplianceScanRequest);
            log.info("submitComplianceScan completed successfully");
            return response;
        } catch (Exception e) {
            log.error("Failed to process submitComplianceScan: {}", e.getMessage(), e);
            throw e;
        }
    }
}
