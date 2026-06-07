package com.probestack.forgesphere.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.annotation.Generated;

@Generated(value = "manual")
public class UpdateOwaspRuleStatusRequest {

    @JsonProperty("enabled")
    private Boolean enabled;

    @JsonProperty("status")
    private RuleStatus status;

    @JsonProperty("updatedBy")
    private String updatedBy;

    public Boolean getEnabled() {
        return enabled;
    }

    public void setEnabled(Boolean enabled) {
        this.enabled = enabled;
    }

    public RuleStatus getStatus() {
        return status;
    }

    public void setStatus(RuleStatus status) {
        this.status = status;
    }

    public String getUpdatedBy() {
        return updatedBy;
    }

    public void setUpdatedBy(String updatedBy) {
        this.updatedBy = updatedBy;
    }
}
