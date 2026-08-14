package com.probestack.forgesphere.service;

import com.probestack.forgesphere.document.ComplianceThresholdDocument;
import com.probestack.forgesphere.model.AssetType;
import com.probestack.forgesphere.model.ComplianceStatus;
import com.probestack.forgesphere.model.ScanKind;
import com.probestack.forgesphere.model.ScanResult;
import com.probestack.forgesphere.model.ScanResultStatus;
import com.probestack.forgesphere.repository.ComplianceThresholdRepository;
import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

/**
 * Pass-rate cut-offs, and the verdict they imply.
 *
 * <h2>Why this does not touch existing behaviour</h2>
 *
 * A scan's stored {@code compliance} verdict is produced by
 * {@link ComplianceScanProcessor#calculateCompliance} — any failed rule makes it NON_COMPLIANT —
 * and that is left exactly as it is. Changing it would silently re-classify history for every
 * consumer of this service, including those that have no interest in cut-offs.
 *
 * Instead a cut-off yields a <em>second</em>, separate verdict, exposed alongside the original.
 * Consumers with no threshold row configured get responses byte-for-byte unchanged: there is no
 * default row, and {@link #decorate} adds nothing at all when no enabled cut-off applies.
 *
 * The verdict is computed at read time rather than stored, so raising or lowering a cut-off
 * re-reads existing history instead of requiring a migration or a re-scan.
 */
@Service
public class ComplianceThresholdService {

    private static final Logger log = LoggerFactory.getLogger(ComplianceThresholdService.class);

    /** Response keys. Additive — nothing here overwrites an existing field. */
    public static final String KEY_PASS_RATE = "passRate";
    public static final String KEY_THRESHOLD = "threshold";
    public static final String KEY_VERDICT = "complianceAtThreshold";

    private final ComplianceThresholdRepository repository;

    public ComplianceThresholdService(ComplianceThresholdRepository repository) {
        this.repository = repository;
    }

    /* ───────────────────────────── Configuration ───────────────────────────── */

    public List<ComplianceThresholdDocument> findAll() {
        return repository.findAll();
    }

    public List<ComplianceThresholdDocument> findByAssetType(AssetType assetType) {
        return assetType == null ? repository.findAll() : repository.findAllByAssetType(assetType);
    }

    public Optional<ComplianceThresholdDocument> find(ScanKind scanKind, AssetType assetType) {
        if (scanKind == null || assetType == null) {
            return Optional.empty();
        }
        return repository.findByScanKindAndAssetType(scanKind, assetType);
    }

    /**
     * Create or update one cut-off.
     *
     * @param threshold percentage of rules that must pass; rejected outside 0-100
     */
    public ComplianceThresholdDocument upsert(ScanKind scanKind, AssetType assetType, int threshold,
            boolean enabled, String actor) {
        if (scanKind == null || assetType == null) {
            throw new IllegalArgumentException("scanKind and assetType are required.");
        }
        if (threshold < 0 || threshold > 100) {
            throw new IllegalArgumentException("threshold must be between 0 and 100, got " + threshold + ".");
        }
        Instant now = Instant.now();
        ComplianceThresholdDocument doc = repository.findByScanKindAndAssetType(scanKind, assetType)
                .orElseGet(() -> {
                    ComplianceThresholdDocument fresh = new ComplianceThresholdDocument();
                    fresh.setScanKind(scanKind);
                    fresh.setAssetType(assetType);
                    fresh.setCreateDate(now);
                    fresh.setCreatedBy(actor);
                    return fresh;
                });
        doc.setThreshold(threshold);
        doc.setEnabled(enabled);
        doc.setUpdatedDate(now);
        doc.setUpdatedBy(actor);
        ComplianceThresholdDocument saved = repository.save(doc);
        log.info("cut-off upserted kind={} asset={} threshold={} enabled={} by={}",
                scanKind, assetType, threshold, enabled, actor);
        return saved;
    }

    /** Removing a cut-off restores this service's original verdict semantics for that pair. */
    public boolean delete(ScanKind scanKind, AssetType assetType) {
        Optional<ComplianceThresholdDocument> existing = find(scanKind, assetType);
        existing.ifPresent(doc -> {
            repository.delete(doc);
            log.info("cut-off removed kind={} asset={}", scanKind, assetType);
        });
        return existing.isPresent();
    }

    /* ─────────────────────────────── Evaluation ────────────────────────────── */

    /** The cut-off in force, or empty when none is configured or it is switched off. */
    public Optional<Integer> effectiveThreshold(ScanKind scanKind, AssetType assetType) {
        return find(scanKind, assetType)
                .filter(ComplianceThresholdDocument::isEnabled)
                .map(ComplianceThresholdDocument::getThreshold);
    }

    /**
     * Share of rules that passed, 0-100, or empty when the run recorded no results to judge.
     * Skipped results still count towards the denominator: a rule that could not be evaluated
     * is not evidence of compliance.
     */
    public Optional<Integer> passRate(List<ScanResult> results) {
        if (results == null || results.isEmpty()) {
            return Optional.empty();
        }
        long passed = results.stream()
                .filter(r -> r != null && ScanResultStatus.PASSED.equals(r.getResult()))
                .count();
        return passRate((int) passed, results.size());
    }

    /**
     * The same rate, from counts rather than from the results themselves.
     *
     * History reads compute these counts inside Mongo so the per-rule array never reaches the JVM;
     * without this overload they would have to load it back just to divide two numbers.
     */
    public Optional<Integer> passRate(int passed, int total) {
        if (total <= 0) {
            return Optional.empty();
        }
        return Optional.of((int) Math.round((passed * 100.0d) / total));
    }

    /**
     * Verdict for a pass rate against a cut-off.
     *
     * Binary by design. A cut-off is a single bar, so an asset is either over it or under it;
     * inventing a middle band would mean a second, unconfigured threshold. PARTIAL therefore
     * never comes from this path — it remains reachable only from the original verdict.
     */
    public ComplianceStatus verdictFor(int passRate, int threshold) {
        return passRate >= threshold ? ComplianceStatus.COMPLIANT : ComplianceStatus.NON_COMPLIANT;
    }

    /**
     * Add the cut-off fields to a scan summary, if and only if one applies.
     *
     * When no enabled cut-off is configured for the pair, or the run has no results, the map is
     * returned untouched — which is what keeps existing consumers unaffected.
     *
     * @param summary a mutable summary map; returned for chaining
     */
    public Map<String, Object> decorate(Map<String, Object> summary, ScanKind scanKind,
            AssetType assetType, List<ScanResult> results) {
        return decorate(summary, scanKind, assetType, passRate(results));
    }

    /** As above, from counts — see {@link #passRate(int, int)} for why both shapes exist. */
    public Map<String, Object> decorate(Map<String, Object> summary, ScanKind scanKind,
            AssetType assetType, int passed, int total) {
        return decorate(summary, scanKind, assetType, passRate(passed, total));
    }

    private Map<String, Object> decorate(Map<String, Object> summary, ScanKind scanKind,
            AssetType assetType, Optional<Integer> rate) {
        if (summary == null) {
            return new LinkedHashMap<>();
        }
        Optional<Integer> threshold = effectiveThreshold(scanKind, assetType);
        if (threshold.isEmpty()) {
            return summary;
        }
        if (rate.isEmpty()) {
            return summary;
        }
        summary.put(KEY_PASS_RATE, rate.get());
        summary.put(KEY_THRESHOLD, threshold.get());
        summary.put(KEY_VERDICT, verdictFor(rate.get(), threshold.get()).getValue());
        return summary;
    }
}
