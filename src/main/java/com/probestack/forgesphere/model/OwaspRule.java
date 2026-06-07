package com.probestack.forgesphere.model;

import java.time.OffsetDateTime;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.annotation.Generated;

@Generated(value = "manual")
public class OwaspRule {

    @JsonProperty("ruleId")
    private String ruleId;

    @JsonProperty("owaspId")
    @NotNull
    private String owaspId;

    @JsonProperty("ruleName")
    @NotNull
    private String ruleName;

    @JsonProperty("ruleDescription")
    private String ruleDescription;

    @JsonProperty("assetType")
    private AssetType assetType;

    @JsonProperty("ruleType")
    private RuleType ruleType;

    @JsonProperty("ruleOwner")
    private String ruleOwner;

    @JsonProperty("category")
    private OwaspCategory category;

    @JsonProperty("severity")
    private RuleSeverity severity;

    @JsonProperty("enabled")
    private Boolean enabled;

    @JsonProperty("mandatory")
    private Boolean mandatory;

    @JsonProperty("displayOrder")
    private Integer displayOrder;

    @JsonProperty("icon")
    private String icon;

    @JsonProperty("status")
    private RuleStatus status;

    @JsonProperty("implementationKey")
    private String implementationKey;

    @JsonProperty("createDate")
    private OffsetDateTime createDate;

    @JsonProperty("createdBy")
    private String createdBy;

    @JsonProperty("updatedDate")
    private OffsetDateTime updatedDate;

    @JsonProperty("updatedBy")
    private String updatedBy;

    public String getRuleId() {
        return ruleId;
    }

    public void setRuleId(String ruleId) {
        this.ruleId = ruleId;
    }

    public String getOwaspId() {
        return owaspId;
    }

    public void setOwaspId(String owaspId) {
        this.owaspId = owaspId;
    }

    public String getRuleName() {
        return ruleName;
    }

    public void setRuleName(String ruleName) {
        this.ruleName = ruleName;
    }

    public String getRuleDescription() {
        return ruleDescription;
    }

    public void setRuleDescription(String ruleDescription) {
        this.ruleDescription = ruleDescription;
    }

    public AssetType getAssetType() {
        return assetType;
    }

    public void setAssetType(AssetType assetType) {
        this.assetType = assetType;
    }

    public RuleType getRuleType() {
        return ruleType;
    }

    public void setRuleType(RuleType ruleType) {
        this.ruleType = ruleType;
    }

    public String getRuleOwner() {
        return ruleOwner;
    }

    public void setRuleOwner(String ruleOwner) {
        this.ruleOwner = ruleOwner;
    }

    public OwaspCategory getCategory() {
        return category;
    }

    public void setCategory(OwaspCategory category) {
        this.category = category;
    }

    public RuleSeverity getSeverity() {
        return severity;
    }

    public void setSeverity(RuleSeverity severity) {
        this.severity = severity;
    }

    public Boolean getEnabled() {
        return enabled;
    }

    public void setEnabled(Boolean enabled) {
        this.enabled = enabled;
    }

    public Boolean getMandatory() {
        return mandatory;
    }

    public void setMandatory(Boolean mandatory) {
        this.mandatory = mandatory;
    }

    public Integer getDisplayOrder() {
        return displayOrder;
    }

    public void setDisplayOrder(Integer displayOrder) {
        this.displayOrder = displayOrder;
    }

    public String getIcon() {
        return icon;
    }

    public void setIcon(String icon) {
        this.icon = icon;
    }

    public RuleStatus getStatus() {
        return status;
    }

    public void setStatus(RuleStatus status) {
        this.status = status;
    }

    public String getImplementationKey() {
        return implementationKey;
    }

    public void setImplementationKey(String implementationKey) {
        this.implementationKey = implementationKey;
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
