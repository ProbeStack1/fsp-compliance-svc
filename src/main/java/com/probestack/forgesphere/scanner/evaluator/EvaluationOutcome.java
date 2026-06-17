package com.probestack.forgesphere.scanner.evaluator;

import com.probestack.forgesphere.model.ScanEvidence;
import com.probestack.forgesphere.model.ScanResultStatus;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Result envelope returned by a single rule handler. The {@link #toJsonMessage()}
 * format is what gets stored on {@link com.probestack.forgesphere.model.ScanResult#getMessage()}
 * so the UI can render a rich "what was checked / how it was checked / evidence /
 * recommendation" panel for every rule (same shape used by OWASP probes).
 */
public final class EvaluationOutcome {

    private static final ObjectMapper MAPPER = new ObjectMapper();

    private final ScanResultStatus status;
    private final String whatItTests;
    private final String howItWorks;
    private final String evidence;
    private final String recommendation;
    private final List<ScanEvidence> structuredEvidence;
    private final long durationMs;

    private EvaluationOutcome(Builder b) {
        this.status = b.status;
        this.whatItTests = b.whatItTests;
        this.howItWorks = b.howItWorks;
        this.evidence = b.evidence;
        this.recommendation = b.recommendation;
        this.structuredEvidence = b.structuredEvidence;
        this.durationMs = b.durationMs;
    }

    public ScanResultStatus status() { return status; }
    public List<ScanEvidence> structuredEvidence() { return structuredEvidence; }

    public String toJsonMessage() {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("status", status == null ? "SKIPPED" : status.name());
        m.put("whatItTests", whatItTests);
        m.put("howItWorks", howItWorks);
        m.put("evidence", evidence);
        m.put("recommended", recommendation);     // matches OWASP probe envelope field name
        m.put("recommendation", recommendation); // keep both for forwards compatibility
        m.put("durationMs", durationMs);
        try {
            return MAPPER.writeValueAsString(m);
        } catch (JsonProcessingException e) {
            return (evidence == null ? "" : evidence);
        }
    }

    public static Builder builder() { return new Builder(); }

    public static EvaluationOutcome passed(String whatItTests, String howItWorks, String evidence) {
        return builder().status(ScanResultStatus.PASSED).whatItTests(whatItTests).howItWorks(howItWorks)
                .evidence(evidence).build();
    }

    public static EvaluationOutcome failed(String whatItTests, String howItWorks, String evidence,
            String recommendation) {
        return builder().status(ScanResultStatus.FAILED).whatItTests(whatItTests).howItWorks(howItWorks)
                .evidence(evidence).recommendation(recommendation).build();
    }

    public static EvaluationOutcome skipped(String whatItTests, String howItWorks, String reason) {
        return builder().status(ScanResultStatus.SKIPPED).whatItTests(whatItTests).howItWorks(howItWorks)
                .evidence(reason).build();
    }

    public static final class Builder {
        private ScanResultStatus status = ScanResultStatus.SKIPPED;
        private String whatItTests;
        private String howItWorks;
        private String evidence;
        private String recommendation;
        private List<ScanEvidence> structuredEvidence = new ArrayList<>();
        private long durationMs;

        public Builder status(ScanResultStatus v) { this.status = v; return this; }
        public Builder whatItTests(String v) { this.whatItTests = v; return this; }
        public Builder howItWorks(String v) { this.howItWorks = v; return this; }
        public Builder evidence(String v) { this.evidence = v; return this; }
        public Builder recommendation(String v) { this.recommendation = v; return this; }
        public Builder durationMs(long v) { this.durationMs = v; return this; }
        public Builder addEvidence(String file, Integer line, String detail) {
            ScanEvidence e = new ScanEvidence();
            e.setFilePath(file);
            e.setLineNumber(line);
            e.setDetails(detail);
            structuredEvidence.add(e);
            return this;
        }
        public EvaluationOutcome build() { return new EvaluationOutcome(this); }
    }
}
