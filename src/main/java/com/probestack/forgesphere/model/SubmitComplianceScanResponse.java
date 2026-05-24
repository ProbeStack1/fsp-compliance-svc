package com.probestack.forgesphere.model;

import java.net.URI;
import java.util.Objects;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import com.probestack.forgesphere.model.ScanStatus;
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
 * SubmitComplianceScanResponse
 */
@Generated(value = "org.openapitools.codegen.languages.SpringCodegen", date = "2026-05-13T05:38:50.494838724Z[GMT]")public class SubmitComplianceScanResponse {

  private String scanId;

  private ScanStatus status;

  private String message;

  @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
  private OffsetDateTime submittedDate;

  public SubmitComplianceScanResponse scanId(String scanId) {
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

  public SubmitComplianceScanResponse status(ScanStatus status) {
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

  public SubmitComplianceScanResponse message(String message) {
    this.message = message;
    return this;
  }

  /**
   * Get message
   * @return message
  */
    @Schema(name = "message", example = "Compliance scan request submitted successfully.", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
  @JsonProperty("message")
  public String getMessage() {
    return message;
  }

  public void setMessage(String message) {
    this.message = message;
  }

  public SubmitComplianceScanResponse submittedDate(OffsetDateTime submittedDate) {
    this.submittedDate = submittedDate;
    return this;
  }

  /**
   * Get submittedDate
   * @return submittedDate
  */
  @Valid   @Schema(name = "submittedDate", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
  @JsonProperty("submittedDate")
  public OffsetDateTime getSubmittedDate() {
    return submittedDate;
  }

  public void setSubmittedDate(OffsetDateTime submittedDate) {
    this.submittedDate = submittedDate;
  }
  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    SubmitComplianceScanResponse submitComplianceScanResponse = (SubmitComplianceScanResponse) o;
    return Objects.equals(this.scanId, submitComplianceScanResponse.scanId) &&
        Objects.equals(this.status, submitComplianceScanResponse.status) &&
        Objects.equals(this.message, submitComplianceScanResponse.message) &&
        Objects.equals(this.submittedDate, submitComplianceScanResponse.submittedDate);
  }

  @Override
  public int hashCode() {
    return Objects.hash(scanId, status, message, submittedDate);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class SubmitComplianceScanResponse {\n");
    sb.append("    scanId: ").append(toIndentedString(scanId)).append("\n");
    sb.append("    status: ").append(toIndentedString(status)).append("\n");
    sb.append("    message: ").append(toIndentedString(message)).append("\n");
    sb.append("    submittedDate: ").append(toIndentedString(submittedDate)).append("\n");
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

