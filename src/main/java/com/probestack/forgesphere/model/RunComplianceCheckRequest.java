package com.probestack.forgesphere.model;

import java.util.List;

public class RunComplianceCheckRequest {

    private String projectName;
    private String projectId;
    private String companyName;
    private AssetType assetType;
    private String microserviceId;
    private List<String> microserviceIds;
    private List<RunComplianceResourceRequest> resources;
    private List<String> resourceIds;
    private List<String> resourceNames;
    private SourceType sourceType;
    private ScanSource source;
    private ScanRules rules;
    private ScanOptions scanOptions;
    private String requestedBy;

    public String getProjectName() {
        return projectName;
    }

    public void setProjectName(String projectName) {
        this.projectName = projectName;
    }

    public String getProjectId() {
        return projectId;
    }

    public void setProjectId(String projectId) {
        this.projectId = projectId;
    }

    public String getCompanyName() {
        return companyName;
    }

    public void setCompanyName(String companyName) {
        this.companyName = companyName;
    }

    public AssetType getAssetType() {
        return assetType;
    }

    public void setAssetType(AssetType assetType) {
        this.assetType = assetType;
    }

    public String getMicroserviceId() {
        return microserviceId;
    }

    public void setMicroserviceId(String microserviceId) {
        this.microserviceId = microserviceId;
    }

    public List<String> getMicroserviceIds() {
        return microserviceIds;
    }

    public void setMicroserviceIds(List<String> microserviceIds) {
        this.microserviceIds = microserviceIds;
    }

    public List<RunComplianceResourceRequest> getResources() {
        return resources;
    }

    public void setResources(List<RunComplianceResourceRequest> resources) {
        this.resources = resources;
    }

    public List<String> getResourceIds() {
        return resourceIds;
    }

    public void setResourceIds(List<String> resourceIds) {
        this.resourceIds = resourceIds;
    }

    public List<String> getResourceNames() {
        return resourceNames;
    }

    public void setResourceNames(List<String> resourceNames) {
        this.resourceNames = resourceNames;
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
