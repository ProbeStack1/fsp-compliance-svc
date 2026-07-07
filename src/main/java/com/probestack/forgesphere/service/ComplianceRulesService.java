package com.probestack.forgesphere.service;

import com.probestack.forgesphere.document.ComplianceRuleDocument;
import com.probestack.forgesphere.model.ComplianceRule;
import com.probestack.forgesphere.model.ComplianceRulesResponse;
import com.probestack.forgesphere.model.CreateComplianceRuleRequest;
import com.probestack.forgesphere.model.CreateComplianceRuleResponse;
import com.probestack.forgesphere.model.UpdateComplianceRuleStatusRequest;
import com.probestack.forgesphere.model.UpdateComplianceRuleStatusResponse;
import com.probestack.forgesphere.repository.ComplianceRuleRepository;
import com.probestack.forgesphere.model.AssetType;
import com.probestack.forgesphere.model.RuleCategory;
import com.probestack.forgesphere.model.RuleStatus;
import com.probestack.forgesphere.model.RuleType;

import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.Comparator;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.stereotype.Service;

/**
 * Service layer for compliance rule catalog operations.
 */
@Service
public class ComplianceRulesService {
    private static final String DEFAULT_RULE_OWNER = "Governance Team";

    private final ComplianceRuleRepository complianceRuleRepository;

    public ComplianceRulesService(ComplianceRuleRepository complianceRuleRepository) {
        this.complianceRuleRepository = complianceRuleRepository;
    }

    public ResponseEntity<CreateComplianceRuleResponse> createComplianceRule(
            CreateComplianceRuleRequest createComplianceRuleRequest) {
        ComplianceRuleDocument savedRule = complianceRuleRepository.save(mapToDocument(createComplianceRuleRequest));
        CreateComplianceRuleResponse response = mapToCreateComplianceRuleResponse(savedRule);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    // ✅ MODIFIED METHOD – status parameter changed from RuleStatus to String
    public ResponseEntity<ComplianceRulesResponse> getComplianceRules(String projectName,
            AssetType resourceType, String resourceName, String status,
            RuleCategory ruleCategory) {
        AssetType assetType = resourceType == null ? AssetType.MICROSERVICE : resourceType;
        List<ComplianceRuleDocument> allRules = complianceRuleRepository.findAllByAssetTypeOrderByDisplayOrderAsc(assetType);
        
        // 🔥 Filter by status string
        List<ComplianceRuleDocument> filtered = allRules.stream()
                .filter(rule -> {
                    if ("ready".equalsIgnoreCase(status)) {
                        return RuleStatus.READY.equals(rule.getStatus()) || RuleStatus.ACTIVE.equals(rule.getStatus());
                    } else if ("requested".equalsIgnoreCase(status)) {
                        return RuleStatus.REQUESTED.equals(rule.getStatus());
                    } else { // "all" or null
                        return true;
                    }
                })
                .filter(rule -> ruleCategory == null || ruleCategory.equals(rule.getCategory()))
                .sorted(Comparator.comparing(ComplianceRuleDocument::getDisplayOrder, Comparator.nullsLast(Integer::compareTo)))
                .toList();
        
        List<ComplianceRule> rules = filtered.stream()
                .map(this::mapToComplianceRule)
                .toList();

        ComplianceRulesResponse response = new ComplianceRulesResponse();
        response.setProjectName(projectName);
        response.setResourceType(assetType);
        response.setResourceName(resourceName);
        response.setTotalRules(allRules.size());
        response.setActiveRules((int) allRules.stream()
                .filter(rule -> (RuleStatus.ACTIVE.equals(rule.getStatus()) || RuleStatus.READY.equals(rule.getStatus())) 
                        && Boolean.TRUE.equals(rule.getEnabled()))
                .count());
        response.setRules(rules);
        return ResponseEntity.ok(response);
    }

    public ResponseEntity<UpdateComplianceRuleStatusResponse> updateComplianceRuleStatus(
            String ruleId, UpdateComplianceRuleStatusRequest request) {
        ComplianceRuleDocument rule = complianceRuleRepository.findByRuleId(ruleId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Compliance rule not found: " + ruleId));

        if (request.getEnabled() != null) {
            rule.setEnabled(request.getEnabled());
            rule.setStatus(Boolean.TRUE.equals(request.getEnabled()) ? RuleStatus.ACTIVE : RuleStatus.INACTIVE);
        }
        if (request.getStatus() != null) {
            rule.setStatus(request.getStatus());
            rule.setEnabled(RuleStatus.ACTIVE.equals(request.getStatus()));
        }
        rule.setUpdatedDate(Instant.now());
        rule.setUpdatedBy(request.getUpdatedBy() == null || request.getUpdatedBy().isBlank()
                ? "governance-ui"
                : request.getUpdatedBy());

        ComplianceRuleDocument savedRule = complianceRuleRepository.save(rule);
        UpdateComplianceRuleStatusResponse response = new UpdateComplianceRuleStatusResponse();
        response.setRuleId(savedRule.getRuleId());
        response.setRuleName(savedRule.getRuleName());
        response.setEnabled(savedRule.getEnabled());
        response.setStatus(savedRule.getStatus());
        response.setMessage("Compliance rule status updated successfully.");
        return ResponseEntity.ok(response);
    }

    private ComplianceRuleDocument mapToDocument(CreateComplianceRuleRequest request) {
        Instant now = Instant.now();
        ComplianceRuleDocument document = new ComplianceRuleDocument();
        document.setRuleId(nextRuleId());
        document.setAssetType(AssetType.MICROSERVICE);
        document.setRuleName(request.getRuleName());
        document.setRuleDescription(request.getRuleDescription());
        document.setRuleType(request.getRuleType() == null ? RuleType.CUSTOM : request.getRuleType());
        document.setRuleOwner(request.getRuleOwner() == null ? DEFAULT_RULE_OWNER : request.getRuleOwner());
        document.setCategory(request.getCategory());
        document.setSeverity(request.getSeverity());
        document.setEnabled(request.getEnabled());
        document.setMandatory(Boolean.TRUE.equals(request.getMandatory()));
        document.setDisplayOrder(request.getDisplayOrder() == null ? nextDisplayOrder() : request.getDisplayOrder());
        document.setIcon(request.getIcon());
        document.setStatus(request.getStatus());
        document.setImplementationKey("CUSTOM_" + document.getRuleId());
        document.setCreateDate(now);
        document.setCreatedBy(request.getCreatedBy());
        document.setUpdatedDate(now);
        document.setUpdatedBy(request.getUpdatedBy());
        return document;
    }

    // ✅ MODIFIED MAPPING – ACTIVE → READY conversion
    private ComplianceRule mapToComplianceRule(ComplianceRuleDocument document) {
        ComplianceRule rule = new ComplianceRule();
        rule.setRuleId(document.getRuleId());
        rule.setRuleName(document.getRuleName());
        rule.setRuleDescription(document.getRuleDescription());
        rule.setCategory(document.getCategory());
        rule.setSeverity(document.getSeverity());
        rule.setRuleType(document.getRuleType());
        rule.setRuleOwner(document.getRuleOwner());
        rule.setEnabled(document.getEnabled());
        rule.setMandatory(document.getMandatory());
        rule.setDisplayOrder(document.getDisplayOrder());
        rule.setIcon(document.getIcon());
        
        // 🔥 ACTIVE → READY mapping for UI
        if (document.getStatus() == RuleStatus.ACTIVE) {
            rule.setStatus(RuleStatus.READY);
        } else {
            rule.setStatus(document.getStatus());
        }
        
        rule.setCreateDate(toOffsetDateTime(document.getCreateDate()));
        rule.setCreatedBy(document.getCreatedBy());
        rule.setUpdatedDate(toOffsetDateTime(document.getUpdatedDate()));
        rule.setUpdatedBy(document.getUpdatedBy());
        return rule;
    }

    private CreateComplianceRuleResponse mapToCreateComplianceRuleResponse(ComplianceRuleDocument document) {
        CreateComplianceRuleResponse response = new CreateComplianceRuleResponse();
        response.setRuleId(document.getRuleId());
        response.setRuleName(document.getRuleName());
        response.setStatus(document.getStatus());
        response.setMessage("Compliance rule created successfully.");
        return response;
    }

    private OffsetDateTime toOffsetDateTime(Instant instant) {
        return instant == null ? null : OffsetDateTime.ofInstant(instant, ZoneOffset.UTC);
    }

    private String nextRuleId() {
        int nextNumber = nextDisplayOrder();
        String ruleId;
        do {
            ruleId = String.format("CR%03d", nextNumber++);
        } while (complianceRuleRepository.existsByRuleId(ruleId));
        return ruleId;
    }

    private int nextDisplayOrder() {
        return (int) complianceRuleRepository.count() + 1;
    }
}
