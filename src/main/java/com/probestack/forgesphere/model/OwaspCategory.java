package com.probestack.forgesphere.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import jakarta.annotation.Generated;

@Generated(value = "manual")
public enum OwaspCategory {

    A01_BROKEN_ACCESS_CONTROL("A01"),
    A02_CRYPTOGRAPHIC_FAILURES("A02"),
    A03_INJECTION("A03"),
    A04_INSECURE_DESIGN("A04"),
    A05_SECURITY_MISCONFIGURATION("A05"),
    A06_VULNERABLE_COMPONENTS("A06"),
    A07_IDENTIFICATION_AUTHENTICATION_FAILURES("A07"),
    A08_SOFTWARE_DATA_INTEGRITY_FAILURES("A08"),
    A09_SECURITY_LOGGING_MONITORING_FAILURES("A09"),
    A10_SERVER_SIDE_REQUEST_FORGERY("A10"),
    OTHER("OTHER");

    private final String value;

    OwaspCategory(String value) {
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

    @JsonCreator
    public static OwaspCategory fromValue(String value) {
        for (OwaspCategory category : OwaspCategory.values()) {
            if (category.value.equals(value)) {
                return category;
            }
        }
        throw new IllegalArgumentException("Unexpected value '" + value + "'");
    }
}
