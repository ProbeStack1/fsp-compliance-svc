package com.probestack.forgesphere.model;

import java.net.URI;
import java.util.Objects;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import com.probestack.forgesphere.model.RuleCategory;
import com.probestack.forgesphere.model.RuleSeverity;
import com.probestack.forgesphere.model.RuleStatus;
import com.probestack.forgesphere.model.RuleType;
import java.time.OffsetDateTime;
import org.springframework.format.annotation.DateTimeFormat;
import org.openapitools.jackson.nullable.JsonNullable;
import java.time.OffsetDateTime;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import io.swagger.v3.oas.annotations.media.Schema;


import java.util.*;
import jakarta.annotation.Generated;

/**
 * MongoDB document schema for governance_compliance_rules collection.
 */
@Schema(name = "GovernanceComplianceRuleDocument", description = "MongoDB document schema for governance_compliance_rules collection.")
@Generated(value = "org.openapitools.codegen.languages.SpringCodegen", date = "2026-05-13T05:38:50.494838724Z[GMT]")public class GovernanceComplianceRuleDocument {

  private String id;

  private String ruleId;

  private String ruleName;

  private String ruleDescription;

  private RuleType ruleType;

  private String ruleOwner;

  private RuleCategory category;

  private RuleSeverity severity;

  private Boolean enabled;

  private Boolean mandatory;

  private Integer displayOrder;

  private String icon;

  private RuleStatus status;

  @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
  private OffsetDateTime createDate;

  private String createdBy;

  @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
  private OffsetDateTime updatedDate;

  private String updatedBy;

  public GovernanceComplianceRuleDocument id(String id) {
    this.id = id;
    return this;
  }

  /**
   * Get id
   * @return id
  */
    @Schema(name = "_id", example = "CR13", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
  @JsonProperty("_id")
  public String getId() {
    return id;
  }

  public void setId(String id) {
    this.id = id;
  }

  public GovernanceComplianceRuleDocument ruleId(String ruleId) {
    this.ruleId = ruleId;
    return this;
  }

  /**
   * Get ruleId
   * @return ruleId
  */
    @Schema(name = "ruleId", example = "CR13", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
  @JsonProperty("ruleId")
  public String getRuleId() {
    return ruleId;
  }

  public void setRuleId(String ruleId) {
    this.ruleId = ruleId;
  }

  public GovernanceComplianceRuleDocument ruleName(String ruleName) {
    this.ruleName = ruleName;
    return this;
  }

  /**
   * Get ruleName
   * @return ruleName
  */
    @Schema(name = "ruleName", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
  @JsonProperty("ruleName")
  public String getRuleName() {
    return ruleName;
  }

  public void setRuleName(String ruleName) {
    this.ruleName = ruleName;
  }

  public GovernanceComplianceRuleDocument ruleDescription(String ruleDescription) {
    this.ruleDescription = ruleDescription;
    return this;
  }

  /**
   * Get ruleDescription
   * @return ruleDescription
  */
    @Schema(name = "ruleDescription", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
  @JsonProperty("ruleDescription")
  public String getRuleDescription() {
    return ruleDescription;
  }

  public void setRuleDescription(String ruleDescription) {
    this.ruleDescription = ruleDescription;
  }

  public GovernanceComplianceRuleDocument ruleType(RuleType ruleType) {
    this.ruleType = ruleType;
    return this;
  }

  /**
   * Get ruleType
   * @return ruleType
  */
  @Valid   @Schema(name = "ruleType", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
  @JsonProperty("ruleType")
  public RuleType getRuleType() {
    return ruleType;
  }

  public void setRuleType(RuleType ruleType) {
    this.ruleType = ruleType;
  }

  public GovernanceComplianceRuleDocument ruleOwner(String ruleOwner) {
    this.ruleOwner = ruleOwner;
    return this;
  }

  /**
   * Get ruleOwner
   * @return ruleOwner
  */
    @Schema(name = "ruleOwner", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
  @JsonProperty("ruleOwner")
  public String getRuleOwner() {
    return ruleOwner;
  }

  public void setRuleOwner(String ruleOwner) {
    this.ruleOwner = ruleOwner;
  }

  public GovernanceComplianceRuleDocument category(RuleCategory category) {
    this.category = category;
    return this;
  }

  /**
   * Get category
   * @return category
  */
  @Valid   @Schema(name = "category", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
  @JsonProperty("category")
  public RuleCategory getCategory() {
    return category;
  }

  public void setCategory(RuleCategory category) {
    this.category = category;
  }

  public GovernanceComplianceRuleDocument severity(RuleSeverity severity) {
    this.severity = severity;
    return this;
  }

  /**
   * Get severity
   * @return severity
  */
  @Valid   @Schema(name = "severity", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
  @JsonProperty("severity")
  public RuleSeverity getSeverity() {
    return severity;
  }

  public void setSeverity(RuleSeverity severity) {
    this.severity = severity;
  }

  public GovernanceComplianceRuleDocument enabled(Boolean enabled) {
    this.enabled = enabled;
    return this;
  }

  /**
   * Get enabled
   * @return enabled
  */
    @Schema(name = "enabled", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
  @JsonProperty("enabled")
  public Boolean getEnabled() {
    return enabled;
  }

  public void setEnabled(Boolean enabled) {
    this.enabled = enabled;
  }

  public GovernanceComplianceRuleDocument mandatory(Boolean mandatory) {
    this.mandatory = mandatory;
    return this;
  }

  /**
   * Get mandatory
   * @return mandatory
  */
    @Schema(name = "mandatory", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
  @JsonProperty("mandatory")
  public Boolean getMandatory() {
    return mandatory;
  }

  public void setMandatory(Boolean mandatory) {
    this.mandatory = mandatory;
  }

  public GovernanceComplianceRuleDocument displayOrder(Integer displayOrder) {
    this.displayOrder = displayOrder;
    return this;
  }

  /**
   * Get displayOrder
   * @return displayOrder
  */
    @Schema(name = "displayOrder", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
  @JsonProperty("displayOrder")
  public Integer getDisplayOrder() {
    return displayOrder;
  }

  public void setDisplayOrder(Integer displayOrder) {
    this.displayOrder = displayOrder;
  }

  public GovernanceComplianceRuleDocument icon(String icon) {
    this.icon = icon;
    return this;
  }

  /**
   * Get icon
   * @return icon
  */
    @Schema(name = "icon", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
  @JsonProperty("icon")
  public String getIcon() {
    return icon;
  }

  public void setIcon(String icon) {
    this.icon = icon;
  }

  public GovernanceComplianceRuleDocument status(RuleStatus status) {
    this.status = status;
    return this;
  }

  /**
   * Get status
   * @return status
  */
  @Valid   @Schema(name = "status", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
  @JsonProperty("status")
  public RuleStatus getStatus() {
    return status;
  }

  public void setStatus(RuleStatus status) {
    this.status = status;
  }

  public GovernanceComplianceRuleDocument createDate(OffsetDateTime createDate) {
    this.createDate = createDate;
    return this;
  }

  /**
   * Get createDate
   * @return createDate
  */
  @Valid   @Schema(name = "createDate", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
  @JsonProperty("createDate")
  public OffsetDateTime getCreateDate() {
    return createDate;
  }

  public void setCreateDate(OffsetDateTime createDate) {
    this.createDate = createDate;
  }

  public GovernanceComplianceRuleDocument createdBy(String createdBy) {
    this.createdBy = createdBy;
    return this;
  }

  /**
   * Get createdBy
   * @return createdBy
  */
    @Schema(name = "createdBy", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
  @JsonProperty("createdBy")
  public String getCreatedBy() {
    return createdBy;
  }

  public void setCreatedBy(String createdBy) {
    this.createdBy = createdBy;
  }

  public GovernanceComplianceRuleDocument updatedDate(OffsetDateTime updatedDate) {
    this.updatedDate = updatedDate;
    return this;
  }

  /**
   * Get updatedDate
   * @return updatedDate
  */
  @Valid   @Schema(name = "updatedDate", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
  @JsonProperty("updatedDate")
  public OffsetDateTime getUpdatedDate() {
    return updatedDate;
  }

  public void setUpdatedDate(OffsetDateTime updatedDate) {
    this.updatedDate = updatedDate;
  }

  public GovernanceComplianceRuleDocument updatedBy(String updatedBy) {
    this.updatedBy = updatedBy;
    return this;
  }

  /**
   * Get updatedBy
   * @return updatedBy
  */
    @Schema(name = "updatedBy", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
  @JsonProperty("updatedBy")
  public String getUpdatedBy() {
    return updatedBy;
  }

  public void setUpdatedBy(String updatedBy) {
    this.updatedBy = updatedBy;
  }
  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    GovernanceComplianceRuleDocument governanceComplianceRuleDocument = (GovernanceComplianceRuleDocument) o;
    return Objects.equals(this.id, governanceComplianceRuleDocument.id) &&
        Objects.equals(this.ruleId, governanceComplianceRuleDocument.ruleId) &&
        Objects.equals(this.ruleName, governanceComplianceRuleDocument.ruleName) &&
        Objects.equals(this.ruleDescription, governanceComplianceRuleDocument.ruleDescription) &&
        Objects.equals(this.ruleType, governanceComplianceRuleDocument.ruleType) &&
        Objects.equals(this.ruleOwner, governanceComplianceRuleDocument.ruleOwner) &&
        Objects.equals(this.category, governanceComplianceRuleDocument.category) &&
        Objects.equals(this.severity, governanceComplianceRuleDocument.severity) &&
        Objects.equals(this.enabled, governanceComplianceRuleDocument.enabled) &&
        Objects.equals(this.mandatory, governanceComplianceRuleDocument.mandatory) &&
        Objects.equals(this.displayOrder, governanceComplianceRuleDocument.displayOrder) &&
        Objects.equals(this.icon, governanceComplianceRuleDocument.icon) &&
        Objects.equals(this.status, governanceComplianceRuleDocument.status) &&
        Objects.equals(this.createDate, governanceComplianceRuleDocument.createDate) &&
        Objects.equals(this.createdBy, governanceComplianceRuleDocument.createdBy) &&
        Objects.equals(this.updatedDate, governanceComplianceRuleDocument.updatedDate) &&
        Objects.equals(this.updatedBy, governanceComplianceRuleDocument.updatedBy);
  }

  @Override
  public int hashCode() {
    return Objects.hash(id, ruleId, ruleName, ruleDescription, ruleType, ruleOwner, category, severity, enabled, mandatory, displayOrder, icon, status, createDate, createdBy, updatedDate, updatedBy);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class GovernanceComplianceRuleDocument {\n");
    sb.append("    id: ").append(toIndentedString(id)).append("\n");
    sb.append("    ruleId: ").append(toIndentedString(ruleId)).append("\n");
    sb.append("    ruleName: ").append(toIndentedString(ruleName)).append("\n");
    sb.append("    ruleDescription: ").append(toIndentedString(ruleDescription)).append("\n");
    sb.append("    ruleType: ").append(toIndentedString(ruleType)).append("\n");
    sb.append("    ruleOwner: ").append(toIndentedString(ruleOwner)).append("\n");
    sb.append("    category: ").append(toIndentedString(category)).append("\n");
    sb.append("    severity: ").append(toIndentedString(severity)).append("\n");
    sb.append("    enabled: ").append(toIndentedString(enabled)).append("\n");
    sb.append("    mandatory: ").append(toIndentedString(mandatory)).append("\n");
    sb.append("    displayOrder: ").append(toIndentedString(displayOrder)).append("\n");
    sb.append("    icon: ").append(toIndentedString(icon)).append("\n");
    sb.append("    status: ").append(toIndentedString(status)).append("\n");
    sb.append("    createDate: ").append(toIndentedString(createDate)).append("\n");
    sb.append("    createdBy: ").append(toIndentedString(createdBy)).append("\n");
    sb.append("    updatedDate: ").append(toIndentedString(updatedDate)).append("\n");
    sb.append("    updatedBy: ").append(toIndentedString(updatedBy)).append("\n");
    sb.append("}");
    return sb.toString();
  }

  /**
   * Convert the given object to string with each line indented by 4 spaces
   * (except the first line).
   */
  private String toIndentedString(Object o) {
    if (o == null) {
      return "null";
    }
    return o.toString().replace("\n", "\n    ");
  }
}

