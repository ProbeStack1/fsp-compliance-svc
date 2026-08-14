package com.probestack.forgesphere.document;

import java.time.Instant;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.mapping.Document;

import com.probestack.forgesphere.model.AssetType;
import com.probestack.forgesphere.model.ScanKind;

/**
 * A decision not to hold one asset's failures against it, for one family of rules.
 *
 * <h2>What an exemption is not</h2>
 *
 * It is not a pass, and it does not edit a scan. The stored {@code compliance} verdict and the
 * per-rule results stay exactly as the scan produced them, so history remains a truthful record of
 * what was actually found. An exemption is a separate, later judgement laid over the top of that
 * record, and it can be lifted without re-scanning.
 *
 * <h2>Why this changes nothing for existing consumers</h2>
 *
 * There is no default row. A caller who never creates one sees responses byte-for-byte unchanged,
 * because {@link com.probestack.forgesphere.service.ResourceExemptionService#decorate} adds
 * nothing at all when no exemption exists. Consumers that do not know about exemptions therefore
 * keep reading the same fields with the same meanings they always had.
 *
 * <p>Scoped per {@code scanKind} rather than per asset: forgiving lint noise on a proxy should not
 * quietly forgive its OWASP findings too.
 *
 * @see com.probestack.forgesphere.service.ResourceExemptionService
 */
@Document(collection = "governance_resource_exemptions")
@CompoundIndex(name = "kind_asset_name_unique_idx",
        def = "{'scanKind': 1, 'assetType': 1, 'assetName': 1}", unique = true)
public class ResourceExemptionDocument {

    @Id
    private String id;

    /** Which family of rules this exemption covers. */
    private ScanKind scanKind;

    /** Which asset family the named asset belongs to. */
    private AssetType assetType;

    /**
     * The asset being exempted, by name.
     *
     * Name rather than id because that is the only identifier every scan record carries across all
     * three kinds — lint runs in particular are recorded against a name.
     */
    private String assetName;

    /** Why it was exempted. Optional, but the whole point of an audit trail is that it is filled in. */
    private String reason;

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

    public String getAssetName() {
        return assetName;
    }

    public void setAssetName(String assetName) {
        this.assetName = assetName;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
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
