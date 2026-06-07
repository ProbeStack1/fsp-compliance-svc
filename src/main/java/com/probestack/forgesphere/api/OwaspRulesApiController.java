package com.probestack.forgesphere.api;

import com.probestack.forgesphere.model.AssetType;
import com.probestack.forgesphere.model.CreateOwaspRuleRequest;
import com.probestack.forgesphere.model.CreateOwaspRuleResponse;
import com.probestack.forgesphere.model.OwaspCategory;
import com.probestack.forgesphere.model.OwaspRulesResponse;
import com.probestack.forgesphere.model.RuleStatus;
import com.probestack.forgesphere.model.UpdateComplianceRuleStatusRequest;
import com.probestack.forgesphere.model.UpdateComplianceRuleStatusResponse;
import com.probestack.forgesphere.service.OwaspRulesService;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@RequestMapping("${openapi.probestackComplianceService.base-path:}")
public class OwaspRulesApiController {

    private static final Logger log = LoggerFactory.getLogger(OwaspRulesApiController.class);

    private final OwaspRulesService owaspRulesService;

    @Autowired
    public OwaspRulesApiController(OwaspRulesService owaspRulesService) {
        this.owaspRulesService = owaspRulesService;
    }

    @PostMapping(value = "/governance/v1/owasp-rules", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<CreateOwaspRuleResponse> createOwaspRule(
            @Valid @RequestBody CreateOwaspRuleRequest request) {
        log.info("Processing createOwaspRule request");
        return owaspRulesService.createOwaspRule(request);
    }

    @GetMapping(value = "/governance/v1/owasp-rules", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<OwaspRulesResponse> getOwaspRules(
            @RequestParam(value = "projectName", required = false) String projectName,
            @RequestParam(value = "resourceType", required = false) AssetType resourceType,
            @RequestParam(value = "resourceName", required = false) String resourceName,
            @RequestParam(value = "status", required = false) RuleStatus status,
            @RequestParam(value = "category", required = false) OwaspCategory category) {
        log.info("Processing getOwaspRules request");
        return owaspRulesService.getOwaspRules(projectName, resourceType, resourceName, status, category);
    }

    @PatchMapping(value = "/governance/v1/owasp-rules/{ruleId}/status", produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<UpdateComplianceRuleStatusResponse> updateOwaspRuleStatus(
            @PathVariable String ruleId,
            @Valid @RequestBody UpdateComplianceRuleStatusRequest request) {
        log.info("Processing updateOwaspRuleStatus request");
        return owaspRulesService.updateOwaspRuleStatus(ruleId, request);
    }
}
