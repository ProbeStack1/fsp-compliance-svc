package com.probestack.forgesphere.scanner.evaluator;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.Map;
import org.springframework.stereotype.Component;

/**
 * Tiny shared HTTP probe wrapper used by the rule evaluators. Wraps
 * {@link HttpClient} with sensible timeouts and never throws — callers receive
 * a {@link ProbeOutcome} that carries either the response or the error string.
 *
 * Kept separate from {@code ApigeeProbeRunner} (which is a richer, OWASP-aware
 * probe builder) so the compliance rule evaluators can issue arbitrary
 * GET/HEAD/POST checks against any deployed endpoint without re-implementing
 * timeout/redirect handling per rule.
 */
@Component
public class HttpProbeClient {

    private static final Duration TIMEOUT = Duration.ofSeconds(6);

    private final HttpClient client = HttpClient.newBuilder()
            .followRedirects(HttpClient.Redirect.NEVER)
            .connectTimeout(TIMEOUT)
            .build();

    public ProbeOutcome get(String url) { return get(url, Map.of()); }

    public ProbeOutcome get(String url, Map<String, String> headers) {
        long t0 = System.currentTimeMillis();
        try {
            HttpRequest.Builder b = HttpRequest.newBuilder(URI.create(url)).timeout(TIMEOUT).GET();
            headers.forEach(b::header);
            HttpResponse<String> r = client.send(b.build(), HttpResponse.BodyHandlers.ofString());
            return new ProbeOutcome(true, r.statusCode(), r.headers().map(), r.body() == null ? "" : r.body(),
                    null, System.currentTimeMillis() - t0, url);
        } catch (Exception e) {
            return new ProbeOutcome(false, 0, Map.of(), "",
                    e.getClass().getSimpleName() + ": " + (e.getMessage() == null ? "(no detail)" : e.getMessage()),
                    System.currentTimeMillis() - t0, url);
        }
    }

    public record ProbeOutcome(
            boolean reachable,
            int statusCode,
            Map<String, java.util.List<String>> headers,
            String body,
            String error,
            long durationMs,
            String url
    ) {
        public boolean hasStatus(int... codes) {
            for (int c : codes) if (statusCode == c) return true;
            return false;
        }

        public String header(String name) {
            if (headers == null || name == null) return null;
            for (Map.Entry<String, java.util.List<String>> e : headers.entrySet()) {
                if (name.equalsIgnoreCase(e.getKey()) && !e.getValue().isEmpty()) {
                    return e.getValue().get(0);
                }
            }
            return null;
        }
    }
}
