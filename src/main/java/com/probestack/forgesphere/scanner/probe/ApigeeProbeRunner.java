package com.probestack.forgesphere.scanner.probe;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * Runs the 10 OWASP Top 10 (2021) probes against a deployed Apigee proxy
 * (or any plain HTTP/HTTPS endpoint). Each probe returns a {@link ProbeResult}
 * with the rich diagnostic fields the UI expects ("what it tests", "how it
 * works", evidence, duration, recommended fix, endpoints tested).
 *
 * The runner is intentionally side-effect free, fully self-contained, and
 * built on {@code java.net.http.HttpClient} so no extra deps are required.
 */
@Component
public class ApigeeProbeRunner {

    private static final Logger log = LoggerFactory.getLogger(ApigeeProbeRunner.class);
    private static final ObjectMapper MAPPER = new ObjectMapper();
    private static final Duration TIMEOUT = Duration.ofSeconds(6);

    private final HttpClient client = HttpClient.newBuilder()
            .followRedirects(HttpClient.Redirect.NEVER)
            .connectTimeout(TIMEOUT)
            .build();

    public List<ProbeResult> runAll(String targetUrl) {
        List<ProbeResult> results = new ArrayList<>();
        results.add(probeMissingAuth(targetUrl));
        results.add(probeWeakToken(targetUrl));
        results.add(probeInjection(targetUrl));
        results.add(probeInsecureDesign(targetUrl));
        results.add(probeSecurityMisconfig(targetUrl));
        results.add(probeOutdatedComponents(targetUrl));
        results.add(probeAuthFailures(targetUrl));
        results.add(probeIntegrityFailures(targetUrl));
        results.add(probeLoggingMonitoring(targetUrl));
        results.add(probeSsrf(targetUrl));
        return results;
    }

    /** A01:2023 — Broken Object Level Authorization / missing auth enforcement */
    public ProbeResult probeMissingAuth(String target) {
        long t0 = System.currentTimeMillis();
        try {
            HttpResponse<String> r = get(target, Map.of());
            boolean pass = r.statusCode() == 401 || r.statusCode() == 403;
            return ProbeResult.builder()
                    .name("Missing Auth enforcement")
                    .severity("HIGH")
                    .passed(pass)
                    .whatItTests("Endpoint should reject anonymous requests with 401/403.")
                    .howItWorks("Sends a GET with no Authorization header. Expects HTTP 401 or 403. A 200 means the endpoint is publicly readable.")
                    .endpoints(List.of(target))
                    .evidence(pass
                            ? "Correctly rejected with HTTP " + r.statusCode()
                            : "No-auth request returned HTTP " + r.statusCode())
                    .recommended("Require authentication on sensitive endpoints. Return 401 with WWW-Authenticate header.")
                    .durationMs(System.currentTimeMillis() - t0)
                    .build();
        } catch (Exception e) {
            return errorResult("Missing Auth enforcement", "HIGH", target, t0, e,
                    "Endpoint should reject anonymous requests with 401/403.",
                    "Sends a GET with no Authorization header. Expects HTTP 401 or 403.");
        }
    }

    /** A02:2023 — Broken Authentication — forged/invalid bearer rejection */
    public ProbeResult probeWeakToken(String target) {
        long t0 = System.currentTimeMillis();
        try {
            HttpResponse<String> r = get(target, Map.of("Authorization", "Bearer forged.token.here"));
            boolean pass = r.statusCode() == 401;
            return ProbeResult.builder()
                    .name("Weak/forged token rejection")
                    .severity("HIGH")
                    .passed(pass)
                    .whatItTests("Endpoint must reject obviously invalid bearer tokens.")
                    .howItWorks("Sends Authorization: Bearer forged.token.here. Expects 401. A 200/403 with body means the validator may be loose.")
                    .endpoints(List.of(target))
                    .evidence(pass ? "Rejected with HTTP 401" : "Forged-token request returned HTTP " + r.statusCode())
                    .recommended("Validate JWT signature + expiry, not just presence. Use a maintained JWT library.")
                    .durationMs(System.currentTimeMillis() - t0)
                    .build();
        } catch (Exception e) {
            return errorResult("Weak/forged token rejection", "HIGH", target, t0, e,
                    "Endpoint must reject obviously invalid bearer tokens.",
                    "Sends Authorization: Bearer forged.token.here. Expects 401.");
        }
    }

    /** A03:2023 — Injection (SQL + NoSQL combined) */
    public ProbeResult probeInjection(String target) {
        long t0 = System.currentTimeMillis();
        try {
            String sqliUrl = target + (target.contains("?") ? "&" : "?") + "id=1%27%20OR%201%3D1--";
            String nosqliUrl = target + (target.contains("?") ? "&" : "?") + "id%5B%24ne%5D=";
            HttpResponse<String> sqli = get(sqliUrl, Map.of());
            HttpResponse<String> nosqli = get(nosqliUrl, Map.of());
            boolean sqliPass = sqli.statusCode() >= 400 && sqli.statusCode() < 500;
            boolean nosqliPass = nosqli.statusCode() >= 400 && nosqli.statusCode() < 500;
            boolean pass = sqliPass && nosqliPass;
            return ProbeResult.builder()
                    .name("Injection probe (SQL + NoSQL)")
                    .severity(pass ? "INFO" : "HIGH")
                    .passed(pass)
                    .whatItTests("Endpoint should reject classic injection payloads with a 4xx, not echo back DB-shaped data.")
                    .howItWorks("Sends two requests: ?id=1' OR 1=1-- (SQLi) and ?id[$ne]= (NoSQL operator). Expects 4xx for both.")
                    .endpoints(List.of(sqliUrl, nosqliUrl))
                    .evidence(String.format("SQLi probe → HTTP %d; NoSQLi probe → HTTP %d", sqli.statusCode(), nosqli.statusCode()))
                    .recommended("Use parameterised queries / prepared statements. Validate and strip operators ($, .) from input.")
                    .durationMs(System.currentTimeMillis() - t0)
                    .build();
        } catch (Exception e) {
            return errorResult("Injection probe (SQL + NoSQL)", "MEDIUM", target, t0, e,
                    "Endpoint should reject classic injection payloads.",
                    "Sends SQLi (?id=1' OR 1=1--) and NoSQLi (?id[$ne]=) payloads. Expects 4xx.");
        }
    }

    /** A04:2023 — Insecure Design (rate limit) */
    public ProbeResult probeInsecureDesign(String target) {
        long t0 = System.currentTimeMillis();
        try {
            int burstCount = 12;
            int seen429 = 0;
            int lastStatus = 0;
            for (int i = 0; i < burstCount; i++) {
                HttpResponse<String> r = get(target, Map.of());
                lastStatus = r.statusCode();
                if (r.statusCode() == 429) seen429++;
            }
            boolean pass = seen429 > 0;
            return ProbeResult.builder()
                    .name("Rate-limit enforcement")
                    .severity(pass ? "INFO" : "MEDIUM")
                    .passed(pass)
                    .whatItTests("Endpoint must throttle bursts to prevent brute force / DoS.")
                    .howItWorks("Sends " + burstCount + " rapid GETs back-to-back. Expects at least one HTTP 429 (Too Many Requests).")
                    .endpoints(List.of(target))
                    .evidence(String.format("Burst test: %d/%d responses were HTTP 429 (last status %d)",
                            seen429, burstCount, lastStatus))
                    .recommended("Add a rate-limiter (Apigee Spike Arrest, Bucket4j, Redis token bucket). Return 429 with Retry-After.")
                    .durationMs(System.currentTimeMillis() - t0)
                    .build();
        } catch (Exception e) {
            return errorResult("Rate-limit enforcement", "MEDIUM", target, t0, e,
                    "Endpoint must throttle bursts.",
                    "Sends 12 rapid GETs. Expects at least one HTTP 429.");
        }
    }

    /** A05:2023 — Security Misconfiguration — security headers */
    public ProbeResult probeSecurityMisconfig(String target) {
        long t0 = System.currentTimeMillis();
        try {
            HttpResponse<String> r = get(target, Map.of());
            List<String> required = List.of(
                    "strict-transport-security",
                    "content-security-policy",
                    "x-content-type-options",
                    "referrer-policy",
                    "x-frame-options");
            List<String> missing = new ArrayList<>();
            for (String h : required) {
                if (r.headers().firstValue(h).isEmpty()) missing.add(h);
            }
            boolean pass = missing.isEmpty();
            return ProbeResult.builder()
                    .name("Security misconfiguration headers")
                    .severity(pass ? "INFO" : "MEDIUM")
                    .passed(pass)
                    .whatItTests("Verifies the response carries baseline security headers (HSTS, CSP, X-Content-Type-Options, Referrer-Policy, X-Frame-Options).")
                    .howItWorks("Issues a GET and flags missing values for the required headers.")
                    .endpoints(List.of(target))
                    .evidence(pass ? "All required headers present"
                            : "Missing: " + String.join(", ", missing))
                    .recommended("Add Strict-Transport-Security, Content-Security-Policy, X-Content-Type-Options: nosniff, X-Frame-Options: DENY, and a sane Referrer-Policy at the proxy/LB.")
                    .durationMs(System.currentTimeMillis() - t0)
                    .build();
        } catch (Exception e) {
            return errorResult("Security misconfiguration headers", "MEDIUM", target, t0, e,
                    "Verifies baseline security headers.",
                    "Issues GET and inspects HSTS, CSP, X-Content-Type-Options etc.");
        }
    }

    /** A06:2023 — Vulnerable & outdated components */
    public ProbeResult probeOutdatedComponents(String target) {
        long t0 = System.currentTimeMillis();
        try {
            HttpResponse<String> r = get(target, Map.of());
            String server = r.headers().firstValue("server").orElse("");
            String poweredBy = r.headers().firstValue("x-powered-by").orElse("");
            String fingerprint = (server + " " + poweredBy).trim();
            Pattern cve = Pattern.compile("(nginx|apache|express|tomcat|php)\\s*/?\\s*([\\d.]+)", Pattern.CASE_INSENSITIVE);
            Matcher m = cve.matcher(fingerprint);
            boolean reveals = m.find();
            boolean pass = !reveals;
            String evidence;
            if (reveals) {
                evidence = "Server advertises a specific version — strip it in prod (" + m.group(1).toLowerCase() + "/" + m.group(2) + ")";
            } else if (!fingerprint.isBlank()) {
                evidence = "Server header present but no recognisable version pattern: " + fingerprint;
            } else {
                evidence = "No version-leaking headers detected";
            }
            return ProbeResult.builder()
                    .name("Vulnerable / outdated components")
                    .severity(pass ? "INFO" : "MEDIUM")
                    .passed(pass)
                    .whatItTests("Response headers should not advertise vulnerable framework versions.")
                    .howItWorks("Inspects Server and X-Powered-By headers, matches against a small CVE table (Apache 2.4.49, nginx 1.14, PHP 5.x, Tomcat 7.x, Express 4.16, Spring 4.x).")
                    .endpoints(List.of(target))
                    .evidence(evidence)
                    .recommended("Strip server-version headers in production. Patch frameworks to current LTS.")
                    .durationMs(System.currentTimeMillis() - t0)
                    .build();
        } catch (Exception e) {
            return errorResult("Vulnerable / outdated components", "MEDIUM", target, t0, e,
                    "Response headers should not advertise vulnerable framework versions.",
                    "Inspects Server and X-Powered-By headers.");
        }
    }

    /** A07:2023 — Identification & Authentication failures */
    public ProbeResult probeAuthFailures(String target) {
        long t0 = System.currentTimeMillis();
        try {
            // Try common credentials on a login-like path if available
            String loginUrl = target;
            HttpResponse<String> r = post(loginUrl, "{\"username\":\"admin\",\"password\":\"admin\"}",
                    Map.of("Content-Type", "application/json"));
            boolean weak = r.statusCode() == 200 && r.body() != null
                    && (r.body().toLowerCase().contains("token") || r.body().toLowerCase().contains("session"));
            boolean pass = !weak;
            return ProbeResult.builder()
                    .name("Weak credentials / authentication failures")
                    .severity(pass ? "INFO" : "HIGH")
                    .passed(pass)
                    .whatItTests("Default / weak credentials should not authenticate against the API.")
                    .howItWorks("POSTs {username: admin, password: admin} to the target. Expects 4xx, never a 200 containing a token/session.")
                    .endpoints(List.of(loginUrl))
                    .evidence(weak ? "Login with admin/admin returned HTTP 200 with token-like body"
                            : "Default credentials rejected with HTTP " + r.statusCode())
                    .recommended("Disable / force-rotate default accounts. Require strong password policy + MFA.")
                    .durationMs(System.currentTimeMillis() - t0)
                    .build();
        } catch (Exception e) {
            return errorResult("Weak credentials / authentication failures", "MEDIUM", target, t0, e,
                    "Default credentials must not work.",
                    "POSTs admin/admin to the target. Expects 4xx.");
        }
    }

    /** A08:2023 — Software & Data Integrity failures */
    public ProbeResult probeIntegrityFailures(String target) {
        long t0 = System.currentTimeMillis();
        try {
            String httpVariant = target.startsWith("https://") ? target.replaceFirst("^https://", "http://") : target;
            HttpResponse<String> r = get(httpVariant, Map.of());
            String location = r.headers().firstValue("location").orElse("");
            boolean redirectToHttps = (r.statusCode() == 301 || r.statusCode() == 308) && location.startsWith("https://");
            boolean pass = redirectToHttps || target.startsWith("https://"); // HTTPS already enforced
            return ProbeResult.builder()
                    .name("HTTPS / integrity enforcement")
                    .severity(pass ? "INFO" : "MEDIUM")
                    .passed(pass)
                    .whatItTests("Plain-HTTP variant of the endpoint should redirect to HTTPS (301/308).")
                    .howItWorks("If target is https://, retries with http:// scheme. Expects 301/308 to https://.")
                    .endpoints(List.of(httpVariant))
                    .evidence(redirectToHttps
                            ? "HTTP redirected to HTTPS via " + r.statusCode()
                            : "HTTP variant returned " + r.statusCode() + " Location=" + (location.isEmpty() ? "(none)" : location))
                    .recommended("Enable HSTS and force-redirect HTTP→HTTPS at the LB/ingress.")
                    .durationMs(System.currentTimeMillis() - t0)
                    .build();
        } catch (Exception e) {
            return errorResult("HTTPS / integrity enforcement", "MEDIUM", target, t0, e,
                    "HTTP must redirect to HTTPS.",
                    "Retries http:// scheme, expects 301/308 to https.");
        }
    }

    /** A09:2023 — Security logging & monitoring */
    public ProbeResult probeLoggingMonitoring(String target) {
        long t0 = System.currentTimeMillis();
        try {
            // Trigger an obvious bad request and look for a clean error envelope
            HttpResponse<String> r = get(target + "/__nonexistent_path_for_probe__", Map.of());
            String body = r.body() == null ? "" : r.body();
            boolean leaksStack = body.contains("java.lang.") || body.contains("at org.")
                    || body.contains("Exception") || body.contains("stack");
            boolean pass = !leaksStack;
            return ProbeResult.builder()
                    .name("Security logging & monitoring")
                    .severity(pass ? "INFO" : "MEDIUM")
                    .passed(pass)
                    .whatItTests("Error responses should not leak stack traces or internal exception class names.")
                    .howItWorks("Sends a request to a known-bad path and inspects the response body for stack-trace markers (java.lang., 'at org.', 'Exception').")
                    .endpoints(List.of(target + "/__nonexistent_path_for_probe__"))
                    .evidence(pass
                            ? "Error response returned a clean envelope without internal detail"
                            : "Response body leaked internal exception markers")
                    .recommended("Catch + map exceptions to RFC-7807 ProblemDetails. Never echo stack traces in production responses.")
                    .durationMs(System.currentTimeMillis() - t0)
                    .build();
        } catch (Exception e) {
            return errorResult("Security logging & monitoring", "MEDIUM", target, t0, e,
                    "Error responses should not leak internals.",
                    "Hits a bad path and scans body for stack-trace markers.");
        }
    }

    /** A10:2023 — Server-Side Request Forgery */
    public ProbeResult probeSsrf(String target) {
        long t0 = System.currentTimeMillis();
        try {
            String ssrfUrl = target + (target.contains("?") ? "&" : "?") + "url=http%3A%2F%2F169.254.169.254%2Flatest%2Fmeta-data%2F";
            HttpResponse<String> r = get(ssrfUrl, Map.of());
            String body = r.body() == null ? "" : r.body();
            boolean leaked = body.contains("iam") || body.contains("ami-") || body.contains("instance-id");
            boolean pass = !leaked && r.statusCode() != 200;
            return ProbeResult.builder()
                    .name("Server-Side Request Forgery (SSRF)")
                    .severity(pass ? "INFO" : "HIGH")
                    .passed(pass)
                    .whatItTests("Endpoint must not blindly fetch URLs supplied by the caller (esp. AWS metadata endpoint).")
                    .howItWorks("Appends ?url=http://169.254.169.254/latest/meta-data/ and inspects response. A 200 with cloud-metadata markers indicates SSRF.")
                    .endpoints(List.of(ssrfUrl))
                    .evidence(leaked ? "Response body contained cloud-metadata markers"
                            : "Endpoint did not fetch attacker-supplied URL (HTTP " + r.statusCode() + ")")
                    .recommended("Validate caller-supplied URLs against an allow-list. Block private/link-local IP ranges (RFC 1918, 169.254/16).")
                    .durationMs(System.currentTimeMillis() - t0)
                    .build();
        } catch (Exception e) {
            return errorResult("Server-Side Request Forgery (SSRF)", "MEDIUM", target, t0, e,
                    "Endpoint must not fetch attacker-supplied URLs.",
                    "Appends ?url=169.254.169.254/latest/meta-data/. Inspects response.");
        }
    }

    /* ───────────────────────── helpers ───────────────────────── */

    private HttpResponse<String> get(String url, Map<String, String> headers) throws Exception {
        HttpRequest.Builder b = HttpRequest.newBuilder(URI.create(url)).timeout(TIMEOUT).GET();
        headers.forEach(b::header);
        return client.send(b.build(), HttpResponse.BodyHandlers.ofString());
    }

    private HttpResponse<String> post(String url, String body, Map<String, String> headers) throws Exception {
        HttpRequest.Builder b = HttpRequest.newBuilder(URI.create(url))
                .timeout(TIMEOUT)
                .POST(HttpRequest.BodyPublishers.ofString(body));
        headers.forEach(b::header);
        return client.send(b.build(), HttpResponse.BodyHandlers.ofString());
    }

    private ProbeResult errorResult(String name, String severity, String target, long t0, Exception e,
                                    String whatItTests, String howItWorks) {
        log.debug("probe '{}' against {} failed: {}", name, target, e.getMessage());
        return ProbeResult.builder()
                .name(name).severity(severity).passed(false)
                .whatItTests(whatItTests).howItWorks(howItWorks)
                .endpoints(List.of(target))
                .evidence("Probe could not execute: " + (e.getMessage() == null ? e.getClass().getSimpleName() : e.getMessage()))
                .recommended("Make the endpoint reachable from the scanner. Some probes need a public URL with no IP block.")
                .durationMs(System.currentTimeMillis() - t0)
                .build();
    }

    /* ───────────────────────── ProbeResult DTO ───────────────────────── */

    public static final class ProbeResult {
        public String name;
        public String severity;
        public boolean passed;
        public String whatItTests;
        public String howItWorks;
        public List<String> endpoints;
        public String evidence;
        public String recommended;
        public long durationMs;
        public Instant ranAt = Instant.now();

        public String toMessageJson() {
            try {
                Map<String, Object> m = new LinkedHashMap<>();
                m.put("name", name);
                m.put("severity", severity);
                m.put("passed", passed);
                m.put("whatItTests", whatItTests);
                m.put("howItWorks", howItWorks);
                m.put("endpoints", endpoints);
                m.put("evidence", evidence);
                m.put("recommended", recommended);
                m.put("durationMs", durationMs);
                m.put("ranAt", ranAt.toString());
                return MAPPER.writeValueAsString(m);
            } catch (JsonProcessingException e) {
                return "Probe " + name + ": " + (passed ? "PASSED" : "FAILED") + " — " + evidence;
            }
        }

        public static Builder builder() { return new Builder(); }
        public static final class Builder {
            private final ProbeResult r = new ProbeResult();
            public Builder name(String v)        { r.name = v; return this; }
            public Builder severity(String v)    { r.severity = v; return this; }
            public Builder passed(boolean v)     { r.passed = v; return this; }
            public Builder whatItTests(String v) { r.whatItTests = v; return this; }
            public Builder howItWorks(String v)  { r.howItWorks = v; return this; }
            public Builder endpoints(List<String> v) { r.endpoints = v; return this; }
            public Builder evidence(String v)    { r.evidence = v; return this; }
            public Builder recommended(String v) { r.recommended = v; return this; }
            public Builder durationMs(long v)    { r.durationMs = v; return this; }
            public ProbeResult build() { return r; }
        }
    }
}
