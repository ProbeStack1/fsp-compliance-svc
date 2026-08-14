package com.probestack.forgesphere.document;

import java.time.Instant;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.mapping.Document;

import com.probestack.forgesphere.model.AssetType;
import com.probestack.forgesphere.model.ScanKind;

/**
 * A pass-rate cut-off: the share of rules an asset must pass before it counts as compliant.
 *
 * This is opt-in configuration and deliberately has no default row. A scan's stored
 * {@code compliance} verdict is never affected by this document — the cut-off produces an
 * <em>additional</em> verdict, surfaced only for the (scanKind, assetType) pairs that have an
 * enabled threshold configured. Consumers that never configure one see responses unchanged.
 *
 * @see com.probestack.forgesphere.service.ComplianceThresholdService
 */
@Document(collection = "governance_compliance_thresholds")
@CompoundIndex(name = "kind_asset_unique_idx", def = "{'scanKind': 1, 'assetType': 1}", unique = true)
public class ComplianceThresholdDocument {

    @Id
    private String id;

    /** Which family of rules the cut-off governs. */
    private ScanKind scanKind;

    /** Which asset family it applies to. Compliance rules are already scoped this way. */
    private AssetType assetType;

    /** Percentage of rules that must pass, 0-100 inclusive. */
    private int threshold;

    /**
     * Off by default on creation so a row can be staged without changing any response. When
     * false the cut-off is ignored entirely, exactly as if the row did not exist.
     */
    private boolean enabled;

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

    public ScanKind getScanKind() {
        return scanKind;
    }

    public void setScanKind(ScanKind scanKind) {
        this.scanKind = scanKind;
    }

    public AssetType getAssetType() {
        return assetType;
    }

    public void setAssetType(AssetType assetType) {
        this.assetType = assetType;
    }

    public int getThreshold() {
        return threshold;
    }

    public void setThreshold(int threshold) {
        this.threshold = threshold;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
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
