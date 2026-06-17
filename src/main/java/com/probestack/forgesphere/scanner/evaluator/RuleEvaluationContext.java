package com.probestack.forgesphere.scanner.evaluator;

import com.probestack.forgesphere.model.AssetType;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Pattern;
import java.util.stream.Stream;

/**
 * Carries all evidence sources a rule evaluator may inspect for a single
 * compliance scan:
 *   • {@link #sourceRoot()} — extracted source bundle (Spring Boot project,
 *     Apigee proxy zip, Kong declarative config dir). Null when not resolvable.
 *   • {@link #liveBaseUrl()} — runtime URL where probes can be fired. Always
 *     non-null (built from {@link com.probestack.forgesphere.scanner.evaluator.LiveEndpointResolver}).
 *   • {@link #assetType()}, {@link #assetName()}, {@link #scanId()} — metadata.
 *
 * The context lazily walks the source tree once and caches small file contents
 * so per-rule handlers can grep without re-reading from disk every time.
 */
public class RuleEvaluationContext {

    private static final long MAX_FILE_BYTES = 1_000_000L;
    private static final Pattern EXCLUDED_PATH = Pattern.compile(
            "(^|[\\\\/])(?:target|build|dist|node_modules|\\.git|\\.idea|\\.vscode)([\\\\/]|$)");

    private final Path sourceRoot;
    private final String liveBaseUrl;
    private final AssetType assetType;
    private final String assetName;
    private final String scanId;
    private final List<Path> files;
    private final Map<Path, String> contentCacheLower = new HashMap<>();
    private final Map<Path, String> contentCacheRaw = new HashMap<>();

    public RuleEvaluationContext(Path sourceRoot, String liveBaseUrl,
            AssetType assetType, String assetName, String scanId) {
        this.sourceRoot = sourceRoot;
        this.liveBaseUrl = liveBaseUrl;
        this.assetType = assetType;
        this.assetName = assetName;
        this.scanId = scanId;
        this.files = sourceRoot == null ? List.of() : discoverFiles(sourceRoot);
    }

    public Path sourceRoot() { return sourceRoot; }
    public String liveBaseUrl() { return liveBaseUrl; }
    public AssetType assetType() { return assetType; }
    public String assetName() { return assetName; }
    public String scanId() { return scanId; }
    public boolean hasSource() { return sourceRoot != null; }
    public boolean hasLiveUrl() { return liveBaseUrl != null && !liveBaseUrl.isBlank(); }

    public List<Path> files() { return files; }

    public boolean hasFileNamed(String name) {
        String wanted = name.toLowerCase(Locale.ROOT);
        return files.stream()
                .anyMatch(p -> p.getFileName().toString().toLowerCase(Locale.ROOT).equals(wanted));
    }

    public Path firstFileNamed(String... names) {
        for (String n : names) {
            String wanted = n.toLowerCase(Locale.ROOT);
            Path hit = files.stream()
                    .filter(p -> p.getFileName().toString().toLowerCase(Locale.ROOT).equals(wanted))
                    .findFirst()
                    .orElse(null);
            if (hit != null) return hit;
        }
        return null;
    }

    public Path firstPathMatching(Pattern pattern) {
        return files.stream()
                .filter(p -> pattern.matcher(normalize(p)).find())
                .findFirst()
                .orElse(null);
    }

    public List<Path> allPathsMatching(Pattern pattern) {
        return files.stream()
                .filter(p -> pattern.matcher(normalize(p)).find())
                .toList();
    }

    /** Greps for a pattern across all files (case-insensitive search uses lowercased cache). */
    public Path firstContentMatch(Pattern pattern) {
        for (Path p : files) {
            if (pattern.matcher(contentLower(p)).find()) {
                return p;
            }
        }
        return null;
    }

    public List<Path> contentMatches(Pattern pattern) {
        List<Path> hits = new ArrayList<>();
        for (Path p : files) {
            if (pattern.matcher(contentLower(p)).find()) hits.add(p);
        }
        return hits;
    }

    /** Returns raw (case-preserved) content of a file or "" if unreadable. */
    public String readRaw(Path path) {
        return contentCacheRaw.computeIfAbsent(path, this::readFile);
    }

    /** Returns lowercased content (for case-insensitive grep). */
    public String contentLower(Path path) {
        return contentCacheLower.computeIfAbsent(path, p -> readFile(p).toLowerCase(Locale.ROOT));
    }

    public String relative(Path path) {
        if (path == null || sourceRoot == null) return "";
        return sourceRoot.relativize(path).toString().replace('\\', '/');
    }

    private String readFile(Path path) {
        try {
            return Files.readString(path, StandardCharsets.UTF_8);
        } catch (IOException | RuntimeException ex) {
            try {
                return new String(Files.readAllBytes(path), StandardCharsets.ISO_8859_1);
            } catch (IOException e2) {
                return "";
            }
        }
    }

    private static List<Path> discoverFiles(Path root) {
        try (Stream<Path> stream = Files.walk(root)) {
            return stream
                    .filter(Files::isRegularFile)
                    .filter(p -> !EXCLUDED_PATH.matcher(p.toString()).find())
                    .filter(p -> readableSize(p) <= MAX_FILE_BYTES)
                    .toList();
        } catch (IOException ex) {
            return List.of();
        }
    }

    private static long readableSize(Path path) {
        try { return Files.size(path); } catch (IOException ex) { return Long.MAX_VALUE; }
    }

    private static String normalize(Path path) {
        return path.toString().replace('\\', '/').toLowerCase(Locale.ROOT);
    }
}
