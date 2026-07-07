package com.probestack.forgesphere.service;

import com.probestack.forgesphere.model.ComplianceStatus;
import com.probestack.forgesphere.document.ComplianceRuleDocument;
import com.probestack.forgesphere.document.ComplianceScanDocument;
import com.probestack.forgesphere.model.ScanResult;
import com.probestack.forgesphere.model.ScanRules;
import com.probestack.forgesphere.repository.ComplianceRuleRepository;
import com.probestack.forgesphere.repository.ComplianceScanRepository;
import com.probestack.forgesphere.scanner.microservice.MicroserviceComplianceScanner;
import com.probestack.forgesphere.scanner.proxy.ProxyComplianceScanner;
import com.probestack.forgesphere.model.AssetType;
import com.probestack.forgesphere.model.RuleStatus;
import com.probestack.forgesphere.model.ScanResultStatus;
import com.probestack.forgesphere.model.ScanStatus;

import java.time.Instant;
import java.util.List;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
public class ComplianceScanProcessor {

    private static final String PROCESSOR_USER = "compliance-scan-processor";

    private final ComplianceScanRepository complianceScanRepository;
    private final ComplianceRuleRepository complianceRuleRepository;
    private final MicroserviceComplianceScanner microserviceComplianceScanner;
    private final ProxyComplianceScanner proxyComplianceScanner;

    public ComplianceScanProcessor(ComplianceScanRepository complianceScanRepository,
            ComplianceRuleRepository complianceRuleRepository,
            MicroserviceComplianceScanner microserviceComplianceScanner,
            ProxyComplianceScanner proxyComplianceScanner) {
        this.complianceScanRepository = complianceScanRepository;
        this.complianceRuleRepository = complianceRuleRepository;
        this.microserviceComplianceScanner = microserviceComplianceScanner;
        this.proxyComplianceScanner = proxyComplianceScanner;
    }

    @Async
    public void processScan(String scanId) {
        complianceScanRepository.findByScanId(scanId).ifPresent(this::process);
    }

    private void process(ComplianceScanDocument scan) {
        try {
            Instant start = Instant.now();
            scan.setStatus(ScanStatus.RUNNING);
            scan.setProcessingStartDate(start);
            scan.setUpdatedDate(start);
            scan.setUpdatedBy(PROCESSOR_USER);
            complianceScanRepository.save(scan);

            List<ComplianceRuleDocument> rules = resolveRules(scan);
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
            complianceScanRepository.save(scan);
        } catch (RuntimeException ex) {
            Instant failedAt = Instant.now();
            scan.setStatus(ScanStatus.ERROR);
            scan.setCompliance(ComplianceStatus.PENDING);
            scan.setProcessingEndDate(failedAt);
            scan.setUpdatedDate(failedAt);
            scan.setUpdatedBy(PROCESSOR_USER);
            scan.setErrorMessage(ex.getMessage());
            complianceScanRepository.save(scan);
        }
    }

    // 🔥 MODIFIED – Added READY check along with ACTIVE
    private List<ComplianceRuleDocument> resolveRules(ComplianceScanDocument scan) {
        ScanRules requestedRules = scan.getRules();
        boolean includeInactive = scan.getScanOptions() != null
                && Boolean.TRUE.equals(scan.getScanOptions().getIncludeInactiveRules());

        return complianceRuleRepository.findAllByAssetTypeOrderByDisplayOrderAsc(scan.getAssetType()).stream()
                .filter(rule -> includeInactive
                        || ((RuleStatus.ACTIVE.equals(rule.getStatus()) || RuleStatus.READY.equals(rule.getStatus()))
                            && Boolean.TRUE.equals(rule.getEnabled())))
                .filter(rule -> requestedRules == null || requestedRules.getRuleTypes() == null
                        || requestedRules.getRuleTypes().isEmpty()
                        || requestedRules.getRuleTypes().contains(rule.getRuleType()))
                .filter(rule -> requestedRules == null || requestedRules.getRuleIds() == null
                        || requestedRules.getRuleIds().isEmpty()
                        || requestedRules.getRuleIds().contains(rule.getRuleId()))
                .toList();
    }

    private List<ScanResult> runScanner(ComplianceScanDocument scan, List<ComplianceRuleDocument> rules) {
        if (AssetType.MICROSERVICE.equals(scan.getAssetType())) {
            return microserviceComplianceScanner.scan(scan, rules);
        }
        if (AssetType.APIGEE.equals(scan.getAssetType()) || AssetType.KONG.equals(scan.getAssetType())) {
            return proxyComplianceScanner.scan(scan, rules);
        }
        return rules.stream()
                .map(rule -> skipped(rule, "Scanner implementation for " + scan.getAssetType() + " is not available."))
                .toList();
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
            return "No matching compliance rules were found for this scan request.";
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

    private ScanResult skipped(ComplianceRuleDocument rule, String message) {
        ScanResult result = new ScanResult();
        result.setRuleId(rule.getRuleId());
        result.setRuleName(rule.getRuleName());
        result.setRuleType(rule.getRuleType());
        result.setCategory(rule.getCategory());
        result.setSeverity(rule.getSeverity());
        result.setResult(ScanResultStatus.SKIPPED);
        result.setMessage(message);
        return result;
    }
}
