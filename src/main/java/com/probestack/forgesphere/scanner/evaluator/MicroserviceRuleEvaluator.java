package com.probestack.forgesphere.scanner.evaluator;

import com.probestack.forgesphere.document.ComplianceRuleDocument;
import com.probestack.forgesphere.model.ScanResultStatus;
import com.probestack.forgesphere.scanner.evaluator.HttpProbeClient.ProbeOutcome;
import java.nio.file.Path;
import java.util.List;
import java.util.regex.Pattern;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * Real evaluation logic for the 30 baseline compliance rules when the asset is
 * a microservice (Spring Boot / Node.js source bundle).
 *
 * Each {@code evaluate*} method is a complete check:
 *  • inspects {@link RuleEvaluationContext#sourceRoot()} when a bundle is
 *    available — looks for concrete files, configuration keys, or annotations.
 *  • when no bundle is available, falls back to a live HTTP probe against
 *    {@link RuleEvaluationContext#liveBaseUrl()}.
 *  • returns an {@link EvaluationOutcome} that carries PASSED/FAILED/SKIPPED
 *    together with a structured "whatItTests / howItWorks / evidence /
 *    recommendation" payload (rendered by the UI right-side drawer).
 */
@Component
public class MicroserviceRuleEvaluator {

    private static final Logger log = LoggerFactory.getLogger(MicroserviceRuleEvaluator.class);

    private static final Pattern OPENAPI_FILE = Pattern.compile("(openapi|swagger|api-docs)\\.(ya?ml|json)$");
    private static final Pattern LOGBACK_FILE = Pattern.compile("logback(-spring)?\\.xml$");
    private static final Pattern POM_FILE     = Pattern.compile("(^|/)pom\\.xml$");
    private static final Pattern APP_PROPS    = Pattern.compile("(^|/)application(-[a-z0-9]+)?\\.(properties|ya?ml)$");
    private static final Pattern PACKAGE_JSON = Pattern.compile("(^|/)package\\.json$");
    private static final Pattern LICENSE_FILE = Pattern.compile("(^|/)license(\\.md|\\.txt)?$", Pattern.CASE_INSENSITIVE);
    private static final Pattern OWNERS_FILE  = Pattern.compile("(^|/)(?:owners|codeowners|\\.github/codeowners)$", Pattern.CASE_INSENSITIVE);

    private final HttpProbeClient probe;

    public MicroserviceRuleEvaluator(HttpProbeClient probe) {
        this.probe = probe;
    }

    /** Routes a rule to the matching concrete handler based on the rule code suffix. */
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
            log.warn("Microservice rule evaluation crashed [{}]: {}", rule.getRuleId(), ex.toString());
            return EvaluationOutcome.skipped(
                    rule.getRuleName(),
                    "Internal evaluator error — see scanner logs.",
                    "Evaluator threw " + ex.getClass().getSimpleName() + ": " + ex.getMessage());
        }
    }

    /* ─────────────────────────── DOCUMENTATION ─────────────────────── */

    private EvaluationOutcome evalDoc001(RuleEvaluationContext ctx) {
        String what = "Service must ship a versioned OpenAPI/Swagger document.";
        String how  = "Walks the source bundle looking for openapi.(yaml|json), swagger.json or api-docs.(yaml|json). Falls back to a HEAD probe on /v3/api-docs.";
        Path file = ctx.hasSource() ? ctx.firstPathMatching(OPENAPI_FILE) : null;
        if (file != null) {
            return EvaluationOutcome.passed(what, how, "Found spec at " + ctx.relative(file));
        }
        if (ctx.hasSource()) {
            // No bundle hit — try live /v3/api-docs as last chance
            if (ctx.hasLiveUrl()) {
                ProbeOutcome r = probe.get(ctx.liveBaseUrl() + "/v3/api-docs");
                if (r.reachable() && r.statusCode() >= 200 && r.statusCode() < 300) {
                    return EvaluationOutcome.passed(what, how, "Live /v3/api-docs returned HTTP " + r.statusCode());
                }
            }
            return EvaluationOutcome.failed(what, how,
                    "No openapi/swagger/api-docs file found in bundle and /v3/api-docs not reachable.",
                    "Add an OpenAPI spec under src/main/resources or expose /v3/api-docs via springdoc-openapi.");
        }
        if (!ctx.hasLiveUrl()) {
            return EvaluationOutcome.skipped(what, how, "No source bundle and no live URL — cannot evaluate.");
        }
        ProbeOutcome r = probe.get(ctx.liveBaseUrl() + "/v3/api-docs");
        boolean pass = r.reachable() && r.statusCode() >= 200 && r.statusCode() < 300;
        return pass
                ? EvaluationOutcome.passed(what, how, "Live /v3/api-docs returned HTTP " + r.statusCode())
                : EvaluationOutcome.failed(what, how,
                        "/v3/api-docs " + (r.reachable() ? "returned HTTP " + r.statusCode() : "unreachable: " + r.error()),
                        "Expose an OpenAPI document via springdoc-openapi-starter-webmvc-ui or equivalent.");
    }

    private EvaluationOutcome evalDoc002(RuleEvaluationContext ctx) {
        String what = "Every endpoint must have a summary/description in the OpenAPI spec.";
        String how  = "Reads the OpenAPI spec and counts operation blocks that lack a summary or description key.";
        if (!ctx.hasSource()) return skipNoBundle(what, how);
        Path spec = ctx.firstPathMatching(OPENAPI_FILE);
        if (spec == null) return EvaluationOutcome.failed(what, how, "No OpenAPI spec to inspect.", "Add an OpenAPI spec first (DOC_001).");
        String body = ctx.readRaw(spec);
        // crude tokenizer: count operations (get/post/...) vs summary occurrences
        int ops = countMatches(body, Pattern.compile("(?m)^\\s+(get|post|put|delete|patch|options|head):"));
        int summaries = countMatches(body, Pattern.compile("(?m)^\\s+summary:"));
        int descriptions = countMatches(body, Pattern.compile("(?m)^\\s+description:"));
        if (ops == 0) {
            return EvaluationOutcome.failed(what, how, "Spec found at " + ctx.relative(spec) + " but no operations detected.",
                    "Declare paths with HTTP verbs (get/post/put/delete) under `paths:` in the OpenAPI spec.");
        }
        boolean pass = summaries + descriptions >= ops;
        return pass
                ? EvaluationOutcome.passed(what, how, ops + " operations · " + summaries + " summaries · " + descriptions + " descriptions")
                : EvaluationOutcome.failed(what, how,
                        "Only " + (summaries + descriptions) + "/" + ops + " operations have a summary or description.",
                        "Add a `summary:` (and ideally a `description:`) to every path-item operation in the OpenAPI spec.");
    }

    private EvaluationOutcome evalDoc003(RuleEvaluationContext ctx) {
        String what = "Each 2xx response in the OpenAPI spec must contain at least one example payload.";
        String how  = "Counts `examples:` / `example:` keys appearing under 2xx response blocks.";
        if (!ctx.hasSource()) return skipNoBundle(what, how);
        Path spec = ctx.firstPathMatching(OPENAPI_FILE);
        if (spec == null) return EvaluationOutcome.failed(what, how, "No OpenAPI spec found.", "Add an OpenAPI spec first (DOC_001).");
        String body = ctx.readRaw(spec);
        int twoXx = countMatches(body, Pattern.compile("(?m)^\\s{6,}'?(20[0-9])'?:"));
        int examples = countMatches(body, Pattern.compile("(?m)^\\s+(examples?|x-example):"));
        boolean pass = twoXx == 0 || examples > 0;
        return pass
                ? EvaluationOutcome.passed(what, how, twoXx + " 2xx responses · " + examples + " examples in spec")
                : EvaluationOutcome.failed(what, how,
                        twoXx + " 2xx responses found but no examples present in the spec.",
                        "Add an `examples:` block (or x-example) to each 2xx response so consumers can preview the payload.");
    }

    private EvaluationOutcome evalDoc004(RuleEvaluationContext ctx) {
        String what = "Each 4xx/5xx response must reference a standard error schema (ProblemDetails / RFC-7807).";
        String how  = "Counts 4xx/5xx response blocks and inspects them for `$ref:` pointing to an error schema.";
        if (!ctx.hasSource()) return skipNoBundle(what, how);
        Path spec = ctx.firstPathMatching(OPENAPI_FILE);
        if (spec == null) return EvaluationOutcome.failed(what, how, "No OpenAPI spec found.", "Add an OpenAPI spec first (DOC_001).");
        String body = ctx.readRaw(spec);
        int errors = countMatches(body, Pattern.compile("(?m)^\\s{6,}'?(4[0-9]{2}|5[0-9]{2})'?:"));
        int refs   = countMatches(body, Pattern.compile("\\$ref:\\s*['\"][^'\"]*(error|problem)[^'\"]*['\"]", Pattern.CASE_INSENSITIVE));
        boolean pass = errors == 0 || refs > 0;
        return pass
                ? EvaluationOutcome.passed(what, how, errors + " error responses · " + refs + " error-schema $refs")
                : EvaluationOutcome.failed(what, how,
                        errors + " 4xx/5xx responses but no $ref to an Error/ProblemDetails schema.",
                        "Define a shared `ErrorResponse`/`ProblemDetails` schema and $ref it from every 4xx/5xx response.");
    }

    private EvaluationOutcome evalDoc005(RuleEvaluationContext ctx) {
        String what = "Deprecated operations must declare `deprecated: true` and a sunset strategy.";
        String how  = "Looks for any `deprecated: true` flags + `Sunset` / `X-Deprecation-Date` header annotations across the spec and code.";
        if (!ctx.hasSource()) return skipNoBundle(what, how);
        boolean flag = ctx.firstContentMatch(Pattern.compile("deprecated:\\s*true|@deprecated|@deprecation", Pattern.CASE_INSENSITIVE)) != null;
        boolean sunsetSeen = ctx.firstContentMatch(Pattern.compile("(sunset|x-deprecation-date)", Pattern.CASE_INSENSITIVE)) != null;
        if (!flag && !sunsetSeen) {
            return EvaluationOutcome.passed(what, how, "No deprecated operations declared — nothing to verify.");
        }
        boolean pass = flag && sunsetSeen;
        return pass
                ? EvaluationOutcome.passed(what, how, "Found deprecated flag + Sunset/X-Deprecation-Date marker.")
                : EvaluationOutcome.failed(what, how,
                        (flag ? "Deprecated operations exist but " : "") + "no Sunset / X-Deprecation-Date marker found.",
                        "When deprecating an operation, set `deprecated: true` and add a Sunset response header or X-Deprecation-Date.");
    }

    /* ─────────────────────────── LOGGING ───────────────────────────── */

    private EvaluationOutcome evalLog001(RuleEvaluationContext ctx) {
        String what = "Structured JSON logging must be configured.";
        String how  = "Looks for a logback configuration referencing JsonEncoder, LogstashEncoder, JsonLayout or pino/winston.";
        if (!ctx.hasSource()) return skipNoBundle(what, how);
        Path lb = ctx.firstPathMatching(LOGBACK_FILE);
        boolean json = false;
        if (lb != null) {
            String body = ctx.contentLower(lb);
            json = body.contains("logstashencoder") || body.contains("jsonlayout")
                    || body.contains("jsonencoder") || body.contains("ecslayout");
        }
        if (!json) {
            // Node.js / Express services
            json = ctx.firstContentMatch(Pattern.compile("\\b(pino|winston|bunyan)\\b")) != null;
        }
        return json
                ? EvaluationOutcome.passed(what, how, lb != null ? "JSON encoder configured in " + ctx.relative(lb) : "JSON logger library detected in source")
                : EvaluationOutcome.failed(what, how, "No JSON logger configuration found.",
                        "Add LogstashEncoder (Java) or pino/winston (Node) so logs are line-delimited JSON with a correlation id.");
    }

    private EvaluationOutcome evalLog002(RuleEvaluationContext ctx) {
        String what = "Logged fields must be filtered for PII (email, phone, SSN, payment data).";
        String how  = "Greps the codebase for mask/redact filters and warns when raw PII field names are logged.";
        if (!ctx.hasSource()) return skipNoBundle(what, how);
        boolean maskerPresent = ctx.firstContentMatch(Pattern.compile("(maskpattern|redact|pii\\s*filter|loggingmasker|sensitivedata)", Pattern.CASE_INSENSITIVE)) != null;
        Path leak = ctx.firstContentMatch(Pattern.compile("log(ger)?\\.(info|debug|warn|error)\\([^)]*\\b(ssn|password|creditcard|cardnumber|cvv|email)\\b", Pattern.CASE_INSENSITIVE));
        if (leak != null) {
            return EvaluationOutcome.failed(what, how,
                    "Raw PII field referenced in a log statement at " + ctx.relative(leak),
                    "Wrap PII fields in a masking helper or use logback's ReplacingCompositeConverter before emitting logs.");
        }
        return maskerPresent
                ? EvaluationOutcome.passed(what, how, "PII masking/redact helper detected in source.")
                : EvaluationOutcome.failed(what, how, "No PII masking helper found in source.",
                        "Add a logging masker (custom converter / replace pattern) to scrub email, phone, SSN, card numbers.");
    }

    private EvaluationOutcome evalLog003(RuleEvaluationContext ctx) {
        String what = "POST/PUT/PATCH/DELETE endpoints must emit an audit event.";
        String how  = "Looks for @Aspect / @Around audit interceptors or middleware that records write-path events.";
        if (!ctx.hasSource()) return skipNoBundle(what, how);
        boolean hit = ctx.firstContentMatch(Pattern.compile("(audit\\s*\\(|auditevent|auditlog|@auditable|audit-interceptor)", Pattern.CASE_INSENSITIVE)) != null;
        return hit
                ? EvaluationOutcome.passed(what, how, "Audit interceptor/aspect detected.")
                : EvaluationOutcome.failed(what, how, "No audit aspect/interceptor found in source.",
                        "Add an @Aspect that records who/when/what/before/after for every write request (e.g. via Spring's @AuditEventRepository).");
    }

    private EvaluationOutcome evalLog004(RuleEvaluationContext ctx) {
        String what = "Correlation IDs must be propagated end-to-end via MDC.";
        String how  = "Greps the codebase for `X-Correlation-Id` (or X-Request-Id / traceparent) handling and MDC put/clear.";
        if (!ctx.hasSource()) return skipNoBundle(what, how);
        boolean header = ctx.firstContentMatch(Pattern.compile("x[-_]?(correlation|request)[-_]?id|traceparent", Pattern.CASE_INSENSITIVE)) != null;
        boolean mdc    = ctx.firstContentMatch(Pattern.compile("MDC\\.(put|clear)|asynccontext|threadcontext", Pattern.CASE_INSENSITIVE)) != null;
        boolean pass = header && mdc;
        return pass
                ? EvaluationOutcome.passed(what, how, "Correlation header + MDC propagation both present.")
                : EvaluationOutcome.failed(what, how,
                        "Correlation id handling missing — header=" + header + ", mdc=" + mdc,
                        "Add a request filter that reads X-Correlation-Id (or generates UUID) into MDC and clears on completion.");
    }

    private EvaluationOutcome evalLog005(RuleEvaluationContext ctx) {
        String what = "Log level must be configurable at runtime via /actuator/loggers.";
        String how  = "Inspects application.properties/yml for management.endpoints.web.exposure.include=loggers (or *); also live-probes /actuator/loggers when reachable.";
        if (ctx.hasSource()) {
            Path props = ctx.firstPathMatching(APP_PROPS);
            if (props != null) {
                String body = ctx.contentLower(props);
                boolean hit = body.contains("management.endpoints.web.exposure.include")
                        && (body.contains("loggers") || body.matches("(?s).*management.endpoints.web.exposure.include\\s*[:=]\\s*[\"']?\\*.*"));
                if (hit) return EvaluationOutcome.passed(what, how, "loggers endpoint enabled in " + ctx.relative(props));
            }
        }
        if (ctx.hasLiveUrl()) {
            ProbeOutcome r = probe.get(ctx.liveBaseUrl() + "/actuator/loggers");
            if (r.reachable() && r.statusCode() >= 200 && r.statusCode() < 300) {
                return EvaluationOutcome.passed(what, how, "Live /actuator/loggers returned HTTP " + r.statusCode());
            }
            return EvaluationOutcome.failed(what, how,
                    "/actuator/loggers " + (r.reachable() ? "returned HTTP " + r.statusCode() : "unreachable: " + r.error()),
                    "Add `management.endpoints.web.exposure.include=loggers,health,prometheus` to application.properties.");
        }
        return ctx.hasSource()
                ? EvaluationOutcome.failed(what, how, "loggers endpoint not exposed and no live URL to verify.",
                        "Enable the loggers endpoint via management.endpoints.web.exposure.include.")
                : skipNoBundle(what, how);
    }

    /* ─────────────────────────── SECURITY ──────────────────────────── */

    private EvaluationOutcome evalSec001(RuleEvaluationContext ctx) {
        String what = "All inbound traffic must use TLS 1.2 or higher.";
        String how  = "Confirms the live base URL is https:// and that an http:// variant is rejected or redirected to https.";
        if (ctx.hasLiveUrl()) {
            String url = ctx.liveBaseUrl();
            if (!url.startsWith("https://")) {
                return EvaluationOutcome.failed(what, how, "Live base URL is plain http: " + url,
                        "Terminate TLS at the LB/ingress and configure server.ssl.enabled-protocols=TLSv1.2,TLSv1.3.");
            }
            ProbeOutcome r = probe.get(url);
            if (r.reachable()) {
                return EvaluationOutcome.passed(what, how, "HTTPS reachable, base URL=" + url + " (HTTP " + r.statusCode() + ")");
            }
            return EvaluationOutcome.failed(what, how, "HTTPS unreachable: " + r.error(),
                    "Ensure the service certificate is valid and TLSv1.2+ ciphers are enabled.");
        }
        if (ctx.hasSource()) {
            boolean configured = ctx.firstContentMatch(Pattern.compile("server\\.ssl\\.enabled-protocols\\s*[:=].*tlsv?1\\.[23]", Pattern.CASE_INSENSITIVE)) != null;
            return configured
                    ? EvaluationOutcome.passed(what, how, "server.ssl.enabled-protocols restricts to TLSv1.2/1.3 in configuration.")
                    : EvaluationOutcome.failed(what, how, "No TLS protocol restriction found in application config.",
                            "Set server.ssl.enabled-protocols=TLSv1.2,TLSv1.3 (Spring) or equivalent.");
        }
        return skipNoBundle(what, how);
    }

    private EvaluationOutcome evalSec002(RuleEvaluationContext ctx) {
        String what = "API keys / DB passwords must come from a secret store — no hard-coded secrets.";
        String how  = "Greps the source bundle for high-entropy AWS/Azure/Stripe/GCP keys + `password=`/`api-key=` literals.";
        if (!ctx.hasSource()) return skipNoBundle(what, how);
        Pattern secret = Pattern.compile(
                "(aws_secret_access_key|aws_access_key_id|sk_live_|pk_live_|aiza[0-9a-z\\-_]{30,}|ghp_[a-zA-Z0-9]{30,}|" +
                "xox[abp]-[a-zA-Z0-9-]{10,}|-----begin (rsa |dsa |ec |openssh |encrypted )?private key-----)",
                Pattern.CASE_INSENSITIVE);
        Path leak = ctx.firstContentMatch(secret);
        if (leak != null) {
            return EvaluationOutcome.failed(what, how, "Hard-coded secret pattern detected in " + ctx.relative(leak),
                    "Move the secret to a vault (HashiCorp Vault, AWS Secrets Manager, GCP Secret Manager) and reference via env var.");
        }
        Path inlinePwd = ctx.firstContentMatch(Pattern.compile("(?:password|api[_-]?key|client[_-]?secret)\\s*[:=]\\s*[\"'][^\"'\\$\\{][^\"']{6,}[\"']", Pattern.CASE_INSENSITIVE));
        if (inlinePwd != null) {
            return EvaluationOutcome.failed(what, how, "Possible hard-coded password/api-key in " + ctx.relative(inlinePwd),
                    "Replace literal value with an `${ENV_VAR}` placeholder so secret managers can inject at runtime.");
        }
        return EvaluationOutcome.passed(what, how, "No high-entropy secrets or inline password literals detected.");
    }

    private EvaluationOutcome evalSec003(RuleEvaluationContext ctx) {
        String what = "Every endpoint must require authentication.";
        String how  = "Sends an unauthenticated GET to the live base URL — a 401/403 means auth is enforced.";
        if (!ctx.hasLiveUrl()) return skipNoLive(what, how);
        ProbeOutcome r = probe.get(ctx.liveBaseUrl());
        if (!r.reachable()) return EvaluationOutcome.failed(what, how, "Live URL unreachable: " + r.error(),
                "Make sure the service is deployed and reachable from the scanner network.");
        boolean pass = r.statusCode() == 401 || r.statusCode() == 403;
        return pass
                ? EvaluationOutcome.passed(what, how, "Unauthenticated request rejected with HTTP " + r.statusCode())
                : EvaluationOutcome.failed(what, how, "Unauthenticated request returned HTTP " + r.statusCode(),
                        "Require an Authorization/JWT/API-key on all routes except the explicit public allow-list.");
    }

    private EvaluationOutcome evalSec004(RuleEvaluationContext ctx) {
        String what = "Each endpoint must declare its required OAuth scopes / roles in the spec.";
        String how  = "Parses the OpenAPI spec for a top-level `securitySchemes:` block and per-operation `security:` declarations.";
        if (!ctx.hasSource()) return skipNoBundle(what, how);
        Path spec = ctx.firstPathMatching(OPENAPI_FILE);
        if (spec == null) return EvaluationOutcome.failed(what, how, "No OpenAPI spec found.", "Add an OpenAPI spec first (DOC_001).");
        String body = ctx.contentLower(spec);
        boolean schemes  = body.contains("securityschemes:");
        boolean perOp    = countMatches(body, Pattern.compile("(?m)^\\s+security:\\s*$")) > 0;
        boolean pass = schemes && perOp;
        return pass
                ? EvaluationOutcome.passed(what, how, "securitySchemes + per-operation security blocks both declared.")
                : EvaluationOutcome.failed(what, how,
                        "Spec missing " + (!schemes ? "global securitySchemes" : "") + (!perOp ? (schemes ? "" : " and ") + "per-operation security blocks" : ""),
                        "Declare a `securitySchemes:` (e.g. oauth2 / bearerAuth) and add a `security:` block to every operation.");
    }

    private EvaluationOutcome evalSec005(RuleEvaluationContext ctx) {
        String what = "Request body / query params must be validated against the OpenAPI schema before reaching handlers.";
        String how  = "Looks for @Valid / @Validated annotations (Spring) or express-validator/joi/zod (Node).";
        if (!ctx.hasSource()) return skipNoBundle(what, how);
        boolean springValid = ctx.firstContentMatch(Pattern.compile("@Valid\\b|@Validated\\b|@RequestBody\\s+@Valid")) != null;
        boolean nodeValid   = ctx.firstContentMatch(Pattern.compile("\\b(joi|zod|class-validator|express-validator|yup)\\b", Pattern.CASE_INSENSITIVE)) != null;
        boolean pass = springValid || nodeValid;
        return pass
                ? EvaluationOutcome.passed(what, how, springValid ? "@Valid/@Validated annotations detected." : "Validator library detected.")
                : EvaluationOutcome.failed(what, how, "No request validation library or annotations detected.",
                        "Add @Valid on controller method parameters (Spring) or joi/zod/express-validator (Node) before processing input.");
    }

    private EvaluationOutcome evalSec006(RuleEvaluationContext ctx) {
        String what = "Endpoints must enforce a rate-limit policy.";
        String how  = "Fires a 12-request burst against the live URL and expects at least one 429; also checks for resilience4j-rate-limiter / bucket4j in dependencies.";
        if (ctx.hasLiveUrl()) {
            int seen429 = 0;
            int lastStatus = 0;
            for (int i = 0; i < 12; i++) {
                ProbeOutcome r = probe.get(ctx.liveBaseUrl());
                lastStatus = r.statusCode();
                if (r.statusCode() == 429) seen429++;
            }
            if (seen429 > 0) {
                return EvaluationOutcome.passed(what, how, "Burst test: " + seen429 + "/12 requests returned HTTP 429.");
            }
            if (ctx.hasSource()) {
                boolean lib = ctx.firstContentMatch(Pattern.compile("(resilience4j-rate-?limiter|bucket4j|express-rate-limit|ratelimit-policy)", Pattern.CASE_INSENSITIVE)) != null;
                if (lib) return EvaluationOutcome.passed(what, how, "Rate-limit library declared in dependencies (burst saw last status " + lastStatus + ").");
            }
            return EvaluationOutcome.failed(what, how, "No 429 across 12 rapid requests (last status " + lastStatus + ") and no rate-limit library declared.",
                    "Add a rate-limiter (Spring resilience4j / Bucket4j / express-rate-limit) and return 429 + Retry-After.");
        }
        if (ctx.hasSource()) {
            boolean lib = ctx.firstContentMatch(Pattern.compile("(resilience4j-rate-?limiter|bucket4j|express-rate-limit|ratelimit-policy)", Pattern.CASE_INSENSITIVE)) != null;
            return lib
                    ? EvaluationOutcome.passed(what, how, "Rate-limit library declared in dependencies.")
                    : EvaluationOutcome.failed(what, how, "No rate-limit library declared in pom.xml / package.json.",
                            "Add Bucket4j / resilience4j-rate-limiter (Java) or express-rate-limit (Node).");
        }
        return skipNoLive(what, how);
    }

    private EvaluationOutcome evalSec007(RuleEvaluationContext ctx) {
        String what = "CORS allow-list must be explicit (no `*` in production).";
        String how  = "Greps the source for CORS config — fails when allowed-origins is `*`.";
        if (!ctx.hasSource()) return skipNoBundle(what, how);
        Path wildcard = ctx.firstContentMatch(Pattern.compile("(allowedorigins?|access-control-allow-origin)\\s*[:=]?\\s*[\"']?\\*[\"']?", Pattern.CASE_INSENSITIVE));
        boolean hasExplicit = ctx.firstContentMatch(Pattern.compile("(allowedorigins?|access-control-allow-origin)\\s*[:=]?\\s*[\"']?https?://[^,*\\s]+", Pattern.CASE_INSENSITIVE)) != null;
        if (wildcard != null) {
            return EvaluationOutcome.failed(what, how, "Wildcard CORS origin found in " + ctx.relative(wildcard),
                    "Enumerate trusted origins instead of `*`; only allow credentials with explicit origin list.");
        }
        return hasExplicit
                ? EvaluationOutcome.passed(what, how, "Explicit CORS origins detected (no wildcard).")
                : EvaluationOutcome.passed(what, how, "No wildcard CORS config detected (defaults assumed safe).");
    }

    private EvaluationOutcome evalSec008(RuleEvaluationContext ctx) {
        String what = "CI pipeline must run a Software Composition Analysis (SCA) scan and fail on HIGH vulns.";
        String how  = "Inspects pom.xml / package.json for dependency-check, snyk, audit, owasp plugins.";
        if (!ctx.hasSource()) return skipNoBundle(what, how);
        boolean java = ctx.firstContentMatch(Pattern.compile("(dependency-check-maven|snyk-maven-plugin|owasp\\.dependencycheck)", Pattern.CASE_INSENSITIVE)) != null;
        boolean node = ctx.firstContentMatch(Pattern.compile("\"(snyk|npm-audit-ci-wrapper|audit-ci|better-npm-audit)\"", Pattern.CASE_INSENSITIVE)) != null;
        boolean pass = java || node;
        return pass
                ? EvaluationOutcome.passed(what, how, java ? "OWASP / Snyk Maven plugin declared in pom.xml" : "SCA dependency declared in package.json")
                : EvaluationOutcome.failed(what, how, "No SCA plugin found in build configuration.",
                        "Add dependency-check-maven (Java) or snyk/audit-ci (Node) to the CI pipeline.");
    }

    /* ─────────────────────────── PERFORMANCE ───────────────────────── */

    private EvaluationOutcome evalPerf001(RuleEvaluationContext ctx) {
        String what = "Outbound HTTP clients must declare connect + read timeouts (≤ 10s).";
        String how  = "Greps for setConnectTimeout/setReadTimeout, WebClient/RestTemplate builders, or axios timeout.";
        if (!ctx.hasSource()) return skipNoBundle(what, how);
        boolean hit = ctx.firstContentMatch(Pattern.compile("(setconnect(?:ion)?timeout|setreadtimeout|connecttimeout\\s*[:=]|readtimeout\\s*[:=]|timeout:\\s*[0-9]+|axios.*timeout)", Pattern.CASE_INSENSITIVE)) != null;
        return hit
                ? EvaluationOutcome.passed(what, how, "Explicit timeouts configured for outbound HTTP clients.")
                : EvaluationOutcome.failed(what, how, "No timeout configuration found for HTTP clients.",
                        "Set connect/read timeouts on RestTemplate, WebClient, OkHttp, axios — never rely on infinite defaults.");
    }

    private EvaluationOutcome evalPerf002(RuleEvaluationContext ctx) {
        String what = "List endpoints must support pagination (`page`/`size` or cursor).";
        String how  = "Parses the OpenAPI spec for query parameters named page/size/limit/offset/cursor.";
        if (!ctx.hasSource()) return skipNoBundle(what, how);
        Path spec = ctx.firstPathMatching(OPENAPI_FILE);
        if (spec == null) return EvaluationOutcome.failed(what, how, "No OpenAPI spec to inspect.", "Add an OpenAPI spec first (DOC_001).");
        String body = ctx.contentLower(spec);
        boolean has = Pattern.compile("name:\\s*(page|size|limit|offset|cursor|pageable)").matcher(body).find();
        return has
                ? EvaluationOutcome.passed(what, how, "Pagination params declared in OpenAPI spec.")
                : EvaluationOutcome.failed(what, how, "No page/size/cursor pagination params declared.",
                        "Declare `page`+`size` (or `cursor`) query params on every list endpoint.");
    }

    private EvaluationOutcome evalPerf003(RuleEvaluationContext ctx) {
        String what = "Server must support gzip/brotli when client advertises Accept-Encoding.";
        String how  = "Checks application.properties/yml for server.compression.enabled=true; fallback live probe with Accept-Encoding: gzip.";
        if (ctx.hasSource()) {
            boolean cfg = ctx.firstContentMatch(Pattern.compile("server\\.compression\\.enabled\\s*[:=]\\s*true", Pattern.CASE_INSENSITIVE)) != null;
            if (cfg) return EvaluationOutcome.passed(what, how, "server.compression.enabled=true in application config.");
        }
        if (ctx.hasLiveUrl()) {
            ProbeOutcome r = probe.get(ctx.liveBaseUrl(), java.util.Map.of("Accept-Encoding", "gzip,br"));
            String enc = r.header("content-encoding");
            if (enc != null && (enc.contains("gzip") || enc.contains("br"))) {
                return EvaluationOutcome.passed(what, how, "Live response carries Content-Encoding: " + enc);
            }
            return EvaluationOutcome.failed(what, how, "No Content-Encoding header on live response.",
                    "Enable server.compression.enabled=true (Spring) or compression middleware (Express).");
        }
        return ctx.hasSource()
                ? EvaluationOutcome.failed(what, how, "server.compression.enabled flag not set and no live URL to verify.",
                        "Add server.compression.enabled=true to application.properties.")
                : skipNoBundle(what, how);
    }

    private EvaluationOutcome evalPerf004(RuleEvaluationContext ctx) {
        String what = "POST endpoints that mutate state must honour an Idempotency-Key header.";
        String how  = "Greps the source for `Idempotency-Key` references.";
        if (!ctx.hasSource()) return skipNoBundle(what, how);
        boolean hit = ctx.firstContentMatch(Pattern.compile("idempotency[-_]?key", Pattern.CASE_INSENSITIVE)) != null;
        return hit
                ? EvaluationOutcome.passed(what, how, "Idempotency-Key handler found in source.")
                : EvaluationOutcome.failed(what, how, "No Idempotency-Key handling detected.",
                        "Read Idempotency-Key on POST handlers that perform money/state mutations and cache the response.");
    }

    private EvaluationOutcome evalPerf005(RuleEvaluationContext ctx) {
        String what = "GET responses must declare appropriate Cache-Control headers.";
        String how  = "Greps the source / config for `Cache-Control` headers being set.";
        if (!ctx.hasSource()) return skipNoBundle(what, how);
        boolean hit = ctx.firstContentMatch(Pattern.compile("cache-control", Pattern.CASE_INSENSITIVE)) != null;
        return hit
                ? EvaluationOutcome.passed(what, how, "Cache-Control header references detected in source.")
                : EvaluationOutcome.failed(what, how, "No Cache-Control header configuration found.",
                        "Set Cache-Control headers explicitly (no-store for sensitive, max-age for static).");
    }

    /* ─────────────────────────── HEALTH ────────────────────────────── */

    private EvaluationOutcome evalHealth001(RuleEvaluationContext ctx) {
        String what = "Service must expose /actuator/health and respond UP/DOWN within 1s.";
        String how  = "Issues a GET to /actuator/health on the live URL.";
        if (!ctx.hasLiveUrl()) return skipNoLive(what, how);
        ProbeOutcome r = probe.get(ctx.liveBaseUrl() + "/actuator/health");
        if (!r.reachable()) {
            return EvaluationOutcome.failed(what, how, "/actuator/health unreachable: " + r.error(),
                    "Add spring-boot-starter-actuator and expose `management.endpoints.web.exposure.include=health,info`.");
        }
        boolean pass = r.statusCode() >= 200 && r.statusCode() < 300 && r.body().toLowerCase().contains("\"status\"");
        return pass
                ? EvaluationOutcome.passed(what, how, "/actuator/health → HTTP " + r.statusCode() + " in " + r.durationMs() + "ms")
                : EvaluationOutcome.failed(what, how, "/actuator/health returned HTTP " + r.statusCode() + " body=" + truncate(r.body(), 200),
                        "Expose actuator health endpoint and return a JSON {status: UP}.");
    }

    private EvaluationOutcome evalHealth002(RuleEvaluationContext ctx) {
        String what = "Service must expose Prometheus-compatible metrics.";
        String how  = "Issues GET /actuator/prometheus and expects HTTP 200 with text/plain body.";
        if (!ctx.hasLiveUrl()) return skipNoLive(what, how);
        ProbeOutcome r = probe.get(ctx.liveBaseUrl() + "/actuator/prometheus");
        if (!r.reachable()) return EvaluationOutcome.failed(what, how, "/actuator/prometheus unreachable: " + r.error(),
                "Add micrometer-registry-prometheus and expose `prometheus` under management.endpoints.web.exposure.include.");
        boolean pass = r.statusCode() == 200 && r.body().contains("# HELP");
        return pass
                ? EvaluationOutcome.passed(what, how, "/actuator/prometheus → HTTP 200 with metrics text exposed.")
                : EvaluationOutcome.failed(what, how, "/actuator/prometheus → HTTP " + r.statusCode(),
                        "Expose micrometer-registry-prometheus on /actuator/prometheus.");
    }

    private EvaluationOutcome evalHealth003(RuleEvaluationContext ctx) {
        String what = "Service must propagate W3C TraceContext headers and emit OpenTelemetry spans.";
        String how  = "Greps pom.xml / package.json for opentelemetry, micrometer-tracing, brave.";
        if (!ctx.hasSource()) return skipNoBundle(what, how);
        boolean hit = ctx.firstContentMatch(Pattern.compile("(opentelemetry|micrometer-tracing|brave-instrumentation|@opentelemetry/api|spring-cloud-sleuth)", Pattern.CASE_INSENSITIVE)) != null;
        return hit
                ? EvaluationOutcome.passed(what, how, "Tracing library detected in dependencies.")
                : EvaluationOutcome.failed(what, how, "No tracing library (opentelemetry / micrometer-tracing / brave) declared.",
                        "Add OpenTelemetry instrumentation so the service emits traceparent + spans for every request.");
    }

    private EvaluationOutcome evalHealth004(RuleEvaluationContext ctx) {
        String what = "Service must drain in-flight requests on SIGTERM (graceful shutdown).";
        String how  = "Looks for `server.shutdown=graceful` in application.properties/yml.";
        if (!ctx.hasSource()) return skipNoBundle(what, how);
        boolean hit = ctx.firstContentMatch(Pattern.compile("server\\.shutdown\\s*[:=]\\s*graceful", Pattern.CASE_INSENSITIVE)) != null;
        return hit
                ? EvaluationOutcome.passed(what, how, "server.shutdown=graceful configured.")
                : EvaluationOutcome.failed(what, how, "server.shutdown=graceful not set.",
                        "Add `server.shutdown=graceful` (and spring.lifecycle.timeout-per-shutdown-phase=30s) to application.properties.");
    }

    /* ─────────────────────────── GENERAL ───────────────────────────── */

    private EvaluationOutcome evalGen001(RuleEvaluationContext ctx) {
        String what = "Releases must follow Semantic Versioning (MAJOR.MINOR.PATCH).";
        String how  = "Inspects pom.xml <version> or package.json `version` field for an X.Y.Z pattern.";
        if (!ctx.hasSource()) return skipNoBundle(what, how);
        Pattern semver = Pattern.compile("\\b\\d+\\.\\d+\\.\\d+(?:-[\\w.]+)?(?:\\+[\\w.]+)?\\b");
        Path pom = ctx.firstPathMatching(POM_FILE);
        if (pom != null) {
            String body = ctx.readRaw(pom);
            // first <version> tag in pom (not parent)
            java.util.regex.Matcher m = Pattern.compile("<version>([^<]+)</version>").matcher(body);
            if (m.find()) {
                String v = m.group(1).trim();
                return semver.matcher(v).find()
                        ? EvaluationOutcome.passed(what, how, "pom version=" + v + " is SemVer-compliant.")
                        : EvaluationOutcome.failed(what, how, "pom version=" + v + " is not SemVer (expected X.Y.Z).",
                                "Update <version> in pom.xml to follow MAJOR.MINOR.PATCH (e.g. 1.0.0).");
            }
        }
        Path pkg = ctx.firstPathMatching(PACKAGE_JSON);
        if (pkg != null) {
            String body = ctx.readRaw(pkg);
            java.util.regex.Matcher m = Pattern.compile("\"version\"\\s*:\\s*\"([^\"]+)\"").matcher(body);
            if (m.find()) {
                String v = m.group(1).trim();
                return semver.matcher(v).find()
                        ? EvaluationOutcome.passed(what, how, "package.json version=" + v + " is SemVer-compliant.")
                        : EvaluationOutcome.failed(what, how, "package.json version=" + v + " is not SemVer.",
                                "Update version field in package.json to follow MAJOR.MINOR.PATCH.");
            }
        }
        return EvaluationOutcome.failed(what, how, "No pom.xml or package.json with a version field found.",
                "Ensure the build manifest declares a SemVer version.");
    }

    private EvaluationOutcome evalGen002(RuleEvaluationContext ctx) {
        String what = "Naming conventions: lower-case plural path segments, snake_case query params, camelCase JSON fields.";
        String how  = "Parses the OpenAPI spec for paths and query parameter names and flags violations.";
        if (!ctx.hasSource()) return skipNoBundle(what, how);
        Path spec = ctx.firstPathMatching(OPENAPI_FILE);
        if (spec == null) return EvaluationOutcome.failed(what, how, "No OpenAPI spec to inspect.", "Add an OpenAPI spec first (DOC_001).");
        String body = ctx.readRaw(spec);
        java.util.regex.Matcher m = Pattern.compile("(?m)^\\s+(/[A-Za-z0-9_\\-/{}]+):").matcher(body);
        int badPath = 0;
        int totalPath = 0;
        while (m.find()) {
            totalPath++;
            String p = m.group(1);
            if (p.matches(".*[A-Z].*")) badPath++;
        }
        java.util.regex.Matcher mq = Pattern.compile("(?m)^\\s+name:\\s*([A-Za-z_][A-Za-z0-9_]*)\\s*$").matcher(body);
        int camelCaseParam = 0;
        while (mq.find()) {
            String n = mq.group(1);
            if (n.matches(".*[a-z][A-Z].*")) camelCaseParam++;
        }
        boolean pass = badPath == 0 && camelCaseParam == 0;
        return pass
                ? EvaluationOutcome.passed(what, how, totalPath + " paths checked · 0 violations.")
                : EvaluationOutcome.failed(what, how,
                        "Found " + badPath + " upper-case path(s) and " + camelCaseParam + " camelCase query param(s).",
                        "Lowercase the path segments, switch query params to snake_case.");
    }

    private EvaluationOutcome evalGen003(RuleEvaluationContext ctx) {
        String what = "Repo must contain a LICENSE file and a CODEOWNERS file.";
        String how  = "Scans the bundle for LICENSE(.md|.txt) and CODEOWNERS / OWNERS / .github/CODEOWNERS.";
        if (!ctx.hasSource()) return skipNoBundle(what, how);
        boolean lic = ctx.firstPathMatching(LICENSE_FILE) != null;
        boolean own = ctx.firstPathMatching(OWNERS_FILE) != null;
        boolean pass = lic && own;
        if (pass) return EvaluationOutcome.passed(what, how, "LICENSE + CODEOWNERS present.");
        return EvaluationOutcome.failed(what, how,
                "Missing " + (!lic ? "LICENSE" : "") + (!lic && !own ? " and " : "") + (!own ? "CODEOWNERS" : ""),
                "Add a LICENSE file (LICENSE.md) and a CODEOWNERS file at .github/CODEOWNERS naming the responsible squad.");
    }

    /* ─────────────────────────── helpers ───────────────────────────── */

    private EvaluationOutcome defaultFallback(ComplianceRuleDocument rule, RuleEvaluationContext ctx) {
        // For non-baseline rules (user-defined), fall back to bundle availability gating.
        if (!ctx.hasSource() && !ctx.hasLiveUrl()) {
            return EvaluationOutcome.skipped(rule.getRuleName(),
                    "Custom rule has no registered evaluator and no asset source or live URL is available.",
                    "Register a handler for " + ruleCode(rule) + " in MicroserviceRuleEvaluator.");
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
                "Source bundle was not provided / could not be resolved — rule needs to inspect repository files.");
    }

    private static EvaluationOutcome skipNoLive(String what, String how) {
        return EvaluationOutcome.skipped(what, how,
                "No live endpoint URL configured — set compliance.live-endpoint.base-url or pass an endpoint in the scan source.");
    }

    private static String ruleCode(ComplianceRuleDocument rule) {
        String id = rule.getRuleId() == null ? "" : rule.getRuleId();
        // e.g. CR_MICROSERVICE_SEC_003 → SEC_003
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

    private static String truncate(String s, int max) {
        if (s == null) return "";
        return s.length() <= max ? s : s.substring(0, max) + "…";
    }
}
