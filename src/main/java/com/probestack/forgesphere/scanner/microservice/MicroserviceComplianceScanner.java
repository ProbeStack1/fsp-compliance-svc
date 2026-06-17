package com.probestack.forgesphere.scanner.microservice;

import com.probestack.forgesphere.document.ComplianceRuleDocument;
import com.probestack.forgesphere.document.ComplianceScanDocument;
import com.probestack.forgesphere.model.ScanResult;
import com.probestack.forgesphere.model.ScanResultStatus;
import com.probestack.forgesphere.scanner.evaluator.EvaluationOutcome;
import com.probestack.forgesphere.scanner.evaluator.LiveEndpointResolver;
import com.probestack.forgesphere.scanner.evaluator.MicroserviceRuleEvaluator;
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
 * Drives compliance scans for assets of type {@link com.probestack.forgesphere.model.AssetType#MICROSERVICE}.
 *
 * Pipeline:
 *   1. Download the source archive (if a URL is provided).
 *   2. Unzip into a temp dir under java.io.tmpdir.
 *   3. Build a {@link RuleEvaluationContext} (source root + live URL).
 *   4. Hand each rule to {@link MicroserviceRuleEvaluator} which runs the real
 *      evaluation logic (HTTP probe / pom inspection / annotation grep …).
 *   5. Map outcomes into {@link ScanResult} rows for persistence.
 */
@Service
public class MicroserviceComplianceScanner {

    private static final Logger log = LoggerFactory.getLogger(MicroserviceComplianceScanner.class);

    private final HttpClient httpClient = HttpClient.newHttpClient();
    private final MicroserviceRuleEvaluator evaluator;
    private final LiveEndpointResolver liveEndpointResolver;

    public MicroserviceComplianceScanner(MicroserviceRuleEvaluator evaluator,
                                         LiveEndpointResolver liveEndpointResolver) {
        this.evaluator = evaluator;
        this.liveEndpointResolver = liveEndpointResolver;
    }

    public List<ScanResult> scan(ComplianceScanDocument scan, List<ComplianceRuleDocument> rules) {
        String archiveUrl = scan.getSource() == null ? null : scan.getSource().getArchiveDownloadUrl();
        String liveUrl = liveEndpointResolver.resolve(scan);
        Path sourceRoot = null;
        Path tempDir = null;
        String sourceFailure = null;

        if (archiveUrl != null && !archiveUrl.isBlank()) {
            try {
                Path zipPath = Files.createTempFile("source", ".zip");
                HttpRequest request = HttpRequest.newBuilder().uri(URI.create(archiveUrl)).GET().build();
                HttpResponse<Path> response = httpClient.send(request, HttpResponse.BodyHandlers.ofFile(zipPath));
                if (response.statusCode() != 200) {
                    throw new IOException("Archive download returned HTTP " + response.statusCode());
                }
                tempDir = Files.createTempDirectory("scanner_");
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
                sourceFailure = "Archive download failed: " + e.getMessage();
                log.warn("scan {} archive download failed: {}", scan.getScanId(), e.toString());
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
                try { deleteDirectory(tempDir); } catch (IOException ignored) { /* best-effort */ }
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
        // Prefix message with source-failure hint so the UI can show "scan still ran on live URL despite archive miss".
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
