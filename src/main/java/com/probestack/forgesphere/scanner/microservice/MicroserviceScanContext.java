package com.probestack.forgesphere.scanner.microservice;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Pattern;
import java.util.stream.Stream;

public class MicroserviceScanContext {

    private static final long MAX_FILE_BYTES = 512_000L;
    private static final Pattern EXCLUDED_PATHS =
            Pattern.compile("(^|[\\\\/])(?:target|build|dist|node_modules|\\.git|\\.idea|\\.vscode)([\\\\/]|$)");

    private final Path root;
    private final List<Path> files;
    private final Map<Path, String> contentCache = new ConcurrentHashMap<>();

    private MicroserviceScanContext(Path root, List<Path> files) {
        this.root = root;
        this.files = files;
    }

    public static MicroserviceScanContext from(Path root) {
        try (Stream<Path> stream = Files.walk(root)) {
            List<Path> discoveredFiles = stream
                    .filter(Files::isRegularFile)
                    .filter(path -> !EXCLUDED_PATHS.matcher(path.toString()).find())
                    .filter(path -> readableSize(path) <= MAX_FILE_BYTES)
                    .toList();
            return new MicroserviceScanContext(root, discoveredFiles);
        } catch (IOException ex) {
            throw new IllegalArgumentException("Unable to inspect source path: " + root, ex);
        }
    }

    public Path root() {
        return root;
    }

    public List<Path> files() {
        return files;
    }

    public boolean hasFileNamed(String fileName) {
        return files.stream().anyMatch(path -> path.getFileName().toString().equalsIgnoreCase(fileName));
    }

    public boolean hasFileMatching(Pattern pattern) {
        return files.stream().anyMatch(path -> pattern.matcher(normalize(path)).find());
    }

    public boolean contains(Pattern pattern) {
        return firstMatch(pattern) != null;
    }

    public Path firstMatch(Pattern pattern) {
        return files.stream()
                .filter(path -> pattern.matcher(content(path)).find())
                .findFirst()
                .orElse(null);
    }

    public Path firstPathMatch(Pattern pattern) {
        return files.stream()
                .filter(path -> pattern.matcher(normalize(path)).find())
                .findFirst()
                .orElse(null);
    }

    public String relative(Path path) {
        if (path == null) {
            return root.toString();
        }
        return root.relativize(path).toString().replace('\\', '/');
    }

    private String content(Path path) {
        return contentCache.computeIfAbsent(path, this::read);
    }

    private String read(Path path) {
        try {
            return Files.readString(path, StandardCharsets.UTF_8).toLowerCase(Locale.ROOT);
        } catch (IOException | RuntimeException ex) {
            return "";
        }
    }

    private static long readableSize(Path path) {
        try {
            return Files.size(path);
        } catch (IOException ex) {
            return Long.MAX_VALUE;
        }
    }

    private static String normalize(Path path) {
        return path.toString().replace('\\', '/').toLowerCase(Locale.ROOT);
    }
}
