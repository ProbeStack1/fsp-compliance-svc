package com.probestack.forgesphere.scanner.microservice;

import java.nio.file.Path;

public record SourceResolution(Path sourcePath, String message) {

    public boolean resolved() {
        return sourcePath != null;
    }

    public static SourceResolution resolved(Path sourcePath) {
        return new SourceResolution(sourcePath, "Source resolved successfully.");
    }

    public static SourceResolution unresolved(String message) {
        return new SourceResolution(null, message);
    }
}
