package com.probestack.forgesphere.scanner.microservice;

import com.probestack.forgesphere.document.ComplianceRuleDocument;
import com.probestack.forgesphere.document.ComplianceScanDocument;
import com.probestack.forgesphere.model.ScanResult;
import com.probestack.forgesphere.model.ScanEvidence;
import com.probestack.forgesphere.model.ScanResultStatus;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.regex.Pattern;
import org.springframework.stereotype.Service;

@Service
public class MicroserviceComplianceScanner {

    private final MicroserviceSourceResolver sourceResolver;
    private final Map<String, Function<MicroserviceScanContext, RuleEvaluation>> checks;

    public MicroserviceComplianceScanner(MicroserviceSourceResolver sourceResolver) {
        this.sourceResolver = sourceResolver;
        this.checks = Map.ofEntries(
                entry("MICROSERVICE_ENDPOINT_DOCUMENTATION", this::endpointDocumentation),
                entry("MICROSERVICE_MAX_PAYLOAD_SIZE", contains("payload size", "max-request-size", "max-file-size", "max-http-request-header-size")),
                entry("MICROSERVICE_LIST_PAGINATION", contains("pageable", "page<", "limit", "offset", "cursor")),
                entry("MICROSERVICE_REQUEST_ID_LOGGING", contains("x-request-id", "correlation-id", "correlationid", "mdc.put")),
                entry("MICROSERVICE_REQUEST_RESPONSE_LOGGING", contains("onceperrequestfilter", "clienthttprequestinterceptor", "logbook", "request logging")),
                entry("MICROSERVICE_HEALTH_ENDPOINT", contains("actuator/health", "@getmapping(\"/health", "@requestmapping(\"/health", "management.endpoints.web.exposure")),
                entry("MICROSERVICE_VERSIONING_POLICY_DOCUMENTED", pathOrContent("version", "api version", "versioning")),
                entry("MICROSERVICE_API_VERSION_IN_PATH", pattern("/v[0-9]+(?:/|\\\")")),
                entry("MICROSERVICE_BREAKING_CHANGE_VERSION_BUMP", contains("breaking change", "major version", "semver")),
                entry("MICROSERVICE_OPENAPI_SPEC_AVAILABLE", this::openApiSpec),
                entry("MICROSERVICE_AUTH_REQUIRED", contains("securityfilterchain", "spring-boot-starter-security", "@preauthorize", "authenticated()")),
                entry("MICROSERVICE_CORS_CONFIGURED", contains("corsconfiguration", "@crossorigin", "allowedorigins", "allowedoriginpatterns")),
                entry("MICROSERVICE_NO_SENSITIVE_URL_DATA", this::sensitiveUrlData),
                entry("MICROSERVICE_TERMINATION_POLICY", contains("deprecation", "sunset", "api termination", "termination policy")),
                entry("MICROSERVICE_XSS_HEADERS", contains("content-security-policy", "x-xss-protection", "x-content-type-options", "httpheaders")),
                entry("MICROSERVICE_RATE_LIMITING_IMPLEMENTED", contains("bucket4j", "ratelimiter", "resilience4j.ratelimiter", "redis-rate-limiter")),
                entry("MICROSERVICE_SQL_INJECTION_PREVENTION", this::sqlInjectionPrevention),
                entry("MICROSERVICE_HTTPS_ONLY", contains("server.ssl", "requiressecure", "requireschannel", "x-forwarded-proto")),
                entry("MICROSERVICE_INPUT_VALIDATION", contains("@valid", "@validated", "@notnull", "@notblank", "@size", "@pattern")),
                entry("MICROSERVICE_CONTENT_TYPE_VALIDATION", contains("consumes =", "mediatype.application_json", "content-type", "unsupported_media_type")),
                entry("MICROSERVICE_JWT_VALIDATION", contains("jwt", "oauth2resourceserver", "bearer", "jsonwebtoken")),
                entry("MICROSERVICE_HTTP_STATUS_CODES", contains("responseentity", "@responsestatus", "httpstatus.", "problem detail")),
                entry("MICROSERVICE_HATEOAS_COMPLIANCE", contains("spring-boot-starter-hateoas", "entitymodel", "linkto", "representationmodel")),
                entry("MICROSERVICE_PLURAL_RESOURCE_NOUNS", this::pluralResourceNouns),
                entry("MICROSERVICE_CACHE_HEADERS", contains("cache-control", "cachecontrol", "etag", "last-modified")),
                entry("MICROSERVICE_COMPRESSION_ENABLED", contains("server.compression.enabled=true", "compression")),
                entry("MICROSERVICE_SEMANTIC_VERSIONING", contains("semver", "semantic version", "major.minor.patch", "version>")),
                entry("MICROSERVICE_NO_HARDCODED_CREDENTIALS", this::hardcodedCredentials),
                entry("MICROSERVICE_OAUTH2_DEFINED", contains("oauth2", "authorizationserver", "clientregistration", "authorization_code")),
                entry("MICROSERVICE_RATE_LIMITING_CONFIGURED", contains("rate limit", "ratelimiter", "bucket4j", "resilience4j.ratelimiter"))
        );
    }

    public List<ScanResult> scan(ComplianceScanDocument scan, List<ComplianceRuleDocument> rules) {
        SourceResolution sourceResolution = sourceResolver.resolve(scan.getScanId(), scan.getSourceType(), scan.getSource());
        if (!sourceResolution.resolved()) {
            return rules.stream()
                    .map(rule -> skipped(rule, sourceResolution.message()))
                    .toList();
        }

        MicroserviceScanContext context = MicroserviceScanContext.from(sourceResolution.sourcePath());
        return rules.stream()
                .map(rule -> evaluate(rule, context))
                .toList();
    }

    private ScanResult evaluate(ComplianceRuleDocument rule, MicroserviceScanContext context) {
        Function<MicroserviceScanContext, RuleEvaluation> check = checks.get(rule.getImplementationKey());
        if (check == null) {
            return skipped(rule, "No scanner executor is registered for " + rule.getImplementationKey() + ".");
        }
        RuleEvaluation evaluation = check.apply(context);
        return result(rule, evaluation.status(), evaluation.message(), evaluation.filePath());
    }

    private RuleEvaluation endpointDocumentation(MicroserviceScanContext context) {
        RuleEvaluation openApi = openApiSpec(context);
        if (openApi.status() == ScanResultStatus.PASSED && context.contains(Pattern.compile("responses\\s*:|@operation|@apiresponse"))) {
            return RuleEvaluation.passed("API documentation artifacts and response metadata were found.", openApi.filePath());
        }
        return RuleEvaluation.warning("OpenAPI documentation exists, but endpoint-level response metadata was not clearly detected.", openApi.filePath());
    }

    private RuleEvaluation openApiSpec(MicroserviceScanContext context) {
        Path match = context.firstPathMatch(Pattern.compile("(^|/)(openapi|swagger)\\.(ya?ml|json)$"));
        return match == null
                ? RuleEvaluation.failed("No OpenAPI or Swagger specification file was found.", null)
                : RuleEvaluation.passed("OpenAPI or Swagger specification file was found.", context.relative(match));
    }

    private RuleEvaluation sensitiveUrlData(MicroserviceScanContext context) {
        Pattern riskyUrlParam = Pattern.compile("(password|passwd|pwd|token|secret|apikey|api_key|ssn|pan)\\s*(=|,|\\))");
        Path match = context.firstMatch(riskyUrlParam);
        return match == null
                ? RuleEvaluation.passed("No obvious sensitive URL parameter names were detected.", null)
                : RuleEvaluation.failed("Potential sensitive parameter name detected in a request mapping or query handling path.", context.relative(match));
    }

    private RuleEvaluation sqlInjectionPrevention(MicroserviceScanContext context) {
        Pattern riskySql = Pattern.compile("(createStatement\\s*\\(|statement\\s+\\w+|@query\\s*\\([^)]*\\+|select\\s+.*\\+)");
        Path match = context.firstMatch(riskySql);
        return match == null
                ? RuleEvaluation.passed("No obvious raw SQL string concatenation pattern was detected.", null)
                : RuleEvaluation.failed("Potential raw SQL or string-concatenated query pattern detected.", context.relative(match));
    }

    private RuleEvaluation hardcodedCredentials(MicroserviceScanContext context) {
        Pattern credentialPattern = Pattern.compile("(password|passwd|pwd|secret|api[_-]?key|access[_-]?token)\\s*[:=]\\s*[\\\"'][^\\\"'${}]{8,}[\\\"']");
        Path match = context.firstMatch(credentialPattern);
        return match == null
                ? RuleEvaluation.passed("No obvious hardcoded credential pattern was detected.", null)
                : RuleEvaluation.failed("Potential hardcoded credential or secret value detected.", context.relative(match));
    }

    private RuleEvaluation pluralResourceNouns(MicroserviceScanContext context) {
        Path mapping = context.firstMatch(Pattern.compile("@(?:get|post|put|delete|patch|request)mapping\\s*\\(\\s*\\\"/[^\\\"/]+\\\""));
        return mapping == null
                ? RuleEvaluation.warning("No simple singular resource mapping pattern was detected; verify route naming in API review.", null)
                : RuleEvaluation.warning("Potential singular top-level resource path detected. Confirm endpoint nouns are plural.", context.relative(mapping));
    }

    private Function<MicroserviceScanContext, RuleEvaluation> contains(String... needles) {
        return context -> {
            for (String needle : needles) {
                Path match = context.firstMatch(Pattern.compile(Pattern.quote(needle.toLowerCase())));
                if (match != null) {
                    return RuleEvaluation.passed("Detected evidence for " + needle + ".", context.relative(match));
                }
            }
            return RuleEvaluation.failed("No matching implementation evidence was detected.");
        };
    }

    private Function<MicroserviceScanContext, RuleEvaluation> pathOrContent(String pathNeedle, String... contentNeedles) {
        return context -> {
            Path pathMatch = context.firstPathMatch(Pattern.compile(Pattern.quote(pathNeedle.toLowerCase())));
            if (pathMatch != null) {
                return RuleEvaluation.passed("Detected supporting file path for " + pathNeedle + ".", context.relative(pathMatch));
            }
            return contains(contentNeedles).apply(context);
        };
    }

    private Function<MicroserviceScanContext, RuleEvaluation> pattern(String regex) {
        Pattern pattern = Pattern.compile(regex);
        return context -> {
            Path match = context.firstMatch(pattern);
            return match == null
                    ? RuleEvaluation.failed("No matching implementation evidence was detected.")
                    : RuleEvaluation.passed("Detected matching implementation evidence.", context.relative(match));
        };
    }

    private ScanResult skipped(ComplianceRuleDocument rule, String message) {
        return result(rule, ScanResultStatus.SKIPPED, message, "scan-source");
    }

    private ScanResult result(ComplianceRuleDocument rule, ScanResultStatus status, String message, String filePath) {
        ScanEvidence evidence = new ScanEvidence();
        evidence.setFilePath(filePath == null ? "scan-source" : filePath);
        evidence.setDetails(message);

        ScanResult result = new ScanResult();
        result.setRuleId(rule.getRuleId());
        result.setRuleName(rule.getRuleName());
        result.setRuleType(rule.getRuleType());
        result.setCategory(rule.getCategory());
        result.setSeverity(rule.getSeverity());
        result.setResult(status);
        result.setMessage(message);
        result.setEvidence(List.of(evidence));
        return result;
    }

    private static Map.Entry<String, Function<MicroserviceScanContext, RuleEvaluation>> entry(
            String key, Function<MicroserviceScanContext, RuleEvaluation> value) {
        return Map.entry(key, value);
    }

    private record RuleEvaluation(ScanResultStatus status, String message, String filePath) {
        static RuleEvaluation passed(String message, String filePath) {
            return new RuleEvaluation(ScanResultStatus.PASSED, message, filePath);
        }

        static RuleEvaluation failed(String message) {
            return new RuleEvaluation(ScanResultStatus.FAILED, message, null);
        }

        static RuleEvaluation failed(String message, String filePath) {
            return new RuleEvaluation(ScanResultStatus.FAILED, message, filePath);
        }

        static RuleEvaluation warning(String message, String filePath) {
            return new RuleEvaluation(ScanResultStatus.WARNING, message, filePath);
        }
    }
}
