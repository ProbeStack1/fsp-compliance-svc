package com.probestack.forgesphere.model;

import java.util.List;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.annotation.Generated;

@Generated(value = "manual")
public class OwaspReportResponse {

    @JsonProperty("projectName")
    private String projectName;

    @JsonProperty("assetType")
    private AssetType assetType;

    @JsonProperty("scanId")
    private String scanId;

    @JsonProperty("compliance")
    private ComplianceStatus compliance;

    @JsonProperty("totalRules")
    private Integer totalRules;

    @JsonProperty("passedRules")
    private Integer passedRules;

    @JsonProperty("failedRules")
    private Integer failedRules;

    @JsonProperty("successRate")
    private Double successRate;

    @JsonProperty("scanResults")
    private List<ScanResult> scanResults;

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

    public ComplianceStatus getCompliance() {
        return compliance;
    }

    public void setCompliance(ComplianceStatus compliance) {
        this.compliance = compliance;
    }

    public Integer getTotalRules() {
        return totalRules;
    }

    public void setTotalRules(Integer totalRules) {
        this.totalRules = totalRules;
    }

    public Integer getPassedRules() {
        return passedRules;
    }

    public void setPassedRules(Integer passedRules) {
        this.passedRules = passedRules;
    }

    public Integer getFailedRules() {
        return failedRules;
    }

    public void setFailedRules(Integer failedRules) {
        this.failedRules = failedRules;
    }

    public Double getSuccessRate() {
        return successRate;
    }

    public void setSuccessRate(Double successRate) {
        this.successRate = successRate;
    }

    public List<ScanResult> getScanResults() {
        return scanResults;
    }

    public void setScanResults(List<ScanResult> scanResults) {
        this.scanResults = scanResults;
    }
}
