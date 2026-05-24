package com.probestack.forgesphere.model;

import java.net.URI;
import java.util.Objects;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import com.probestack.forgesphere.model.ScanOptions;
import com.probestack.forgesphere.model.ScanRules;
import com.probestack.forgesphere.model.AssetType;
import com.probestack.forgesphere.model.ScanSource;
import com.probestack.forgesphere.model.SourceType;
import org.openapitools.jackson.nullable.JsonNullable;
import java.time.OffsetDateTime;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import io.swagger.v3.oas.annotations.media.Schema;


import java.util.*;
import jakarta.annotation.Generated;

/**
 * SubmitComplianceScanRequest
 */
@Generated(value = "org.openapitools.codegen.languages.SpringCodegen", date = "2026-05-13T05:38:50.494838724Z[GMT]")public class SubmitComplianceScanRequest {

  private String projectName;

  private String companyName;

  private String assetId;

  private String assetName;

  private AssetType assetType;

  private SourceType sourceType;

  private ScanSource source;

  private ScanRules rules;

  private ScanOptions scanOptions;

  private String requestedBy;

  public SubmitComplianceScanRequest() {
    super();
  }

  /**
   * Constructor with only required parameters
   */
  public SubmitComplianceScanRequest(String projectName, String companyName, String assetId, String assetName, AssetType assetType, SourceType sourceType, ScanSource source, ScanRules rules, ScanOptions scanOptions, String requestedBy) {
    this.projectName = projectName;
    this.companyName = companyName;
    this.assetId = assetId;
    this.assetName = assetName;
    this.assetType = assetType;
    this.sourceType = sourceType;
    this.source = source;
    this.rules = rules;
    this.scanOptions = scanOptions;
    this.requestedBy = requestedBy;
  }

  public SubmitComplianceScanRequest projectName(String projectName) {
    this.projectName = projectName;
    return this;
  }

  /**
   * Get projectName
   * @return projectName
  */
  @NotNull   @Schema(name = "projectName", example = "Payment Gateway", requiredMode = Schema.RequiredMode.REQUIRED)
  @JsonProperty("projectName")
  public String getProjectName() {
    return projectName;
  }

  public void setProjectName(String projectName) {
    this.projectName = projectName;
  }

  public SubmitComplianceScanRequest companyName(String companyName) {
    this.companyName = companyName;
    return this;
  }

  /**
   * Get companyName
   * @return companyName
  */
  @NotNull   @Schema(name = "companyName", example = "Probestack", requiredMode = Schema.RequiredMode.REQUIRED)
  @JsonProperty("companyName")
  public String getCompanyName() {
    return companyName;
  }

  public void setCompanyName(String companyName) {
    this.companyName = companyName;
  }

  public SubmitComplianceScanRequest assetId(String assetId) {
    this.assetId = assetId;
    return this;
  }

  /**
   * Get assetId
   * @return assetId
  */
  @NotNull   @Schema(name = "assetId", example = "PG-001", requiredMode = Schema.RequiredMode.REQUIRED)
  @JsonProperty("assetId")
  public String getAssetId() {
    return assetId;
  }

  public void setAssetId(String assetId) {
    this.assetId = assetId;
  }

  public SubmitComplianceScanRequest assetName(String assetName) {
    this.assetName = assetName;
    return this;
  }

  /**
   * Get assetName
   * @return assetName
  */
  @NotNull   @Schema(name = "assetName", example = "Remit2Any", requiredMode = Schema.RequiredMode.REQUIRED)
  @JsonProperty("assetName")
  public String getAssetName() {
    return assetName;
  }

  public void setAssetName(String assetName) {
    this.assetName = assetName;
  }

  public SubmitComplianceScanRequest assetType(AssetType assetType) {
    this.assetType = assetType;
    return this;
  }

  /**
   * Get assetType
   * @return assetType
  */
  @NotNull @Valid   @Schema(name = "assetType", requiredMode = Schema.RequiredMode.REQUIRED)
  @JsonProperty("assetType")
  public AssetType getAssetType() {
    return assetType;
  }

  public void setAssetType(AssetType assetType) {
    this.assetType = assetType;
  }

  public SubmitComplianceScanRequest sourceType(SourceType sourceType) {
    this.sourceType = sourceType;
    return this;
  }

  /**
   * Get sourceType
   * @return sourceType
  */
  @Valid   @Schema(name = "sourceType", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
  @JsonProperty("sourceType")
  public SourceType getSourceType() {
    return sourceType;
  }

  public void setSourceType(SourceType sourceType) {
    this.sourceType = sourceType;
  }

  public SubmitComplianceScanRequest source(ScanSource source) {
    this.source = source;
    return this;
  }

  /**
   * Get source
   * @return source
  */
  @Valid   @Schema(name = "source", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
  @JsonProperty("source")
  public ScanSource getSource() {
    return source;
  }

  public void setSource(ScanSource source) {
    this.source = source;
  }

  public SubmitComplianceScanRequest rules(ScanRules rules) {
    this.rules = rules;
    return this;
  }

  /**
   * Get rules
   * @return rules
  */
  @Valid   @Schema(name = "rules", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
  @JsonProperty("rules")
  public ScanRules getRules() {
    return rules;
  }

  public void setRules(ScanRules rules) {
    this.rules = rules;
  }

  public SubmitComplianceScanRequest scanOptions(ScanOptions scanOptions) {
    this.scanOptions = scanOptions;
    return this;
  }

  /**
   * Get scanOptions
   * @return scanOptions
  */
  @Valid   @Schema(name = "scanOptions", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
  @JsonProperty("scanOptions")
  public ScanOptions getScanOptions() {
    return scanOptions;
  }

  public void setScanOptions(ScanOptions scanOptions) {
    this.scanOptions = scanOptions;
  }

  public SubmitComplianceScanRequest requestedBy(String requestedBy) {
    this.requestedBy = requestedBy;
    return this;
  }

  /**
   * Get requestedBy
   * @return requestedBy
  */
  @NotNull   @Schema(name = "requestedBy", example = "admin@probestack.io", requiredMode = Schema.RequiredMode.REQUIRED)
  @JsonProperty("requestedBy")
  public String getRequestedBy() {
    return requestedBy;
  }

  public void setRequestedBy(String requestedBy) {
    this.requestedBy = requestedBy;
  }
  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    SubmitComplianceScanRequest submitComplianceScanRequest = (SubmitComplianceScanRequest) o;
    return Objects.equals(this.projectName, submitComplianceScanRequest.projectName) &&
        Objects.equals(this.companyName, submitComplianceScanRequest.companyName) &&
        Objects.equals(this.assetId, submitComplianceScanRequest.assetId) &&
        Objects.equals(this.assetName, submitComplianceScanRequest.assetName) &&
        Objects.equals(this.assetType, submitComplianceScanRequest.assetType) &&
        Objects.equals(this.sourceType, submitComplianceScanRequest.sourceType) &&
        Objects.equals(this.source, submitComplianceScanRequest.source) &&
        Objects.equals(this.rules, submitComplianceScanRequest.rules) &&
        Objects.equals(this.scanOptions, submitComplianceScanRequest.scanOptions) &&
        Objects.equals(this.requestedBy, submitComplianceScanRequest.requestedBy);
  }

  @Override
  public int hashCode() {
    return Objects.hash(projectName, companyName, assetId, assetName, assetType, sourceType, source, rules, scanOptions, requestedBy);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class SubmitComplianceScanRequest {\n");
    sb.append("    projectName: ").append(toIndentedString(projectName)).append("\n");
    sb.append("    companyName: ").append(toIndentedString(companyName)).append("\n");
    sb.append("    assetId: ").append(toIndentedString(assetId)).append("\n");
    sb.append("    assetName: ").append(toIndentedString(assetName)).append("\n");
    sb.append("    assetType: ").append(toIndentedString(assetType)).append("\n");
    sb.append("    sourceType: ").append(toIndentedString(sourceType)).append("\n");
    sb.append("    source: ").append(toIndentedString(source)).append("\n");
    sb.append("    rules: ").append(toIndentedString(rules)).append("\n");
    sb.append("    scanOptions: ").append(toIndentedString(scanOptions)).append("\n");
    sb.append("    requestedBy: ").append(toIndentedString(requestedBy)).append("\n");
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
