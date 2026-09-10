package com.probestack.forgesphere.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder;

/**
 * forge-auth-lib's own autoconfiguration ({@code ForgeAuthnSecurityAutoConfiguration}) defines its
 * own plain {@code ObjectMapper} bean ({@code forgeAuthnObjectMapper}, used for its own JSON error
 * responses) with no modules registered. Once that bean exists, Boot's own {@code
 * JacksonAutoConfiguration} — the one that would normally register {@code JavaTimeModule}
 * automatically from whatever's on the classpath — backs off ({@code @ConditionalOnMissingBean}),
 * and Spring MVC's JSON message converter ends up using forge-auth-lib's bare one instead. The
 * result, confirmed for real: any response containing a plain {@code java.time.Instant} field
 * (ComplianceThresholdDocument#createDate, etc.) throws {@code InvalidDefinitionException: Java 8
 * date/time type java.time.Instant not supported by default} instead of serializing.
 * <p>
 * This service's own, {@code @Primary}, fully-configured {@code ObjectMapper} bean is what reliably
 * wins here — {@code @Primary} resolves the now-multiple-candidates ambiguity in this bean's favor
 * everywhere an unqualified {@code ObjectMapper} is autowired, forge-auth-lib's own beans included.
 * <p>
 * Built from the injected {@code Jackson2ObjectMapperBuilder} (Boot's own, still driven by whatever
 * {@code spring.jackson.*} properties this service sets — {@code spring.jackson.date-format} here —
 * rather than a from-scratch builder) so this fix adds JavaTimeModule without silently dropping any
 * of that existing customization.
 */
@Configuration
public class JacksonConfig {

    @Bean
    @Primary
    public ObjectMapper objectMapper(Jackson2ObjectMapperBuilder builder) {
        return builder.modulesToInstall(new JavaTimeModule()).build();
    }
}
