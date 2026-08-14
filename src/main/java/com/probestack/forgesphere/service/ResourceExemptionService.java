package com.probestack.forgesphere.service;

import com.probestack.forgesphere.document.ResourceExemptionDocument;
import com.probestack.forgesphere.model.AssetType;
import com.probestack.forgesphere.model.ScanKind;
import com.probestack.forgesphere.repository.ResourceExemptionRepository;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

/**
 * Asset exemptions: recording that a failure has been seen and consciously set aside.
 *
 * <h2>Why this does not touch existing behaviour</h2>
 *
 * Exactly the same contract as {@link ComplianceThresholdService}. A scan's stored
 * {@code compliance} verdict, its {@code status}, and its per-rule results are never rewritten —
 * an exemption is reported as <em>additional</em> fields and consumers decide what to do with
 * them. There is no default row, and {@link #decorate} adds nothing at all when the asset has no
 * exemption, so a caller who never creates one gets byte-for-byte identical responses.
 *
 * <p>Keeping the scan data intact is not just about compatibility. A report that quietly turned
 * failures into passes would be worthless as evidence; the failure and the decision to excuse it
 * are two separate facts and both need to survive.
 */
@Service
public class ResourceExemptionService {

    private static final Logger log = LoggerFactory.getLogger(ResourceExemptionService.class);

    /** Response keys. Additive — nothing here overwrites an existing field. */
    public static final String KEY_EXEMPTED = "exempted";
    public static final String KEY_REASON = "exemptionReason";
    public static final String KEY_BY = "exemptedBy";
    public static final String KEY_AT = "exemptedAt";

    private final ResourceExemptionRepository repository;

    public ResourceExemptionService(ResourceExemptionRepository repository) {
        this.repository = repository;
    }

    /* ───────────────────────────── Configuration ───────────────────────────── */

    public List<ResourceExemptionDocument> findAll() {
        return repository.findAll();
    }

    public List<ResourceExemptionDocument> find(ScanKind scanKind, AssetType assetType) {
        if (assetType == null) {
            return repository.findAll();
        }
        return scanKind == null
                ? repository.findAllByAssetType(assetType)
                : repository.findAllByScanKindAndAssetType(scanKind, assetType);
    }

    public Optional<ResourceExemptionDocument> find(ScanKind scanKind, AssetType assetType, String assetName) {
        if (scanKind == null || assetType == null || assetName == null || assetName.isBlank()) {
            return Optional.empty();
        }
        return repository.findByScanKindAndAssetTypeAndAssetName(scanKind, assetType, assetName);
    }

    /**
     * Exempt an asset, or update the reason on an existing exemption.
     *
     * Idempotent: exempting something already exempt refreshes who and when rather than failing on
     * the unique index, so a client retrying after a dropped response is not punished for it.
     */
    public ResourceExemptionDocument exempt(ScanKind scanKind, AssetType assetType, String assetName,
            String reason, String actor) {
        if (scanKind == null || assetType == null || assetName == null || assetName.isBlank()) {
            throw new IllegalArgumentException("scanKind, assetType and assetName are required.");
        }
        Instant now = Instant.now();
        ResourceExemptionDocument doc = repository
                .findByScanKindAndAssetTypeAndAssetName(scanKind, assetType, assetName)
                .orElseGet(() -> {
                    ResourceExemptionDocument fresh = new ResourceExemptionDocument();
                    fresh.setScanKind(scanKind);
                    fresh.setAssetType(assetType);
                    fresh.setAssetName(assetName);
                    fresh.setCreateDate(now);
                    fresh.setCreatedBy(actor);
                    return fresh;
                });
        doc.setReason(reason == null || reason.isBlank() ? null : reason.trim());
        doc.setUpdatedDate(now);
        doc.setUpdatedBy(actor);
        ResourceExemptionDocument saved = repository.save(doc);
        log.info("exemption set kind={} asset={} name={} by={}", scanKind, assetType, assetName, actor);
        return saved;
    }

    /** Lifting an exemption restores this service's original semantics for that asset. */
    public boolean lift(ScanKind scanKind, AssetType assetType, String assetName) {
        Optional<ResourceExemptionDocument> existing = find(scanKind, assetType, assetName);
        existing.ifPresent(doc -> {
            repository.delete(doc);
            log.info("exemption lifted kind={} asset={} name={}", scanKind, assetType, assetName);
        });
        return existing.isPresent();
    }

    /* ─────────────────────────────── Evaluation ────────────────────────────── */

    /**
     * Add the exemption fields to a scan summary, if and only if one exists.
     *
     * When the asset is not exempt the map is returned untouched — which is what keeps existing
     * consumers unaffected. Note what is <em>not</em> written: {@code compliance}, {@code status}
     * and {@code scanResults} are left alone, so a client that ignores {@code exempted} sees the
     * unvarnished scan.
     *
     * @param summary a mutable summary map; returned for chaining
     */
    public Map<String, Object> decorate(Map<String, Object> summary, ScanKind scanKind,
            AssetType assetType, String assetName) {
        if (summary == null) {
            return new LinkedHashMap<>();
        }
        Optional<ResourceExemptionDocument> found = find(scanKind, assetType, assetName);
        if (found.isEmpty()) {
            return summary;
        }
        ResourceExemptionDocument doc = found.get();
        summary.put(KEY_EXEMPTED, Boolean.TRUE);
        summary.put(KEY_REASON, doc.getReason());
        summary.put(KEY_BY, doc.getUpdatedBy() != null ? doc.getUpdatedBy() : doc.getCreatedBy());
        Instant at = doc.getUpdatedDate() != null ? doc.getUpdatedDate() : doc.getCreateDate();
        summary.put(KEY_AT, at == null ? null : OffsetDateTime.ofInstant(at, ZoneOffset.UTC));
        return summary;
    }
}
