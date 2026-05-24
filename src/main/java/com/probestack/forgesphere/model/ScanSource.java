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
 * Repository or uploaded bundle details.
 */
@Schema(name = "ScanSource", description = "Repository or uploaded bundle details.")
@Generated(value = "org.openapitools.codegen.languages.SpringCodegen", date = "2026-05-13T05:38:50.494838724Z[GMT]")public class ScanSource {

  private String repositoryUrl;

  private String branch;

  private String accessTokenRef;

  private String bundleFileId;

  private String bundleName;

  private String archiveDownloadUrl;

  public ScanSource repositoryUrl(String repositoryUrl) {
    this.repositoryUrl = repositoryUrl;
    return this;
  }

  /**
   * Get repositoryUrl
   * @return repositoryUrl
  */
    @Schema(name = "repositoryUrl", example = "https://github.com/probestack/payment-service", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
  @JsonProperty("repositoryUrl")
  public String getRepositoryUrl() {
    return repositoryUrl;
  }

  public void setRepositoryUrl(String repositoryUrl) {
    this.repositoryUrl = repositoryUrl;
  }

  public ScanSource branch(String branch) {
    this.branch = branch;
    return this;
  }

  /**
   * Get branch
   * @return branch
  */
    @Schema(name = "branch", example = "main", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
  @JsonProperty("branch")
  public String getBranch() {
    return branch;
  }

  public void setBranch(String branch) {
    this.branch = branch;
  }

  public ScanSource accessTokenRef(String accessTokenRef) {
    this.accessTokenRef = accessTokenRef;
    return this;
  }

  /**
   * Get accessTokenRef
   * @return accessTokenRef
  */
    @Schema(name = "accessTokenRef", example = "github-token-secret-ref", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
  @JsonProperty("accessTokenRef")
  public String getAccessTokenRef() {
    return accessTokenRef;
  }

  public void setAccessTokenRef(String accessTokenRef) {
    this.accessTokenRef = accessTokenRef;
  }

  public ScanSource bundleFileId(String bundleFileId) {
    this.bundleFileId = bundleFileId;
    return this;
  }

  /**
   * Get bundleFileId
   * @return bundleFileId
  */
    @Schema(name = "bundleFileId", example = "file_12345", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
  @JsonProperty("bundleFileId")
  public String getBundleFileId() {
    return bundleFileId;
  }

  public void setBundleFileId(String bundleFileId) {
    this.bundleFileId = bundleFileId;
  }

  public ScanSource bundleName(String bundleName) {
    this.bundleName = bundleName;
    return this;
  }

  /**
   * Get bundleName
   * @return bundleName
  */
    @Schema(name = "bundleName", example = "Remit2Any.zip", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
  @JsonProperty("bundleName")
  public String getBundleName() {
    return bundleName;
  }

  public void setBundleName(String bundleName) {
    this.bundleName = bundleName;
  }

  public ScanSource archiveDownloadUrl(String archiveDownloadUrl) {
    this.archiveDownloadUrl = archiveDownloadUrl;
    return this;
  }

  /**
   * Download URL for the generated code/proxy archive.
   * @return archiveDownloadUrl
  */
    @Schema(name = "archiveDownloadUrl", example = "https://storage.probestack.io/codegen-result/archive.zip", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
  @JsonProperty("archiveDownloadUrl")
  public String getArchiveDownloadUrl() {
    return archiveDownloadUrl;
  }

  public void setArchiveDownloadUrl(String archiveDownloadUrl) {
    this.archiveDownloadUrl = archiveDownloadUrl;
  }
  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    ScanSource scanSource = (ScanSource) o;
    return Objects.equals(this.repositoryUrl, scanSource.repositoryUrl) &&
        Objects.equals(this.branch, scanSource.branch) &&
        Objects.equals(this.accessTokenRef, scanSource.accessTokenRef) &&
        Objects.equals(this.bundleFileId, scanSource.bundleFileId) &&
        Objects.equals(this.bundleName, scanSource.bundleName) &&
        Objects.equals(this.archiveDownloadUrl, scanSource.archiveDownloadUrl);
  }

  @Override
  public int hashCode() {
    return Objects.hash(repositoryUrl, branch, accessTokenRef, bundleFileId, bundleName, archiveDownloadUrl);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class ScanSource {\n");
    sb.append("    repositoryUrl: ").append(toIndentedString(repositoryUrl)).append("\n");
    sb.append("    branch: ").append(toIndentedString(branch)).append("\n");
    sb.append("    accessTokenRef: ").append(toIndentedString(accessTokenRef)).append("\n");
    sb.append("    bundleFileId: ").append(toIndentedString(bundleFileId)).append("\n");
    sb.append("    bundleName: ").append(toIndentedString(bundleName)).append("\n");
    sb.append("    archiveDownloadUrl: ").append(toIndentedString(archiveDownloadUrl)).append("\n");
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
