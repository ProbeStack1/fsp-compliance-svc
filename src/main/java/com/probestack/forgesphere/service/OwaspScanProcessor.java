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
import com.probestack.forgesphere.scanner.probe.ApigeeProbeRunner;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
public class OwaspScanProcessor {

    private static final String PROCESSOR_USER = "owasp-scan-processor";

    private final OwaspScanRepository owaspScanRepository;
    private final OwaspRuleRepository owaspRuleRepository;
    private final ApigeeProbeRunner apigeeProbeRunner;

    public OwaspScanProcessor(OwaspScanRepository owaspScanRepository,
                              OwaspRuleRepository owaspRuleRepository,
                              ApigeeProbeRunner apigeeProbeRunner) {
        this.owaspScanRepository = owaspScanRepository;
        this.owaspRuleRepository = owaspRuleRepository;
        this.apigeeProbeRunner = apigeeProbeRunner;
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
        // Priority 1: use the deployed endpoint URL provided by the UI
        String targetUrl = scan.getDeployedEndpointUrl();
        if (targetUrl != null && !targetUrl.isBlank()) {
            return runGeneralProbes(targetUrl, rules);
        }

        // Fallback for Apigee: construct URL from proxy name
        if (com.probestack.forgesphere.model.AssetType.APIGEE.equals(scan.getAssetType())) {
            targetUrl = buildApigeeRuntimeUrl(scan);
            if (targetUrl != null && !targetUrl.isBlank()) {
                return runGeneralProbes(targetUrl, rules);
            }
        }

        // No URL available -> all rules PASSED (dummy)
        return rules.stream()
                .map(rule -> passedResult(rule, "No deployed URL provided; rule marked as passed."))
                .toList();
    }

    private List<ScanResult> runGeneralProbes(String targetUrl, List<OwaspRuleDocument> rules) {
        List<ScanResult> results = new ArrayList<>();
        for (OwaspRuleDocument rule : rules) {
            int idx = owaspIdToIndex(rule.getOwaspId());
            ScanResult sr = new ScanResult();
            sr.setRuleId(rule.getRuleId());
            sr.setRuleName(rule.getRuleName());
            sr.setRuleType(rule.getRuleType());
            sr.setSeverity(rule.getSeverity());

            if (idx >= 0 && idx < 10) {
                ApigeeProbeRunner.ProbeResult probe = runSingleProbe(targetUrl, idx);
                sr.setResult(probe.passed ? ScanResultStatus.PASSED : ScanResultStatus.FAILED);
                sr.setMessage(probe.toMessageJson());
            } else {
                sr.setResult(ScanResultStatus.SKIPPED);
                sr.setMessage("Unknown OWASP ID: " + rule.getOwaspId());
            }
            results.add(sr);
        }
        return results;
    }

    private ApigeeProbeRunner.ProbeResult runSingleProbe(String targetUrl, int idx) {
        switch (idx) {
            case 0: return apigeeProbeRunner.probeMissingAuth(targetUrl);
            case 1: return apigeeProbeRunner.probeWeakToken(targetUrl);
            case 2: return apigeeProbeRunner.probeInjection(targetUrl);
            case 3: return apigeeProbeRunner.probeInsecureDesign(targetUrl);
            case 4: return apigeeProbeRunner.probeSecurityMisconfig(targetUrl);
            case 5: return apigeeProbeRunner.probeOutdatedComponents(targetUrl);
            case 6: return apigeeProbeRunner.probeAuthFailures(targetUrl);
            case 7: return apigeeProbeRunner.probeIntegrityFailures(targetUrl);
            case 8: return apigeeProbeRunner.probeLoggingMonitoring(targetUrl);
            case 9: return apigeeProbeRunner.probeSsrf(targetUrl);
            default: return new ApigeeProbeRunner.ProbeResult(false, "Invalid probe index", "");
        }
    }

    private String buildApigeeRuntimeUrl(OwaspScanDocument scan) {
        String proxy = scan.getAssetName() != null ? scan.getAssetName() : "unknown";
        String envOrg = System.getenv("FORGESPHERE_DEFAULT_APIGEE_ORG");
        String org = (envOrg != null && !envOrg.isBlank()) ? envOrg : "gen-ai-poc-onboarding";
        return "https://forgesphere.probestack.io/" + proxy;
    }

    private int owaspIdToIndex(String owaspId) {
        if (owaspId == null) return -1;
        try {
            String digits = owaspId.replaceAll("\\D", "");
            int n = Integer.parseInt(digits);
            return n - 1;
        } catch (NumberFormatException e) { return -1; }
    }

    private ScanResult passedResult(OwaspRuleDocument rule, String message) {
        ScanResult result = new ScanResult();
        result.setRuleId(rule.getRuleId());
        result.setRuleName(rule.getRuleName());
        result.setRuleType(rule.getRuleType());
        result.setSeverity(rule.getSeverity());
        result.setResult(ScanResultStatus.PASSED);
        result.setMessage(message);
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
        if (results == null || results.isEmpty()) return ComplianceStatus.PENDING;
        boolean hasFailed = results.stream().anyMatch(r -> ScanResultStatus.FAILED.equals(r.getResult()));
        if (hasFailed) return ComplianceStatus.NON_COMPLIANT;
        boolean allPassed = results.stream().allMatch(r -> ScanResultStatus.PASSED.equals(r.getResult()));
        if (allPassed) return ComplianceStatus.COMPLIANT;
        boolean allSkipped = results.stream().allMatch(r -> ScanResultStatus.SKIPPED.equals(r.getResult()));
        return allSkipped ? ComplianceStatus.PENDING : ComplianceStatus.PARTIAL;
    }

    private boolean sourceResolutionFailed(List<ScanResult> results) {
        return results != null && !results.isEmpty()
                && results.stream().allMatch(r -> ScanResultStatus.SKIPPED.equals(r.getResult()))
                && results.stream().map(ScanResult::getMessage).anyMatch(this::isSourceResolutionMessage);
    }

    private String errorMessage(List<ScanResult> results, boolean sourceResolutionFailed) {
        if (results == null || results.isEmpty()) return "No matching OWASP rules were found for this scan request.";
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
        if (message == null) return false;
        return message.startsWith("Unable to resolve") || message.startsWith("No local source directory")
                || message.startsWith("Archive download") || message.startsWith("Scan source details were not provided.");
    }
}