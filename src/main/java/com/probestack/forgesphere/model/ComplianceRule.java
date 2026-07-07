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
@Generated(value = "org.openapitools.codegen.languages.SpringCodegen", date = "2026-05-13T05:38:50.494838724Z[GMT]")
public class ComplianceRule {

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

  //  NEW FIELDS
  private String scope;
  private String field;
  private String condition;

  // Getters & Setters
  public ComplianceRule ruleId(String ruleId) {
    this.ruleId = ruleId;
    return this;
  }

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

  @Valid
  @Schema(name = "category", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
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

  @Valid
  @Schema(name = "severity", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
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

  @Valid
  @Schema(name = "ruleType", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
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

  @Valid
  @Schema(name = "status", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
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

  @Valid
  @Schema(name = "createDate", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
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

  @Valid
  @Schema(name = "updatedDate", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
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

  @Schema(name = "updatedBy", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
  @JsonProperty("updatedBy")
  public String getUpdatedBy() {
    return updatedBy;
  }

  public void setUpdatedBy(String updatedBy) {
    this.updatedBy = updatedBy;
  }

  // NEW GETTERS/SETTERS
  @Schema(name = "scope", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
  @JsonProperty("scope")
  public String getScope() {
    return scope;
  }

  public void setScope(String scope) {
    this.scope = scope;
  }

  @Schema(name = "field", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
  @JsonProperty("field")
  public String getField() {
    return field;
  }

  public void setField(String field) {
    this.field = field;
  }

  @Schema(name = "condition", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
  @JsonProperty("condition")
  public String getCondition() {
    return condition;
  }

  public void setCondition(String condition) {
    this.condition = condition;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    ComplianceRule that = (ComplianceRule) o;
    return Objects.equals(this.ruleId, that.ruleId) &&
        Objects.equals(this.ruleName, that.ruleName) &&
        Objects.equals(this.ruleDescription, that.ruleDescription) &&
        Objects.equals(this.category, that.category) &&
        Objects.equals(this.severity, that.severity) &&
        Objects.equals(this.ruleType, that.ruleType) &&
        Objects.equals(this.ruleOwner, that.ruleOwner) &&
        Objects.equals(this.enabled, that.enabled) &&
        Objects.equals(this.mandatory, that.mandatory) &&
        Objects.equals(this.displayOrder, that.displayOrder) &&
        Objects.equals(this.icon, that.icon) &&
        Objects.equals(this.status, that.status) &&
        Objects.equals(this.createDate, that.createDate) &&
        Objects.equals(this.createdBy, that.createdBy) &&
        Objects.equals(this.updatedDate, that.updatedDate) &&
        Objects.equals(this.updatedBy, that.updatedBy) &&
        Objects.equals(this.scope, that.scope) &&
        Objects.equals(this.field, that.field) &&
        Objects.equals(this.condition, that.condition);
  }

  @Override
  public int hashCode() {
    return Objects.hash(ruleId, ruleName, ruleDescription, category, severity, ruleType, ruleOwner, enabled, mandatory, displayOrder, icon, status, createDate, createdBy, updatedDate, updatedBy, scope, field, condition);
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
    sb.append("    scope: ").append(toIndentedString(scope)).append("\n");
    sb.append("    field: ").append(toIndentedString(field)).append("\n");
    sb.append("    condition: ").append(toIndentedString(condition)).append("\n");
    sb.append("}");
    return sb.toString();
  }

  /**
   * Convert the given object to string with each line indented by 4 spaces
   * (except the first line).
   */
  private String toIndentedString(Object o) {
    if (o == null) return "null";
    return o.toString().replace("\n", "\n    ");
  }
}

