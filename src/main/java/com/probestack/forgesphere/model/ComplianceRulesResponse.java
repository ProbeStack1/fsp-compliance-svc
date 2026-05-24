package com.probestack.forgesphere.model;

import java.net.URI;
import java.util.Objects;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import com.probestack.forgesphere.model.ComplianceRule;
import com.probestack.forgesphere.model.AssetType;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.openapitools.jackson.nullable.JsonNullable;
import java.time.OffsetDateTime;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import io.swagger.v3.oas.annotations.media.Schema;


import java.util.*;
import jakarta.annotation.Generated;

/**
 * ComplianceRulesResponse
 */
@Generated(value = "org.openapitools.codegen.languages.SpringCodegen", date = "2026-05-13T05:38:50.494838724Z[GMT]")public class ComplianceRulesResponse {

  private String projectName;

  private AssetType resourceType;

  private String resourceName;

  private Integer activeRules;

  private Integer totalRules;

  @Valid
  private List<@Valid ComplianceRule> rules;

  public ComplianceRulesResponse projectName(String projectName) {
    this.projectName = projectName;
    return this;
  }

  /**
   * Get projectName
   * @return projectName
  */
    @Schema(name = "projectName", example = "Payment Gateway", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
  @JsonProperty("projectName")
  public String getProjectName() {
    return projectName;
  }

  public void setProjectName(String projectName) {
    this.projectName = projectName;
  }

  public ComplianceRulesResponse resourceType(AssetType resourceType) {
    this.resourceType = resourceType;
    return this;
  }

  /**
   * Get resourceType
   * @return resourceType
  */
  @Valid   @Schema(name = "resourceType", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
  @JsonProperty("resourceType")
  public AssetType getResourceType() {
    return resourceType;
  }

  public void setResourceType(AssetType resourceType) {
    this.resourceType = resourceType;
  }

  public ComplianceRulesResponse resourceName(String resourceName) {
    this.resourceName = resourceName;
    return this;
  }

  /**
   * Get resourceName
   * @return resourceName
  */
    @Schema(name = "resourceName", example = "Remit2Any", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
  @JsonProperty("resourceName")
  public String getResourceName() {
    return resourceName;
  }

  public void setResourceName(String resourceName) {
    this.resourceName = resourceName;
  }

  public ComplianceRulesResponse activeRules(Integer activeRules) {
    this.activeRules = activeRules;
    return this;
  }

  /**
   * Get activeRules
   * @return activeRules
  */
    @Schema(name = "activeRules", example = "29", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
  @JsonProperty("activeRules")
  public Integer getActiveRules() {
    return activeRules;
  }

  public void setActiveRules(Integer activeRules) {
    this.activeRules = activeRules;
  }

  public ComplianceRulesResponse totalRules(Integer totalRules) {
    this.totalRules = totalRules;
    return this;
  }

  /**
   * Get totalRules
   * @return totalRules
  */
    @Schema(name = "totalRules", example = "30", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
  @JsonProperty("totalRules")
  public Integer getTotalRules() {
    return totalRules;
  }

  public void setTotalRules(Integer totalRules) {
    this.totalRules = totalRules;
  }

  public ComplianceRulesResponse rules(List<@Valid ComplianceRule> rules) {
    this.rules = rules;
    return this;
  }

  public ComplianceRulesResponse addRulesItem(ComplianceRule rulesItem) {
    if (this.rules == null) {
      this.rules = new ArrayList<>();
    }
    this.rules.add(rulesItem);
    return this;
  }

  /**
   * Get rules
   * @return rules
  */
  @Valid   @Schema(name = "rules", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
  @JsonProperty("rules")
  public List<@Valid ComplianceRule> getRules() {
    return rules;
  }

  public void setRules(List<@Valid ComplianceRule> rules) {
    this.rules = rules;
  }
  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    ComplianceRulesResponse complianceRulesResponse = (ComplianceRulesResponse) o;
    return Objects.equals(this.projectName, complianceRulesResponse.projectName) &&
        Objects.equals(this.resourceType, complianceRulesResponse.resourceType) &&
        Objects.equals(this.resourceName, complianceRulesResponse.resourceName) &&
        Objects.equals(this.activeRules, complianceRulesResponse.activeRules) &&
        Objects.equals(this.totalRules, complianceRulesResponse.totalRules) &&
        Objects.equals(this.rules, complianceRulesResponse.rules);
  }

  @Override
  public int hashCode() {
    return Objects.hash(projectName, resourceType, resourceName, activeRules, totalRules, rules);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class ComplianceRulesResponse {\n");
    sb.append("    projectName: ").append(toIndentedString(projectName)).append("\n");
    sb.append("    resourceType: ").append(toIndentedString(resourceType)).append("\n");
    sb.append("    resourceName: ").append(toIndentedString(resourceName)).append("\n");
    sb.append("    activeRules: ").append(toIndentedString(activeRules)).append("\n");
    sb.append("    totalRules: ").append(toIndentedString(totalRules)).append("\n");
    sb.append("    rules: ").append(toIndentedString(rules)).append("\n");
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

