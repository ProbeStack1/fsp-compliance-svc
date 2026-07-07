package com.probestack.forgesphere.model;

import java.util.List;

public class LintingRulesResponse {
    private AssetType assetType;
    private Integer totalRules;
    private Integer activeRules;
    private List<LintingRule> rules;

    // Getters & Setters
    public AssetType getAssetType() { return assetType; }
    public void setAssetType(AssetType assetType) { this.assetType = assetType; }

    public Integer getTotalRules() { return totalRules; }
    public void setTotalRules(Integer totalRules) { this.totalRules = totalRules; }

    public Integer getActiveRules() { return activeRules; }
    public void setActiveRules(Integer activeRules) { this.activeRules = activeRules; }

    public List<LintingRule> getRules() { return rules; }
    public void setRules(List<LintingRule> rules) { this.rules = rules; }
}