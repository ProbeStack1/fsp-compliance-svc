package com.probestack.forgesphere.model;

public class ApproveRuleRequestResponse {
    private String ruleId;
    private RuleStatus status;
    private String message;

    // Constructors
    public ApproveRuleRequestResponse() {}

    public ApproveRuleRequestResponse(String ruleId, RuleStatus status, String message) {
        this.ruleId = ruleId;
        this.status = status;
        this.message = message;
    }

    // Getters & Setters
    public String getRuleId() { return ruleId; }
    public void setRuleId(String ruleId) { this.ruleId = ruleId; }

    public RuleStatus getStatus() { return status; }
    public void setStatus(RuleStatus status) { this.status = status; }

    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }
}