package com.probestack.forgesphere.api;

import com.probestack.forgesphere.model.ApproveRuleRequest;
import com.probestack.forgesphere.model.ApproveRuleRequestResponse;
import com.probestack.forgesphere.model.SubmitRuleRequest;
import com.probestack.forgesphere.model.SubmitRuleRequestResponse;
import com.probestack.forgesphere.service.RuleRequestService;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("${openapi.probestackComplianceService.base-path:}")
public class RuleRequestController {

    private static final Logger log = LoggerFactory.getLogger(RuleRequestController.class);

    private final RuleRequestService ruleRequestService;

    @Autowired
    public RuleRequestController(RuleRequestService ruleRequestService) {
        this.ruleRequestService = ruleRequestService;
    }

    /**
     * Endpoint 1: User submits a new rule request.
     * POST /governance/v1/rule-requests
     */
    @PostMapping(value = "/governance/v1/rule-requests",
                 consumes = MediaType.APPLICATION_JSON_VALUE,
                 produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<SubmitRuleRequestResponse> submitRuleRequest(
            @Valid @RequestBody SubmitRuleRequest request) {
        log.info("Received rule request: ruleType={}, ruleName={}, createdBy={}",
                request.getRuleType(), request.getRuleName(), request.getCreatedBy());
        SubmitRuleRequestResponse response = ruleRequestService.submitRequest(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * Endpoint 2: Admin approves/rejects a rule request.
     * PATCH /governance/v1/rule-requests/{ruleId}/approve
     */
    @PatchMapping(value = "/governance/v1/rule-requests/{ruleId}/approve",
                  consumes = MediaType.APPLICATION_JSON_VALUE,
                  produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<ApproveRuleRequestResponse> approveRuleRequest(
            @PathVariable String ruleId,
            @Valid @RequestBody ApproveRuleRequest approveRequest) {
        log.info("Approving request for ruleId: {} with status: {}", ruleId, approveRequest.getStatus());
        ApproveRuleRequestResponse response = ruleRequestService.approveRequest(ruleId, approveRequest);
        return ResponseEntity.ok(response);
    }
}