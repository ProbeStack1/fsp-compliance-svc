package com.probestack.forgesphere.document;

import java.time.Instant;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import com.probestack.forgesphere.model.AssetType;
import com.probestack.forgesphere.model.OwaspCategory;
import com.probestack.forgesphere.model.RuleSeverity;
import com.probestack.forgesphere.model.RuleStatus;
import com.probestack.forgesphere.model.RuleType;

@Document(collection = "governance_owasp_rules")
@CompoundIndex(name = "asset_status_category_type_idx", def = "{'assetType': 1, 'status': 1, 'category': 1, 'ruleType': 1}")
public class OwaspRuleDocument {

    @Id
    private String id;

    @Indexed(unique = true)
    private String ruleId;

    @Indexed
    private AssetType assetType;

    private String owaspId;
    private String ruleName;
    private String ruleDescription;
    private RuleType ruleType;
    private String ruleOwner;
    private OwaspCategory category;
    private RuleSeverity severity;
    private Boolean enabled;
    private Boolean mandatory;
    private Integer displayOrder;
    private String icon;
    private RuleStatus status;
    private String implementationKey;
    private Instant createDate;
    private String createdBy;
    private Instant updatedDate;
    private String updatedBy;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getRuleId() {
        return ruleId;
    }

    public void setRuleId(String ruleId) {
        this.ruleId = ruleId;
    }

    public AssetType getAssetType() {
        return assetType;
    }

    public void setAssetType(AssetType assetType) {
        this.assetType = assetType;
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

    public Instant getCreateDate() {
        return createDate;
    }

    public void setCreateDate(Instant createDate) {
        this.createDate = createDate;
    }

    public String getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(String createdBy) {
        this.createdBy = createdBy;
    }

    public Instant getUpdatedDate() {
        return updatedDate;
    }

    public void setUpdatedDate(Instant updatedDate) {
        this.updatedDate = updatedDate;
    }

    public String getUpdatedBy() {
        return updatedBy;
    }

    public void setUpdatedBy(String updatedBy) {
        this.updatedBy = updatedBy;
    }
}
