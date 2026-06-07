package com.probestack.forgesphere.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.annotation.Generated;

@Generated(value = "manual")
public class CreateOwaspRuleResponse {

    @JsonProperty("ruleId")
    private String ruleId;

    @JsonProperty("ruleName")
    private String ruleName;

    @JsonProperty("status")
    private RuleStatus status;

    @JsonProperty("message")
    private String message;

    public String getRuleId() {
        return ruleId;
    }

    public void setRuleId(String ruleId) {
        this.ruleId = ruleId;
    }

    public String getRuleName() {
        return ruleName;
    }

    public void setRuleName(String ruleName) {
        this.ruleName = ruleName;
    }

    public RuleStatus getStatus() {
        return status;
    }

    public void setStatus(RuleStatus status) {
        this.status = status;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
