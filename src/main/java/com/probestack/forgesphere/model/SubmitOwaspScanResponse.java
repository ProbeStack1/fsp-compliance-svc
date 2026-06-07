package com.probestack.forgesphere.model;

import java.time.OffsetDateTime;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.annotation.Generated;

@Generated(value = "manual")
public class SubmitOwaspScanResponse {

    @JsonProperty("scanId")
    private String scanId;

    @JsonProperty("status")
    private ScanStatus status;

    @JsonProperty("submittedDate")
    private OffsetDateTime submittedDate;

    @JsonProperty("message")
    private String message;

    public String getScanId() {
        return scanId;
    }

    public void setScanId(String scanId) {
        this.scanId = scanId;
    }

    public ScanStatus getStatus() {
        return status;
    }

    public void setStatus(ScanStatus status) {
        this.status = status;
    }

    public OffsetDateTime getSubmittedDate() {
        return submittedDate;
    }

    public void setSubmittedDate(OffsetDateTime submittedDate) {
        this.submittedDate = submittedDate;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
