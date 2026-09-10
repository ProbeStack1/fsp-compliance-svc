package com.probestack.forgesphere.config;

import com.forge.security.authn.model.AuthnToken;
import com.forge.security.authn.security.ForgeAuthnAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Optional;

/**
 * A handful of endpoints here (compliance thresholds, resource exemptions, lint scan results)
 * used to read "who made this change" straight off a client-supplied {@code updatedBy}/{@code
 * createdBy} request-body field — trusted at face value, so any caller could credit (or frame) any
 * name they liked for a compliance threshold change or a security exemption. Once a request is
 * past {@link SecurityConfig}'s gate, forge-auth-lib has already verified a real, signed token,
 * and that token's own {@code email} claim is exactly this same information, except a caller
 * genuinely cannot forge it without the identity provider's signing key.
 * <p>
 * Returns empty when there's no verified token on this request — forge.authn disabled (local dev)
 * — callers decide their own fallback for that case rather than this class silently inventing one.
 */
public final class AuthenticatedCaller {

    private AuthenticatedCaller() {
    }

    private static Optional<AuthnToken> token() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth instanceof ForgeAuthnAuthenticationToken && auth.getDetails() instanceof AuthnToken authnToken) {
            return Optional.of(authnToken);
        }
        return Optional.empty();
    }

    public static Optional<String> email() {
        return token().map(t -> stringClaim(t, "email")).filter(e -> e != null && !e.isBlank());
    }

    private static String stringClaim(AuthnToken token, String claimName) {
        Object value = token.getClaim(claimName);
        return value == null ? null : value.toString();
    }
}
