package com.probestack.forgesphere.model;

import java.util.List;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.annotation.Generated;

@Generated(value = "manual")
public class OwaspRulesResponse {

    @JsonProperty("projectName")
    private String projectName;

    @JsonProperty("resourceType")
    private AssetType resourceType;

    @JsonProperty("resourceName")
    private String resourceName;

    @JsonProperty("totalRules")
    private Integer totalRules;

    @JsonProperty("activeRules")
    private Integer activeRules;

    @JsonProperty("rules")
    private List<OwaspRule> rules;

    public String getProjectName() {
        return projectName;
    }

    public void setProjectName(String projectName) {
        this.projectName = projectName;
    }

    public AssetType getResourceType() {
        return resourceType;
    }

    public void setResourceType(AssetType resourceType) {
        this.resourceType = resourceType;
    }

    public String getResourceName() {
        return resourceName;
    }

    public void setResourceName(String resourceName) {
        this.resourceName = resourceName;
    }

    public Integer getTotalRules() {
        return totalRules;
    }

    public void setTotalRules(Integer totalRules) {
        this.totalRules = totalRules;
    }

    public Integer getActiveRules() {
        return activeRules;
    }

    public void setActiveRules(Integer activeRules) {
        this.activeRules = activeRules;
    }

    public List<OwaspRule> getRules() {
        return rules;
    }

    public void setRules(List<OwaspRule> rules) {
        this.rules = rules;
    }
}
