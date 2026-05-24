package com.probestack.forgesphere.model;

import java.net.URI;
import java.util.Objects;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.probestack.forgesphere.model.RuleType;
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
 * ScanRules
 */
@Generated(value = "org.openapitools.codegen.languages.SpringCodegen", date = "2026-05-13T05:38:50.494838724Z[GMT]")public class ScanRules {

  @Valid
  private List<@Valid RuleType> ruleTypes = new ArrayList<>();

  @Valid
  private List<String> ruleIds;

  public ScanRules() {
    super();
  }

  /**
   * Constructor with only required parameters
   */
  public ScanRules(List<@Valid RuleType> ruleTypes) {
    this.ruleTypes = ruleTypes;
  }

  public ScanRules ruleTypes(List<@Valid RuleType> ruleTypes) {
    this.ruleTypes = ruleTypes;
    return this;
  }

  public ScanRules addRuleTypesItem(RuleType ruleTypesItem) {
    if (this.ruleTypes == null) {
      this.ruleTypes = new ArrayList<>();
    }
    this.ruleTypes.add(ruleTypesItem);
    return this;
  }

  /**
   * Get ruleTypes
   * @return ruleTypes
  */
  @NotNull @Valid   @Schema(name = "ruleTypes", example = "[\"PRE_DEFINED\",\"CUSTOM\"]", requiredMode = Schema.RequiredMode.REQUIRED)
  @JsonProperty("ruleTypes")
  public List<@Valid RuleType> getRuleTypes() {
    return ruleTypes;
  }

  public void setRuleTypes(List<@Valid RuleType> ruleTypes) {
    this.ruleTypes = ruleTypes;
  }

  public ScanRules ruleIds(List<String> ruleIds) {
    this.ruleIds = ruleIds;
    return this;
  }

  public ScanRules addRuleIdsItem(String ruleIdsItem) {
    if (this.ruleIds == null) {
      this.ruleIds = new ArrayList<>();
    }
    this.ruleIds.add(ruleIdsItem);
    return this;
  }

  /**
   * Get ruleIds
   * @return ruleIds
  */
    @Schema(name = "ruleIds", example = "[\"CR1\",\"CR2\"]", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
  @JsonProperty("ruleIds")
  public List<String> getRuleIds() {
    return ruleIds;
  }

  public void setRuleIds(List<String> ruleIds) {
    this.ruleIds = ruleIds;
  }
  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    ScanRules scanRules = (ScanRules) o;
    return Objects.equals(this.ruleTypes, scanRules.ruleTypes) &&
        Objects.equals(this.ruleIds, scanRules.ruleIds);
  }

  @Override
  public int hashCode() {
    return Objects.hash(ruleTypes, ruleIds);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class ScanRules {\n");
    sb.append("    ruleTypes: ").append(toIndentedString(ruleTypes)).append("\n");
    sb.append("    ruleIds: ").append(toIndentedString(ruleIds)).append("\n");
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

