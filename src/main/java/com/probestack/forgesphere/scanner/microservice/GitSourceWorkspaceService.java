package com.probestack.forgesphere.scanner.microservice;

import com.probestack.forgesphere.model.ScanSource;
import com.probestack.forgesphere.model.SourceType;
import java.io.IOException;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
class GitSourceWorkspaceService {

    private static final Duration CLONE_TIMEOUT = Duration.ofMinutes(2);

    private final Path workspaceRoot;
    private final AccessTokenResolver accessTokenResolver;

    GitSourceWorkspaceService(@Value("${compliance.scan.workspace-root}") String workspaceRoot,
            AccessTokenResolver accessTokenResolver) {
        this.workspaceRoot = Path.of(workspaceRoot).toAbsolutePath().normalize();
        this.accessTokenResolver = accessTokenResolver;
    }

    SourceResolution cloneRepository(String scanId, SourceType sourceType, ScanSource source) {
        if (!isGitSource(sourceType)) {
            return SourceResolution.unresolved("Source type " + sourceType + " is not a Git repository source.");
        }
        if (source == null || source.getRepositoryUrl() == null || source.getRepositoryUrl().isBlank()) {
            return SourceResolution.unresolved("Repository URL is required for " + sourceType + " source scans.");
        }
        String repositoryUrl = source.getRepositoryUrl();
        if (!repositoryUrl.startsWith("http://") && !repositoryUrl.startsWith("https://")) {
            return SourceResolution.unresolved("Only HTTP(S) repository URLs are supported for remote Git source acquisition.");
        }

        Path destination = workspaceRoot.resolve(sanitize(scanId)).resolve("source").normalize();
        ensureWithinWorkspace(destination);
        prepareDestination(destination);

        List<String> command = new ArrayList<>();
        command.add("git");
        command.add("clone");
        command.add("--depth");
        command.add("1");
        if (source.getBranch() != null && !source.getBranch().isBlank()) {
            command.add("--branch");
            command.add(source.getBranch());
        }
        command.add(repositoryUrl);
        command.add(destination.toString());

        ProcessBuilder processBuilder = new ProcessBuilder(command);
        processBuilder.redirectOutput(ProcessBuilder.Redirect.DISCARD);
        processBuilder.redirectError(ProcessBuilder.Redirect.DISCARD);
        accessTokenResolver.resolve(source.getAccessTokenRef()).ifPresent(token -> {
            processBuilder.environment().put("GIT_CONFIG_COUNT", "1");
            processBuilder.environment().put("GIT_CONFIG_KEY_0", "http.extraHeader");
            processBuilder.environment().put("GIT_CONFIG_VALUE_0", "Authorization: Bearer " + token);
        });

        try {
            Process process = processBuilder.start();
            boolean completed = process.waitFor(CLONE_TIMEOUT.toSeconds(), TimeUnit.SECONDS);
            if (!completed) {
                process.destroyForcibly();
                return SourceResolution.unresolved("Git clone timed out while fetching repository source.");
            }
            if (process.exitValue() != 0) {
                return SourceResolution.unresolved("Git clone failed. Verify repository URL, branch, network access, and accessTokenRef.");
            }
            return SourceResolution.resolved(destination);
        } catch (IOException ex) {
            return SourceResolution.unresolved("Unable to execute git clone: " + ex.getMessage());
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
            return SourceResolution.unresolved("Git clone was interrupted.");
        }
    }

    private boolean isGitSource(SourceType sourceType) {
        return SourceType.GITHUB.equals(sourceType)
                || SourceType.GITLAB.equals(sourceType)
                || SourceType.BITBUCKET.equals(sourceType);
    }

    private void prepareDestination(Path destination) {
        try {
            Files.createDirectories(workspaceRoot);
            if (Files.exists(destination)) {
                deleteRecursively(destination);
            }
            Files.createDirectories(destination.getParent());
        } catch (IOException ex) {
            throw new IllegalArgumentException("Unable to prepare scan workspace: " + destination, ex);
        }
    }

    private void deleteRecursively(Path path) throws IOException {
        ensureWithinWorkspace(path);
        try (var paths = Files.walk(path)) {
            paths.sorted(Comparator.reverseOrder()).forEach(this::deletePath);
        }
    }

    private void deletePath(Path path) {
        try {
            Files.deleteIfExists(path);
        } catch (IOException ex) {
            throw new IllegalArgumentException("Unable to clean scan workspace path: " + path, ex);
        }
    }

    private void ensureWithinWorkspace(Path path) {
        if (!path.toAbsolutePath().normalize().startsWith(workspaceRoot)) {
            throw new IllegalArgumentException("Refusing to write outside compliance scan workspace.");
        }
    }

    private String sanitize(String value) {
        return Optional.ofNullable(value)
                .orElse("scan")
                .toLowerCase(Locale.ROOT)
                .replaceAll("[^a-z0-9._-]", "-");
    }
}
