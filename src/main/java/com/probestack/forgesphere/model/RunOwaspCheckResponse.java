package com.probestack.forgesphere.model;

import java.util.List;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.annotation.Generated;

@Generated(value = "manual")
public class RunOwaspCheckResponse {

    @JsonProperty("submittedScans")
    private Integer submittedScans;

    @JsonProperty("scans")
    private List<SubmitOwaspScanResponse> scans;

    @JsonProperty("message")
    private String message;

    public Integer getSubmittedScans() {
        return submittedScans;
    }

    public void setSubmittedScans(Integer submittedScans) {
        this.submittedScans = submittedScans;
    }

    public List<SubmitOwaspScanResponse> getScans() {
        return scans;
    }

    public void setScans(List<SubmitOwaspScanResponse> scans) {
        this.scans = scans;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
