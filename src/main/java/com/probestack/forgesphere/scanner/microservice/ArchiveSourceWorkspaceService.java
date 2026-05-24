package com.probestack.forgesphere.scanner.microservice;

import com.probestack.forgesphere.model.ScanSource;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.time.Duration;
import java.util.Comparator;
import java.util.Locale;
import java.util.Optional;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
class ArchiveSourceWorkspaceService {

    private static final Duration DOWNLOAD_TIMEOUT = Duration.ofMinutes(2);

    private final Path workspaceRoot;
    private final HttpClient httpClient;

    ArchiveSourceWorkspaceService(@Value("${compliance.scan.workspace-root}") String workspaceRoot) {
        this.workspaceRoot = Path.of(workspaceRoot).toAbsolutePath().normalize();
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(20))
                .followRedirects(HttpClient.Redirect.NORMAL)
                .build();
    }

    SourceResolution resolveArchive(String scanId, ScanSource source) {
        if (source == null || isBlank(source.getArchiveDownloadUrl())) {
            return SourceResolution.unresolved("Archive download URL was not provided.");
        }

        Path scanWorkspace = workspaceRoot.resolve(sanitize(scanId)).normalize();
        Path archiveFile = scanWorkspace.resolve("archive.zip").normalize();
        Path extractDirectory = scanWorkspace.resolve("source").normalize();
        ensureWithinWorkspace(scanWorkspace);
        ensureWithinWorkspace(archiveFile);
        ensureWithinWorkspace(extractDirectory);

        try {
            prepareWorkspace(scanWorkspace);
            fetchArchive(source.getArchiveDownloadUrl(), archiveFile);
            unzip(archiveFile, extractDirectory);
            return SourceResolution.resolved(extractDirectory);
        } catch (IOException ex) {
            return SourceResolution.unresolved("Unable to resolve archiveDownloadUrl source: " + ex.getMessage());
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
            return SourceResolution.unresolved("Archive download was interrupted.");
        } catch (RuntimeException ex) {
            return SourceResolution.unresolved(ex.getMessage());
        }
    }

    private void fetchArchive(String archiveDownloadUrl, Path archiveFile) throws IOException, InterruptedException {
        if (archiveDownloadUrl.startsWith("http://") || archiveDownloadUrl.startsWith("https://")) {
            HttpRequest request = HttpRequest.newBuilder(URI.create(archiveDownloadUrl))
                    .timeout(DOWNLOAD_TIMEOUT)
                    .GET()
                    .build();
            HttpResponse<Path> response = httpClient.send(request, HttpResponse.BodyHandlers.ofFile(archiveFile));
            if (response.statusCode() < 200 || response.statusCode() > 299) {
                throw new IOException("archive download failed with HTTP status " + response.statusCode());
            }
            return;
        }

        Path localArchive = archiveDownloadUrl.startsWith("file:")
                ? Path.of(URI.create(archiveDownloadUrl))
                : Path.of(archiveDownloadUrl);
        if (!Files.isRegularFile(localArchive)) {
            throw new IOException("archiveDownloadUrl does not point to an existing archive file");
        }
        Files.copy(localArchive, archiveFile, StandardCopyOption.REPLACE_EXISTING);
    }

    private void unzip(Path archiveFile, Path extractDirectory) throws IOException {
        Files.createDirectories(extractDirectory);
        try (InputStream inputStream = Files.newInputStream(archiveFile);
                ZipInputStream zipInputStream = new ZipInputStream(inputStream)) {
            ZipEntry entry;
            while ((entry = zipInputStream.getNextEntry()) != null) {
                Path target = extractDirectory.resolve(entry.getName()).normalize();
                if (!target.startsWith(extractDirectory)) {
                    throw new IOException("archive contains an unsafe path: " + entry.getName());
                }
                if (entry.isDirectory()) {
                    Files.createDirectories(target);
                } else {
                    Files.createDirectories(target.getParent());
                    Files.copy(zipInputStream, target, StandardCopyOption.REPLACE_EXISTING);
                }
                zipInputStream.closeEntry();
            }
        }
    }

    private void prepareWorkspace(Path scanWorkspace) throws IOException {
        Files.createDirectories(workspaceRoot);
        if (Files.exists(scanWorkspace)) {
            deleteRecursively(scanWorkspace);
        }
        Files.createDirectories(scanWorkspace);
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

    private boolean isBlank(String value) {
        return value == null || value.isBlank();
    }

    private String sanitize(String value) {
        return Optional.ofNullable(value)
                .orElse("scan")
                .toLowerCase(Locale.ROOT)
                .replaceAll("[^a-z0-9._-]", "-");
    }
}
