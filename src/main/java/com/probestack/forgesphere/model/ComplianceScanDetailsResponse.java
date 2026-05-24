package com.probestack.forgesphere.model;

import java.net.URI;
import java.util.Objects;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import com.probestack.forgesphere.model.ScanOptions;
import com.probestack.forgesphere.model.ScanResult;
import com.probestack.forgesphere.model.ScanRules;
import com.probestack.forgesphere.model.AssetType;
import com.probestack.forgesphere.model.ComplianceStatus;
import com.probestack.forgesphere.model.ScanSource;
import com.probestack.forgesphere.model.ScanStatus;
import com.probestack.forgesphere.model.SourceType;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.springframework.format.annotation.DateTimeFormat;
import org.openapitools.jackson.nullable.JsonNullable;
import java.time.OffsetDateTime;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import io.swagger.v3.oas.annotations.media.Schema;


import java.util.*;
import jakarta.annotation.Generated;

/**
 * ComplianceScanDetailsResponse
 */
@Generated(value = "org.openapitools.codegen.languages.SpringCodegen", date = "2026-05-13T05:38:50.494838724Z[GMT]")public class ComplianceScanDetailsResponse {

  private String scanId;

  private String projectName;

  private String companyName;

  private String assetId;

  private String assetName;

  private AssetType assetType;

  private SourceType sourceType;

  private ScanSource source;

  private ScanRules rules;

  private ScanOptions scanOptions;

  private ScanStatus status;

  private ComplianceStatus compliance;

  @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
  private OffsetDateTime scanDate;

  @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
  private OffsetDateTime processingStartDate;

  @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
  private OffsetDateTime processingEndDate;

  @Valid
  private List<@Valid ScanResult> scanResults;

  private String errorMessage;

  @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
  private OffsetDateTime createDate;

  private String createdBy;

  @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
  private OffsetDateTime updatedDate;

  private String updatedBy;

  public ComplianceScanDetailsResponse scanId(String scanId) {
    this.scanId = scanId;
    return this;
  }

  /**
   * Get scanId
   * @return scanId
  */
    @Schema(name = "scanId", example = "SCAN-20260511-000001", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
  @JsonProperty("scanId")
  public String getScanId() {
    return scanId;
  }

  public void setScanId(String scanId) {
    this.scanId = scanId;
  }

  public ComplianceScanDetailsResponse projectName(String projectName) {
    this.projectName = projectName;
    return this;
  }

  /**
   * Get projectName
   * @return projectName
  */
    @Schema(name = "projectName", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
  @JsonProperty("projectName")
  public String getProjectName() {
    return projectName;
  }

  public void setProjectName(String projectName) {
    this.projectName = projectName;
  }

  public ComplianceScanDetailsResponse companyName(String companyName) {
    this.companyName = companyName;
    return this;
  }

  /**
   * Get companyName
   * @return companyName
  */
    @Schema(name = "companyName", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
  @JsonProperty("companyName")
  public String getCompanyName() {
    return companyName;
  }

  public void setCompanyName(String companyName) {
    this.companyName = companyName;
  }

  public ComplianceScanDetailsResponse assetId(String assetId) {
    this.assetId = assetId;
    return this;
  }

  /**
   * Get assetId
   * @return assetId
  */
    @Schema(name = "assetId", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
  @JsonProperty("assetId")
  public String getAssetId() {
    return assetId;
  }

  public void setAssetId(String assetId) {
    this.assetId = assetId;
  }

  public ComplianceScanDetailsResponse assetName(String assetName) {
    this.assetName = assetName;
    return this;
  }

  /**
   * Get assetName
   * @return assetName
  */
    @Schema(name = "assetName", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
  @JsonProperty("assetName")
  public String getAssetName() {
    return assetName;
  }

  public void setAssetName(String assetName) {
    this.assetName = assetName;
  }

  public ComplianceScanDetailsResponse assetType(AssetType assetType) {
    this.assetType = assetType;
    return this;
  }

  /**
   * Get assetType
   * @return assetType
  */
  @Valid   @Schema(name = "assetType", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
  @JsonProperty("assetType")
  public AssetType getAssetType() {
    return assetType;
  }

  public void setAssetType(AssetType assetType) {
    this.assetType = assetType;
  }

  public ComplianceScanDetailsResponse sourceType(SourceType sourceType) {
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

  public ComplianceScanDetailsResponse source(ScanSource source) {
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

  public ComplianceScanDetailsResponse rules(ScanRules rules) {
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

  public ComplianceScanDetailsResponse scanOptions(ScanOptions scanOptions) {
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

  public ComplianceScanDetailsResponse status(ScanStatus status) {
    this.status = status;
    return this;
  }

  /**
   * Get status
   * @return status
  */
  @Valid   @Schema(name = "status", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
  @JsonProperty("status")
  public ScanStatus getStatus() {
    return status;
  }

  public void setStatus(ScanStatus status) {
    this.status = status;
  }

  public ComplianceScanDetailsResponse compliance(ComplianceStatus compliance) {
    this.compliance = compliance;
    return this;
  }

  /**
   * Get compliance
   * @return compliance
  */
  @Valid   @Schema(name = "compliance", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
  @JsonProperty("compliance")
  public ComplianceStatus getCompliance() {
    return compliance;
  }

  public void setCompliance(ComplianceStatus compliance) {
    this.compliance = compliance;
  }

  public ComplianceScanDetailsResponse scanDate(OffsetDateTime scanDate) {
    this.scanDate = scanDate;
    return this;
  }

  /**
   * Get scanDate
   * @return scanDate
  */
  @Valid   @Schema(name = "scanDate", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
  @JsonProperty("scanDate")
  public OffsetDateTime getScanDate() {
    return scanDate;
  }

  public void setScanDate(OffsetDateTime scanDate) {
    this.scanDate = scanDate;
  }

  public ComplianceScanDetailsResponse processingStartDate(OffsetDateTime processingStartDate) {
    this.processingStartDate = processingStartDate;
    return this;
  }

  /**
   * Get processingStartDate
   * @return processingStartDate
  */
  @Valid   @Schema(name = "processingStartDate", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
  @JsonProperty("processingStartDate")
  public OffsetDateTime getProcessingStartDate() {
    return processingStartDate;
  }

  public void setProcessingStartDate(OffsetDateTime processingStartDate) {
    this.processingStartDate = processingStartDate;
  }

  public ComplianceScanDetailsResponse processingEndDate(OffsetDateTime processingEndDate) {
    this.processingEndDate = processingEndDate;
    return this;
  }

  /**
   * Get processingEndDate
   * @return processingEndDate
  */
  @Valid   @Schema(name = "processingEndDate", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
  @JsonProperty("processingEndDate")
  public OffsetDateTime getProcessingEndDate() {
    return processingEndDate;
  }

  public void setProcessingEndDate(OffsetDateTime processingEndDate) {
    this.processingEndDate = processingEndDate;
  }

  public ComplianceScanDetailsResponse scanResults(List<@Valid ScanResult> scanResults) {
    this.scanResults = scanResults;
    return this;
  }

  public ComplianceScanDetailsResponse addScanResultsItem(ScanResult scanResultsItem) {
    if (this.scanResults == null) {
      this.scanResults = new ArrayList<>();
    }
    this.scanResults.add(scanResultsItem);
    return this;
  }

  /**
   * Get scanResults
   * @return scanResults
  */
  @Valid   @Schema(name = "scanResults", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
  @JsonProperty("scanResults")
  public List<@Valid ScanResult> getScanResults() {
    return scanResults;
  }

  public void setScanResults(List<@Valid ScanResult> scanResults) {
    this.scanResults = scanResults;
  }

  public ComplianceScanDetailsResponse errorMessage(String errorMessage) {
    this.errorMessage = errorMessage;
    return this;
  }

  /**
   * Get errorMessage
   * @return errorMessage
  */
    @Schema(name = "errorMessage", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
  @JsonProperty("errorMessage")
  public String getErrorMessage() {
    return errorMessage;
  }

  public void setErrorMessage(String errorMessage) {
    this.errorMessage = errorMessage;
  }

  public ComplianceScanDetailsResponse createDate(OffsetDateTime createDate) {
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

  public ComplianceScanDetailsResponse createdBy(String createdBy) {
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

  public ComplianceScanDetailsResponse updatedDate(OffsetDateTime updatedDate) {
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

  public ComplianceScanDetailsResponse updatedBy(String updatedBy) {
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
    ComplianceScanDetailsResponse complianceScanDetailsResponse = (ComplianceScanDetailsResponse) o;
    return Objects.equals(this.scanId, complianceScanDetailsResponse.scanId) &&
        Objects.equals(this.projectName, complianceScanDetailsResponse.projectName) &&
        Objects.equals(this.companyName, complianceScanDetailsResponse.companyName) &&
        Objects.equals(this.assetId, complianceScanDetailsResponse.assetId) &&
        Objects.equals(this.assetName, complianceScanDetailsResponse.assetName) &&
        Objects.equals(this.assetType, complianceScanDetailsResponse.assetType) &&
        Objects.equals(this.sourceType, complianceScanDetailsResponse.sourceType) &&
        Objects.equals(this.source, complianceScanDetailsResponse.source) &&
        Objects.equals(this.rules, complianceScanDetailsResponse.rules) &&
        Objects.equals(this.scanOptions, complianceScanDetailsResponse.scanOptions) &&
        Objects.equals(this.status, complianceScanDetailsResponse.status) &&
        Objects.equals(this.compliance, complianceScanDetailsResponse.compliance) &&
        Objects.equals(this.scanDate, complianceScanDetailsResponse.scanDate) &&
        Objects.equals(this.processingStartDate, complianceScanDetailsResponse.processingStartDate) &&
        Objects.equals(this.processingEndDate, complianceScanDetailsResponse.processingEndDate) &&
        Objects.equals(this.scanResults, complianceScanDetailsResponse.scanResults) &&
        Objects.equals(this.errorMessage, complianceScanDetailsResponse.errorMessage) &&
        Objects.equals(this.createDate, complianceScanDetailsResponse.createDate) &&
        Objects.equals(this.createdBy, complianceScanDetailsResponse.createdBy) &&
        Objects.equals(this.updatedDate, complianceScanDetailsResponse.updatedDate) &&
        Objects.equals(this.updatedBy, complianceScanDetailsResponse.updatedBy);
  }

  @Override
  public int hashCode() {
    return Objects.hash(scanId, projectName, companyName, assetId, assetName, assetType, sourceType, source, rules, scanOptions, status, compliance, scanDate, processingStartDate, processingEndDate, scanResults, errorMessage, createDate, createdBy, updatedDate, updatedBy);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class ComplianceScanDetailsResponse {\n");
    sb.append("    scanId: ").append(toIndentedString(scanId)).append("\n");
    sb.append("    projectName: ").append(toIndentedString(projectName)).append("\n");
    sb.append("    companyName: ").append(toIndentedString(companyName)).append("\n");
    sb.append("    assetId: ").append(toIndentedString(assetId)).append("\n");
    sb.append("    assetName: ").append(toIndentedString(assetName)).append("\n");
    sb.append("    assetType: ").append(toIndentedString(assetType)).append("\n");
    sb.append("    sourceType: ").append(toIndentedString(sourceType)).append("\n");
    sb.append("    source: ").append(toIndentedString(source)).append("\n");
    sb.append("    rules: ").append(toIndentedString(rules)).append("\n");
    sb.append("    scanOptions: ").append(toIndentedString(scanOptions)).append("\n");
    sb.append("    status: ").append(toIndentedString(status)).append("\n");
    sb.append("    compliance: ").append(toIndentedString(compliance)).append("\n");
    sb.append("    scanDate: ").append(toIndentedString(scanDate)).append("\n");
    sb.append("    processingStartDate: ").append(toIndentedString(processingStartDate)).append("\n");
    sb.append("    processingEndDate: ").append(toIndentedString(processingEndDate)).append("\n");
    sb.append("    scanResults: ").append(toIndentedString(scanResults)).append("\n");
    sb.append("    errorMessage: ").append(toIndentedString(errorMessage)).append("\n");
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

