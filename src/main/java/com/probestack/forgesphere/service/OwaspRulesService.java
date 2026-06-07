package com.probestack.forgesphere.service;

import com.probestack.forgesphere.document.OwaspRuleDocument;
import com.probestack.forgesphere.model.AssetType;
import com.probestack.forgesphere.model.CreateOwaspRuleRequest;
import com.probestack.forgesphere.model.CreateOwaspRuleResponse;
import com.probestack.forgesphere.model.OwaspCategory;
import com.probestack.forgesphere.model.OwaspRule;
import com.probestack.forgesphere.model.OwaspRulesResponse;
import com.probestack.forgesphere.model.RuleStatus;
import com.probestack.forgesphere.model.RuleType;
import com.probestack.forgesphere.repository.OwaspRuleRepository;
import com.probestack.forgesphere.model.UpdateComplianceRuleStatusRequest;
import com.probestack.forgesphere.model.UpdateComplianceRuleStatusResponse;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.Comparator;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
public class OwaspRulesService {

    private static final String DEFAULT_RULE_OWNER = "Governance Team";

    private final OwaspRuleRepository owaspRuleRepository;

    public OwaspRulesService(OwaspRuleRepository owaspRuleRepository) {
        this.owaspRuleRepository = owaspRuleRepository;
    }

    public ResponseEntity<CreateOwaspRuleResponse> createOwaspRule(CreateOwaspRuleRequest createOwaspRuleRequest) {
        OwaspRuleDocument savedRule = owaspRuleRepository.save(mapToDocument(createOwaspRuleRequest));
        CreateOwaspRuleResponse response = mapToCreateOwaspRuleResponse(savedRule);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    public ResponseEntity<OwaspRulesResponse> getOwaspRules(String projectName, AssetType resourceType,
            String resourceName, RuleStatus status, OwaspCategory category) {
        AssetType assetType = resourceType == null ? AssetType.MICROSERVICE : resourceType;

        List<OwaspRuleDocument> allRules = status == null
                ? owaspRuleRepository.findAllByAssetTypeOrderByDisplayOrderAsc(assetType)
                : owaspRuleRepository.findAllByAssetTypeAndStatusOrderByDisplayOrderAsc(assetType, status);

        if (category != null) {
            allRules = allRules.stream()
                    .filter(rule -> category.equals(rule.getCategory()))
                    .toList();
        }

        List<OwaspRule> rules = allRules.stream()
                .sorted(Comparator.comparing(OwaspRuleDocument::getDisplayOrder, Comparator.nullsLast(Integer::compareTo)))
                .map(this::mapToOwaspRule)
                .toList();

        OwaspRulesResponse response = new OwaspRulesResponse();
        response.setProjectName(projectName);
        response.setResourceType(assetType);
        response.setResourceName(resourceName);
        response.setTotalRules(allRules.size());
        response.setActiveRules((int) allRules.stream()
                .filter(rule -> RuleStatus.ACTIVE.equals(rule.getStatus()) && Boolean.TRUE.equals(rule.getEnabled()))
                .count());
        response.setRules(rules);
        return ResponseEntity.ok(response);
    }

    public ResponseEntity<UpdateComplianceRuleStatusResponse> updateOwaspRuleStatus(String ruleId,
            UpdateComplianceRuleStatusRequest request) {
        OwaspRuleDocument rule = owaspRuleRepository.findByRuleId(ruleId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "OWASP rule not found: " + ruleId));

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

        OwaspRuleDocument savedRule = owaspRuleRepository.save(rule);
        UpdateComplianceRuleStatusResponse response = new UpdateComplianceRuleStatusResponse();
        response.setRuleId(savedRule.getRuleId());
        response.setRuleName(savedRule.getRuleName());
        response.setEnabled(savedRule.getEnabled());
        response.setStatus(savedRule.getStatus());
        response.setMessage("OWASP rule status updated successfully.");
        return ResponseEntity.ok(response);
    }

    private OwaspRuleDocument mapToDocument(CreateOwaspRuleRequest request) {
        Instant now = Instant.now();
        OwaspRuleDocument document = new OwaspRuleDocument();
        document.setRuleId(nextRuleId(request.getAssetType()));
        document.setAssetType(request.getAssetType());
        document.setOwaspId(request.getOwaspId());
        document.setRuleName(request.getRuleName());
        document.setRuleDescription(request.getRuleDescription());
        document.setRuleType(request.getRuleType() == null ? RuleType.CUSTOM : request.getRuleType());
        document.setRuleOwner(request.getRuleOwner() == null ? DEFAULT_RULE_OWNER : request.getRuleOwner());
        document.setCategory(request.getCategory());
        document.setSeverity(request.getSeverity());
        document.setEnabled(request.getEnabled());
        document.setMandatory(Boolean.TRUE.equals(request.getMandatory()));
        document.setDisplayOrder(request.getDisplayOrder() == null ? nextDisplayOrder(request.getAssetType()) : request.getDisplayOrder());
        document.setIcon(request.getIcon());
        document.setStatus(request.getStatus() == null
                ? (Boolean.TRUE.equals(request.getEnabled()) ? RuleStatus.ACTIVE : RuleStatus.INACTIVE)
                : request.getStatus());
        document.setImplementationKey(request.getImplementationKey() == null
                ? "OWASP_" + document.getRuleId()
                : request.getImplementationKey());
        document.setCreateDate(now);
        document.setCreatedBy(request.getCreatedBy());
        document.setUpdatedDate(now);
        document.setUpdatedBy(request.getUpdatedBy());
        return document;
    }

    private OwaspRule mapToOwaspRule(OwaspRuleDocument document) {
        OwaspRule rule = new OwaspRule();
        rule.setRuleId(document.getRuleId());
        rule.setOwaspId(document.getOwaspId());
        rule.setRuleName(document.getRuleName());
        rule.setRuleDescription(document.getRuleDescription());
        rule.setAssetType(document.getAssetType());
        rule.setRuleType(document.getRuleType());
        rule.setRuleOwner(document.getRuleOwner());
        rule.setCategory(document.getCategory());
        rule.setSeverity(document.getSeverity());
        rule.setEnabled(document.getEnabled());
        rule.setMandatory(document.getMandatory());
        rule.setDisplayOrder(document.getDisplayOrder());
        rule.setIcon(document.getIcon());
        rule.setStatus(document.getStatus());
        rule.setImplementationKey(document.getImplementationKey());
        rule.setCreateDate(toOffsetDateTime(document.getCreateDate()));
        rule.setCreatedBy(document.getCreatedBy());
        rule.setUpdatedDate(toOffsetDateTime(document.getUpdatedDate()));
        rule.setUpdatedBy(document.getUpdatedBy());
        return rule;
    }

    private CreateOwaspRuleResponse mapToCreateOwaspRuleResponse(OwaspRuleDocument document) {
        CreateOwaspRuleResponse response = new CreateOwaspRuleResponse();
        response.setRuleId(document.getRuleId());
        response.setRuleName(document.getRuleName());
        response.setStatus(document.getStatus());
        response.setMessage("OWASP rule created successfully.");
        return response;
    }

    private OffsetDateTime toOffsetDateTime(Instant instant) {
        return instant == null ? null : OffsetDateTime.ofInstant(instant, ZoneOffset.UTC);
    }

    private String nextRuleId(AssetType assetType) {
        int nextNumber = nextDisplayOrder(assetType);
        String ruleId;
        do {
            ruleId = String.format("OR%03d", nextNumber++);
        } while (owaspRuleRepository.existsByRuleId(ruleId));
        return ruleId;
    }

    private int nextDisplayOrder(AssetType assetType) {
        return (int) owaspRuleRepository.countByAssetType(assetType) + 1;
    }
}
