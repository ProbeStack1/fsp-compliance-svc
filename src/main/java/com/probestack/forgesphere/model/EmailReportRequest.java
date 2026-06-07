package com.probestack.forgesphere.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.probestack.forgesphere.model.AssetType;
import jakarta.annotation.Generated;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

@Generated(value = "manual")
public class EmailReportRequest {

    @JsonProperty("to")
    @NotBlank
    @Email
    private String to;

    @JsonProperty("subject")
    @NotBlank
    private String subject;

    @JsonProperty("reportType")
    @NotBlank
    private String reportType;

    @JsonProperty("projectName")
    private String projectName;

    @JsonProperty("assetType")
    private AssetType assetType;

    @JsonProperty("scanId")
    private String scanId;

    @JsonProperty("complianceScanId")
    private String complianceScanId;

    @JsonProperty("owaspScanId")
    private String owaspScanId;

    @JsonProperty("from")
    private String from;

    public String getTo() {
        return to;
    }

    public void setTo(String to) {
        this.to = to;
    }

    public String getSubject() {
        return subject;
    }

    public void setSubject(String subject) {
        this.subject = subject;
    }

    public String getReportType() {
        return reportType;
    }

    public void setReportType(String reportType) {
        this.reportType = reportType;
    }

    public String getProjectName() {
        return projectName;
    }

    public void setProjectName(String projectName) {
        this.projectName = projectName;
    }

    public AssetType getAssetType() {
        return assetType;
    }

    public void setAssetType(AssetType assetType) {
        this.assetType = assetType;
    }

    public String getScanId() {
        return scanId;
    }

    public void setScanId(String scanId) {
        this.scanId = scanId;
    }

    public String getComplianceScanId() {
        return complianceScanId;
    }

    public void setComplianceScanId(String complianceScanId) {
        this.complianceScanId = complianceScanId;
    }

    public String getOwaspScanId() {
        return owaspScanId;
    }

    public void setOwaspScanId(String owaspScanId) {
        this.owaspScanId = owaspScanId;
    }

    public String getFrom() {
        return from;
    }

    public void setFrom(String from) {
        this.from = from;
    }
}
