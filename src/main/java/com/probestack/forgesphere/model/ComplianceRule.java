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
 * ComplianceRule
 */
@Generated(value = "org.openapitools.codegen.languages.SpringCodegen", date = "2026-05-13T05:38:50.494838724Z[GMT]")public class ComplianceRule {

  private String ruleId;

  private String ruleName;

  private String ruleDescription;

  private RuleCategory category;

  private RuleSeverity severity;

  private RuleType ruleType;

  private String ruleOwner;

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

  public ComplianceRule ruleId(String ruleId) {
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

  public ComplianceRule ruleName(String ruleName) {
    this.ruleName = ruleName;
    return this;
  }

  /**
   * Get ruleName
   * @return ruleName
  */
    @Schema(name = "ruleName", example = "Documentation required for all endpoints", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
  @JsonProperty("ruleName")
  public String getRuleName() {
    return ruleName;
  }

  public void setRuleName(String ruleName) {
    this.ruleName = ruleName;
  }

  public ComplianceRule ruleDescription(String ruleDescription) {
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

  public ComplianceRule category(RuleCategory category) {
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

  public ComplianceRule severity(RuleSeverity severity) {
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

  public ComplianceRule ruleType(RuleType ruleType) {
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

  public ComplianceRule ruleOwner(String ruleOwner) {
    this.ruleOwner = ruleOwner;
    return this;
  }

  /**
   * Get ruleOwner
   * @return ruleOwner
  */
    @Schema(name = "ruleOwner", example = "Governance Team", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
  @JsonProperty("ruleOwner")
  public String getRuleOwner() {
    return ruleOwner;
  }

  public void setRuleOwner(String ruleOwner) {
    this.ruleOwner = ruleOwner;
  }

  public ComplianceRule enabled(Boolean enabled) {
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

  public ComplianceRule mandatory(Boolean mandatory) {
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

  public ComplianceRule displayOrder(Integer displayOrder) {
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

  public ComplianceRule icon(String icon) {
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

  public ComplianceRule status(RuleStatus status) {
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

  public ComplianceRule createDate(OffsetDateTime createDate) {
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

  public ComplianceRule createdBy(String createdBy) {
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

  public ComplianceRule updatedDate(OffsetDateTime updatedDate) {
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

  public ComplianceRule updatedBy(String updatedBy) {
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
    ComplianceRule complianceRule = (ComplianceRule) o;
    return Objects.equals(this.ruleId, complianceRule.ruleId) &&
        Objects.equals(this.ruleName, complianceRule.ruleName) &&
        Objects.equals(this.ruleDescription, complianceRule.ruleDescription) &&
        Objects.equals(this.category, complianceRule.category) &&
        Objects.equals(this.severity, complianceRule.severity) &&
        Objects.equals(this.ruleType, complianceRule.ruleType) &&
        Objects.equals(this.ruleOwner, complianceRule.ruleOwner) &&
        Objects.equals(this.enabled, complianceRule.enabled) &&
        Objects.equals(this.mandatory, complianceRule.mandatory) &&
        Objects.equals(this.displayOrder, complianceRule.displayOrder) &&
        Objects.equals(this.icon, complianceRule.icon) &&
        Objects.equals(this.status, complianceRule.status) &&
        Objects.equals(this.createDate, complianceRule.createDate) &&
        Objects.equals(this.createdBy, complianceRule.createdBy) &&
        Objects.equals(this.updatedDate, complianceRule.updatedDate) &&
        Objects.equals(this.updatedBy, complianceRule.updatedBy);
  }

  @Override
  public int hashCode() {
    return Objects.hash(ruleId, ruleName, ruleDescription, category, severity, ruleType, ruleOwner, enabled, mandatory, displayOrder, icon, status, createDate, createdBy, updatedDate, updatedBy);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class ComplianceRule {\n");
    sb.append("    ruleId: ").append(toIndentedString(ruleId)).append("\n");
    sb.append("    ruleName: ").append(toIndentedString(ruleName)).append("\n");
    sb.append("    ruleDescription: ").append(toIndentedString(ruleDescription)).append("\n");
    sb.append("    category: ").append(toIndentedString(category)).append("\n");
    sb.append("    severity: ").append(toIndentedString(severity)).append("\n");
    sb.append("    ruleType: ").append(toIndentedString(ruleType)).append("\n");
    sb.append("    ruleOwner: ").append(toIndentedString(ruleOwner)).append("\n");
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

