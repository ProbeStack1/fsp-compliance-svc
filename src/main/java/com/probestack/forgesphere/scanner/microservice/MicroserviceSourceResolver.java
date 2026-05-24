package com.probestack.forgesphere.scanner.microservice;

import com.probestack.forgesphere.model.ScanSource;
import com.probestack.forgesphere.model.SourceType;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;
import org.springframework.stereotype.Component;

@Component
public class MicroserviceSourceResolver {

    private final GitSourceWorkspaceService gitSourceWorkspaceService;
    private final ArchiveSourceWorkspaceService archiveSourceWorkspaceService;

    public MicroserviceSourceResolver(GitSourceWorkspaceService gitSourceWorkspaceService,
            ArchiveSourceWorkspaceService archiveSourceWorkspaceService) {
        this.gitSourceWorkspaceService = gitSourceWorkspaceService;
        this.archiveSourceWorkspaceService = archiveSourceWorkspaceService;
    }

    public SourceResolution resolve(String scanId, SourceType sourceType, ScanSource source) {
        if (source == null) {
            return SourceResolution.unresolved("Scan source details were not provided.");
        }

        if (source.getArchiveDownloadUrl() != null && !source.getArchiveDownloadUrl().isBlank()) {
            return archiveSourceWorkspaceService.resolveArchive(scanId, source);
        }

        Optional<Path> localPath = firstExisting(source.getRepositoryUrl())
                .or(() -> firstExisting(source.getBundleFileId()))
                .or(() -> firstExisting(source.getBundleName()));
        if (localPath.isPresent()) {
            return SourceResolution.resolved(localPath.get());
        }

        if (source.getRepositoryUrl() != null && isRemoteRepository(source.getRepositoryUrl())) {
            return gitSourceWorkspaceService.cloneRepository(scanId, sourceType, source);
        }

        return SourceResolution.unresolved("No local source directory or remote Git repository URL could be resolved.");
    }

    private Optional<Path> firstExisting(String location) {
        if (location == null || location.isBlank()) {
            return Optional.empty();
        }
        if (isRemoteRepository(location)) {
            return Optional.empty();
        }

        try {
            Path path = location.startsWith("file:")
                    ? Path.of(URI.create(location))
                    : Path.of(location);
            return Files.exists(path) && Files.isDirectory(path) ? Optional.of(path.toAbsolutePath().normalize()) : Optional.empty();
        } catch (IllegalArgumentException ex) {
            return Optional.empty();
        }
    }

    private boolean isRemoteRepository(String location) {
        return location.startsWith("http://") || location.startsWith("https://") || location.endsWith(".git");
    }
}
