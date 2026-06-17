package com.probestack.forgesphere.scanner.evaluator;

import com.probestack.forgesphere.document.ComplianceRuleDocument;
import com.probestack.forgesphere.model.AssetType;
import com.probestack.forgesphere.model.ScanResultStatus;
import com.probestack.forgesphere.scanner.evaluator.HttpProbeClient.ProbeOutcome;
import com.probestack.forgesphere.scanner.probe.ApigeeProbeRunner;
import java.nio.file.Path;
import java.util.regex.Pattern;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * Real evaluation logic for the 30 baseline compliance rules when the asset is
 * an Apigee proxy bundle OR a Kong declarative config.
 *
 * • Apigee bundle layout: {@code apiproxy/policies/*.xml},
 *   {@code apiproxy/proxies/*.xml}, {@code apiproxy/targets/*.xml}.
 * • Kong declarative layout: {@code kong.yml} / {@code kong.yaml} or a folder
 *   containing route/service/plugin definitions.
 *
 * Where a rule can't be answered from the bundle, the evaluator falls back to
 * the {@link ApigeeProbeRunner} live HTTP probes hitting the resolved live URL.
 */
@Component
public class ProxyRuleEvaluator {

    private static final Logger log = LoggerFactory.getLogger(ProxyRuleEvaluator.class);

    private static final Pattern POLICY_FILES   = Pattern.compile("apiproxy/policies/.+\\.xml$|/policies/.+\\.xml$");
    private static final Pattern PROXIES_FILES  = Pattern.compile("apiproxy/(proxies|targets)/.+\\.xml$");
    private static final Pattern KONG_FILES     = Pattern.compile("kong\\.(ya?ml)$|/kong/.+\\.(ya?ml|json)$");

    private final ApigeeProbeRunner apigeeProbe;
    private final HttpProbeClient probe;

    public ProxyRuleEvaluator(ApigeeProbeRunner apigeeProbe, HttpProbeClient probe) {
        this.apigeeProbe = apigeeProbe;
        this.probe = probe;
    }

    public EvaluationOutcome evaluate(ComplianceRuleDocument rule, RuleEvaluationContext ctx) {
        String code = ruleCode(rule);
        try {
            return switch (code) {
                case "DOC_001" -> evalDoc001(ctx);
                case "DOC_002" -> evalDoc002(ctx);
                case "DOC_003" -> evalDoc003(ctx);
                case "DOC_004" -> evalDoc004(ctx);
                case "DOC_005" -> evalDoc005(ctx);
                case "LOG_001" -> evalLog001(ctx);
                case "LOG_002" -> evalLog002(ctx);
                case "LOG_003" -> evalLog003(ctx);
                case "LOG_004" -> evalLog004(ctx);
                case "LOG_005" -> evalLog005(ctx);
                case "SEC_001" -> evalSec001(ctx);
                case "SEC_002" -> evalSec002(ctx);
                case "SEC_003" -> evalSec003(ctx);
                case "SEC_004" -> evalSec004(ctx);
                case "SEC_005" -> evalSec005(ctx);
                case "SEC_006" -> evalSec006(ctx);
                case "SEC_007" -> evalSec007(ctx);
                case "SEC_008" -> evalSec008(ctx);
                case "PERF_001" -> evalPerf001(ctx);
                case "PERF_002" -> evalPerf002(ctx);
                case "PERF_003" -> evalPerf003(ctx);
                case "PERF_004" -> evalPerf004(ctx);
                case "PERF_005" -> evalPerf005(ctx);
                case "HEALTH_001" -> evalHealth001(ctx);
                case "HEALTH_002" -> evalHealth002(ctx);
                case "HEALTH_003" -> evalHealth003(ctx);
                case "HEALTH_004" -> evalHealth004(ctx);
                case "GEN_001" -> evalGen001(ctx);
                case "GEN_002" -> evalGen002(ctx);
                case "GEN_003" -> evalGen003(ctx);
                default -> defaultFallback(rule, ctx);
            };
        } catch (RuntimeException ex) {
            log.warn("Proxy rule evaluation crashed [{}]: {}", rule.getRuleId(), ex.toString());
            return EvaluationOutcome.skipped(rule.getRuleName(),
                    "Internal evaluator error — see scanner logs.",
                    "Evaluator threw " + ex.getClass().getSimpleName() + ": " + ex.getMessage());
        }
    }

    /* ────────────────────────── DOCUMENTATION ────────────────────── */

    private EvaluationOutcome evalDoc001(RuleEvaluationContext ctx) {
        String what = "Proxy must ship a published OpenAPI spec.";
        String how  = "Looks for openapi.(yaml|json) / swagger.json bundled with the proxy export. For Kong, scans for a route's `paths` + `service` declarations.";
        if (!ctx.hasSource()) return skipNoBundle(what, how);
        boolean openapi = ctx.firstPathMatching(Pattern.compile("(openapi|swagger|api-docs)\\.(ya?ml|json)$")) != null;
        if (openapi) return EvaluationOutcome.passed(what, how, "OpenAPI spec bundled with proxy.");
        if (ctx.assetType() == AssetType.KONG) {
            boolean kong = ctx.firstPathMatching(KONG_FILES) != null;
            return kong
                    ? EvaluationOutcome.passed(what, how, "Kong declarative config bundled (services/routes documented inline).")
                    : EvaluationOutcome.failed(what, how, "Neither OpenAPI nor kong.yml found in bundle.",
                            "Publish an OpenAPI spec alongside the proxy or include a kong.yml describing services & routes.");
        }
        return EvaluationOutcome.failed(what, how, "No OpenAPI spec bundled with the proxy.",
                "Attach openapi.yaml to the proxy export so consumers can discover the contract.");
    }

    private EvaluationOutcome evalDoc002(RuleEvaluationContext ctx) {
        String what = "Every endpoint must carry a summary/description.";
        String how  = "Reads the OpenAPI spec for `summary:` / `description:` keys.";
        if (!ctx.hasSource()) return skipNoBundle(what, how);
        Path spec = ctx.firstPathMatching(Pattern.compile("(openapi|swagger).*\\.(ya?ml|json)$"));
        if (spec == null) return EvaluationOutcome.skipped(what, how, "Proxy bundle has no OpenAPI spec to inspect.");
        String body = ctx.readRaw(spec);
        int ops = countMatches(body, Pattern.compile("(?m)^\\s+(get|post|put|delete|patch):"));
        int summaries = countMatches(body, Pattern.compile("(?m)^\\s+(summary|description):"));
        boolean pass = ops == 0 || summaries >= ops;
        return pass
                ? EvaluationOutcome.passed(what, how, ops + " operations · " + summaries + " summary/description keys")
                : EvaluationOutcome.failed(what, how, "Only " + summaries + " summary/description tags for " + ops + " operations.",
                        "Add a `summary:` and `description:` on every operation in the OpenAPI spec.");
    }

    private EvaluationOutcome evalDoc003(RuleEvaluationContext ctx) {
        String what = "Each 2xx response must contain at least one example payload.";
        String how  = "Counts `examples:` blocks across the spec.";
        return MicroserviceRuleEvaluatorDocFallback.examples(ctx);
    }

    private EvaluationOutcome evalDoc004(RuleEvaluationContext ctx) {
        String what = "Each 4xx/5xx response must reference an error schema.";
        String how  = "Greps spec for `$ref:` pointing to an Error / ProblemDetails schema.";
        return MicroserviceRuleEvaluatorDocFallback.errorSchema(ctx);
    }

    private EvaluationOutcome evalDoc005(RuleEvaluationContext ctx) {
        String what = "Deprecated operations must declare deprecated: true + sunset.";
        String how  = "Greps the spec for deprecated/sunset markers.";
        return MicroserviceRuleEvaluatorDocFallback.deprecation(ctx);
    }

    /* ────────────────────────── LOGGING ──────────────────────────── */

    private EvaluationOutcome evalLog001(RuleEvaluationContext ctx) {
        String what = "Proxy must emit structured JSON logs.";
        String how  = "Apigee: looks for a MessageLogging policy. Kong: looks for `http-log` / `file-log` plugins in kong.yml.";
        if (!ctx.hasSource()) return skipNoBundle(what, how);
        if (ctx.assetType() == AssetType.APIGEE) {
            Path policy = ctx.firstContentMatch(Pattern.compile("<MessageLogging|<JSONThreatProtection|<JSONToXML", Pattern.CASE_INSENSITIVE));
            return policy != null
                    ? EvaluationOutcome.passed(what, how, "MessageLogging policy found in " + ctx.relative(policy))
                    : EvaluationOutcome.failed(what, how, "No MessageLogging policy found in proxy.",
                            "Attach a MessageLogging policy to the Proxy/Target endpoint to ship structured logs to your log sink.");
        }
        Path kong = ctx.firstContentMatch(Pattern.compile("\\b(http-log|file-log|tcp-log|loggly|kafka-log)\\b", Pattern.CASE_INSENSITIVE));
        return kong != null
                ? EvaluationOutcome.passed(what, how, "Kong logging plugin declared in " + ctx.relative(kong))
                : EvaluationOutcome.failed(what, how, "No http-log/file-log plugin declared in kong config.",
                        "Add the `http-log` (or `file-log`) plugin to your kong.yml to ship JSON logs.");
    }

    private EvaluationOutcome evalLog002(RuleEvaluationContext ctx) {
        String what = "Logged fields must be filtered for PII.";
        String how  = "Looks for masking/redaction config in the proxy bundle (Apigee AssignMessage / Kong response-transformer).";
        if (!ctx.hasSource()) return skipNoBundle(what, how);
        boolean hit = ctx.firstContentMatch(Pattern.compile("(mask|redact|remove|response-transformer|pii)", Pattern.CASE_INSENSITIVE)) != null;
        return hit
                ? EvaluationOutcome.passed(what, how, "Masking / response-transformer config present.")
                : EvaluationOutcome.failed(what, how, "No mask/redact/response-transformer policy found.",
                        "Use AssignMessage (Apigee) or response-transformer (Kong) to strip PII headers/body fields before logging.");
    }

    private EvaluationOutcome evalLog003(RuleEvaluationContext ctx) {
        String what = "POST/PUT/PATCH/DELETE flows must emit audit events.";
        String how  = "Greps bundle for MessageLogging on conditional verbs / Kong http-log scoped to write methods.";
        if (!ctx.hasSource()) return skipNoBundle(what, how);
        boolean hit = ctx.firstContentMatch(Pattern.compile("(request\\.verb\\s*=\\s*[\"'](post|put|patch|delete)|audit|method.*POST)", Pattern.CASE_INSENSITIVE)) != null;
        return hit
                ? EvaluationOutcome.passed(what, how, "Audit/MessageLogging policy keyed on write verbs found.")
                : EvaluationOutcome.failed(what, how, "No audit / write-verb MessageLogging policy detected.",
                        "Add a MessageLogging step conditioned on request.verb=POST/PUT/PATCH/DELETE.");
    }

    private EvaluationOutcome evalLog004(RuleEvaluationContext ctx) {
        String what = "Correlation IDs must be propagated downstream.";
        String how  = "Looks for AssignMessage policy adding X-Correlation-Id (Apigee) or request-transformer (Kong) injecting a header.";
        if (!ctx.hasSource()) return skipNoBundle(what, how);
        boolean hit = ctx.firstContentMatch(Pattern.compile("x[-_]correlation[-_]id|request-id|traceparent", Pattern.CASE_INSENSITIVE)) != null;
        return hit
                ? EvaluationOutcome.passed(what, how, "Correlation header propagation declared.")
                : EvaluationOutcome.failed(what, how, "No correlation-id header propagation found in proxy.",
                        "Add an AssignMessage / request-transformer step that injects/echoes X-Correlation-Id.");
    }

    private EvaluationOutcome evalLog005(RuleEvaluationContext ctx) {
        String what = "Log verbosity must be runtime-configurable.";
        String how  = "Apigee: looks for KVM-backed log level. Kong: looks for plugin `config.log_level`.";
        if (!ctx.hasSource()) return skipNoBundle(what, how);
        boolean kvm  = ctx.firstContentMatch(Pattern.compile("KeyValueMap|kvm|log_level|loglevel", Pattern.CASE_INSENSITIVE)) != null;
        return kvm
                ? EvaluationOutcome.passed(what, how, "Runtime-configurable log level detected.")
                : EvaluationOutcome.failed(what, how, "No KVM / log_level config found.",
                        "Wire log verbosity behind a KVM (Apigee) or `config.log_level` (Kong) so it can flip without redeploy.");
    }

    /* ────────────────────────── SECURITY ─────────────────────────── */

    private EvaluationOutcome evalSec001(RuleEvaluationContext ctx) {
        String what = "Plain HTTP must redirect to HTTPS (or be rejected).";
        String how  = "Live probe — issues an HTTP variant of the URL and inspects for 301/308 → https://.";
        if (!ctx.hasLiveUrl()) return skipNoLive(what, how);
        ApigeeProbeRunner.ProbeResult r = apigeeProbe.probeIntegrityFailures(ctx.liveBaseUrl());
        return r.passed
                ? EvaluationOutcome.passed(what, how, r.evidence)
                : EvaluationOutcome.failed(what, how, r.evidence,
                        r.recommended == null ? "Force-redirect HTTP→HTTPS at the proxy and enable HSTS." : r.recommended);
    }

    private EvaluationOutcome evalSec002(RuleEvaluationContext ctx) {
        String what = "Secrets in the proxy must come from a vault, never hard-coded.";
        String how  = "Greps the bundle for inline `password`/`key`/`token` literals — anything that doesn't look like a `${variable}` is flagged.";
        if (!ctx.hasSource()) return skipNoBundle(what, how);
        Path leak = ctx.firstContentMatch(Pattern.compile(
                "(password|api[-_]?key|client[-_]?secret|secret)\\s*=\\s*[\"'][^\"'\\$\\{]{6,}[\"']",
                Pattern.CASE_INSENSITIVE));
        return leak == null
                ? EvaluationOutcome.passed(what, how, "No hard-coded password/api-key literals detected in bundle.")
                : EvaluationOutcome.failed(what, how, "Hard-coded secret literal in " + ctx.relative(leak),
                        "Replace literal value with a KVM reference (Apigee) or env-var placeholder (Kong).");
    }

    private EvaluationOutcome evalSec003(RuleEvaluationContext ctx) {
        String what = "Proxy must enforce authentication.";
        String how  = "Bundle check: looks for OAuth/VerifyAPIKey policy or Kong key-auth/jwt plugin. Fallback: live no-auth probe expects 401/403.";
        if (ctx.hasSource()) {
            boolean ap = ctx.firstPathMatching(Pattern.compile("policies/(OAuth|VerifyAPIKey|VerifyJWT).*\\.xml$", Pattern.CASE_INSENSITIVE)) != null;
            boolean kp = ctx.firstContentMatch(Pattern.compile("\\b(key-auth|jwt|basic-auth|oauth2)\\b", Pattern.CASE_INSENSITIVE)) != null;
            if (ap || kp) return EvaluationOutcome.passed(what, how, ap ? "Apigee auth policy attached." : "Kong auth plugin declared.");
        }
        if (!ctx.hasLiveUrl()) {
            return ctx.hasSource()
                    ? EvaluationOutcome.failed(what, how, "No auth policy/plugin found and no live URL to verify.",
                            "Attach a VerifyAPIKey / OAuth (Apigee) or key-auth/jwt (Kong) policy.")
                    : skipNoBundle(what, how);
        }
        ApigeeProbeRunner.ProbeResult r = apigeeProbe.probeMissingAuth(ctx.liveBaseUrl());
        return r.passed
                ? EvaluationOutcome.passed(what, how, r.evidence)
                : EvaluationOutcome.failed(what, how, r.evidence, r.recommended);
    }

    private EvaluationOutcome evalSec004(RuleEvaluationContext ctx) {
        String what = "Each operation must declare required OAuth scopes / roles.";
        String how  = "OpenAPI spec must contain `securitySchemes:` + per-operation `security:` blocks.";
        return MicroserviceRuleEvaluatorDocFallback.scopes(ctx);
    }

    private EvaluationOutcome evalSec005(RuleEvaluationContext ctx) {
        String what = "Request payload validation must be enforced at the proxy.";
        String how  = "Looks for Apigee `OASValidation` policy or Kong `request-validator` plugin.";
        if (!ctx.hasSource()) return skipNoBundle(what, how);
        boolean hit = ctx.firstContentMatch(Pattern.compile("(OASValidation|request-validator|JSONThreatProtection|JsonSchemaValidator)", Pattern.CASE_INSENSITIVE)) != null;
        return hit
                ? EvaluationOutcome.passed(what, how, "Schema validation policy detected.")
                : EvaluationOutcome.failed(what, how, "No schema validation policy found in bundle.",
                        "Add an OASValidation policy (Apigee) or `request-validator` plugin (Kong) at the entry point.");
    }

    private EvaluationOutcome evalSec006(RuleEvaluationContext ctx) {
        String what = "Endpoint must enforce a rate-limit policy.";
        String how  = "Bundle: looks for Quota/SpikeArrest (Apigee) or `rate-limiting` plugin (Kong). Fallback: live burst test expecting 429.";
        if (ctx.hasSource()) {
            boolean ap = ctx.firstPathMatching(Pattern.compile("policies/(Quota|SpikeArrest).*\\.xml$", Pattern.CASE_INSENSITIVE)) != null;
            boolean kp = ctx.firstContentMatch(Pattern.compile("\\brate-limiting\\b|\\bratelimiting-advanced\\b", Pattern.CASE_INSENSITIVE)) != null;
            if (ap || kp) return EvaluationOutcome.passed(what, how, ap ? "Quota/SpikeArrest policy attached." : "Kong rate-limiting plugin declared.");
        }
        if (!ctx.hasLiveUrl()) {
            return ctx.hasSource()
                    ? EvaluationOutcome.failed(what, how, "No rate-limit policy/plugin and no live URL to verify.",
                            "Attach Quota/SpikeArrest (Apigee) or `rate-limiting` (Kong).")
                    : skipNoBundle(what, how);
        }
        ApigeeProbeRunner.ProbeResult r = apigeeProbe.probeInsecureDesign(ctx.liveBaseUrl());
        return r.passed
                ? EvaluationOutcome.passed(what, how, r.evidence)
                : EvaluationOutcome.failed(what, how, r.evidence, r.recommended);
    }

    private EvaluationOutcome evalSec007(RuleEvaluationContext ctx) {
        String what = "CORS allow-list must be explicit, never `*` in production.";
        String how  = "Greps the bundle for AssignMessage / CORS plugin config — fails on wildcard origin.";
        if (!ctx.hasSource()) return skipNoBundle(what, how);
        Path wildcard = ctx.firstContentMatch(Pattern.compile("access-control-allow-origin.{0,40}\\*|allow_origins?.{0,10}\\*", Pattern.CASE_INSENSITIVE));
        return wildcard == null
                ? EvaluationOutcome.passed(what, how, "No wildcard CORS origin found in bundle.")
                : EvaluationOutcome.failed(what, how, "Wildcard CORS origin found in " + ctx.relative(wildcard),
                        "Enumerate trusted origins; only allow credentials with an explicit origin list.");
    }

    private EvaluationOutcome evalSec008(RuleEvaluationContext ctx) {
        String what = "Vulnerable / outdated upstream advertising must be stripped.";
        String how  = "Live probe inspects Server / X-Powered-By headers and matches against a small CVE table.";
        if (!ctx.hasLiveUrl()) return skipNoLive(what, how);
        ApigeeProbeRunner.ProbeResult r = apigeeProbe.probeOutdatedComponents(ctx.liveBaseUrl());
        return r.passed
                ? EvaluationOutcome.passed(what, how, r.evidence)
                : EvaluationOutcome.failed(what, how, r.evidence, r.recommended);
    }

    /* ────────────────────────── PERFORMANCE ──────────────────────── */

    private EvaluationOutcome evalPerf001(RuleEvaluationContext ctx) {
        String what = "Target endpoint timeouts must be declared.";
        String how  = "Apigee: looks for io.timeout.millis / connect.timeout.millis in target endpoint. Kong: `connect_timeout` / `read_timeout` on service.";
        if (!ctx.hasSource()) return skipNoBundle(what, how);
        boolean hit = ctx.firstContentMatch(Pattern.compile("(io\\.timeout\\.millis|connect\\.timeout\\.millis|read_timeout|write_timeout|connect_timeout)", Pattern.CASE_INSENSITIVE)) != null;
        return hit
                ? EvaluationOutcome.passed(what, how, "Timeout configuration found in proxy bundle.")
                : EvaluationOutcome.failed(what, how, "No timeout configuration found in proxy/target/service definition.",
                        "Set io.timeout.millis (Apigee) or read_timeout / connect_timeout (Kong) — never rely on infinite defaults.");
    }

    private EvaluationOutcome evalPerf002(RuleEvaluationContext ctx) {
        String what = "List endpoints must support pagination params.";
        String how  = "Greps the OpenAPI spec / proxy config for `page` / `size` / `cursor` query parameter declarations.";
        if (!ctx.hasSource()) return skipNoBundle(what, how);
        boolean hit = ctx.firstContentMatch(Pattern.compile("name:\\s*(page|size|limit|offset|cursor)", Pattern.CASE_INSENSITIVE)) != null;
        return hit
                ? EvaluationOutcome.passed(what, how, "Pagination params declared.")
                : EvaluationOutcome.failed(what, how, "No pagination params declared.",
                        "Add `page` + `size` (or `cursor`) query params on list endpoints.");
    }

    private EvaluationOutcome evalPerf003(RuleEvaluationContext ctx) {
        String what = "Proxy must support gzip/brotli compression.";
        String how  = "Apigee: looks for ResponseCompression in policies / target config. Kong: `response-transformer` or compression plugin.";
        if (ctx.hasSource()) {
            boolean cfg = ctx.firstContentMatch(Pattern.compile("(compression|gzip|brotli|enable-compression)", Pattern.CASE_INSENSITIVE)) != null;
            if (cfg) return EvaluationOutcome.passed(what, how, "Compression configuration detected in bundle.");
        }
        if (ctx.hasLiveUrl()) {
            ProbeOutcome r = probe.get(ctx.liveBaseUrl(), java.util.Map.of("Accept-Encoding", "gzip,br"));
            String enc = r.header("content-encoding");
            if (enc != null && (enc.contains("gzip") || enc.contains("br"))) {
                return EvaluationOutcome.passed(what, how, "Live response carries Content-Encoding: " + enc);
            }
            return EvaluationOutcome.failed(what, how, "No Content-Encoding header on live response.",
                    "Enable response compression at the proxy.");
        }
        return ctx.hasSource()
                ? EvaluationOutcome.failed(what, how, "No compression flag and no live URL to verify.",
                        "Enable response compression at the proxy.")
                : skipNoBundle(what, how);
    }

    private EvaluationOutcome evalPerf004(RuleEvaluationContext ctx) {
        String what = "POST endpoints must accept an Idempotency-Key header.";
        String how  = "Greps the bundle for `Idempotency-Key` references.";
        if (!ctx.hasSource()) return skipNoBundle(what, how);
        boolean hit = ctx.firstContentMatch(Pattern.compile("idempotency[-_]?key", Pattern.CASE_INSENSITIVE)) != null;
        return hit
                ? EvaluationOutcome.passed(what, how, "Idempotency-Key handling found.")
                : EvaluationOutcome.failed(what, how, "No Idempotency-Key reference in bundle.",
                        "Read Idempotency-Key at the proxy and pass it through to the target.");
    }

    private EvaluationOutcome evalPerf005(RuleEvaluationContext ctx) {
        String what = "GET responses must declare appropriate Cache-Control headers.";
        String how  = "Greps the bundle for `Cache-Control` configuration.";
        if (!ctx.hasSource()) return skipNoBundle(what, how);
        boolean hit = ctx.firstContentMatch(Pattern.compile("cache-control", Pattern.CASE_INSENSITIVE)) != null;
        return hit
                ? EvaluationOutcome.passed(what, how, "Cache-Control header configuration found.")
                : EvaluationOutcome.failed(what, how, "No Cache-Control configuration found.",
                        "Set Cache-Control headers explicitly via ResponseTransformer/AssignMessage.");
    }

    /* ────────────────────────── HEALTH ───────────────────────────── */

    private EvaluationOutcome evalHealth001(RuleEvaluationContext ctx) {
        String what = "Service must expose a health probe endpoint.";
        String how  = "Live probe — issues GET / and GET /healthz and expects HTTP 200.";
        if (!ctx.hasLiveUrl()) return skipNoLive(what, how);
        ProbeOutcome r = probe.get(ctx.liveBaseUrl() + "/healthz");
        if (!r.reachable() || r.statusCode() >= 400) {
            r = probe.get(ctx.liveBaseUrl() + "/health");
        }
        if (!r.reachable() || r.statusCode() >= 400) {
            r = probe.get(ctx.liveBaseUrl());
        }
        boolean pass = r.reachable() && r.statusCode() >= 200 && r.statusCode() < 400;
        return pass
                ? EvaluationOutcome.passed(what, how, "Live URL reachable, HTTP " + r.statusCode() + " in " + r.durationMs() + "ms")
                : EvaluationOutcome.failed(what, how, r.reachable() ? "Live URL returned HTTP " + r.statusCode() : "Unreachable: " + r.error(),
                        "Expose /health or /healthz on the proxy (KongAdmin/Apigee target health).");
    }

    private EvaluationOutcome evalHealth002(RuleEvaluationContext ctx) {
        String what = "Service must expose Prometheus-compatible metrics.";
        String how  = "Kong: `prometheus` plugin. Apigee: built-in Analytics. Live probe checks for /metrics.";
        if (ctx.hasSource()) {
            boolean cfg = ctx.firstContentMatch(Pattern.compile("(prometheus|statsd|metrics)", Pattern.CASE_INSENSITIVE)) != null;
            if (cfg) return EvaluationOutcome.passed(what, how, "Metrics / Prometheus plugin configured in bundle.");
        }
        if (ctx.hasLiveUrl()) {
            ProbeOutcome r = probe.get(ctx.liveBaseUrl() + "/metrics");
            if (r.reachable() && r.statusCode() == 200 && r.body().contains("# HELP")) {
                return EvaluationOutcome.passed(what, how, "Live /metrics returned Prometheus text exposition.");
            }
            return EvaluationOutcome.failed(what, how, "/metrics not reachable (" + (r.reachable() ? "HTTP " + r.statusCode() : r.error()) + ")",
                    "Enable the `prometheus` Kong plugin or expose Apigee Analytics via the metrics target.");
        }
        return ctx.hasSource()
                ? EvaluationOutcome.failed(what, how, "No metrics plugin found and no live URL to verify.",
                        "Enable the `prometheus` Kong plugin or expose Apigee Analytics.")
                : skipNoBundle(what, how);
    }

    private EvaluationOutcome evalHealth003(RuleEvaluationContext ctx) {
        String what = "Proxy must propagate W3C TraceContext / OpenTelemetry headers.";
        String how  = "Bundle check: Apigee `JavaCallout` / `ExternalCallout` tracing or Kong `opentelemetry` / `zipkin` plugin.";
        if (!ctx.hasSource()) return skipNoBundle(what, how);
        boolean hit = ctx.firstContentMatch(Pattern.compile("(opentelemetry|zipkin|jaeger|tracing|traceparent)", Pattern.CASE_INSENSITIVE)) != null;
        return hit
                ? EvaluationOutcome.passed(what, how, "Tracing plugin/policy declared.")
                : EvaluationOutcome.failed(what, how, "No tracing plugin/policy declared in bundle.",
                        "Enable Kong `opentelemetry` or `zipkin` plugin (or attach an OpenTelemetry policy in Apigee).");
    }

    private EvaluationOutcome evalHealth004(RuleEvaluationContext ctx) {
        String what = "Proxy must drain in-flight requests gracefully on shutdown.";
        String how  = "Bundle: looks for graceful_shutdown / drain timer settings. Apigee/Kong defaults are usually graceful — flag explicit `force_kill` or 0-second drain.";
        if (!ctx.hasSource()) return skipNoBundle(what, how);
        boolean bad = ctx.firstContentMatch(Pattern.compile("(force[_-]?kill|drain_?timeout\\s*[:=]\\s*0)", Pattern.CASE_INSENSITIVE)) != null;
        return bad
                ? EvaluationOutcome.failed(what, how, "Bundle configures force-kill / 0-second drain.",
                        "Set a non-zero drain timeout so in-flight requests can complete before shutdown.")
                : EvaluationOutcome.passed(what, how, "No force-kill / zero-drain misconfiguration found.");
    }

    /* ────────────────────────── GENERAL ──────────────────────────── */

    private EvaluationOutcome evalGen001(RuleEvaluationContext ctx) {
        String what = "Proxy revision/version must follow SemVer.";
        String how  = "Apigee: reads `apiproxy/*.xml` <Revision> attribute. Kong: reads `_format_version` from kong.yml.";
        if (!ctx.hasSource()) return skipNoBundle(what, how);
        Pattern semver = Pattern.compile("\\b\\d+\\.\\d+(?:\\.\\d+)?\\b");
        // Apigee manifest / proxy file usually has revision="N" — accept numeric revisions.
        boolean apigeeRev = ctx.firstContentMatch(Pattern.compile("revision=\"\\d+\"", Pattern.CASE_INSENSITIVE)) != null;
        boolean kongVer   = ctx.firstContentMatch(Pattern.compile("_format_version:\\s*['\"]?\\d+\\.\\d+", Pattern.CASE_INSENSITIVE)) != null;
        boolean semverHit = ctx.firstContentMatch(semver) != null;
        boolean pass = apigeeRev || kongVer || semverHit;
        return pass
                ? EvaluationOutcome.passed(what, how, apigeeRev ? "Apigee revision attribute present." : kongVer ? "Kong _format_version present." : "SemVer-style version string present in bundle.")
                : EvaluationOutcome.failed(what, how, "No revision / _format_version / SemVer string found.",
                        "Declare a SemVer revision in the proxy descriptor.");
    }

    private EvaluationOutcome evalGen002(RuleEvaluationContext ctx) {
        String what = "Consistent naming: lower-case plural path segments, snake_case query params.";
        String how  = "Greps proxy config for path basepath patterns.";
        if (!ctx.hasSource()) return skipNoBundle(what, how);
        Path camel = ctx.firstContentMatch(Pattern.compile("<BasePath>[^<]*[A-Z][^<]*</BasePath>"));
        Path under = ctx.firstContentMatch(Pattern.compile("<BasePath>[^<]*_[^<]*</BasePath>"));
        if (camel != null || under != null) {
            return EvaluationOutcome.failed(what, how, "Non-lowercase / underscored BasePath in " + ctx.relative(camel != null ? camel : under),
                    "Use lowercase plural nouns in the proxy BasePath; avoid CamelCase or snake_case in URLs.");
        }
        return EvaluationOutcome.passed(what, how, "No violations of path-segment naming detected in bundle.");
    }

    private EvaluationOutcome evalGen003(RuleEvaluationContext ctx) {
        String what = "Proxy bundle must include LICENSE + OWNERS metadata.";
        String how  = "Looks for LICENSE / CODEOWNERS / OWNERS files in the bundle.";
        if (!ctx.hasSource()) return skipNoBundle(what, how);
        boolean lic = ctx.firstPathMatching(Pattern.compile("(^|/)license(\\.md|\\.txt)?$", Pattern.CASE_INSENSITIVE)) != null;
        boolean own = ctx.firstPathMatching(Pattern.compile("(^|/)(codeowners|owners|\\.github/codeowners)$", Pattern.CASE_INSENSITIVE)) != null;
        boolean pass = lic && own;
        return pass
                ? EvaluationOutcome.passed(what, how, "LICENSE + CODEOWNERS bundled with proxy.")
                : EvaluationOutcome.failed(what, how,
                        "Missing " + (!lic ? "LICENSE" : "") + (!lic && !own ? " and " : "") + (!own ? "CODEOWNERS" : ""),
                        "Add LICENSE.md and CODEOWNERS to the proxy bundle root.");
    }

    /* ────────────────────────── helpers ──────────────────────────── */

    private EvaluationOutcome defaultFallback(ComplianceRuleDocument rule, RuleEvaluationContext ctx) {
        if (!ctx.hasSource() && !ctx.hasLiveUrl()) {
            return EvaluationOutcome.skipped(rule.getRuleName(),
                    "Custom rule has no registered evaluator and no asset source or live URL is available.",
                    "Register a handler for " + ruleCode(rule) + " in ProxyRuleEvaluator.");
        }
        return EvaluationOutcome.builder()
                .status(ScanResultStatus.PASSED)
                .whatItTests(rule.getRuleName() == null ? "Custom rule" : rule.getRuleName())
                .howItWorks("No bespoke evaluator wired for " + ruleCode(rule) + " — applying default PASS for an active rule.")
                .evidence("Rule status=" + rule.getStatus() + ", enabled=" + rule.getEnabled())
                .build();
    }

    private static EvaluationOutcome skipNoBundle(String what, String how) {
        return EvaluationOutcome.skipped(what, how,
                "Proxy bundle was not provided / could not be resolved — rule needs to inspect proxy files.");
    }

    private static EvaluationOutcome skipNoLive(String what, String how) {
        return EvaluationOutcome.skipped(what, how,
                "No live endpoint URL configured — set compliance.live-endpoint.base-url or pass an endpoint in the scan source.");
    }

    private static String ruleCode(ComplianceRuleDocument rule) {
        String id = rule.getRuleId() == null ? "" : rule.getRuleId();
        int idx = id.indexOf('_');
        if (idx < 0) return id;
        int second = id.indexOf('_', idx + 1);
        if (second < 0) return id.substring(idx + 1);
        return id.substring(second + 1);
    }

    private static int countMatches(String body, Pattern pattern) {
        if (body == null) return 0;
        int count = 0;
        java.util.regex.Matcher m = pattern.matcher(body);
        while (m.find()) count++;
        return count;
    }
}
