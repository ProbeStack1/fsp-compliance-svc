package com.probestack.forgesphere.model;

import java.net.URI;
import java.util.Objects;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import com.probestack.forgesphere.model.ScanMode;
import org.openapitools.jackson.nullable.JsonNullable;
import java.time.OffsetDateTime;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import io.swagger.v3.oas.annotations.media.Schema;


import java.util.*;
import jakarta.annotation.Generated;

/**
 * ScanOptions
 */
@Generated(value = "org.openapitools.codegen.languages.SpringCodegen", date = "2026-05-13T05:38:50.494838724Z[GMT]")public class ScanOptions {

  private ScanMode scanMode;

  private Boolean includeInactiveRules;

  private Boolean saveResult;

  public ScanOptions() {
    super();
  }

  /**
   * Constructor with only required parameters
   */
  public ScanOptions(ScanMode scanMode, Boolean includeInactiveRules, Boolean saveResult) {
    this.scanMode = scanMode;
    this.includeInactiveRules = includeInactiveRules;
    this.saveResult = saveResult;
  }

  public ScanOptions scanMode(ScanMode scanMode) {
    this.scanMode = scanMode;
    return this;
  }

  /**
   * Get scanMode
   * @return scanMode
  */
  @NotNull @Valid   @Schema(name = "scanMode", requiredMode = Schema.RequiredMode.REQUIRED)
  @JsonProperty("scanMode")
  public ScanMode getScanMode() {
    return scanMode;
  }

  public void setScanMode(ScanMode scanMode) {
    this.scanMode = scanMode;
  }

  public ScanOptions includeInactiveRules(Boolean includeInactiveRules) {
    this.includeInactiveRules = includeInactiveRules;
    return this;
  }

  /**
   * Get includeInactiveRules
   * @return includeInactiveRules
  */
  @NotNull   @Schema(name = "includeInactiveRules", example = "false", requiredMode = Schema.RequiredMode.REQUIRED)
  @JsonProperty("includeInactiveRules")
  public Boolean getIncludeInactiveRules() {
    return includeInactiveRules;
  }

  public void setIncludeInactiveRules(Boolean includeInactiveRules) {
    this.includeInactiveRules = includeInactiveRules;
  }

  public ScanOptions saveResult(Boolean saveResult) {
    this.saveResult = saveResult;
    return this;
  }

  /**
   * Get saveResult
   * @return saveResult
  */
  @NotNull   @Schema(name = "saveResult", example = "true", requiredMode = Schema.RequiredMode.REQUIRED)
  @JsonProperty("saveResult")
  public Boolean getSaveResult() {
    return saveResult;
  }

  public void setSaveResult(Boolean saveResult) {
    this.saveResult = saveResult;
  }
  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    ScanOptions scanOptions = (ScanOptions) o;
    return Objects.equals(this.scanMode, scanOptions.scanMode) &&
        Objects.equals(this.includeInactiveRules, scanOptions.includeInactiveRules) &&
        Objects.equals(this.saveResult, scanOptions.saveResult);
  }

  @Override
  public int hashCode() {
    return Objects.hash(scanMode, includeInactiveRules, saveResult);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class ScanOptions {\n");
    sb.append("    scanMode: ").append(toIndentedString(scanMode)).append("\n");
    sb.append("    includeInactiveRules: ").append(toIndentedString(includeInactiveRules)).append("\n");
    sb.append("    saveResult: ").append(toIndentedString(saveResult)).append("\n");
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

