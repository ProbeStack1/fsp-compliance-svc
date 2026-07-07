package com.probestack.forgesphere.model;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public class ApproveRuleRequest {

    @NotNull(message = "status is required")
    private RuleStatus status;  // READY or REJECTED

    @NotBlank(message = "updatedBy (admin email) is required")
    private String updatedBy;

    // Optional override fields
    private String ruleName;
    private String ruleDescription;
    private RuleCategory category;
    private RuleSeverity severity;
    private Boolean mandatory;
    private String icon;
    private String implementationKey;  // Optional for Linting, required for Compliance

    // Getters & Setters
    public RuleStatus getStatus() { return status; }
    public void setStatus(RuleStatus status) { this.status = status; }

    public String getUpdatedBy() { return updatedBy; }
    public void setUpdatedBy(String updatedBy) { this.updatedBy = updatedBy; }

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

    public String getImplementationKey() { return implementationKey; }
    public void setImplementationKey(String implementationKey) { this.implementationKey = implementationKey; }
}