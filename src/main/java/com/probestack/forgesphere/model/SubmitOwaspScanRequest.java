package com.probestack.forgesphere.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.annotation.Generated;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;

@Generated(value = "manual")
public class SubmitOwaspScanRequest {

    @JsonProperty("projectName")
    private String projectName;

    @JsonProperty("companyName")
    private String companyName;

    @JsonProperty("assetId")
    private String assetId;

    @JsonProperty("assetName")
    private String assetName;

    @JsonProperty("assetType")
    @NotNull
    private AssetType assetType;

    @JsonProperty("sourceType")
    private SourceType sourceType;

    @JsonProperty("source")
    @Valid
    private ScanSource source;

    @JsonProperty("rules")
    @Valid
    private ScanRules rules;

    @JsonProperty("scanOptions")
    @Valid
    private ScanOptions scanOptions;

    @JsonProperty("requestedBy")
    private String requestedBy;

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

    public String getRequestedBy() {
        return requestedBy;
    }

    public void setRequestedBy(String requestedBy) {
        this.requestedBy = requestedBy;
    }
}
