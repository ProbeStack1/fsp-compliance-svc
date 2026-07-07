package com.probestack.forgesphere.api;

import com.probestack.forgesphere.document.LintingRuleDocument;
import com.probestack.forgesphere.model.AssetType;
import com.probestack.forgesphere.model.LintingRule;
import com.probestack.forgesphere.model.LintingRulesResponse;
import com.probestack.forgesphere.model.RuleCategory;
import com.probestack.forgesphere.model.RuleStatus;
import com.probestack.forgesphere.repository.LintingRuleRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Linting Rules Controller – provides GET endpoints for linting rules.
 * Supports status filter: all, ready, requested.
 */
@RestController
@RequestMapping("${openapi.probestackComplianceService.base-path:}")
public class LintingRulesController {

    private static final Logger log = LoggerFactory.getLogger(LintingRulesController.class);

    private final LintingRuleRepository lintingRuleRepository;

    @Autowired
    public LintingRulesController(LintingRuleRepository lintingRuleRepository) {
        this.lintingRuleRepository = lintingRuleRepository;
    }

    /**
     * GET /governance/v1/linting-rules
     * Fetch linting rules with status filter.
     *
     * @param assetType  Filter by asset type (optional)
     * @param status     Filter by status: all, ready, requested (optional, default: all)
     * @param category   Filter by rule category (optional)
     * @return Linting rules response
     */
    @GetMapping(value = "/governance/v1/linting-rules", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<LintingRulesResponse> getLintingRules(
            @RequestParam(value = "assetType", required = false) AssetType assetType,
            @RequestParam(value = "status", required = false, defaultValue = "all") String status,
            @RequestParam(value = "category", required = false) RuleCategory category) {

        log.info("Fetching linting rules with status={}, assetType={}, category={}", status, assetType, category);

        AssetType effectiveAssetType = assetType != null ? assetType : AssetType.MICROSERVICE;
        List<LintingRuleDocument> allRules = lintingRuleRepository.findAllByAssetTypeOrderByDisplayOrderAsc(effectiveAssetType);

        // Filter by status
        List<LintingRuleDocument> filtered = allRules.stream()
                .filter(rule -> {
                    if ("ready".equalsIgnoreCase(status)) {
                        return RuleStatus.READY.equals(rule.getStatus()) || RuleStatus.ACTIVE.equals(rule.getStatus());
                    } else if ("requested".equalsIgnoreCase(status)) {
                        return RuleStatus.REQUESTED.equals(rule.getStatus());
                    } else { // "all" or null
                        return true;
                    }
                })
                .filter(rule -> category == null || category.equals(rule.getCategory()))
                .sorted(Comparator.comparing(LintingRuleDocument::getDisplayOrder, Comparator.nullsLast(Integer::compareTo)))
                .collect(Collectors.toList());

        List<LintingRule> rules = filtered.stream()
                .map(this::mapToLintingRule)
                .collect(Collectors.toList());

        LintingRulesResponse response = new LintingRulesResponse();
        response.setAssetType(effectiveAssetType);
        response.setTotalRules(allRules.size());
        response.setActiveRules((int) allRules.stream()
                .filter(rule -> (RuleStatus.ACTIVE.equals(rule.getStatus()) || RuleStatus.READY.equals(rule.getStatus()))
                        && Boolean.TRUE.equals(rule.getEnabled()))
                .count());
        response.setRules(rules);

        return ResponseEntity.ok(response);
    }

    /**
     * GET /governance/v1/linting-rules/ready
     * Special endpoint for Senior's external service – returns only READY rules.
     */
    @GetMapping(value = "/governance/v1/linting-rules/ready", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<LintingRulesResponse> getReadyLintingRules(
            @RequestParam(value = "assetType", required = false) AssetType assetType) {

        log.info("Fetching READY linting rules for external service, assetType={}", assetType);
        return getLintingRules(assetType, "ready", null);
    }

    private LintingRule mapToLintingRule(LintingRuleDocument doc) {
        LintingRule rule = new LintingRule();
        rule.setRuleId(doc.getRuleId());
        rule.setRuleName(doc.getRuleName());
        rule.setRuleDescription(doc.getRuleDescription());
        rule.setAssetType(doc.getAssetType());
        rule.setCategory(doc.getCategory());
        rule.setSeverity(doc.getSeverity());
        rule.setRuleType(doc.getRuleType());
        rule.setRuleOwner(doc.getRuleOwner());
        rule.setEnabled(doc.getEnabled());
        rule.setMandatory(doc.getMandatory());
        rule.setDisplayOrder(doc.getDisplayOrder());
        rule.setIcon(doc.getIcon());
        // ACTIVE → READY mapping
        if (doc.getStatus() == RuleStatus.ACTIVE) {
            rule.setStatus(RuleStatus.READY);
        } else {
            rule.setStatus(doc.getStatus());
        }
        rule.setImplementationKey(doc.getImplementationKey());
        rule.setCreateDate(doc.getCreateDate() != null ? OffsetDateTime.ofInstant(doc.getCreateDate(), ZoneOffset.UTC) : null);
        rule.setCreatedBy(doc.getCreatedBy());
        rule.setUpdatedDate(doc.getUpdatedDate() != null ? OffsetDateTime.ofInstant(doc.getUpdatedDate(), ZoneOffset.UTC) : null);
        rule.setUpdatedBy(doc.getUpdatedBy());
        return rule;
    }
}