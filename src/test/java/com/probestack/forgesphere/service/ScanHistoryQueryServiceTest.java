package com.probestack.forgesphere.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.probestack.forgesphere.model.AssetType;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.Date;
import java.util.List;
import java.util.Map;
import org.bson.Document;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.Aggregation;
import org.springframework.data.mongodb.core.aggregation.AggregationResults;
import org.springframework.data.mongodb.core.query.Query;

/**
 * The point of this class is that a history request costs the page, not the collection.
 *
 * So the assertions that matter are about the pipeline: that the work happens in Mongo, that the
 * per-rule array is reduced to counts there and never projected out, and that a caller cannot ask
 * for an unbounded page. `neverProjectsTheResultsArray` is the one that fails if someone
 * "simplifies" this back into loading documents.
 */
class ScanHistoryQueryServiceTest {

    private static final String COLLECTION = "governance_compliance_scans";
    private static final long EPOCH_MILLIS = 1_760_000_000_000L;

    private static Document row() {
        return new Document("scanId", "CS-1")
                .append("projectName", "acme")
                .append("assetType", "APIGEE")
                .append("assetName", "my-proxy")
                .append("status", "COMPLETED")
                .append("compliance", "NON_COMPLIANT")
                .append("totalResults", 60)
                .append("passed", 42)
                .append("failed", 15)
                .append("createDate", new Date(EPOCH_MILLIS))
                .append("createdBy", "me@acme.io");
    }

    @SuppressWarnings("unchecked")
    private static MongoTemplate template(long total, List<Document> rows) {
        MongoTemplate mt = mock(MongoTemplate.class);
        when(mt.count(any(Query.class), anyString())).thenReturn(total);
        when(mt.aggregate(any(Aggregation.class), anyString(), eq(Document.class)))
                .thenReturn(new AggregationResults<>(rows, new Document()));
        return mt;
    }

    /** The pipeline as Mongo would receive it. */
    private static List<Document> pipelineOf(MongoTemplate mt) {
        ArgumentCaptor<Aggregation> captor = ArgumentCaptor.forClass(Aggregation.class);
        verify(mt).aggregate(captor.capture(), anyString(), eq(Document.class));
        return captor.getValue().toPipeline(Aggregation.DEFAULT_CONTEXT);
    }

    /* ────────────────────── The reason this class exists ────────────────────── */

    @Test
    void neverProjectsTheResultsArray() {
        MongoTemplate mt = template(500, List.of(row()));
        new ScanHistoryQueryService(mt).page(COLLECTION, null, null, null, 0, 20);

        Document project = pipelineOf(mt).stream()
                .filter(d -> d.containsKey("$project")).findFirst().orElseThrow();
        Document fields = project.get("$project", Document.class);

        // The array is reduced to three integers inside Mongo. Projecting it would put ~60 objects
        // per row back on the heap, which is exactly what this class was written to stop.
        assertFalse(fields.containsKey("scanResults"), "scanResults must never be projected");
        assertTrue(fields.containsKey("totalResults"));
        assertTrue(fields.containsKey("passed"));
        assertTrue(fields.containsKey("failed"));
        assertTrue(fields.toJson().contains("$size"), "counts are computed server-side");
    }

    @Test
    void pushesMatchingSortingAndPagingIntoMongo() {
        MongoTemplate mt = template(500, List.of(row()));
        new ScanHistoryQueryService(mt).page(COLLECTION, null, null, null, 3, 20);

        List<String> stages = pipelineOf(mt).stream().map(d -> d.keySet().iterator().next()).toList();
        assertEquals(List.of("$match", "$sort", "$skip", "$limit", "$project"), stages);

        Document skip = pipelineOf(mt).stream().filter(d -> d.containsKey("$skip")).findFirst().orElseThrow();
        assertEquals(60L, skip.get("$skip"), "page 3 at size 20 starts at 60");
    }

    @Test
    void clampsThePageSizeSoOneRequestCannotAskForEverything() {
        MongoTemplate mt = template(5000, List.of(row()));
        ScanHistoryQueryService.HistoryPage p =
                new ScanHistoryQueryService(mt).page(COLLECTION, null, null, null, 0, 5000);

        assertEquals(ScanHistoryQueryService.MAX_PAGE_SIZE, p.size());
        Document limit = pipelineOf(mt).stream().filter(d -> d.containsKey("$limit")).findFirst().orElseThrow();
        assertEquals((long) ScanHistoryQueryService.MAX_PAGE_SIZE, limit.get("$limit"));
    }

    @Test
    void doesNotQueryAtAllForAPagePastTheEnd() {
        MongoTemplate mt = template(30, List.of(row()));
        ScanHistoryQueryService.HistoryPage p =
                new ScanHistoryQueryService(mt).page(COLLECTION, null, null, null, 9, 20);

        assertTrue(p.items().isEmpty());
        assertEquals(30, p.total(), "the total is still reported so a client can correct itself");
        verify(mt, never()).aggregate(any(Aggregation.class), anyString(), eq(Document.class));
    }

    @Test
    void doesNotQueryAnEmptyCollection() {
        MongoTemplate mt = template(0, List.of());
        assertTrue(new ScanHistoryQueryService(mt).page(COLLECTION, null, null, null, 0, 20).items().isEmpty());
        verify(mt, never()).aggregate(any(Aggregation.class), anyString(), eq(Document.class));
    }

    /* ─────────────────── The response other teams already read ─────────────────── */

    @Test
    void producesTheSameSummaryShapeAsBefore() {
        MongoTemplate mt = template(1, List.of(row()));
        Map<String, Object> m = new ScanHistoryQueryService(mt)
                .page(COLLECTION, null, null, null, 0, 20).items().get(0);

        assertEquals(List.of("scanId", "projectName", "assetType", "assetName", "status", "compliance",
                "totalResults", "passed", "failed", "createDate", "createdBy"),
                List.copyOf(m.keySet()), "field order and names are part of the contract");
        assertEquals("CS-1", m.get("scanId"));
        assertEquals("APIGEE", m.get("assetType"));
        assertEquals(60, m.get("totalResults"));
        // Counts were longs before, from a stream count; keep them longs so the JSON is identical.
        assertEquals(42L, m.get("passed"));
        assertEquals(15L, m.get("failed"));
        assertEquals(OffsetDateTime.ofInstant(Instant.ofEpochMilli(EPOCH_MILLIS), ZoneOffset.UTC),
                m.get("createDate"), "surfaced as UTC, whatever the server's zone");
    }

    @Test
    void appliesTheFiltersTheEndpointsAlwaysHad() {
        MongoTemplate mt = template(10, List.of(row()));
        new ScanHistoryQueryService(mt).page(COLLECTION, "Acme", AssetType.APIGEE, "Me@Acme.io", 0, 20);

        Document match = pipelineOf(mt).stream()
                .filter(d -> d.containsKey("$match")).findFirst().orElseThrow();
        String json = match.toJson();
        // Case-insensitive, as the previous equalsIgnoreCase comparisons were; and requestedBy
        // still matches either actor field.
        assertTrue(json.contains("projectName"));
        assertTrue(json.contains("APIGEE"));
        assertTrue(json.contains("createdBy") && json.contains("updatedBy"));
        assertTrue(json.contains("\"i\""), "name matching stays case-insensitive");
    }

    @Test
    void treatsNegativePagesAsTheFirstPage() {
        MongoTemplate mt = template(100, List.of(row()));
        new ScanHistoryQueryService(mt).page(COLLECTION, null, null, null, -5, 20);

        Document skip = pipelineOf(mt).stream().filter(d -> d.containsKey("$skip")).findFirst().orElseThrow();
        assertEquals(0L, skip.get("$skip"));
    }
}
