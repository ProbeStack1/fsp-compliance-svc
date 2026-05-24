package com.probestack.forgesphere.model;

import java.util.List;

public class RunComplianceCheckResponse {

    private String message;
    private Integer submittedScans;
    private List<SubmitComplianceScanResponse> scans;

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public Integer getSubmittedScans() {
        return submittedScans;
    }

    public void setSubmittedScans(Integer submittedScans) {
        this.submittedScans = submittedScans;
    }

    public List<SubmitComplianceScanResponse> getScans() {
        return scans;
    }

    public void setScans(List<SubmitComplianceScanResponse> scans) {
        this.scans = scans;
    }
}
