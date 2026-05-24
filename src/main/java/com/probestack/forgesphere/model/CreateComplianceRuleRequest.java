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
import org.openapitools.jackson.nullable.JsonNullable;
import java.time.OffsetDateTime;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import io.swagger.v3.oas.annotations.media.Schema;


import java.util.*;
import jakarta.annotation.Generated;

/**
 * CreateComplianceRuleRequest
 */
@Generated(value = "org.openapitools.codegen.languages.SpringCodegen", date = "2026-05-13T05:38:50.494838724Z[GMT]")public class CreateComplianceRuleRequest {

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

  private String createdBy;

  private String updatedBy;

  public CreateComplianceRuleRequest() {
    super();
  }

  /**
   * Constructor with only required parameters
   */
  public CreateComplianceRuleRequest(String ruleName, RuleType ruleType, String ruleOwner, RuleCategory category, RuleSeverity severity, Boolean enabled, RuleStatus status, String createdBy, String updatedBy) {
    this.ruleName = ruleName;
    this.ruleType = ruleType;
    this.ruleOwner = ruleOwner;
    this.category = category;
    this.severity = severity;
    this.enabled = enabled;
    this.status = status;
    this.createdBy = createdBy;
    this.updatedBy = updatedBy;
  }

  public CreateComplianceRuleRequest ruleName(String ruleName) {
    this.ruleName = ruleName;
    return this;
  }

  /**
   * Get ruleName
   * @return ruleName
  */
  @NotNull   @Schema(name = "ruleName", example = "Documentation required for all endpoints", requiredMode = Schema.RequiredMode.REQUIRED)
  @JsonProperty("ruleName")
  public String getRuleName() {
    return ruleName;
  }

  public void setRuleName(String ruleName) {
    this.ruleName = ruleName;
  }

  public CreateComplianceRuleRequest ruleDescription(String ruleDescription) {
    this.ruleDescription = ruleDescription;
    return this;
  }

  /**
   * Get ruleDescription
   * @return ruleDescription
  */
    @Schema(name = "ruleDescription", example = "Every API endpoint must have comprehensive documentation including request/response schemas, error codes, and usage examples.", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
  @JsonProperty("ruleDescription")
  public String getRuleDescription() {
    return ruleDescription;
  }

  public void setRuleDescription(String ruleDescription) {
    this.ruleDescription = ruleDescription;
  }

  public CreateComplianceRuleRequest ruleType(RuleType ruleType) {
    this.ruleType = ruleType;
    return this;
  }

  /**
   * Get ruleType
   * @return ruleType
  */
  @NotNull @Valid   @Schema(name = "ruleType", requiredMode = Schema.RequiredMode.REQUIRED)
  @JsonProperty("ruleType")
  public RuleType getRuleType() {
    return ruleType;
  }

  public void setRuleType(RuleType ruleType) {
    this.ruleType = ruleType;
  }

  public CreateComplianceRuleRequest ruleOwner(String ruleOwner) {
    this.ruleOwner = ruleOwner;
    return this;
  }

  /**
   * Get ruleOwner
   * @return ruleOwner
  */
  @NotNull   @Schema(name = "ruleOwner", example = "Governance Team", requiredMode = Schema.RequiredMode.REQUIRED)
  @JsonProperty("ruleOwner")
  public String getRuleOwner() {
    return ruleOwner;
  }

  public void setRuleOwner(String ruleOwner) {
    this.ruleOwner = ruleOwner;
  }

  public CreateComplianceRuleRequest category(RuleCategory category) {
    this.category = category;
    return this;
  }

  /**
   * Get category
   * @return category
  */
  @NotNull @Valid   @Schema(name = "category", requiredMode = Schema.RequiredMode.REQUIRED)
  @JsonProperty("category")
  public RuleCategory getCategory() {
    return category;
  }

  public void setCategory(RuleCategory category) {
    this.category = category;
  }

  public CreateComplianceRuleRequest severity(RuleSeverity severity) {
    this.severity = severity;
    return this;
  }

  /**
   * Get severity
   * @return severity
  */
  @NotNull @Valid   @Schema(name = "severity", requiredMode = Schema.RequiredMode.REQUIRED)
  @JsonProperty("severity")
  public RuleSeverity getSeverity() {
    return severity;
  }

  public void setSeverity(RuleSeverity severity) {
    this.severity = severity;
  }

  public CreateComplianceRuleRequest enabled(Boolean enabled) {
    this.enabled = enabled;
    return this;
  }

  /**
   * Get enabled
   * @return enabled
  */
  @NotNull   @Schema(name = "enabled", example = "true", requiredMode = Schema.RequiredMode.REQUIRED)
  @JsonProperty("enabled")
  public Boolean getEnabled() {
    return enabled;
  }

  public void setEnabled(Boolean enabled) {
    this.enabled = enabled;
  }

  public CreateComplianceRuleRequest mandatory(Boolean mandatory) {
    this.mandatory = mandatory;
    return this;
  }

  /**
   * Get mandatory
   * @return mandatory
  */
    @Schema(name = "mandatory", example = "true", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
  @JsonProperty("mandatory")
  public Boolean getMandatory() {
    return mandatory;
  }

  public void setMandatory(Boolean mandatory) {
    this.mandatory = mandatory;
  }

  public CreateComplianceRuleRequest displayOrder(Integer displayOrder) {
    this.displayOrder = displayOrder;
    return this;
  }

  /**
   * Get displayOrder
   * @return displayOrder
  */
    @Schema(name = "displayOrder", example = "1", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
  @JsonProperty("displayOrder")
  public Integer getDisplayOrder() {
    return displayOrder;
  }

  public void setDisplayOrder(Integer displayOrder) {
    this.displayOrder = displayOrder;
  }

  public CreateComplianceRuleRequest icon(String icon) {
    this.icon = icon;
    return this;
  }

  /**
   * Get icon
   * @return icon
  */
    @Schema(name = "icon", example = "file-text", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
  @JsonProperty("icon")
  public String getIcon() {
    return icon;
  }

  public void setIcon(String icon) {
    this.icon = icon;
  }

  public CreateComplianceRuleRequest status(RuleStatus status) {
    this.status = status;
    return this;
  }

  /**
   * Get status
   * @return status
  */
  @NotNull @Valid   @Schema(name = "status", requiredMode = Schema.RequiredMode.REQUIRED)
  @JsonProperty("status")
  public RuleStatus getStatus() {
    return status;
  }

  public void setStatus(RuleStatus status) {
    this.status = status;
  }

  public CreateComplianceRuleRequest createdBy(String createdBy) {
    this.createdBy = createdBy;
    return this;
  }

  /**
   * Get createdBy
   * @return createdBy
  */
  @NotNull   @Schema(name = "createdBy", example = "admin@probestack.io", requiredMode = Schema.RequiredMode.REQUIRED)
  @JsonProperty("createdBy")
  public String getCreatedBy() {
    return createdBy;
  }

  public void setCreatedBy(String createdBy) {
    this.createdBy = createdBy;
  }

  public CreateComplianceRuleRequest updatedBy(String updatedBy) {
    this.updatedBy = updatedBy;
    return this;
  }

  /**
   * Get updatedBy
   * @return updatedBy
  */
  @NotNull   @Schema(name = "updatedBy", example = "admin@probestack.io", requiredMode = Schema.RequiredMode.REQUIRED)
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
    CreateComplianceRuleRequest createComplianceRuleRequest = (CreateComplianceRuleRequest) o;
    return Objects.equals(this.ruleName, createComplianceRuleRequest.ruleName) &&
        Objects.equals(this.ruleDescription, createComplianceRuleRequest.ruleDescription) &&
        Objects.equals(this.ruleType, createComplianceRuleRequest.ruleType) &&
        Objects.equals(this.ruleOwner, createComplianceRuleRequest.ruleOwner) &&
        Objects.equals(this.category, createComplianceRuleRequest.category) &&
        Objects.equals(this.severity, createComplianceRuleRequest.severity) &&
        Objects.equals(this.enabled, createComplianceRuleRequest.enabled) &&
        Objects.equals(this.mandatory, createComplianceRuleRequest.mandatory) &&
        Objects.equals(this.displayOrder, createComplianceRuleRequest.displayOrder) &&
        Objects.equals(this.icon, createComplianceRuleRequest.icon) &&
        Objects.equals(this.status, createComplianceRuleRequest.status) &&
        Objects.equals(this.createdBy, createComplianceRuleRequest.createdBy) &&
        Objects.equals(this.updatedBy, createComplianceRuleRequest.updatedBy);
  }

  @Override
  public int hashCode() {
    return Objects.hash(ruleName, ruleDescription, ruleType, ruleOwner, category, severity, enabled, mandatory, displayOrder, icon, status, createdBy, updatedBy);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class CreateComplianceRuleRequest {\n");
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
    sb.append("    createdBy: ").append(toIndentedString(createdBy)).append("\n");
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

