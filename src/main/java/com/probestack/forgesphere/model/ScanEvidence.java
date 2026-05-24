package com.probestack.forgesphere.model;

import java.net.URI;
import java.util.Objects;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonCreator;
import org.openapitools.jackson.nullable.JsonNullable;
import java.time.OffsetDateTime;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import io.swagger.v3.oas.annotations.media.Schema;


import java.util.*;
import jakarta.annotation.Generated;

/**
 * ScanEvidence
 */
@Generated(value = "org.openapitools.codegen.languages.SpringCodegen", date = "2026-05-13T05:38:50.494838724Z[GMT]")public class ScanEvidence {

  private String filePath;

  private Integer lineNumber;

  private String details;

  public ScanEvidence filePath(String filePath) {
    this.filePath = filePath;
    return this;
  }

  /**
   * Get filePath
   * @return filePath
  */
    @Schema(name = "filePath", example = "src/main/java/com/probestack/controller/PaymentController.java", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
  @JsonProperty("filePath")
  public String getFilePath() {
    return filePath;
  }

  public void setFilePath(String filePath) {
    this.filePath = filePath;
  }

  public ScanEvidence lineNumber(Integer lineNumber) {
    this.lineNumber = lineNumber;
    return this;
  }

  /**
   * Get lineNumber
   * @return lineNumber
  */
    @Schema(name = "lineNumber", example = "42", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
  @JsonProperty("lineNumber")
  public Integer getLineNumber() {
    return lineNumber;
  }

  public void setLineNumber(Integer lineNumber) {
    this.lineNumber = lineNumber;
  }

  public ScanEvidence details(String details) {
    this.details = details;
    return this;
  }

  /**
   * Get details
   * @return details
  */
    @Schema(name = "details", example = "POST /payments endpoint does not have OpenAPI documentation.", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
  @JsonProperty("details")
  public String getDetails() {
    return details;
  }

  public void setDetails(String details) {
    this.details = details;
  }
  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    ScanEvidence scanEvidence = (ScanEvidence) o;
    return Objects.equals(this.filePath, scanEvidence.filePath) &&
        Objects.equals(this.lineNumber, scanEvidence.lineNumber) &&
        Objects.equals(this.details, scanEvidence.details);
  }

  @Override
  public int hashCode() {
    return Objects.hash(filePath, lineNumber, details);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class ScanEvidence {\n");
    sb.append("    filePath: ").append(toIndentedString(filePath)).append("\n");
    sb.append("    lineNumber: ").append(toIndentedString(lineNumber)).append("\n");
    sb.append("    details: ").append(toIndentedString(details)).append("\n");
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

