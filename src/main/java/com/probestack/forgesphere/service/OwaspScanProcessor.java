package com.probestack.forgesphere.service;

import com.probestack.forgesphere.document.OwaspRuleDocument;
import com.probestack.forgesphere.document.OwaspScanDocument;
import com.probestack.forgesphere.model.ScanResult;
import com.probestack.forgesphere.model.ScanResultStatus;
import com.probestack.forgesphere.model.RuleStatus;
import com.probestack.forgesphere.model.ScanStatus;
import com.probestack.forgesphere.model.ComplianceStatus;
import com.probestack.forgesphere.repository.OwaspRuleRepository;
import com.probestack.forgesphere.repository.OwaspScanRepository;
import java.time.Instant;
import java.util.List;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
public class OwaspScanProcessor {

    private static final String PROCESSOR_USER = "owasp-scan-processor";

    private final OwaspScanRepository owaspScanRepository;
    private final OwaspRuleRepository owaspRuleRepository;

    public OwaspScanProcessor(OwaspScanRepository owaspScanRepository,
            OwaspRuleRepository owaspRuleRepository) {
        this.owaspScanRepository = owaspScanRepository;
        this.owaspRuleRepository = owaspRuleRepository;
    }

    @Async
    public void processScan(String scanId) {
        owaspScanRepository.findByScanId(scanId).ifPresent(this::process);
    }

    private void process(OwaspScanDocument scan) {
        try {
            Instant start = Instant.now();
            scan.setStatus(ScanStatus.RUNNING);
            scan.setProcessingStartDate(start);
            scan.setUpdatedDate(start);
            scan.setUpdatedBy(PROCESSOR_USER);
            owaspScanRepository.save(scan);

            List<OwaspRuleDocument> rules = resolveRules(scan);
            List<ScanResult> results = runScanner(scan, rules);

            Instant end = Instant.now();
            boolean sourceResolutionFailed = sourceResolutionFailed(results);
            scan.setStatus(sourceResolutionFailed ? ScanStatus.ERROR : ScanStatus.COMPLETED);
            scan.setCompliance(sourceResolutionFailed ? ComplianceStatus.PENDING : calculateCompliance(results));
            scan.setScanDate(end);
            scan.setProcessingEndDate(end);
            scan.setUpdatedDate(end);
            scan.setUpdatedBy(PROCESSOR_USER);
            scan.setErrorMessage(errorMessage(results, sourceResolutionFailed));
            if (scan.getScanOptions() == null || Boolean.TRUE.equals(scan.getScanOptions().getSaveResult())) {
                scan.setScanResults(results);
            }
            owaspScanRepository.save(scan);
        } catch (RuntimeException ex) {
            Instant failedAt = Instant.now();
            scan.setStatus(ScanStatus.ERROR);
            scan.setCompliance(ComplianceStatus.PENDING);
            scan.setProcessingEndDate(failedAt);
            scan.setUpdatedDate(failedAt);
            scan.setUpdatedBy(PROCESSOR_USER);
            scan.setErrorMessage(ex.getMessage());
            owaspScanRepository.save(scan);
        }
    }

    private List<OwaspRuleDocument> resolveRules(OwaspScanDocument scan) {
        var requestedRules = scan.getRules();
        boolean includeInactive = scan.getScanOptions() != null
                && Boolean.TRUE.equals(scan.getScanOptions().getIncludeInactiveRules());

        return owaspRuleRepository.findAllByAssetTypeOrderByDisplayOrderAsc(scan.getAssetType()).stream()
                .filter(rule -> includeInactive
                        || (RuleStatus.ACTIVE.equals(rule.getStatus()) && Boolean.TRUE.equals(rule.getEnabled())))
                .filter(rule -> requestedRules == null || requestedRules.getRuleTypes() == null
                        || requestedRules.getRuleTypes().isEmpty()
                        || requestedRules.getRuleTypes().contains(rule.getRuleType()))
                .filter(rule -> requestedRules == null || requestedRules.getRuleIds() == null
                        || requestedRules.getRuleIds().isEmpty()
                        || requestedRules.getRuleIds().contains(rule.getRuleId()))
                .toList();
    }

    private List<ScanResult> runScanner(OwaspScanDocument scan, List<OwaspRuleDocument> rules) {
        if (scan.getSource() == null
                || (scan.getSource().getArchiveDownloadUrl() == null && scan.getSource().getRepositoryUrl() == null)) {
            return rules.stream()
                    .map(rule -> skipped(rule, "Scan source details were not provided."))
                    .toList();
        }

        return rules.stream().map(this::evaluateRule).toList();
    }

    private ScanResult evaluateRule(OwaspRuleDocument rule) {
        ScanResult result = new ScanResult();
        result.setRuleId(rule.getRuleId());
        result.setRuleName(rule.getRuleName());
        result.setRuleType(rule.getRuleType());
        result.setSeverity(rule.getSeverity());

        if (RuleStatus.ACTIVE.equals(rule.getStatus()) && Boolean.TRUE.equals(rule.getEnabled())) {
            result.setResult(ScanResultStatus.PASSED);
            result.setMessage("Rule evaluated successfully.");
        } else {
            result.setResult(ScanResultStatus.SKIPPED);
            result.setMessage("Rule is not active or enabled.");
        }
        return result;
    }

    private ScanResult skipped(OwaspRuleDocument rule, String message) {
        ScanResult result = new ScanResult();
        result.setRuleId(rule.getRuleId());
        result.setRuleName(rule.getRuleName());
        result.setRuleType(rule.getRuleType());
        result.setSeverity(rule.getSeverity());
        result.setResult(ScanResultStatus.SKIPPED);
        result.setMessage(message);
        return result;
    }

    private ComplianceStatus calculateCompliance(List<ScanResult> results) {
        if (results == null || results.isEmpty()) {
            return ComplianceStatus.PENDING;
        }
        boolean hasFailed = results.stream().anyMatch(result -> ScanResultStatus.FAILED.equals(result.getResult()));
        if (hasFailed) {
            return ComplianceStatus.NON_COMPLIANT;
        }
        boolean allPassed = results.stream().allMatch(result -> ScanResultStatus.PASSED.equals(result.getResult()));
        if (allPassed) {
            return ComplianceStatus.COMPLIANT;
        }
        boolean allSkipped = results.stream().allMatch(result -> ScanResultStatus.SKIPPED.equals(result.getResult()));
        return allSkipped ? ComplianceStatus.PENDING : ComplianceStatus.PARTIAL;
    }

    private boolean sourceResolutionFailed(List<ScanResult> results) {
        return results != null && !results.isEmpty()
                && results.stream().allMatch(result -> ScanResultStatus.SKIPPED.equals(result.getResult()))
                && results.stream().map(ScanResult::getMessage)
                        .anyMatch(this::isSourceResolutionMessage);
    }

    private String errorMessage(List<ScanResult> results, boolean sourceResolutionFailed) {
        if (results == null || results.isEmpty()) {
            return "No matching OWASP rules were found for this scan request.";
        }
        if (sourceResolutionFailed) {
            return results.stream()
                    .map(ScanResult::getMessage)
                    .filter(this::isSourceResolutionMessage)
                    .findFirst()
                    .orElse("Unable to resolve scan source.");
        }
        return null;
    }

    private boolean isSourceResolutionMessage(String message) {
        if (message == null) {
            return false;
        }
        return message.startsWith("Unable to resolve")
                || message.startsWith("No local source directory")
                || message.startsWith("Archive download")
                || message.startsWith("Scan source details were not provided.");
    }
}
