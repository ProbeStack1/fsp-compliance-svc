package com.probestack.forgesphere.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.probestack.forgesphere.document.ComplianceThresholdDocument;
import com.probestack.forgesphere.model.AssetType;
import com.probestack.forgesphere.model.ComplianceStatus;
import com.probestack.forgesphere.model.ScanKind;
import com.probestack.forgesphere.model.ScanResult;
import com.probestack.forgesphere.model.ScanResultStatus;
import com.probestack.forgesphere.repository.ComplianceThresholdRepository;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.Test;

/**
 * The most important assertions here are the negative ones.
 *
 * A cut-off must stay invisible until someone configures one, because other teams consume these
 * same endpoints and have no interest in it. The `leavesSummaryUntouched…` and
 * `doesNotLeakAcross…` tests are the ones that fail if that guarantee is ever broken.
 */
class ComplianceThresholdServiceTest {

    /**
     * A repository mock with map-backed storage, stubbing only what the service calls.
     *
     * Deliberately a mock rather than a hand-written implementation of MongoRepository: that
     * interface carries roughly twenty-five inherited methods, and stubbing them all by hand
     * would break the build on the next Spring Data bump for no benefit.
     */
    private static ComplianceThresholdRepository fakeRepository() {
        ComplianceThresholdRepository repo = mock(ComplianceThresholdRepository.class);
        Map<String, ComplianceThresholdDocument> store = new LinkedHashMap<>();

        when(repo.save(any(ComplianceThresholdDocument.class))).thenAnswer(inv -> {
            ComplianceThresholdDocument d = inv.getArgument(0);
            if (d.getId() == null) {
                d.setId(UUID.randomUUID().toString());
            }
            store.put(d.getId(), d);
            return d;
        });
        when(repo.findAll()).thenAnswer(inv -> new ArrayList<>(store.values()));
        when(repo.findByScanKindAndAssetType(any(), any())).thenAnswer(inv -> {
            ScanKind kind = inv.getArgument(0);
            AssetType asset = inv.getArgument(1);
            return store.values().stream()
                    .filter(d -> kind == d.getScanKind() && asset == d.getAssetType())
                    .findFirst();
        });
        when(repo.findAllByAssetType(any())).thenAnswer(inv -> {
            AssetType asset = inv.getArgument(0);
            return store.values().stream().filter(d -> asset == d.getAssetType()).toList();
        });
        doAnswer(inv -> {
            ComplianceThresholdDocument d = inv.getArgument(0);
            store.remove(d.getId());
            return null;
        }).when(repo).delete(any(ComplianceThresholdDocument.class));

        return repo;
    }

    private static ScanResult result(ScanResultStatus status) {
        ScanResult r = new ScanResult();
        r.setResult(status);
        return r;
    }

    /** A run with {@code passed} passing rules and {@code failed} failing ones. */
    private static List<ScanResult> results(int passed, int failed) {
        List<ScanResult> list = new ArrayList<>();
        for (int i = 0; i < passed; i++) {
            list.add(result(ScanResultStatus.PASSED));
        }
        for (int i = 0; i < failed; i++) {
            list.add(result(ScanResultStatus.FAILED));
        }
        return list;
    }

    /** A summary shaped like the one GovernanceExtensionsController builds. */
    private static Map<String, Object> summary() {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("scanId", "SCAN-1");
        m.put("compliance", ComplianceStatus.NON_COMPLIANT);
        m.put("totalResults", 33);
        m.put("passed", 14L);
        return m;
    }

    /* ────────── The guarantee: no cut-off configured means no change ────────── */

    @Test
    void leavesSummaryUntouchedWhenNoThresholdConfigured() {
        ComplianceThresholdService service = new ComplianceThresholdService(fakeRepository());

        Map<String, Object> after = service.decorate(summary(), ScanKind.COMPLIANCE, AssetType.APIGEE, results(14, 19));

        assertEquals(summary().keySet(), after.keySet(), "no keys may be added when no cut-off exists");
        assertEquals(summary(), after);
    }

    @Test
    void leavesSummaryUntouchedWhenThresholdIsDisabled() {
        ComplianceThresholdService service = new ComplianceThresholdService(fakeRepository());
        service.upsert(ScanKind.COMPLIANCE, AssetType.APIGEE, 80, false, "tester");

        Map<String, Object> after = service.decorate(summary(), ScanKind.COMPLIANCE, AssetType.APIGEE, results(14, 19));

        assertEquals(summary().keySet(), after.keySet(), "a staged cut-off must stay inert");
    }

    @Test
    void doesNotLeakAcrossAssetTypesOrScanKinds() {
        ComplianceThresholdService service = new ComplianceThresholdService(fakeRepository());
        service.upsert(ScanKind.COMPLIANCE, AssetType.APIGEE, 40, true, "tester");

        // Another team's asset type: untouched.
        assertEquals(summary().keySet(),
                service.decorate(summary(), ScanKind.COMPLIANCE, AssetType.MICROSERVICE, results(14, 19)).keySet());
        // Another rule family on the configured asset type: untouched.
        assertEquals(summary().keySet(),
                service.decorate(summary(), ScanKind.OWASP, AssetType.APIGEE, results(14, 19)).keySet());
    }

    @Test
    void neverRewritesTheStoredComplianceVerdict() {
        ComplianceThresholdService service = new ComplianceThresholdService(fakeRepository());
        service.upsert(ScanKind.COMPLIANCE, AssetType.APIGEE, 40, true, "tester");

        Map<String, Object> after = service.decorate(summary(), ScanKind.COMPLIANCE, AssetType.APIGEE, results(14, 19));

        // 42% clears a 40% bar, so the added verdict disagrees with the stored one — and the
        // stored one must still stand, because that is what other consumers read.
        assertEquals(ComplianceStatus.NON_COMPLIANT, after.get("compliance"));
        assertEquals("COMPLIANT", after.get(ComplianceThresholdService.KEY_VERDICT));
    }

    /* ──────────────────────────── Evaluation behaviour ─────────────────────── */

    @Test
    void addsPassRateThresholdAndVerdictWhenConfigured() {
        ComplianceThresholdService service = new ComplianceThresholdService(fakeRepository());
        service.upsert(ScanKind.COMPLIANCE, AssetType.APIGEE, 80, true, "tester");

        Map<String, Object> after = service.decorate(summary(), ScanKind.COMPLIANCE, AssetType.APIGEE, results(14, 19));

        assertEquals(42, after.get(ComplianceThresholdService.KEY_PASS_RATE));
        assertEquals(80, after.get(ComplianceThresholdService.KEY_THRESHOLD));
        assertEquals("NON_COMPLIANT", after.get(ComplianceThresholdService.KEY_VERDICT));
    }

    @Test
    void treatsTheThresholdAsInclusive() {
        ComplianceThresholdService service = new ComplianceThresholdService(fakeRepository());

        assertEquals(ComplianceStatus.COMPLIANT, service.verdictFor(80, 80));
        assertEquals(ComplianceStatus.NON_COMPLIANT, service.verdictFor(79, 80));
    }

    @Test
    void countsSkippedRulesAgainstThePassRate() {
        ComplianceThresholdService service = new ComplianceThresholdService(fakeRepository());
        List<ScanResult> mixed = new ArrayList<>(results(1, 0));
        mixed.add(result(ScanResultStatus.SKIPPED));

        // A rule that could not be evaluated is not evidence of compliance: 1 of 2, not 1 of 1.
        assertEquals(Optional.of(50), service.passRate(mixed));
    }

    @Test
    void hasNoPassRateWhenNothingWasRecorded() {
        ComplianceThresholdService service = new ComplianceThresholdService(fakeRepository());

        assertTrue(service.passRate(null).isEmpty());
        assertTrue(service.passRate(List.of()).isEmpty());
    }

    @Test
    void leavesSummaryUntouchedWhenRunHasNoResults() {
        ComplianceThresholdService service = new ComplianceThresholdService(fakeRepository());
        service.upsert(ScanKind.COMPLIANCE, AssetType.APIGEE, 80, true, "tester");

        assertEquals(summary().keySet(),
                service.decorate(summary(), ScanKind.COMPLIANCE, AssetType.APIGEE, List.of()).keySet());
    }

    /* ────────────────────────────── Configuration ──────────────────────────── */

    @Test
    void rejectsThresholdsOutsideZeroToOneHundred() {
        ComplianceThresholdService service = new ComplianceThresholdService(fakeRepository());

        assertThrows(IllegalArgumentException.class,
                () -> service.upsert(ScanKind.COMPLIANCE, AssetType.APIGEE, 101, true, "tester"));
        assertThrows(IllegalArgumentException.class,
                () -> service.upsert(ScanKind.COMPLIANCE, AssetType.APIGEE, -1, true, "tester"));
    }

    @Test
    void upsertReplacesRatherThanDuplicates() {
        ComplianceThresholdRepository repo = fakeRepository();
        ComplianceThresholdService service = new ComplianceThresholdService(repo);

        service.upsert(ScanKind.COMPLIANCE, AssetType.APIGEE, 80, true, "first");
        service.upsert(ScanKind.COMPLIANCE, AssetType.APIGEE, 65, true, "second");

        assertEquals(1, repo.findAll().size());
        assertEquals(Optional.of(65), service.effectiveThreshold(ScanKind.COMPLIANCE, AssetType.APIGEE));
        assertEquals("second", repo.findAll().get(0).getUpdatedBy());
        assertEquals("first", repo.findAll().get(0).getCreatedBy(), "createdBy must survive an update");
    }

    @Test
    void deleteRestoresOriginalSemantics() {
        ComplianceThresholdService service = new ComplianceThresholdService(fakeRepository());
        service.upsert(ScanKind.COMPLIANCE, AssetType.APIGEE, 80, true, "tester");

        assertTrue(service.delete(ScanKind.COMPLIANCE, AssetType.APIGEE));
        assertTrue(service.effectiveThreshold(ScanKind.COMPLIANCE, AssetType.APIGEE).isEmpty());
        assertEquals(summary().keySet(),
                service.decorate(summary(), ScanKind.COMPLIANCE, AssetType.APIGEE, results(14, 19)).keySet());
        assertFalse(service.delete(ScanKind.COMPLIANCE, AssetType.APIGEE), "a second delete is a no-op");
    }
}
