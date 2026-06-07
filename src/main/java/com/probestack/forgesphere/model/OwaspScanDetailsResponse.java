package com.probestack.forgesphere.model;

import java.time.OffsetDateTime;
import java.util.List;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.annotation.Generated;

@Generated(value = "manual")
public class OwaspScanDetailsResponse {

    @JsonProperty("scanId")
    private String scanId;

    @JsonProperty("projectName")
    private String projectName;

    @JsonProperty("companyName")
    private String companyName;

    @JsonProperty("assetId")
    private String assetId;

    @JsonProperty("assetName")
    private String assetName;

    @JsonProperty("assetType")
    private AssetType assetType;

    @JsonProperty("sourceType")
    private SourceType sourceType;

    @JsonProperty("source")
    private ScanSource source;

    @JsonProperty("rules")
    private ScanRules rules;

    @JsonProperty("scanOptions")
    private ScanOptions scanOptions;

    @JsonProperty("status")
    private ScanStatus status;

    @JsonProperty("compliance")
    private ComplianceStatus compliance;

    @JsonProperty("scanDate")
    private OffsetDateTime scanDate;

    @JsonProperty("processingStartDate")
    private OffsetDateTime processingStartDate;

    @JsonProperty("processingEndDate")
    private OffsetDateTime processingEndDate;

    @JsonProperty("scanResults")
    private List<ScanResult> scanResults;

    @JsonProperty("errorMessage")
    private String errorMessage;

    @JsonProperty("createDate")
    private OffsetDateTime createDate;

    @JsonProperty("createdBy")
    private String createdBy;

    @JsonProperty("updatedDate")
    private OffsetDateTime updatedDate;

    @JsonProperty("updatedBy")
    private String updatedBy;

    public String getScanId() {
        return scanId;
    }

    public void setScanId(String scanId) {
        this.scanId = scanId;
    }

    public String getProjectName() {
        return projectName;
    }

    public void setProjectName(String projectName) {
        this.projectName = projectName;
    }

    public String getCompanyName() {
        return companyName;
    }

    public void setCompanyName(String companyName) {
        this.companyName = companyName;
    }

    public String getAssetId() {
        return assetId;
    }

    public void setAssetId(String assetId) {
        this.assetId = assetId;
    }

    public String getAssetName() {
        return assetName;
    }

    public void setAssetName(String assetName) {
        this.assetName = assetName;
    }

    public AssetType getAssetType() {
        return assetType;
    }

    public void setAssetType(AssetType assetType) {
        this.assetType = assetType;
    }

    public SourceType getSourceType() {
        return sourceType;
    }

    public void setSourceType(SourceType sourceType) {
        this.sourceType = sourceType;
    }

    public ScanSource getSource() {
        return source;
    }

    public void setSource(ScanSource source) {
        this.source = source;
    }

    public ScanRules getRules() {
        return rules;
    }

    public void setRules(ScanRules rules) {
        this.rules = rules;
    }

    public ScanOptions getScanOptions() {
        return scanOptions;
    }

    public void setScanOptions(ScanOptions scanOptions) {
        this.scanOptions = scanOptions;
    }

    public ScanStatus getStatus() {
        return status;
    }

    public void setStatus(ScanStatus status) {
        this.status = status;
    }

    public ComplianceStatus getCompliance() {
        return compliance;
    }

    public void setCompliance(ComplianceStatus compliance) {
        this.compliance = compliance;
    }

    public OffsetDateTime getScanDate() {
        return scanDate;
    }

    public void setScanDate(OffsetDateTime scanDate) {
        this.scanDate = scanDate;
    }

    public OffsetDateTime getProcessingStartDate() {
        return processingStartDate;
    }

    public void setProcessingStartDate(OffsetDateTime processingStartDate) {
        this.processingStartDate = processingStartDate;
    }

    public OffsetDateTime getProcessingEndDate() {
        return processingEndDate;
    }

    public void setProcessingEndDate(OffsetDateTime processingEndDate) {
        this.processingEndDate = processingEndDate;
    }

    public List<ScanResult> getScanResults() {
        return scanResults;
    }

    public void setScanResults(List<ScanResult> scanResults) {
        this.scanResults = scanResults;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }

    public OffsetDateTime getCreateDate() {
        return createDate;
    }

    public void setCreateDate(OffsetDateTime createDate) {
        this.createDate = createDate;
    }

    public String getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(String createdBy) {
        this.createdBy = createdBy;
    }

    public OffsetDateTime getUpdatedDate() {
        return updatedDate;
    }

    public void setUpdatedDate(OffsetDateTime updatedDate) {
        this.updatedDate = updatedDate;
    }

    public String getUpdatedBy() {
        return updatedBy;
    }

    public void setUpdatedBy(String updatedBy) {
        this.updatedBy = updatedBy;
    }
}
