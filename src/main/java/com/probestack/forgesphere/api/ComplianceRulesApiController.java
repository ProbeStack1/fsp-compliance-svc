package com.probestack.forgesphere.api;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.probestack.forgesphere.model.AssetType;
import com.probestack.forgesphere.model.ComplianceRulesResponse;
import com.probestack.forgesphere.model.CreateComplianceRuleRequest;
import com.probestack.forgesphere.model.CreateComplianceRuleResponse;
import com.probestack.forgesphere.model.ErrorResponse;
import com.probestack.forgesphere.model.RuleCategory;
import com.probestack.forgesphere.model.RuleStatus;
import com.probestack.forgesphere.model.UpdateComplianceRuleStatusRequest;
import com.probestack.forgesphere.model.UpdateComplianceRuleStatusResponse;
import com.probestack.forgesphere.service.ComplianceRulesService;

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
public class ComplianceRulesApiController implements ComplianceRulesApi {

    private static final Logger log = LoggerFactory.getLogger(ComplianceRulesApiController.class);

    private final ComplianceRulesService complianceRulesService;

    @Autowired()
    public ComplianceRulesApiController(ComplianceRulesService complianceRulesService) {
        this.complianceRulesService = complianceRulesService;
    }

    @Override()
    public ResponseEntity<CreateComplianceRuleResponse> createComplianceRule(@Valid() @RequestBody() CreateComplianceRuleRequest createComplianceRuleRequest) {
        log.info("Processing createComplianceRule request");
        try {
            var response = complianceRulesService.createComplianceRule(createComplianceRuleRequest);
            log.info("createComplianceRule completed successfully");
            return ResponseEntity.status(HttpStatus.CREATED).body(response.getBody());
        } catch (Exception e) {
            log.error("Failed to process createComplianceRule: {}", e.getMessage(), e);
            throw e;
        }
    }

    //  MODIFIED – status param type changed to String
    @Override()
    public ResponseEntity<ComplianceRulesResponse> getComplianceRules(
            @Valid() @RequestParam(value = "projectName", required = false) String projectName,
            @Valid() @RequestParam(value = "resourceType", required = false) AssetType resourceType,
            @Valid() @RequestParam(value = "resourceName", required = false) String resourceName,
            @Valid() @RequestParam(value = "status", required = false) String status,
            @Valid() @RequestParam(value = "ruleCategory", required = false) RuleCategory ruleCategory) {
        log.info("Processing getComplianceRules request");
        try {
            var response = complianceRulesService.getComplianceRules(projectName, resourceType, resourceName, status, ruleCategory);
            log.info("getComplianceRules completed successfully");
            return response;
        } catch (Exception e) {
            log.error("Failed to process getComplianceRules: {}", e.getMessage(), e);
            throw e;
        }
    }

    @PatchMapping(value = "/governance/v1/compliance-rules/{ruleId}/status", produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<UpdateComplianceRuleStatusResponse> updateComplianceRuleStatus(
            @PathVariable String ruleId,
            @Valid @RequestBody UpdateComplianceRuleStatusRequest request) {
        log.info("Processing updateComplianceRuleStatus request");
        try {
            var response = complianceRulesService.updateComplianceRuleStatus(ruleId, request);
            log.info("updateComplianceRuleStatus completed successfully");
            return response;
        } catch (Exception e) {
            log.error("Failed to process updateComplianceRuleStatus: {}", e.getMessage(), e);
            throw e;
        }
    }
}
