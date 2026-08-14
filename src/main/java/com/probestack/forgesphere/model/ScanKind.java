package com.probestack.forgesphere.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

/**
 * The family of rules a scan ran against.
 *
 * The service already keeps these in separate collections behind separate endpoints; this names
 * them so a cut-off can be configured per family. Hand-written rather than generated, and not
 * referenced by any existing model, so no current schema changes.
 */
public enum ScanKind {

    COMPLIANCE("COMPLIANCE"),

    OWASP("OWASP"),

    LINTING("LINTING");

    private final String value;

    ScanKind(String value) {
        this.value = value;
    }

    @JsonValue
    public String getValue() {
        return value;
    }

    @Override
    public String toString() {
        return String.valueOf(value);
    }

    /** Lenient so a path variable may be lower-case. */
    @JsonCreator
    public static ScanKind fromValue(String value) {
        if (value == null) {
            return null;
        }
        for (ScanKind kind : ScanKind.values()) {
            if (kind.value.equalsIgnoreCase(value.trim())) {
                return kind;
            }
        }
        throw new IllegalArgumentException("Unexpected scan kind '" + value + "'");
    }
}
