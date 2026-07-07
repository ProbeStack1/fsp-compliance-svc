package com.probestack.forgesphere.model;

public class SubmitRuleRequestResponse {
    private String ruleId;
    private RuleStatus status;
    private Boolean emailSent;
    private String emailMessage;
    private String message;

    // Constructors
    public SubmitRuleRequestResponse() {}

    public SubmitRuleRequestResponse(String ruleId, RuleStatus status, Boolean emailSent, String emailMessage, String message) {
        this.ruleId = ruleId;
        this.status = status;
        this.emailSent = emailSent;
        this.emailMessage = emailMessage;
        this.message = message;
    }

    // Getters & Setters
    public String getRuleId() { return ruleId; }
    public void setRuleId(String ruleId) { this.ruleId = ruleId; }

    public RuleStatus getStatus() { return status; }
    public void setStatus(RuleStatus status) { this.status = status; }

    public Boolean getEmailSent() { return emailSent; }
    public void setEmailSent(Boolean emailSent) { this.emailSent = emailSent; }

    public String getEmailMessage() { return emailMessage; }
    public void setEmailMessage(String emailMessage) { this.emailMessage = emailMessage; }

    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }
}