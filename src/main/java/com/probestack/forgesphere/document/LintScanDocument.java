package com.probestack.forgesphere.document;

import java.time.Instant;
import java.util.List;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import com.probestack.forgesphere.model.AssetType;
import com.probestack.forgesphere.model.ComplianceStatus;
import com.probestack.forgesphere.model.ScanResult;
import com.probestack.forgesphere.model.ScanStatus;

/**
 * A recorded linting run.
 *
 * Unlike compliance and OWASP, linting is executed by a separate lint service that answers
 * synchronously and stores nothing. The results therefore only ever existed in the browser, and
 * a refresh lost every one of them. This collection is where a client can record a completed run
 * so it survives, using the same shape as the other two scan collections — same fields, same
 * history summary, so the screens treat all three alike.
 *
 * Entirely additive: a new collection behind new routes. Nothing that existed before reads or
 * writes it, so services and teams that do not lint are unaffected.
 */
@Document(collection = "governance_lint_scans")
@CompoundIndex(name = "status_created_idx", def = "{'status': 1, 'createDate': -1}")
@CompoundIndex(name = "project_asset_resource_idx", def = "{'projectName': 1, 'assetType': 1, 'assetName': 1}")
public class LintScanDocument {

    @Id
    private String id;

    @Indexed(unique = true)
    private String scanId;

    @Indexed
    private String projectName;

    private String companyName;
    private String assetId;
    private String assetName;
    private AssetType assetType;
    private ScanStatus status;
    private ComplianceStatus compliance;
    private List<ScanResult> scanResults;
    private Instant scanDate;
    private String errorMessage;
    private Instant createDate;
    private String createdBy;
    private Instant updatedDate;
    private String updatedBy;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getScanId() {
        return scanId;
    }

    public void setScanId(String scanId) {
        this.scanId = scanId;
    }

    public String getProjectName() {
        return projectName;
    }

    public void setProjectName(String projectName) {
        this.projectName = projectName;
    }

    public String getCompanyName() {
        return companyName;
    }

    public void setCompanyName(String companyName) {
        this.companyName = companyName;
    }

    public String getAssetId() {
        return assetId;
    }

    public void setAssetId(String assetId) {
        this.assetId = assetId;
    }

    public String getAssetName() {
        return assetName;
    }

    public void setAssetName(String assetName) {
        this.assetName = assetName;
    }

    public AssetType getAssetType() {
        return assetType;
    }

    public void setAssetType(AssetType assetType) {
        this.assetType = assetType;
    }

    public ScanStatus getStatus() {
        return status;
    }

    public void setStatus(ScanStatus status) {
        this.status = status;
    }

    public ComplianceStatus getCompliance() {
        return compliance;
    }

    public void setCompliance(ComplianceStatus compliance) {
        this.compliance = compliance;
    }

    public List<ScanResult> getScanResults() {
        return scanResults;
    }

    public void setScanResults(List<ScanResult> scanResults) {
        this.scanResults = scanResults;
    }

    public Instant getScanDate() {
        return scanDate;
    }

    public void setScanDate(Instant scanDate) {
        this.scanDate = scanDate;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }

    public Instant getCreateDate() {
        return createDate;
    }

    public void setCreateDate(Instant createDate) {
        this.createDate = createDate;
    }

    public String getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(String createdBy) {
        this.createdBy = createdBy;
    }

    public Instant getUpdatedDate() {
        return updatedDate;
    }

    public void setUpdatedDate(Instant updatedDate) {
        this.updatedDate = updatedDate;
    }

    public String getUpdatedBy() {
        return updatedBy;
    }

    public void setUpdatedBy(String updatedBy) {
        this.updatedBy = updatedBy;
    }
}
