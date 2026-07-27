package com.probestack.forgesphere.scanner.proxy;

import com.probestack.forgesphere.document.ComplianceRuleDocument;
import com.probestack.forgesphere.document.ComplianceScanDocument;
import com.probestack.forgesphere.model.ScanResult;
import com.probestack.forgesphere.model.ScanResultStatus;
import com.probestack.forgesphere.scanner.evaluator.EvaluationOutcome;
import com.probestack.forgesphere.scanner.evaluator.LiveEndpointResolver;
import com.probestack.forgesphere.scanner.evaluator.ProxyRuleEvaluator;
import com.probestack.forgesphere.scanner.evaluator.RuleEvaluationContext;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.zip.ZipInputStream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

/**
 * Drives compliance scans for proxy assets — Apigee proxy bundles and Kong
 * declarative configs. The actual rule-by-rule evaluation lives in
 * {@link ProxyRuleEvaluator}; this class is responsible for materialising the
 * bundle on disk and constructing the live-URL probe context.
 */
@Service
public class ProxyComplianceScanner {

    private static final Logger log = LoggerFactory.getLogger(ProxyComplianceScanner.class);

    private final HttpClient httpClient = HttpClient.newHttpClient();
    private final ProxyRuleEvaluator evaluator;
    private final LiveEndpointResolver liveEndpointResolver;

    public ProxyComplianceScanner(ProxyRuleEvaluator evaluator, LiveEndpointResolver liveEndpointResolver) {
        this.evaluator = evaluator;
        this.liveEndpointResolver = liveEndpointResolver;
    }

    public List<ScanResult> scan(ComplianceScanDocument scan, List<ComplianceRuleDocument> rules) {
        String archiveUrl = scan.getSource() == null ? null : scan.getSource().getArchiveDownloadUrl();
        String liveUrl = liveEndpointResolver.resolve(scan);
        Path sourceRoot = null;
        Path tempDir = null;
        Path zipPath = null;
        String sourceFailure = null;

        if (archiveUrl != null && !archiveUrl.isBlank()) {
            try {
                zipPath = Files.createTempFile("proxy", ".zip");
                HttpRequest request = HttpRequest.newBuilder().uri(URI.create(archiveUrl)).GET().build();
                HttpResponse<Path> response = httpClient.send(request, HttpResponse.BodyHandlers.ofFile(zipPath));
                if (response.statusCode() != 200) {
                    throw new IOException("Bundle download returned HTTP " + response.statusCode());
                }
                tempDir = Files.createTempDirectory("proxy_scanner_");
                try (ZipInputStream zis = new ZipInputStream(Files.newInputStream(zipPath))) {
                    java.util.zip.ZipEntry entry;
                    while ((entry = zis.getNextEntry()) != null) {
                        Path target = tempDir.resolve(entry.getName()).normalize();
                        if (!target.startsWith(tempDir)) { // zip-slip guard
                            zis.closeEntry();
                            continue;
                        }
                        if (entry.isDirectory()) {
                            Files.createDirectories(target);
                        } else {
                            if (target.getParent() != null) Files.createDirectories(target.getParent());
                            Files.copy(zis, target);
                        }
                        zis.closeEntry();
                    }
                }
                sourceRoot = tempDir;
            } catch (Exception e) {
                sourceFailure = "Bundle download failed: " + e.getMessage();
                log.warn("proxy scan {} bundle download failed: {}", scan.getScanId(), e.toString());
            }
        }

        RuleEvaluationContext ctx = new RuleEvaluationContext(
                sourceRoot, liveUrl, scan.getAssetType(), scan.getAssetName(), scan.getScanId());

        List<ScanResult> results = new ArrayList<>(rules.size());
        try {
            for (ComplianceRuleDocument rule : rules) {
                EvaluationOutcome outcome = evaluator.evaluate(rule, ctx);
                results.add(toScanResult(rule, outcome, sourceFailure));
            }
        } finally {
            if (tempDir != null) {
                try { deleteDirectory(tempDir); } catch (IOException ignored) { }
            }
            if (zipPath != null) {
                try { Files.deleteIfExists(zipPath); } catch (IOException ignored) { }
            }
        }
        return results;
    }

    private static ScanResult toScanResult(ComplianceRuleDocument rule, EvaluationOutcome outcome, String sourceFailure) {
        ScanResult r = new ScanResult();
        r.setRuleId(rule.getRuleId());
        r.setRuleName(rule.getRuleName());
        r.setRuleType(rule.getRuleType());
        r.setCategory(rule.getCategory());
        r.setSeverity(rule.getSeverity());
        ScanResultStatus status = outcome.status();
        r.setResult(status == null ? ScanResultStatus.SKIPPED : status);
        String message = outcome.toJsonMessage();
        if (sourceFailure != null && ScanResultStatus.SKIPPED.equals(r.getResult())) {
            r.setMessage(sourceFailure + " | " + message);
        } else {
            r.setMessage(message);
        }
        if (outcome.structuredEvidence() != null && !outcome.structuredEvidence().isEmpty()) {
            r.setEvidence(outcome.structuredEvidence());
        }
        return r;
    }

    private static void deleteDirectory(Path dir) throws IOException {
        if (!Files.exists(dir)) return;
        try (var stream = Files.walk(dir)) {
            stream.sorted(Comparator.reverseOrder()).forEach(p -> {
                try { Files.deleteIfExists(p); } catch (IOException ignored) { }
            });
        }
    }
}
