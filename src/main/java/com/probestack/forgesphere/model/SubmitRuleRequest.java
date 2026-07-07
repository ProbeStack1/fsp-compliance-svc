package com.probestack.forgesphere.model;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public class SubmitRuleRequest {

    @NotBlank(message = "ruleType is required (COMPLIANCE or LINTING)")
    private String ruleType;

    @NotNull(message = "assetType is required")
    private AssetType assetType;

    @NotBlank(message = "ruleName is required")
    private String ruleName;

    @NotBlank(message = "ruleDescription is required")
    private String ruleDescription;

    private RuleCategory category;
    private RuleSeverity severity;
    private Boolean mandatory;
    private String icon;

    @NotBlank(message = "createdBy (user email) is required")
    private String createdBy;

    @NotBlank(message = "approverEmail is required")
    private String approverEmail;

    // NEW FIELDS (Linting specific)
    private String scope;       // "internal" or "public"
    private String field;       // e.g., "spring.datasource.url"
    private String condition;   // "Available", "pattern [^[A-Z]]", etc.

    // Getters & Setters
    public String getRuleType() { return ruleType; }
    public void setRuleType(String ruleType) { this.ruleType = ruleType; }

    public AssetType getAssetType() { return assetType; }
    public void setAssetType(AssetType assetType) { this.assetType = assetType; }

    public String getRuleName() { return ruleName; }
    public void setRuleName(String ruleName) { this.ruleName = ruleName; }

    public String getRuleDescription() { return ruleDescription; }
    public void setRuleDescription(String ruleDescription) { this.ruleDescription = ruleDescription; }

    public RuleCategory getCategory() { return category; }
    public void setCategory(RuleCategory category) { this.category = category; }

    public RuleSeverity getSeverity() { return severity; }
    public void setSeverity(RuleSeverity severity) { this.severity = severity; }

    public Boolean getMandatory() { return mandatory; }
    public void setMandatory(Boolean mandatory) { this.mandatory = mandatory; }

    public String getIcon() { return icon; }
    public void setIcon(String icon) { this.icon = icon; }

    public String getCreatedBy() { return createdBy; }
    public void setCreatedBy(String createdBy) { this.createdBy = createdBy; }

    public String getApproverEmail() { return approverEmail; }
    public void setApproverEmail(String approverEmail) { this.approverEmail = approverEmail; }

    public String getScope() { return scope; }
    public void setScope(String scope) { this.scope = scope; }

    public String getField() { return field; }
    public void setField(String field) { this.field = field; }

    public String getCondition() { return condition; }
    public void setCondition(String condition) { this.condition = condition; }
}