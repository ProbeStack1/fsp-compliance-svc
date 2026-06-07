package com.probestack.forgesphere.service;

import com.probestack.forgesphere.document.ComplianceRuleDocument;
import com.probestack.forgesphere.model.AssetType;
import com.probestack.forgesphere.model.RuleCategory;
import com.probestack.forgesphere.model.RuleSeverity;
import com.probestack.forgesphere.model.RuleStatus;
import com.probestack.forgesphere.model.RuleType;
import com.probestack.forgesphere.repository.ComplianceRuleRepository;
import java.time.Instant;
import java.util.List;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

/**
 * Seeds the default compliance rule catalog on application boot. Mirrors
 * {@link OwaspRuleSeeder} but for compliance/governance baseline checks.
 *
 * Rules are seeded per AssetType (MICROSERVICE, APIGEE, KONG) so the UI
 * "Asset Type" filter has something to show on day-one.
 */
@Component
public class ComplianceRuleSeeder {

    private static final String DEFAULT_RULE_OWNER = "Governance Team";

    private final ComplianceRuleRepository complianceRuleRepository;

    public ComplianceRuleSeeder(ComplianceRuleRepository complianceRuleRepository) {
        this.complianceRuleRepository = complianceRuleRepository;
    }

    @EventListener(ApplicationReadyEvent.class)
    public void seedComplianceRules() {
        for (AssetType assetType : AssetType.values()) {
            for (DefaultRule defaultRule : DEFAULT_RULES) {
                String ruleId = buildRuleId(assetType, defaultRule.code());
                if (!complianceRuleRepository.existsByRuleId(ruleId)) {
                    complianceRuleRepository.save(buildDocument(assetType, defaultRule, ruleId));
                }
            }
        }
    }

    private String buildRuleId(AssetType assetType, String code) {
        return String.format("CR_%s_%s", assetType.name(), code);
    }

    private ComplianceRuleDocument buildDocument(AssetType assetType, DefaultRule rule, String ruleId) {
        Instant now = Instant.now();
        ComplianceRuleDocument doc = new ComplianceRuleDocument();
        doc.setRuleId(ruleId);
        doc.setAssetType(assetType);
        doc.setRuleName(rule.ruleName());
        doc.setRuleDescription(rule.ruleDescription());
        doc.setRuleType(RuleType.PRE_DEFINED);
        doc.setRuleOwner(DEFAULT_RULE_OWNER);
        doc.setCategory(rule.category());
        doc.setSeverity(rule.severity());
        doc.setEnabled(true);
        doc.setMandatory(rule.mandatory());
        doc.setDisplayOrder(rule.displayOrder());
        doc.setIcon(rule.icon());
        doc.setStatus(RuleStatus.ACTIVE);
        doc.setImplementationKey("COMPLIANCE_" + ruleId);
        doc.setCreateDate(now);
        doc.setCreatedBy("system");
        doc.setUpdatedDate(now);
        doc.setUpdatedBy("system");
        return doc;
    }

    private record DefaultRule(
            String code,
            String ruleName,
            String ruleDescription,
            RuleCategory category,
            RuleSeverity severity,
            Boolean mandatory,
            Integer displayOrder,
            String icon) {
    }

    private static final List<DefaultRule> DEFAULT_RULES = List.of(
            // ── Documentation (5) ──────────────────────────────────────
            new DefaultRule("DOC_001", "OpenAPI documentation present",
                    "Every API must ship a versioned OpenAPI/Swagger document under /openapi or /swagger.",
                    RuleCategory.DOCUMENTATION, RuleSeverity.MANDATORY, true, 1, "file-text"),
            new DefaultRule("DOC_002", "Endpoint summaries documented",
                    "Every endpoint must have a summary + description in the OpenAPI spec (no missing docs).",
                    RuleCategory.DOCUMENTATION, RuleSeverity.MANDATORY, true, 2, "book-open"),
            new DefaultRule("DOC_003", "Response examples present",
                    "Each 2xx response in the OpenAPI spec must contain at least one example payload.",
                    RuleCategory.DOCUMENTATION, RuleSeverity.RECOMMENDED, false, 3, "list-checks"),
            new DefaultRule("DOC_004", "Error response schemas",
                    "Each 4xx / 5xx response must reference a standard error schema (RFC-7807 / ProblemDetails).",
                    RuleCategory.DOCUMENTATION, RuleSeverity.MANDATORY, true, 4, "alert-circle"),
            new DefaultRule("DOC_005", "Deprecation flags",
                    "Deprecated operations must set `deprecated: true` and include a sunset header strategy.",
                    RuleCategory.DOCUMENTATION, RuleSeverity.RECOMMENDED, false, 5, "trending-down"),

            // ── Logging (5) ────────────────────────────────────────────
            new DefaultRule("LOG_001", "Structured logging enabled",
                    "Logs must be JSON-structured with correlation IDs and request context (logback-spring.xml).",
                    RuleCategory.LOGGING, RuleSeverity.MANDATORY, true, 6, "terminal"),
            new DefaultRule("LOG_002", "No PII in logs",
                    "Logged fields must be filtered for PII (email, phone, SSN, payment data).",
                    RuleCategory.LOGGING, RuleSeverity.MANDATORY, true, 7, "eye-off"),
            new DefaultRule("LOG_003", "Audit log on write paths",
                    "POST/PUT/PATCH/DELETE endpoints must emit an audit event (who, when, what, before, after).",
                    RuleCategory.LOGGING, RuleSeverity.MANDATORY, true, 8, "history"),
            new DefaultRule("LOG_004", "Correlation ID propagation",
                    "Inbound `X-Correlation-Id` (or generated) must be propagated to all downstream calls + emitted in logs.",
                    RuleCategory.LOGGING, RuleSeverity.MANDATORY, true, 9, "git-merge"),
            new DefaultRule("LOG_005", "Log level configurable",
                    "Log level must be configurable at runtime via /actuator/loggers without restart.",
                    RuleCategory.LOGGING, RuleSeverity.RECOMMENDED, false, 10, "sliders"),

            // ── Security (8) ───────────────────────────────────────────
            new DefaultRule("SEC_001", "TLS 1.2+ enforced",
                    "All inbound traffic must reject connections below TLS 1.2 (no SSLv3, no TLS 1.0/1.1).",
                    RuleCategory.SECURITY, RuleSeverity.MANDATORY, true, 11, "lock"),
            new DefaultRule("SEC_002", "Secrets via vault, never code",
                    "API keys / DB passwords must be sourced from a secret store (Vault, AWS SM, GCP SM). Static repo scan must yield no high-entropy secrets.",
                    RuleCategory.SECURITY, RuleSeverity.MANDATORY, true, 12, "key"),
            new DefaultRule("SEC_003", "Authentication required",
                    "Every endpoint must be behind authentication unless explicitly listed as public in the spec.",
                    RuleCategory.SECURITY, RuleSeverity.MANDATORY, true, 13, "shield"),
            new DefaultRule("SEC_004", "Authorization scopes declared",
                    "Each endpoint must declare the OAuth scopes / roles required to invoke it (in the spec).",
                    RuleCategory.SECURITY, RuleSeverity.MANDATORY, true, 14, "shield-check"),
            new DefaultRule("SEC_005", "Input validation",
                    "Every request body and query param must be validated against the OpenAPI schema before reaching handlers.",
                    RuleCategory.SECURITY, RuleSeverity.MANDATORY, true, 15, "check-square"),
            new DefaultRule("SEC_006", "Rate limiting policy",
                    "Endpoints must declare a rate-limit policy (per-API-key or per-consumer). 429 responses must include Retry-After.",
                    RuleCategory.SECURITY, RuleSeverity.MANDATORY, true, 16, "gauge"),
            new DefaultRule("SEC_007", "CORS allow-list explicit",
                    "CORS must enumerate origins (no `*` in production); credentials only allowed with explicit origin list.",
                    RuleCategory.SECURITY, RuleSeverity.MANDATORY, true, 17, "globe"),
            new DefaultRule("SEC_008", "Dependency vulnerability scan",
                    "CI pipeline must run an SCA scan (e.g. Snyk, OWASP Dependency-Check) and fail the build on HIGH vulns.",
                    RuleCategory.SECURITY, RuleSeverity.RECOMMENDED, false, 18, "alert-triangle"),

            // ── Performance (5) ────────────────────────────────────────
            new DefaultRule("PERF_001", "Timeouts configured",
                    "Outbound HTTP clients must declare connect + read timeouts (≤ 10s) — no infinite waits.",
                    RuleCategory.PERFORMANCE, RuleSeverity.RECOMMENDED, false, 19, "zap"),
            new DefaultRule("PERF_002", "Pagination on list endpoints",
                    "Any endpoint returning a collection must support `page` + `size` (or cursor) query params.",
                    RuleCategory.PERFORMANCE, RuleSeverity.RECOMMENDED, false, 20, "list"),
            new DefaultRule("PERF_003", "Response compression",
                    "Server must support gzip / brotli when client advertises `Accept-Encoding`.",
                    RuleCategory.PERFORMANCE, RuleSeverity.RECOMMENDED, false, 21, "package"),
            new DefaultRule("PERF_004", "Idempotency keys on writes",
                    "POST endpoints performing money/state mutations must accept and honor an `Idempotency-Key` header.",
                    RuleCategory.PERFORMANCE, RuleSeverity.MANDATORY, true, 22, "repeat"),
            new DefaultRule("PERF_005", "Caching headers set",
                    "GET responses must declare appropriate `Cache-Control` (e.g. `no-store` for sensitive, `max-age` for static).",
                    RuleCategory.PERFORMANCE, RuleSeverity.RECOMMENDED, false, 23, "save"),

            // ── Health (4) ─────────────────────────────────────────────
            new DefaultRule("HEALTH_001", "Liveness / readiness probes",
                    "Service must expose /actuator/health (or equivalent) returning UP/DOWN within 1s.",
                    RuleCategory.HEALTH, RuleSeverity.MANDATORY, true, 24, "heart"),
            new DefaultRule("HEALTH_002", "Metrics endpoint exposed",
                    "Service must expose Prometheus-compatible metrics under /actuator/prometheus.",
                    RuleCategory.HEALTH, RuleSeverity.RECOMMENDED, false, 25, "activity"),
            new DefaultRule("HEALTH_003", "Distributed tracing enabled",
                    "Service must propagate W3C TraceContext headers and emit OpenTelemetry spans.",
                    RuleCategory.HEALTH, RuleSeverity.RECOMMENDED, false, 26, "git-branch"),
            new DefaultRule("HEALTH_004", "Graceful shutdown",
                    "Service must drain in-flight requests on SIGTERM (Spring `server.shutdown=graceful`).",
                    RuleCategory.HEALTH, RuleSeverity.MANDATORY, true, 27, "power"),

            // ── General (3) ────────────────────────────────────────────
            new DefaultRule("GEN_001", "Semantic versioning",
                    "Releases must follow SemVer (MAJOR.MINOR.PATCH); breaking changes bump MAJOR.",
                    RuleCategory.GENERAL, RuleSeverity.RECOMMENDED, false, 28, "git-branch"),
            new DefaultRule("GEN_002", "Consistent naming",
                    "Path segments must be lowercase plural nouns; query params snake_case; JSON fields camelCase.",
                    RuleCategory.GENERAL, RuleSeverity.RECOMMENDED, false, 29, "type"),
            new DefaultRule("GEN_003", "License + ownership file",
                    "Repo must contain LICENSE + OWNERS/CODEOWNERS file naming the responsible squad.",
                    RuleCategory.GENERAL, RuleSeverity.MANDATORY, true, 30, "users")
    );
}
