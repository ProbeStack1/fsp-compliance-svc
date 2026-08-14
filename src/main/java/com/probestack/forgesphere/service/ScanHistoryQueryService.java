package com.probestack.forgesphere.service;

import com.probestack.forgesphere.model.AssetType;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;
import org.bson.Document;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.Aggregation;
import org.springframework.data.mongodb.core.aggregation.AggregationOperation;
import org.springframework.data.mongodb.core.aggregation.AggregationResults;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Service;

/**
 * Paged reads of the three scan history collections.
 *
 * <h2>Why this exists</h2>
 *
 * The history endpoints used to call {@code findAll()}, then filter, sort and page the result in
 * memory. Every request therefore materialised the entire collection — and a scan document carries
 * its full {@code scanResults} array, roughly sixty entries each — to return twenty summaries that
 * do not contain {@code scanResults} at all. At ~500 stored scans that is on the order of thirty
 * thousand result objects per request, twice over while sorting. A handful of concurrent requests
 * exhausted the heap, and the instance answered 502 until it was replaced.
 *
 * <h2>What this does instead</h2>
 *
 * Mongo does the matching, sorting and paging, and the projection computes the three counts the
 * summary needs with {@code $size} and {@code $filter} <em>server-side</em>. The array itself is
 * never sent to the JVM. Per-request heap becomes proportional to the page size rather than to the
 * size of the collection, and is independent of how much history has accumulated.
 *
 * <p>The response shape is deliberately unchanged, field for field, so existing consumers see
 * exactly what they saw before.
 */
@Service
public class ScanHistoryQueryService {

    private static final Logger log = LoggerFactory.getLogger(ScanHistoryQueryService.class);

    /**
     * Ceiling on how many records one request may ask for.
     *
     * A caller asking for 5000 would undo the point of this class. Callers are told what they
     * actually got — the response echoes {@code size} — so a client that honours it still walks
     * the whole history, just in more steps.
     */
    public static final int MAX_PAGE_SIZE = 200;

    /** One page of summaries, plus the total the filters matched. */
    public record HistoryPage(List<Map<String, Object>> items, long total, int size) { }

    private final MongoTemplate mongoTemplate;

    public ScanHistoryQueryService(MongoTemplate mongoTemplate) {
        this.mongoTemplate = mongoTemplate;
    }

    /**
     * Read one page of a scan history collection.
     *
     * @param collection Mongo collection name
     * @param page       zero-based
     * @param size       requested page size, clamped to {@link #MAX_PAGE_SIZE}
     */
    public HistoryPage page(String collection, String projectName, AssetType assetType,
            String requestedBy, int page, int size) {
        int safeSize = Math.max(1, Math.min(size, MAX_PAGE_SIZE));
        int safePage = Math.max(0, page);
        Criteria criteria = criteria(projectName, assetType, requestedBy);

        long total = mongoTemplate.count(Query.query(criteria), collection);
        if (total == 0 || (long) safePage * safeSize >= total) {
            return new HistoryPage(List.of(), total, safeSize);
        }

        List<AggregationOperation> ops = new ArrayList<>();
        ops.add(Aggregation.match(criteria));
        // Missing dates sort last under a descending sort, matching the previous
        // Comparator.nullsLast(reverseOrder()) ordering.
        ops.add(Aggregation.sort(Sort.by(Sort.Direction.DESC, "createDate")));
        ops.add(Aggregation.skip((long) safePage * safeSize));
        ops.add(Aggregation.limit(safeSize));
        ops.add(projection());

        AggregationResults<Document> results = mongoTemplate.aggregate(
                Aggregation.newAggregation(ops), collection, Document.class);

        List<Map<String, Object>> items = new ArrayList<>();
        for (Document d : results.getMappedResults()) {
            items.add(summary(d));
        }
        log.debug("history page collection={} page={} size={} total={} returned={}",
                collection, safePage, safeSize, total, items.size());
        return new HistoryPage(items, total, safeSize);
    }

    /* ─────────────────────────────── Internals ──────────────────────────────── */

    /**
     * The same three filters the endpoints have always applied.
     *
     * Anchored case-insensitive regexes rather than plain equality, because the previous in-memory
     * version used {@code equalsIgnoreCase} and dropping that would silently change which records
     * a caller gets back.
     */
    private Criteria criteria(String projectName, AssetType assetType, String requestedBy) {
        List<Criteria> parts = new ArrayList<>();
        if (projectName != null) {
            parts.add(Criteria.where("projectName").regex(exactIgnoreCase(projectName)));
        }
        if (assetType != null) {
            parts.add(Criteria.where("assetType").is(assetType.name()));
        }
        if (requestedBy != null) {
            Pattern who = exactIgnoreCase(requestedBy);
            parts.add(new Criteria().orOperator(
                    Criteria.where("createdBy").regex(who),
                    Criteria.where("updatedBy").regex(who)));
        }
        return parts.isEmpty() ? new Criteria() : new Criteria().andOperator(parts.toArray(new Criteria[0]));
    }

    private Pattern exactIgnoreCase(String value) {
        return Pattern.compile("^" + Pattern.quote(value) + "$", Pattern.CASE_INSENSITIVE);
    }

    /**
     * Summary fields, with the counts computed inside Mongo.
     *
     * {@code $size}/{@code $filter} over {@code scanResults} means the array is read by the server
     * and reduced to three integers before anything crosses the wire — which is the whole point.
     */
    private AggregationOperation projection() {
        return Aggregation
                .project("scanId", "projectName", "assetType", "assetName", "status", "compliance",
                        "createDate", "createdBy")
                .and(context -> new Document("$size", safeResults())).as("totalResults")
                .and(context -> countWhere("PASSED")).as("passed")
                .and(context -> countWhere("FAILED")).as("failed");
    }

    /** A scan recorded before results existed has no array at all; treat it as empty, not null. */
    private Document safeResults() {
        return new Document("$ifNull", List.of("$scanResults", List.of()));
    }

    private Document countWhere(String status) {
        return new Document("$size", new Document("$filter", new Document()
                .append("input", safeResults())
                .append("cond", new Document("$eq", List.of("$$this.result", status)))));
    }

    /** Key order matches what the controllers produced before, so the JSON is unchanged. */
    private Map<String, Object> summary(Document d) {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("scanId", d.getString("scanId"));
        m.put("projectName", d.getString("projectName"));
        m.put("assetType", d.getString("assetType"));
        m.put("assetName", d.getString("assetName"));
        m.put("status", d.getString("status"));
        m.put("compliance", d.getString("compliance"));
        m.put("totalResults", intOf(d.get("totalResults")));
        m.put("passed", (long) intOf(d.get("passed")));
        m.put("failed", (long) intOf(d.get("failed")));
        m.put("createDate", toOffset(d.get("createDate")));
        m.put("createdBy", d.getString("createdBy"));
        return m;
    }

    private int intOf(Object o) {
        return o instanceof Number n ? n.intValue() : 0;
    }

    private OffsetDateTime toOffset(Object raw) {
        Instant i = raw instanceof Date date ? date.toInstant() : raw instanceof Instant inst ? inst : null;
        return i == null ? null : OffsetDateTime.ofInstant(i, ZoneOffset.UTC);
    }
}
