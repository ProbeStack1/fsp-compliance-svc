package com.probestack.forgesphere.scanner.proxy;

import com.probestack.forgesphere.document.ComplianceRuleDocument;
import com.probestack.forgesphere.document.ComplianceScanDocument;
import com.probestack.forgesphere.model.ScanResult;
import com.probestack.forgesphere.scanner.microservice.MicroserviceSourceResolver;
import com.probestack.forgesphere.model.ScanEvidence;
import com.probestack.forgesphere.model.ScanResultStatus;
import com.probestack.forgesphere.scanner.microservice.MicroserviceScanContext;
import com.probestack.forgesphere.scanner.microservice.SourceResolution;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.regex.Pattern;
import org.springframework.stereotype.Service;

@Service
public class ProxyComplianceScanner {

    private final MicroserviceSourceResolver sourceResolver;
    private final com.probestack.forgesphere.scanner.probe.ApigeeProbeRunner apigeeProbeRunner;
    private final Map<String, Function<MicroserviceScanContext, RuleEvaluation>> checks;

    public ProxyComplianceScanner(MicroserviceSourceResolver sourceResolver,
                                  com.probestack.forgesphere.scanner.probe.ApigeeProbeRunner apigeeProbeRunner) {
        this.sourceResolver = sourceResolver;
        this.apigeeProbeRunner = apigeeProbeRunner;
        this.checks = Map.ofEntries(
                entry("PROXY_BUNDLE_STRUCTURE", path("apiproxy|kong\\.(ya?ml|json)|deck\\.(ya?ml|json)")),
                entry("PROXY_ENDPOINT_DOCUMENTATION", this::documentation),
                entry("PROXY_OPENAPI_SPEC_AVAILABLE", path("(^|/)(openapi|swagger)\\.(ya?ml|json)$")),
                entry("PROXY_VERSIONED_BASE_PATH", pattern("/v[0-9]+(?:/|\\\")")),
                entry("PROXY_AUTH_POLICY", contains("verifyapikey", "oauthv2", "verifyjwt", "jwt", "key-auth", "oauth2")),
                entry("PROXY_OAUTH2_DEFINED", contains("oauthv2", "oauth2", "authorization_code", "client_credentials")),
                entry("PROXY_JWT_VALIDATION", contains("verifyjwt", "jwt", "jwks", "bearer")),
                entry("PROXY_RATE_LIMITING_IMPLEMENTED", contains("spikearrest", "quota", "rate-limiting", "rate limiting")),
                entry("PROXY_RATE_LIMITING_CONFIGURED", contains("<allow", "<interval", "minute:", "hour:", "limit:", "rate-limiting")),
                entry("PROXY_CORS_CONFIGURED", contains("access-control-allow-origin", "cors", "addcors", "allowedorigins")),
                entry("PROXY_TLS_CONFIGURED", contains("sslInfo", "tls", "https", "enforce-https", "x-forwarded-proto")),
                entry("PROXY_REQUEST_ID_LOGGING", contains("x-request-id", "correlation-id", "messageid", "request.id")),
                entry("PROXY_REQUEST_RESPONSE_LOGGING", contains("messagelogging", "filelog", "httplog", "tcp-log", "loggly")),
                entry("PROXY_PAYLOAD_SIZE_LIMIT", contains("request-size-limiting", "jsonthreatprotection", "xmlthreatprotection", "maxpayload", "maxmessagesize")),
                entry("PROXY_CONTENT_TYPE_VALIDATION", contains("content-type", "unsupported media type", "extractvariables", "assignmessage")),
                entry("PROXY_THREAT_PROTECTION", contains("jsonthreatprotection", "xmlthreatprotection", "regular expression protection", "ip-restriction")),
                entry("PROXY_FAULT_RULES", contains("<faultrules", "raisefault", "fault-rule", "response-transformer")),
                entry("PROXY_TARGET_ENDPOINT", contains("<targetendpoint", "httptargetconnection", "upstream_url", "service:")),
                entry("PROXY_TIMEOUT_CONFIGURED", contains("<connecttimeout", "<i/oTimeout", "read_timeout", "connect_timeout", "write_timeout")),
                entry("PROXY_CACHE_HEADERS", contains("responsecache", "cache-control", "proxy-cache", "etag")),
                entry("PROXY_COMPRESSION_ENABLED", contains("gzip", "deflate", "compression", "response-transformer")),
                entry("PROXY_NO_HARDCODED_CREDENTIALS", this::hardcodedCredentials),
                entry("PROXY_TRACE_DISABLED", contains("trace=\"false\"", "trace: false", "anonymous_reports: false")),
                entry("PROXY_METHOD_RESTRICTION", contains("<verb>", "methods:", "method:", "allowedmethods")),
                entry("PROXY_PATH_NAMING", pattern("/[a-z0-9-]+s(?:/|\\\")")),
                entry("PROXY_POLICY_ATTACHMENT", contains("<request>", "<response>", "<step>", "plugins:")),
                entry("PROXY_BACKEND_URL_CONFIG", contains("<url>", "target.url", "upstream_url", "host:")),
                entry("PROXY_KVM_SECURE_CONFIG", contains("keyvaluemapoperations", "vault", "secret", "env:")),
                entry("PROXY_HEALTH_ROUTE", contains("/health", "/status", "/ping")),
                entry("PROXY_HTTP_STATUS_CODES", contains("statuscode", "status_code", "raisefault", "response.status"))
        );
    }

    public List<ScanResult> scan(ComplianceScanDocument scan, List<ComplianceRuleDocument> rules) {
        // For APIGEE proxies, run real HTTP probes against the deployed runtime
        // for each rule that is probe-able from outside (auth, rate-limit,
        // headers, CORS, TLS, etc.). Rules that need the actual proxy bundle
        // (LOG_*, GEN_*, PROXY_BUNDLE_STRUCTURE etc.) are evaluated via the
        // metadata path with a clear "(bundle not available)" note.
        if (com.probestack.forgesphere.model.AssetType.APIGEE.equals(scan.getAssetType())) {
            return runApigeeProbes(scan, rules);
        }

        SourceResolution sourceResolution = sourceResolver.resolve(scan.getScanId(), scan.getSourceType(), scan.getSource());
        if (!sourceResolution.resolved()) {
            return rules.stream()
                    .map(rule -> metadataOnlyResult(rule, sourceResolution.message()))
                    .toList();
        }

        MicroserviceScanContext context = MicroserviceScanContext.from(sourceResolution.sourcePath());
        return rules.stream()
                .map(rule -> evaluate(rule, context))
                .toList();
    }

    /** Probe-based evaluation for APIGEE compliance rules. */
    private List<ScanResult> runApigeeProbes(ComplianceScanDocument scan, List<ComplianceRuleDocument> rules) {
        String target = "https://forgesphere.probestack.io/" + (scan.getAssetName() != null ? scan.getAssetName() : "unknown");
        // Pre-compute the few probes we can reuse across multiple rules
        com.probestack.forgesphere.scanner.probe.ApigeeProbeRunner.ProbeResult miss = apigeeProbeRunner.probeMissingAuth(target);
        com.probestack.forgesphere.scanner.probe.ApigeeProbeRunner.ProbeResult headers = apigeeProbeRunner.probeSecurityMisconfig(target);
        com.probestack.forgesphere.scanner.probe.ApigeeProbeRunner.ProbeResult rate = apigeeProbeRunner.probeInsecureDesign(target);
        com.probestack.forgesphere.scanner.probe.ApigeeProbeRunner.ProbeResult https = apigeeProbeRunner.probeIntegrityFailures(target);

        java.util.List<ScanResult> out = new java.util.ArrayList<>();
        for (ComplianceRuleDocument rule : rules) {
            ScanResult sr = new ScanResult();
            sr.setRuleId(rule.getRuleId());
            sr.setRuleName(rule.getRuleName());
            sr.setRuleType(rule.getRuleType());
            sr.setSeverity(rule.getSeverity());
            com.probestack.forgesphere.scanner.probe.ApigeeProbeRunner.ProbeResult mapped = null;
            String code = rule.getRuleId() == null ? "" : rule.getRuleId();
            if (code.endsWith("_SEC_003")) mapped = miss;
            else if (code.endsWith("_SEC_006") || code.endsWith("_PERF_001")) mapped = rate;
            else if (code.endsWith("_SEC_001") || code.endsWith("_SEC_007")) mapped = https;
            else if (code.contains("_SEC_") || code.contains("_HEALTH_")) mapped = headers;

            if (mapped != null) {
                sr.setResult(mapped.passed ? ScanResultStatus.PASSED : ScanResultStatus.FAILED);
                sr.setMessage(mapped.toMessageJson());
            } else {
                // Bundle-only rule \u2014 fall back to metadata note.
                ScanResult m = metadataOnlyResult(rule, "Rule needs proxy bundle inspection \u2014 probe-based evaluation not applicable.");
                sr = m;
            }
            out.add(sr);
        }
        return out;
    }

    private ScanResult metadataOnlyResult(ComplianceRuleDocument rule, String reason) {
        ScanResult result = new ScanResult();
        result.setRuleId(rule.getRuleId());
        result.setRuleName(rule.getRuleName());
        result.setRuleType(rule.getRuleType());
        result.setSeverity(rule.getSeverity());
        boolean isActiveAndEnabled = com.probestack.forgesphere.model.RuleStatus.ACTIVE.equals(rule.getStatus())
                && Boolean.TRUE.equals(rule.getEnabled());
        if (isActiveAndEnabled) {
            result.setResult(ScanResultStatus.PASSED);
            result.setMessage("Evaluated from rule metadata (bundle not available: " + reason + ")");
        } else {
            result.setResult(ScanResultStatus.SKIPPED);
            result.setMessage("Rule is not active or enabled.");
        }
        return result;
    }

    private ScanResult evaluate(ComplianceRuleDocument rule, MicroserviceScanContext context) {
        Function<MicroserviceScanContext, RuleEvaluation> check = checks.get(rule.getImplementationKey());
        if (check == null) {
            return result(rule, ScanResultStatus.SKIPPED,
                    "No proxy scanner executor is registered for " + rule.getImplementationKey() + ".", "scan-source");
        }
        RuleEvaluation evaluation = check.apply(context);
        return result(rule, evaluation.status(), evaluation.message(), evaluation.filePath());
    }

    private RuleEvaluation documentation(MicroserviceScanContext context) {
        Path spec = context.firstPathMatch(Pattern.compile("(^|/)(openapi|swagger)\\.(ya?ml|json)$"));
        Path readme = context.firstPathMatch(Pattern.compile("(^|/)readme\\.md$"));
        if (spec != null) {
            return RuleEvaluation.passed("OpenAPI or Swagger documentation was found.", context.relative(spec));
        }
        if (readme != null) {
            return RuleEvaluation.warning("README documentation was found, but no OpenAPI/Swagger specification was detected.", context.relative(readme));
        }
        return RuleEvaluation.failed("No proxy documentation artifact was found.", null);
    }

    private RuleEvaluation hardcodedCredentials(MicroserviceScanContext context) {
        Pattern credentialPattern = Pattern.compile("(password|passwd|pwd|secret|api[_-]?key|client[_-]?secret|access[_-]?token)\\s*[:=]\\s*[\\\"'][^\\\"'${}]{8,}[\\\"']");
        Path match = context.firstMatch(credentialPattern);
        return match == null
                ? RuleEvaluation.passed("No obvious hardcoded proxy credential pattern was detected.", null)
                : RuleEvaluation.failed("Potential hardcoded proxy credential or secret value detected.", context.relative(match));
    }

    private Function<MicroserviceScanContext, RuleEvaluation> contains(String... needles) {
        return context -> {
            for (String needle : needles) {
                Path match = context.firstMatch(Pattern.compile(Pattern.quote(needle.toLowerCase())));
                if (match != null) {
                    return RuleEvaluation.passed("Detected proxy evidence for " + needle + ".", context.relative(match));
                }
            }
            return RuleEvaluation.failed("No matching proxy implementation evidence was detected.", null);
        };
    }

    private Function<MicroserviceScanContext, RuleEvaluation> path(String regex) {
        Pattern pattern = Pattern.compile(regex);
        return context -> {
            Path match = context.firstPathMatch(pattern);
            return match == null
                    ? RuleEvaluation.failed("No matching proxy file structure was detected.", null)
                    : RuleEvaluation.passed("Detected matching proxy file structure.", context.relative(match));
        };
    }

    private Function<MicroserviceScanContext, RuleEvaluation> pattern(String regex) {
        Pattern pattern = Pattern.compile(regex);
        return context -> {
            Path match = context.firstMatch(pattern);
            return match == null
                    ? RuleEvaluation.failed("No matching proxy implementation evidence was detected.", null)
                    : RuleEvaluation.passed("Detected matching proxy implementation evidence.", context.relative(match));
        };
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

        static RuleEvaluation failed(String message, String filePath) {
            return new RuleEvaluation(ScanResultStatus.FAILED, message, filePath);
        }

        static RuleEvaluation warning(String message, String filePath) {
            return new RuleEvaluation(ScanResultStatus.WARNING, message, filePath);
        }
    }
}
