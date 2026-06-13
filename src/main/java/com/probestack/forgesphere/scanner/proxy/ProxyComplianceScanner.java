package com.probestack.forgesphere.scanner.proxy;

import com.probestack.forgesphere.document.ComplianceRuleDocument;
import com.probestack.forgesphere.document.ComplianceScanDocument;
import com.probestack.forgesphere.model.ScanResult;
import com.probestack.forgesphere.model.ScanEvidence;
import com.probestack.forgesphere.model.ScanResultStatus;
import com.probestack.forgesphere.scanner.probe.ApigeeProbeRunner;
import org.springframework.stereotype.Service;

import java.io.*;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.*;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.ZipInputStream;

@Service
public class ProxyComplianceScanner {

    private final HttpClient httpClient = HttpClient.newHttpClient();
    private final ApigeeProbeRunner apigeeProbeRunner;

    public ProxyComplianceScanner(ApigeeProbeRunner apigeeProbeRunner) {
        this.apigeeProbeRunner = apigeeProbeRunner;
    }

    public List<ScanResult> scan(ComplianceScanDocument scan, List<ComplianceRuleDocument> rules) {
        // For Apigee, we can also run some probe‑based checks for certain rules
        if (com.probestack.forgesphere.model.AssetType.APIGEE.equals(scan.getAssetType())) {
            return runApigeeProbes(scan, rules);
        }

        String archiveUrl = scan.getSource().getArchiveDownloadUrl();
        if (archiveUrl == null || archiveUrl.isBlank()) {
            return rules.stream()
                    .map(rule -> metadataOnlyResult(rule, "No proxy bundle URL provided."))
                    .toList();
        }

        Path tempDir = null;
        try {
            Path zipPath = Files.createTempFile("proxy", ".zip");
            HttpRequest request = HttpRequest.newBuilder().uri(URI.create(archiveUrl)).GET().build();
            HttpResponse<Path> response = httpClient.send(request, HttpResponse.BodyHandlers.ofFile(zipPath));
            if (response.statusCode() != 200) {
                throw new IOException("Download failed: " + response.statusCode());
            }

            tempDir = Files.createTempDirectory("proxy_scanner_");
            try (ZipInputStream zis = new ZipInputStream(Files.newInputStream(zipPath))) {
                java.util.zip.ZipEntry entry;
                while ((entry = zis.getNextEntry()) != null) {
                    Path target = tempDir.resolve(entry.getName());
                    if (entry.isDirectory()) {
                        Files.createDirectories(target);
                    } else {
                        Files.createDirectories(target.getParent());
                        Files.copy(zis, target);
                    }
                    zis.closeEntry();
                }
            }

            List<ScanResult> results = new ArrayList<>();
            for (ComplianceRuleDocument rule : rules) {
                results.add(evaluateProxyRule(rule, tempDir));
            }
            return results;
        } catch (Exception e) {
            return rules.stream()
                    .map(rule -> metadataOnlyResult(rule, "Scan failed: " + e.getMessage()))
                    .toList();
        } finally {
            if (tempDir != null) {
                try { deleteDirectory(tempDir); } catch (IOException ignored) {}
            }
        }
    }

    private List<ScanResult> runApigeeProbes(ComplianceScanDocument scan, List<ComplianceRuleDocument> rules) {
        String targetUrl = "https://forgesphere.probestack.io/" + (scan.getAssetName() != null ? scan.getAssetName() : "unknown");
        // Pre-run a few probes that can be reused for multiple rules
        ApigeeProbeRunner.ProbeResult missingAuth = apigeeProbeRunner.probeMissingAuth(targetUrl);
        ApigeeProbeRunner.ProbeResult securityHeaders = apigeeProbeRunner.probeSecurityMisconfig(targetUrl);
        ApigeeProbeRunner.ProbeResult rateLimit = apigeeProbeRunner.probeInsecureDesign(targetUrl);
        ApigeeProbeRunner.ProbeResult httpsRedirect = apigeeProbeRunner.probeIntegrityFailures(targetUrl);

        List<ScanResult> results = new ArrayList<>();
        for (ComplianceRuleDocument rule : rules) {
            String ruleId = rule.getRuleId() != null ? rule.getRuleId() : "";
            ApigeeProbeRunner.ProbeResult mapped = null;
            if (ruleId.endsWith("_SEC_003")) mapped = missingAuth;
            else if (ruleId.endsWith("_SEC_006") || ruleId.endsWith("_PERF_001")) mapped = rateLimit;
            else if (ruleId.endsWith("_SEC_001") || ruleId.endsWith("_SEC_007")) mapped = httpsRedirect;
            else if (ruleId.contains("_SEC_") || ruleId.contains("_HEALTH_")) mapped = securityHeaders;

            if (mapped != null) {
                ScanResult sr = new ScanResult();
                sr.setRuleId(rule.getRuleId());
                sr.setRuleName(rule.getRuleName());
                sr.setRuleType(rule.getRuleType());
                sr.setSeverity(rule.getSeverity());
                sr.setResult(mapped.passed ? ScanResultStatus.PASSED : ScanResultStatus.FAILED);
                sr.setMessage(mapped.toMessageJson());
                results.add(sr);
            } else {
                // Bundle‑only rule – fallback to metadata check
                results.add(metadataOnlyResult(rule, "Rule requires proxy bundle inspection; probe‑based evaluation not applicable."));
            }
        }
        return results;
    }

    private ScanResult evaluateProxyRule(ComplianceRuleDocument rule, Path extractedDir) {
        ScanResult result = new ScanResult();
        result.setRuleId(rule.getRuleId());
        result.setRuleName(rule.getRuleName());
        result.setRuleType(rule.getRuleType());
        result.setSeverity(rule.getSeverity());

        String implKey = rule.getImplementationKey();

        if (implKey.contains("OAUTH") || implKey.contains("AUTH")) {
            boolean hasOAuth = Files.exists(extractedDir.resolve("policies/OAuth.xml")) ||
                               Files.exists(extractedDir.resolve("policies/VerifyAPIKey.xml"));
            result.setResult(hasOAuth ? ScanResultStatus.PASSED : ScanResultStatus.FAILED);
            result.setMessage(hasOAuth ? "Auth policy found." : "No auth policy.");
        }
        else if (implKey.contains("QUOTA") || implKey.contains("RATE_LIMIT")) {
            boolean hasQuota = Files.exists(extractedDir.resolve("policies/Quota.xml")) ||
                               Files.exists(extractedDir.resolve("policies/SpikeArrest.xml"));
            result.setResult(hasQuota ? ScanResultStatus.PASSED : ScanResultStatus.FAILED);
            result.setMessage(hasQuota ? "Rate limiting policy present." : "Missing rate limiting.");
        }
        else {
            result.setResult(ScanResultStatus.PASSED);
            result.setMessage("Proxy rule passed by default.");
        }
        return result;
    }

    private ScanResult metadataOnlyResult(ComplianceRuleDocument rule, String reason) {
        ScanResult result = new ScanResult();
        result.setRuleId(rule.getRuleId());
        result.setRuleName(rule.getRuleName());
        result.setRuleType(rule.getRuleType());
        result.setSeverity(rule.getSeverity());
        boolean isActive = com.probestack.forgesphere.model.RuleStatus.ACTIVE.equals(rule.getStatus())
                && Boolean.TRUE.equals(rule.getEnabled());
        if (isActive) {
            result.setResult(ScanResultStatus.PASSED);
            result.setMessage("Evaluated from rule metadata (bundle not available: " + reason + ")");
        } else {
            result.setResult(ScanResultStatus.SKIPPED);
            result.setMessage("Rule is not active or enabled.");
        }
        return result;
    }

    private void deleteDirectory(Path dir) throws IOException {
        if (Files.exists(dir)) {
            Files.walk(dir).sorted(java.util.Comparator.reverseOrder()).forEach(p -> {
                try { Files.deleteIfExists(p); } catch (IOException ignored) {}
            });
        }
    }
}