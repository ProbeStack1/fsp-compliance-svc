package com.probestack.forgesphere.scanner.evaluator;

import java.nio.file.Path;
import java.util.regex.Pattern;

/**
 * Small package-private helper that holds DOC_003/004/005 + SEC_004 logic
 * shared between {@link MicroserviceRuleEvaluator} and {@link ProxyRuleEvaluator}.
 * Both flavours look at the OpenAPI spec in the bundle — having one copy
 * here avoids duplicating the spec-parsing regex in two places.
 */
final class MicroserviceRuleEvaluatorDocFallback {

    private static final Pattern OPENAPI_FILE = Pattern.compile("(openapi|swagger|api-docs)\\.(ya?ml|json)$");

    private MicroserviceRuleEvaluatorDocFallback() { }

    static EvaluationOutcome examples(RuleEvaluationContext ctx) {
        String what = "Each 2xx response in the spec must contain at least one example payload.";
        String how  = "Counts `examples:` / `example:` keys appearing under 2xx response blocks.";
        if (!ctx.hasSource()) return EvaluationOutcome.skipped(what, how,
                "Bundle was not provided — cannot inspect spec.");
        Path spec = ctx.firstPathMatching(OPENAPI_FILE);
        if (spec == null) return EvaluationOutcome.skipped(what, how, "No OpenAPI spec bundled with the asset.");
        String body = ctx.readRaw(spec);
        int twoXx = countMatches(body, Pattern.compile("(?m)^\\s{6,}'?(20[0-9])'?:"));
        int examples = countMatches(body, Pattern.compile("(?m)^\\s+(examples?|x-example):"));
        boolean pass = twoXx == 0 || examples > 0;
        return pass
                ? EvaluationOutcome.passed(what, how, twoXx + " 2xx responses · " + examples + " examples in spec")
                : EvaluationOutcome.failed(what, how,
                        twoXx + " 2xx responses found but no examples present.",
                        "Add an `examples:` block (or x-example) on each 2xx response.");
    }

    static EvaluationOutcome errorSchema(RuleEvaluationContext ctx) {
        String what = "Each 4xx/5xx response must reference a standard error schema.";
        String how  = "Counts 4xx/5xx response blocks and inspects them for `$ref:` pointing to an Error / ProblemDetails schema.";
        if (!ctx.hasSource()) return EvaluationOutcome.skipped(what, how,
                "Bundle was not provided — cannot inspect spec.");
        Path spec = ctx.firstPathMatching(OPENAPI_FILE);
        if (spec == null) return EvaluationOutcome.skipped(what, how, "No OpenAPI spec bundled with the asset.");
        String body = ctx.readRaw(spec);
        int errors = countMatches(body, Pattern.compile("(?m)^\\s{6,}'?(4[0-9]{2}|5[0-9]{2})'?:"));
        int refs = countMatches(body, Pattern.compile("\\$ref:\\s*['\"][^'\"]*(error|problem)[^'\"]*['\"]", Pattern.CASE_INSENSITIVE));
        boolean pass = errors == 0 || refs > 0;
        return pass
                ? EvaluationOutcome.passed(what, how, errors + " error responses · " + refs + " error-schema $refs")
                : EvaluationOutcome.failed(what, how,
                        errors + " 4xx/5xx responses but no $ref to an Error/ProblemDetails schema.",
                        "Define a shared `ErrorResponse` / `ProblemDetails` schema and $ref it from every 4xx/5xx response.");
    }

    static EvaluationOutcome deprecation(RuleEvaluationContext ctx) {
        String what = "Deprecated operations must declare `deprecated: true` and a sunset strategy.";
        String how  = "Greps the bundle for `deprecated: true` and `Sunset` / `X-Deprecation-Date` markers.";
        if (!ctx.hasSource()) return EvaluationOutcome.skipped(what, how,
                "Bundle was not provided — cannot inspect spec.");
        boolean flag = ctx.firstContentMatch(Pattern.compile("deprecated:\\s*true|@deprecated|@deprecation", Pattern.CASE_INSENSITIVE)) != null;
        boolean sunset = ctx.firstContentMatch(Pattern.compile("(sunset|x-deprecation-date)", Pattern.CASE_INSENSITIVE)) != null;
        if (!flag && !sunset) {
            return EvaluationOutcome.passed(what, how, "No deprecated operations declared — nothing to verify.");
        }
        boolean pass = flag && sunset;
        return pass
                ? EvaluationOutcome.passed(what, how, "Found deprecated flag + Sunset/X-Deprecation-Date marker.")
                : EvaluationOutcome.failed(what, how,
                        (flag ? "Deprecated operations exist but " : "") + "no Sunset / X-Deprecation-Date marker found.",
                        "When deprecating, set `deprecated: true` and add a Sunset response header or X-Deprecation-Date.");
    }

    static EvaluationOutcome scopes(RuleEvaluationContext ctx) {
        String what = "Each operation must declare required OAuth scopes / roles.";
        String how  = "Parses the OpenAPI spec for top-level `securitySchemes:` + per-operation `security:` blocks.";
        if (!ctx.hasSource()) return EvaluationOutcome.skipped(what, how,
                "Bundle was not provided — cannot inspect spec.");
        Path spec = ctx.firstPathMatching(OPENAPI_FILE);
        if (spec == null) return EvaluationOutcome.skipped(what, how, "No OpenAPI spec bundled with the asset.");
        String body = ctx.contentLower(spec);
        boolean schemes = body.contains("securityschemes:");
        boolean perOp = countMatches(body, Pattern.compile("(?m)^\\s+security:\\s*$")) > 0;
        boolean pass = schemes && perOp;
        return pass
                ? EvaluationOutcome.passed(what, how, "securitySchemes + per-operation security blocks declared.")
                : EvaluationOutcome.failed(what, how,
                        "Spec missing " + (!schemes ? "global securitySchemes" : "") + (!perOp ? (schemes ? "" : " and ") + "per-operation security blocks" : ""),
                        "Declare a `securitySchemes:` block and add a `security:` block to every operation.");
    }

    private static int countMatches(String body, Pattern pattern) {
        if (body == null) return 0;
        int count = 0;
        java.util.regex.Matcher m = pattern.matcher(body);
        while (m.find()) count++;
        return count;
    }
}
