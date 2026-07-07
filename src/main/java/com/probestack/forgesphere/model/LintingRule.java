package com.probestack.forgesphere.model;

import java.time.OffsetDateTime;

public class LintingRule {
    private String ruleId;
    private String ruleName;
    private String ruleDescription;
    private AssetType assetType;
    private RuleCategory category;
    private RuleSeverity severity;
    private RuleType ruleType;
    private String ruleOwner;
    private Boolean enabled;
    private Boolean mandatory;
    private Integer displayOrder;
    private String icon;
    private RuleStatus status;
    private String implementationKey;
    private OffsetDateTime createDate;
    private String createdBy;
    private OffsetDateTime updatedDate;
    private String updatedBy;

    // Getters & Setters
    public String getRuleId() { return ruleId; }
    public void setRuleId(String ruleId) { this.ruleId = ruleId; }

    public String getRuleName() { return ruleName; }
    public void setRuleName(String ruleName) { this.ruleName = ruleName; }

    public String getRuleDescription() { return ruleDescription; }
    public void setRuleDescription(String ruleDescription) { this.ruleDescription = ruleDescription; }

    public AssetType getAssetType() { return assetType; }
    public void setAssetType(AssetType assetType) { this.assetType = assetType; }

    public RuleCategory getCategory() { return category; }
    public void setCategory(RuleCategory category) { this.category = category; }

    public RuleSeverity getSeverity() { return severity; }
    public void setSeverity(RuleSeverity severity) { this.severity = severity; }

    public RuleType getRuleType() { return ruleType; }
    public void setRuleType(RuleType ruleType) { this.ruleType = ruleType; }

    public String getRuleOwner() { return ruleOwner; }
    public void setRuleOwner(String ruleOwner) { this.ruleOwner = ruleOwner; }

    public Boolean getEnabled() { return enabled; }
    public void setEnabled(Boolean enabled) { this.enabled = enabled; }

    public Boolean getMandatory() { return mandatory; }
    public void setMandatory(Boolean mandatory) { this.mandatory = mandatory; }

    public Integer getDisplayOrder() { return displayOrder; }
    public void setDisplayOrder(Integer displayOrder) { this.displayOrder = displayOrder; }

    public String getIcon() { return icon; }
    public void setIcon(String icon) { this.icon = icon; }

    public RuleStatus getStatus() { return status; }
    public void setStatus(RuleStatus status) { this.status = status; }

    public String getImplementationKey() { return implementationKey; }
    public void setImplementationKey(String implementationKey) { this.implementationKey = implementationKey; }

    public OffsetDateTime getCreateDate() { return createDate; }
    public void setCreateDate(OffsetDateTime createDate) { this.createDate = createDate; }

    public String getCreatedBy() { return createdBy; }
    public void setCreatedBy(String createdBy) { this.createdBy = createdBy; }

    public OffsetDateTime getUpdatedDate() { return updatedDate; }
    public void setUpdatedDate(OffsetDateTime updatedDate) { this.updatedDate = updatedDate; }

    public String getUpdatedBy() { return updatedBy; }
    public void setUpdatedBy(String updatedBy) { this.updatedBy = updatedBy; }
}