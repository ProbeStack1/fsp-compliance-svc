package com.probestack.forgesphere.model;

import java.net.URI;
import java.util.Objects;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import com.probestack.forgesphere.model.RuleCategory;
import com.probestack.forgesphere.model.RuleSeverity;
import com.probestack.forgesphere.model.RuleType;
import com.probestack.forgesphere.model.ScanEvidence;
import com.probestack.forgesphere.model.ScanResultStatus;
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
 * ScanResult
 */
@Generated(value = "org.openapitools.codegen.languages.SpringCodegen", date = "2026-05-13T05:38:50.494838724Z[GMT]")public class ScanResult {

  private String ruleId;

  private String ruleName;

  private RuleType ruleType;

  private RuleCategory category;

  private RuleSeverity severity;

  private ScanResultStatus result;

  private String message;

  @Valid
  private List<@Valid ScanEvidence> evidence;

  public ScanResult ruleId(String ruleId) {
    this.ruleId = ruleId;
    return this;
  }

  /**
   * Get ruleId
   * @return ruleId
  */
    @Schema(name = "ruleId", example = "CR1", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
  @JsonProperty("ruleId")
  public String getRuleId() {
    return ruleId;
  }

  public void setRuleId(String ruleId) {
    this.ruleId = ruleId;
  }

  public ScanResult ruleName(String ruleName) {
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

  public ScanResult ruleType(RuleType ruleType) {
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

  public ScanResult category(RuleCategory category) {
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

  public ScanResult severity(RuleSeverity severity) {
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

  public ScanResult result(ScanResultStatus result) {
    this.result = result;
    return this;
  }

  /**
   * Get result
   * @return result
  */
  @Valid   @Schema(name = "result", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
  @JsonProperty("result")
  public ScanResultStatus getResult() {
    return result;
  }

  public void setResult(ScanResultStatus result) {
    this.result = result;
  }

  public ScanResult message(String message) {
    this.message = message;
    return this;
  }

  /**
   * Get message
   * @return message
  */
    @Schema(name = "message", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
  @JsonProperty("message")
  public String getMessage() {
    return message;
  }

  public void setMessage(String message) {
    this.message = message;
  }

  public ScanResult evidence(List<@Valid ScanEvidence> evidence) {
    this.evidence = evidence;
    return this;
  }

  public ScanResult addEvidenceItem(ScanEvidence evidenceItem) {
    if (this.evidence == null) {
      this.evidence = new ArrayList<>();
    }
    this.evidence.add(evidenceItem);
    return this;
  }

  /**
   * Get evidence
   * @return evidence
  */
  @Valid   @Schema(name = "evidence", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
  @JsonProperty("evidence")
  public List<@Valid ScanEvidence> getEvidence() {
    return evidence;
  }

  public void setEvidence(List<@Valid ScanEvidence> evidence) {
    this.evidence = evidence;
  }
  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    ScanResult scanResult = (ScanResult) o;
    return Objects.equals(this.ruleId, scanResult.ruleId) &&
        Objects.equals(this.ruleName, scanResult.ruleName) &&
        Objects.equals(this.ruleType, scanResult.ruleType) &&
        Objects.equals(this.category, scanResult.category) &&
        Objects.equals(this.severity, scanResult.severity) &&
        Objects.equals(this.result, scanResult.result) &&
        Objects.equals(this.message, scanResult.message) &&
        Objects.equals(this.evidence, scanResult.evidence);
  }

  @Override
  public int hashCode() {
    return Objects.hash(ruleId, ruleName, ruleType, category, severity, result, message, evidence);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class ScanResult {\n");
    sb.append("    ruleId: ").append(toIndentedString(ruleId)).append("\n");
    sb.append("    ruleName: ").append(toIndentedString(ruleName)).append("\n");
    sb.append("    ruleType: ").append(toIndentedString(ruleType)).append("\n");
    sb.append("    category: ").append(toIndentedString(category)).append("\n");
    sb.append("    severity: ").append(toIndentedString(severity)).append("\n");
    sb.append("    result: ").append(toIndentedString(result)).append("\n");
    sb.append("    message: ").append(toIndentedString(message)).append("\n");
    sb.append("    evidence: ").append(toIndentedString(evidence)).append("\n");
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

