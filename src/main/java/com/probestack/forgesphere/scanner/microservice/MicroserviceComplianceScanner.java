package com.probestack.forgesphere.scanner.microservice;

import com.probestack.forgesphere.document.ComplianceRuleDocument;
import com.probestack.forgesphere.document.ComplianceScanDocument;
import com.probestack.forgesphere.model.ScanResult;
import com.probestack.forgesphere.model.ScanEvidence;
import com.probestack.forgesphere.model.ScanResultStatus;
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
public class MicroserviceComplianceScanner {

    private final HttpClient httpClient = HttpClient.newHttpClient();

    public List<ScanResult> scan(ComplianceScanDocument scan, List<ComplianceRuleDocument> rules) {
        List<ScanResult> results = new ArrayList<>();

        String archiveUrl = scan.getSource().getArchiveDownloadUrl();
        if (archiveUrl == null || archiveUrl.isBlank()) {
            for (ComplianceRuleDocument rule : rules) {
                results.add(skippedResult(rule, "No source archive URL provided."));
            }
            return results;
        }

        Path tempDir = null;
        try {
            // Download archive
            Path zipPath = Files.createTempFile("source", ".zip");
            HttpRequest request = HttpRequest.newBuilder().uri(URI.create(archiveUrl)).GET().build();
            HttpResponse<Path> response = httpClient.send(request, HttpResponse.BodyHandlers.ofFile(zipPath));
            if (response.statusCode() != 200) {
                throw new IOException("Download failed: " + response.statusCode());
            }

            // Extract to temp directory
            tempDir = Files.createTempDirectory("scanner_");
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

            // Evaluate each rule
            for (ComplianceRuleDocument rule : rules) {
                results.add(evaluateRule(rule, tempDir));
            }

        } catch (Exception e) {
            for (ComplianceRuleDocument rule : rules) {
                results.add(skippedResult(rule, "Scan failed: " + e.getMessage()));
            }
        } finally {
            if (tempDir != null) {
                try { deleteDirectory(tempDir); } catch (IOException ignored) {}
            }
        }
        return results;
    }

    private ScanResult evaluateRule(ComplianceRuleDocument rule, Path sourceDir) {
        ScanResult result = new ScanResult();
        result.setRuleId(rule.getRuleId());
        result.setRuleName(rule.getRuleName());
        result.setRuleType(rule.getRuleType());
        result.setSeverity(rule.getSeverity());

        String implKey = rule.getImplementationKey();

        // Example rules – extend as needed
        if (implKey.contains("OPENAPI") || implKey.contains("API_DOCUMENTATION")) {
            boolean hasOpenApi = Files.exists(sourceDir.resolve("openapi.yaml")) ||
                                 Files.exists(sourceDir.resolve("swagger.json")) ||
                                 Files.exists(sourceDir.resolve("api-docs.yaml"));
            result.setResult(hasOpenApi ? ScanResultStatus.PASSED : ScanResultStatus.FAILED);
            result.setMessage(hasOpenApi ? "OpenAPI spec found." : "OpenAPI spec missing.");
        }
        else if (implKey.contains("LOGGING")) {
            boolean hasLogging = Files.exists(sourceDir.resolve("logback.xml")) ||
                                 fileContains(sourceDir, "application.properties", "logging.level");
            result.setResult(hasLogging ? ScanResultStatus.PASSED : ScanResultStatus.FAILED);
            result.setMessage(hasLogging ? "Logging configured." : "No logging configuration.");
        }
        else {
            // Default: PASS if rule enabled and active
            if (Boolean.TRUE.equals(rule.getEnabled()) && "ACTIVE".equals(rule.getStatus())) {
                result.setResult(ScanResultStatus.PASSED);
                result.setMessage("Rule passed by default.");
            } else {
                result.setResult(ScanResultStatus.SKIPPED);
                result.setMessage("Rule inactive/disabled.");
            }
        }
        return result;
    }

    private boolean fileContains(Path dir, String fileName, String text) {
        Path file = dir.resolve(fileName);
        if (Files.exists(file)) {
            try {
                return Files.readString(file).contains(text);
            } catch (IOException ignored) {}
        }
        return false;
    }

    private ScanResult skippedResult(ComplianceRuleDocument rule, String message) {
        ScanResult result = new ScanResult();
        result.setRuleId(rule.getRuleId());
        result.setRuleName(rule.getRuleName());
        result.setResult(ScanResultStatus.SKIPPED);
        result.setMessage(message);
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