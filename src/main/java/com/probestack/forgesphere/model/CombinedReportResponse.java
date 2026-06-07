package com.probestack.forgesphere.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.time.OffsetDateTime;
import jakarta.annotation.Generated;
import java.util.List;

@Generated(value = "manual")
public class CombinedReportResponse {

    @JsonProperty("projectName")
    private String projectName;

    @JsonProperty("assetType")
    private AssetType assetType;

    @JsonProperty("complianceScanId")
    private String complianceScanId;

    @JsonProperty("owaspScanId")
    private String owaspScanId;

    @JsonProperty("complianceStatus")
    private ComplianceStatus complianceStatus;

    @JsonProperty("owaspStatus")
    private ComplianceStatus owaspStatus;

    @JsonProperty("complianceSuccessRate")
    private Double complianceSuccessRate;

    @JsonProperty("owaspSuccessRate")
    private Double owaspSuccessRate;

    @JsonProperty("combinedSuccessRate")
    private Double combinedSuccessRate;

    @JsonProperty("generatedAt")
    private OffsetDateTime generatedAt;

    @JsonProperty("details")
    private List<String> details;

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

    public ComplianceStatus getComplianceStatus() {
        return complianceStatus;
    }

    public void setComplianceStatus(ComplianceStatus complianceStatus) {
        this.complianceStatus = complianceStatus;
    }

    public ComplianceStatus getOwaspStatus() {
        return owaspStatus;
    }

    public void setOwaspStatus(ComplianceStatus owaspStatus) {
        this.owaspStatus = owaspStatus;
    }

    public Double getComplianceSuccessRate() {
        return complianceSuccessRate;
    }

    public void setComplianceSuccessRate(Double complianceSuccessRate) {
        this.complianceSuccessRate = complianceSuccessRate;
    }

    public Double getOwaspSuccessRate() {
        return owaspSuccessRate;
    }

    public void setOwaspSuccessRate(Double owaspSuccessRate) {
        this.owaspSuccessRate = owaspSuccessRate;
    }

    public Double getCombinedSuccessRate() {
        return combinedSuccessRate;
    }

    public void setCombinedSuccessRate(Double combinedSuccessRate) {
        this.combinedSuccessRate = combinedSuccessRate;
    }

    public OffsetDateTime getGeneratedAt() {
        return generatedAt;
    }

    public void setGeneratedAt(OffsetDateTime generatedAt) {
        this.generatedAt = generatedAt;
    }

    public List<String> getDetails() {
        return details;
    }

    public void setDetails(List<String> details) {
        this.details = details;
    }
}
