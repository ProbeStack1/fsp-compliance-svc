package com.probestack.forgesphere.config;

import com.forge.security.authn.security.ForgeAuthnAuthenticationFilter;
import jakarta.servlet.Filter;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import java.util.Optional;

/**
 * The real session credential the browser sends is the HttpOnly {@code ps_auth_token} cookie, not
 * an Authorization header — JavaScript can never read or forward an HttpOnly cookie itself, so
 * {@link CookieToHeaderBridgeFilter} copies it into a synthetic Authorization header, server-side,
 * right before forge-auth-lib's own {@code ForgeAuthnAuthenticationFilter} runs its real
 * JWKS-backed signature/issuer/audience/expiry check.
 * <p>
 * No preflight bypass or business-endpoint exceptions needed here beyond actuator/swagger — every
 * controller in this service (rules, scans, thresholds, exemptions) is reached only by the
 * signed-in UI, and nothing else in the platform calls into this service directly.
 */
@Configuration
public class SecurityConfig {

    private static final String[] OPEN_PATHS = {
            "/actuator/health", "/actuator/info", "/v3/api-docs/**", "/swagger-ui/**", "/swagger-ui.html",
    };

    /**
     * Spring Boot auto-registers any {@code Filter}-typed bean as a global servlet-container
     * filter, independent of which SecurityFilterChain Spring Security ends up picking — disabling
     * that auto-registration here means the real filter only ever runs where
     * {@link #securityFilterChain} explicitly wires it in via {@code addFilterAt}.
     */
    @Bean
    public FilterRegistrationBean<Filter> forgeAuthnAuthenticationFilterRegistration(
            Optional<ForgeAuthnAuthenticationFilter> forgeAuthnFilter) {
        FilterRegistrationBean<Filter> registration = new FilterRegistrationBean<>();
        // RegistrationBean#onStartup calls getDescription() unconditionally, before it even checks
        // isEnabled() — a null filter here throws "Filter must not be null" at startup regardless
        // of setEnabled(false) below, so a genuine no-op filter (never actually invoked once
        // registration is disabled) is supplied purely so getDescription() has something to name.
        registration.setFilter(forgeAuthnFilter.<Filter>map(f -> f)
                .orElse((request, response, chain) -> chain.doFilter(request, response)));
        registration.setEnabled(false);
        return registration;
    }

    /**
     * Lowest @Order — every CORS preflight, on every path, ahead of anything that could reject it.
     * {@link CorsConfig} registers a plain {@code CorsFilter} bean at Boot's default filter order,
     * which is AFTER Spring Security's own FilterChainProxy — without this chain, forge-auth-lib's
     * filter would see (and reject) every preflight before CorsFilter ever gets a chance to add its
     * headers, which a browser reports as a bare "CORS error".
     */
    @Bean
    @Order(0)
    public SecurityFilterChain preflightFilterChain(HttpSecurity http) throws Exception {
        http.securityMatcher((HttpServletRequest request) -> "OPTIONS".equalsIgnoreCase(request.getMethod()))
                .csrf(csrf -> csrf.disable())
                .authorizeHttpRequests(auth -> auth.anyRequest().permitAll());
        return http.build();
    }

    /** Next @Order — actuator/swagger, open regardless of forge.authn state. */
    @Bean
    @Order(1)
    public SecurityFilterChain openPathsFilterChain(HttpSecurity http) throws Exception {
        http.securityMatcher(OPEN_PATHS)
                .csrf(csrf -> csrf.disable())
                .authorizeHttpRequests(auth -> auth.anyRequest().permitAll());
        return http.build();
    }

    /** Everything else — cookie-bridged, forge-auth-lib-verified when forge.authn is enabled; wide open when it isn't. */
    @Bean
    @Order(2)
    public SecurityFilterChain securityFilterChain(
            HttpSecurity http,
            @Value("${compliance.authn.cookie-name:ps_auth_token}") String authCookieName,
            Optional<ForgeAuthnAuthenticationFilter> forgeAuthnFilter,
            Optional<AuthenticationEntryPoint> authenticationEntryPoint,
            Optional<AccessDeniedHandler> accessDeniedHandler) throws Exception {

        http.csrf(csrf -> csrf.disable())
                .httpBasic(httpBasic -> httpBasic.disable())
                .formLogin(formLogin -> formLogin.disable())
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS));

        if (forgeAuthnFilter.isEmpty()) {
            http.authorizeHttpRequests(auth -> auth.anyRequest().permitAll());
            return http.build();
        }

        http.authorizeHttpRequests(auth -> auth.anyRequest().authenticated());

        if (authenticationEntryPoint.isPresent() && accessDeniedHandler.isPresent()) {
            http.exceptionHandling(ex -> ex
                    .authenticationEntryPoint(authenticationEntryPoint.get())
                    .accessDeniedHandler(accessDeniedHandler.get()));
        }

        http.addFilterAt(forgeAuthnFilter.get(), UsernamePasswordAuthenticationFilter.class);
        http.addFilterBefore(new CookieToHeaderBridgeFilter(authCookieName), ForgeAuthnAuthenticationFilter.class);

        return http.build();
    }
}
