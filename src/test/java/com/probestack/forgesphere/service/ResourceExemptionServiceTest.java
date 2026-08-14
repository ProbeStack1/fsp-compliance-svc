package com.probestack.forgesphere.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.probestack.forgesphere.document.ResourceExemptionDocument;
import com.probestack.forgesphere.model.AssetType;
import com.probestack.forgesphere.model.ComplianceStatus;
import com.probestack.forgesphere.model.ScanKind;
import com.probestack.forgesphere.repository.ResourceExemptionRepository;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import org.junit.jupiter.api.Test;

/**
 * The most important assertions here are the negative ones.
 *
 * An exemption must stay invisible until someone creates one, and must never rewrite a scan's own
 * verdict, because other teams consume these same endpoints and have no interest in exemptions.
 * The `leavesSummaryUntouched…`, `neverRewrites…` and `doesNotLeakAcross…` tests are the ones that
 * fail if that guarantee is ever broken.
 */
class ResourceExemptionServiceTest {

    /** Map-backed repository mock, stubbing only what the service calls. Same approach as the cut-off tests. */
    private static ResourceExemptionRepository fakeRepository() {
        ResourceExemptionRepository repo = mock(ResourceExemptionRepository.class);
        Map<String, ResourceExemptionDocument> store = new LinkedHashMap<>();

        when(repo.save(any(ResourceExemptionDocument.class))).thenAnswer(inv -> {
            ResourceExemptionDocument d = inv.getArgument(0);
            if (d.getId() == null) {
                d.setId(UUID.randomUUID().toString());
            }
            store.put(d.getId(), d);
            return d;
        });
        when(repo.findAll()).thenAnswer(inv -> new ArrayList<>(store.values()));
        when(repo.findByScanKindAndAssetTypeAndAssetName(any(), any(), any())).thenAnswer(inv -> {
            ScanKind kind = inv.getArgument(0);
            AssetType asset = inv.getArgument(1);
            String name = inv.getArgument(2);
            return store.values().stream()
                    .filter(d -> kind == d.getScanKind() && asset == d.getAssetType()
                            && Objects.equals(name, d.getAssetName()))
                    .findFirst();
        });
        when(repo.findAllByAssetType(any())).thenAnswer(inv -> {
            AssetType asset = inv.getArgument(0);
            return store.values().stream().filter(d -> asset == d.getAssetType()).toList();
        });
        when(repo.findAllByScanKindAndAssetType(any(), any())).thenAnswer(inv -> {
            ScanKind kind = inv.getArgument(0);
            AssetType asset = inv.getArgument(1);
            return store.values().stream()
                    .filter(d -> kind == d.getScanKind() && asset == d.getAssetType()).toList();
        });
        doAnswer(inv -> {
            ResourceExemptionDocument d = inv.getArgument(0);
            store.remove(d.getId());
            return null;
        }).when(repo).delete(any(ResourceExemptionDocument.class));

        return repo;
    }

    /** A scan summary as the controllers build it, before any decoration. */
    private static Map<String, Object> summary(String assetName, ComplianceStatus compliance) {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("scanId", "CS-1");
        m.put("assetName", assetName);
        m.put("status", "COMPLETED");
        m.put("compliance", compliance);
        m.put("totalResults", 10);
        m.put("passed", 3L);
        m.put("failed", 7L);
        return m;
    }

    /* ─────────────────────── The guarantees other teams rely on ─────────────────────── */

    @Test
    void leavesSummaryUntouchedWhenNothingIsExempt() {
        ResourceExemptionService service = new ResourceExemptionService(fakeRepository());
        Map<String, Object> before = summary("my-proxy", ComplianceStatus.NON_COMPLIANT);
        Map<String, Object> expected = new LinkedHashMap<>(before);

        Map<String, Object> after = service.decorate(before, ScanKind.COMPLIANCE, AssetType.APIGEE, "my-proxy");

        assertEquals(expected, after, "an un-exempt asset must produce the response it always did");
        assertFalse(after.containsKey(ResourceExemptionService.KEY_EXEMPTED));
    }

    @Test
    void neverRewritesTheScansOwnVerdictOrStatus() {
        ResourceExemptionService service = new ResourceExemptionService(fakeRepository());
        service.exempt(ScanKind.COMPLIANCE, AssetType.APIGEE, "my-proxy", "accepted risk", "me@x.io");

        Map<String, Object> after = service.decorate(
                summary("my-proxy", ComplianceStatus.NON_COMPLIANT), ScanKind.COMPLIANCE, AssetType.APIGEE, "my-proxy");

        // The exemption is reported alongside the finding, never in place of it: a report that
        // turned failures into passes would be useless as evidence.
        assertEquals(ComplianceStatus.NON_COMPLIANT, after.get("compliance"));
        assertEquals("COMPLETED", after.get("status"));
        assertEquals(7L, after.get("failed"));
        assertEquals(Boolean.TRUE, after.get(ResourceExemptionService.KEY_EXEMPTED));
    }

    @Test
    void doesNotLeakAcrossScanKinds() {
        ResourceExemptionService service = new ResourceExemptionService(fakeRepository());
        service.exempt(ScanKind.LINTING, AssetType.APIGEE, "my-proxy", null, "me@x.io");

        // Forgiving lint noise must not quietly forgive the security findings too.
        assertFalse(service.decorate(summary("my-proxy", ComplianceStatus.NON_COMPLIANT),
                ScanKind.OWASP, AssetType.APIGEE, "my-proxy")
                .containsKey(ResourceExemptionService.KEY_EXEMPTED));
    }

    @Test
    void doesNotLeakAcrossAssetTypesOrNames() {
        ResourceExemptionService service = new ResourceExemptionService(fakeRepository());
        service.exempt(ScanKind.COMPLIANCE, AssetType.APIGEE, "my-proxy", null, "me@x.io");

        assertFalse(service.decorate(summary("my-proxy", null), ScanKind.COMPLIANCE, AssetType.KONG, "my-proxy")
                .containsKey(ResourceExemptionService.KEY_EXEMPTED));
        assertFalse(service.decorate(summary("other", null), ScanKind.COMPLIANCE, AssetType.APIGEE, "other")
                .containsKey(ResourceExemptionService.KEY_EXEMPTED));
    }

    /* ───────────────────────────────── Behaviour ───────────────────────────────── */

    @Test
    void decorateReportsWhoAndWhenAndWhy() {
        ResourceExemptionService service = new ResourceExemptionService(fakeRepository());
        service.exempt(ScanKind.COMPLIANCE, AssetType.APIGEE, "my-proxy", "  legacy, retiring Q3  ", "me@x.io");

        Map<String, Object> after = service.decorate(
                summary("my-proxy", ComplianceStatus.NON_COMPLIANT), ScanKind.COMPLIANCE, AssetType.APIGEE, "my-proxy");

        assertEquals("legacy, retiring Q3", after.get(ResourceExemptionService.KEY_REASON), "reason is trimmed");
        assertEquals("me@x.io", after.get(ResourceExemptionService.KEY_BY));
        assertTrue(after.get(ResourceExemptionService.KEY_AT) != null);
    }

    @Test
    void exemptingTwiceUpdatesRatherThanDuplicating() {
        ResourceExemptionRepository repo = fakeRepository();
        ResourceExemptionService service = new ResourceExemptionService(repo);

        service.exempt(ScanKind.COMPLIANCE, AssetType.APIGEE, "my-proxy", "first", "a@x.io");
        service.exempt(ScanKind.COMPLIANCE, AssetType.APIGEE, "my-proxy", "second", "b@x.io");

        assertEquals(1, repo.findAll().size(), "a retry must not create a second row");
        Map<String, Object> after = service.decorate(
                summary("my-proxy", null), ScanKind.COMPLIANCE, AssetType.APIGEE, "my-proxy");
        assertEquals("second", after.get(ResourceExemptionService.KEY_REASON));
        assertEquals("b@x.io", after.get(ResourceExemptionService.KEY_BY));
    }

    @Test
    void blankReasonIsStoredAsAbsentRatherThanEmpty() {
        ResourceExemptionService service = new ResourceExemptionService(fakeRepository());
        service.exempt(ScanKind.COMPLIANCE, AssetType.APIGEE, "my-proxy", "   ", "me@x.io");

        assertNull(service.decorate(summary("my-proxy", null), ScanKind.COMPLIANCE, AssetType.APIGEE, "my-proxy")
                .get(ResourceExemptionService.KEY_REASON));
    }

    @Test
    void liftingRestoresTheUndecoratedResponse() {
        ResourceExemptionService service = new ResourceExemptionService(fakeRepository());
        service.exempt(ScanKind.COMPLIANCE, AssetType.APIGEE, "my-proxy", "temporary", "me@x.io");

        assertTrue(service.lift(ScanKind.COMPLIANCE, AssetType.APIGEE, "my-proxy"));
        assertFalse(service.lift(ScanKind.COMPLIANCE, AssetType.APIGEE, "my-proxy"), "second lift is a no-op");
        assertFalse(service.decorate(summary("my-proxy", null), ScanKind.COMPLIANCE, AssetType.APIGEE, "my-proxy")
                .containsKey(ResourceExemptionService.KEY_EXEMPTED));
    }

    @Test
    void rejectsIncompleteKeys() {
        ResourceExemptionService service = new ResourceExemptionService(fakeRepository());
        assertThrows(IllegalArgumentException.class,
                () -> service.exempt(null, AssetType.APIGEE, "my-proxy", null, "me@x.io"));
        assertThrows(IllegalArgumentException.class,
                () -> service.exempt(ScanKind.COMPLIANCE, null, "my-proxy", null, "me@x.io"));
        assertThrows(IllegalArgumentException.class,
                () -> service.exempt(ScanKind.COMPLIANCE, AssetType.APIGEE, "  ", null, "me@x.io"));
    }

    @Test
    void decorateToleratesMissingInputs() {
        ResourceExemptionService service = new ResourceExemptionService(fakeRepository());
        assertTrue(service.decorate(null, ScanKind.COMPLIANCE, AssetType.APIGEE, "x").isEmpty());
        assertFalse(service.decorate(summary("x", null), ScanKind.COMPLIANCE, AssetType.APIGEE, null)
                .containsKey(ResourceExemptionService.KEY_EXEMPTED));
    }
}
