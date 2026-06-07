package com.probestack.forgesphere.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.annotation.Generated;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;

@Generated(value = "manual")
public class RunOwaspCheckRequest {

    @JsonProperty("assetType")
    @NotNull
    private AssetType assetType;

    @JsonProperty("projectName")
    private String projectName;

    @JsonProperty("companyName")
    private String companyName;

    @JsonProperty("microserviceId")
    private String microserviceId;

    @JsonProperty("microserviceIds")
    private java.util.List<String> microserviceIds;

    @JsonProperty("resourceIds")
    private java.util.List<String> resourceIds;

    @JsonProperty("resources")
    @Valid
    private java.util.List<RunComplianceResourceRequest> resources;

    @JsonProperty("rules")
    @Valid
    private ScanRules rules;

    @JsonProperty("scanOptions")
    @Valid
    private ScanOptions scanOptions;

    @JsonProperty("requestedBy")
    private String requestedBy;

    public AssetType getAssetType() {
        return assetType;
    }

    public void setAssetType(AssetType assetType) {
        this.assetType = assetType;
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

    public String getMicroserviceId() {
        return microserviceId;
    }

    public void setMicroserviceId(String microserviceId) {
        this.microserviceId = microserviceId;
    }

    public java.util.List<String> getMicroserviceIds() {
        return microserviceIds;
    }

    public void setMicroserviceIds(java.util.List<String> microserviceIds) {
        this.microserviceIds = microserviceIds;
    }

    public java.util.List<String> getResourceIds() {
        return resourceIds;
    }

    public void setResourceIds(java.util.List<String> resourceIds) {
        this.resourceIds = resourceIds;
    }

    public java.util.List<RunComplianceResourceRequest> getResources() {
        return resources;
    }

    public void setResources(java.util.List<RunComplianceResourceRequest> resources) {
        this.resources = resources;
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
