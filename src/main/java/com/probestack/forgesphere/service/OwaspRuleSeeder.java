package com.probestack.forgesphere.service;

import com.probestack.forgesphere.document.OwaspRuleDocument;
import com.probestack.forgesphere.model.AssetType;
import com.probestack.forgesphere.model.OwaspCategory;
import com.probestack.forgesphere.model.RuleSeverity;
import com.probestack.forgesphere.model.RuleStatus;
import com.probestack.forgesphere.model.RuleType;
import com.probestack.forgesphere.repository.OwaspRuleRepository;
import java.time.Instant;
import java.util.List;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Component
public class OwaspRuleSeeder {

    private static final String DEFAULT_RULE_OWNER = "Governance Team";

    private final OwaspRuleRepository owaspRuleRepository;

    public OwaspRuleSeeder(OwaspRuleRepository owaspRuleRepository) {
        this.owaspRuleRepository = owaspRuleRepository;
    }

    @EventListener(ApplicationReadyEvent.class)
    public void seedOwaspRules() {
        for (AssetType assetType : AssetType.values()) {
            for (DefaultOwaspRule defaultRule : DEFAULT_RULES) {
                String ruleId = buildRuleId(assetType, defaultRule.owaspId());
                if (!owaspRuleRepository.existsByRuleId(ruleId)) {
                    owaspRuleRepository.save(buildRuleDocument(assetType, defaultRule, ruleId));
                }
            }
        }
    }

    private String buildRuleId(AssetType assetType, String owaspId) {
        return String.format("OR_%s_%s", assetType.name(), owaspId);
    }

    private OwaspRuleDocument buildRuleDocument(AssetType assetType, DefaultOwaspRule rule, String ruleId) {
        Instant now = Instant.now();
        OwaspRuleDocument document = new OwaspRuleDocument();
        document.setRuleId(ruleId);
        document.setAssetType(assetType);
        document.setOwaspId(rule.owaspId());
        document.setRuleName(rule.ruleName());
        document.setRuleDescription(rule.ruleDescription());
        document.setRuleType(RuleType.PRE_DEFINED);
        document.setRuleOwner(DEFAULT_RULE_OWNER);
        document.setCategory(rule.category());
        document.setSeverity(rule.severity());
        document.setEnabled(true);
        document.setMandatory(true);
        document.setDisplayOrder(rule.displayOrder());
        document.setIcon(rule.icon());
        document.setStatus(RuleStatus.ACTIVE);
        document.setImplementationKey("OWASP_" + ruleId);
        document.setCreateDate(now);
        document.setCreatedBy("system");
        document.setUpdatedDate(now);
        document.setUpdatedBy("system");
        return document;
    }

    private record DefaultOwaspRule(
            String owaspId,
            String ruleName,
            String ruleDescription,
            OwaspCategory category,
            RuleSeverity severity,
            Integer displayOrder,
            String icon) {
    }

    private static final List<DefaultOwaspRule> DEFAULT_RULES = List.of(
            new DefaultOwaspRule("A01", "Broken Access Control",
                    "Ensure access control restrictions are properly enforced.", OwaspCategory.A01_BROKEN_ACCESS_CONTROL,
                    RuleSeverity.MANDATORY, 1, "shield"),
            new DefaultOwaspRule("A02", "Cryptographic Failures",
                    "Prevent sensitive data exposure by using strong cryptography.", OwaspCategory.A02_CRYPTOGRAPHIC_FAILURES,
                    RuleSeverity.MANDATORY, 2, "lock"),
            new DefaultOwaspRule("A03", "Injection",
                    "Validate and sanitize all inputs to avoid injection attacks.", OwaspCategory.A03_INJECTION,
                    RuleSeverity.MANDATORY, 3, "code"),
            new DefaultOwaspRule("A04", "Insecure Design",
                    "Design systems to prevent security flaws from architecture and business logic.", OwaspCategory.A04_INSECURE_DESIGN,
                    RuleSeverity.MANDATORY, 4, "puzzle"),
            new DefaultOwaspRule("A05", "Security Misconfiguration",
                    "Keep security settings and configurations consistent and hardened.", OwaspCategory.A05_SECURITY_MISCONFIGURATION,
                    RuleSeverity.MANDATORY, 5, "settings"),
            new DefaultOwaspRule("A06", "Vulnerable and Outdated Components",
                    "Avoid vulnerable dependencies and keep software components updated.",
                    OwaspCategory.A06_VULNERABLE_COMPONENTS, RuleSeverity.MANDATORY, 6, "package"),
            new DefaultOwaspRule("A07", "Identification and Authentication Failures",
                    "Enforce strong authentication and session management controls.",
                    OwaspCategory.A07_IDENTIFICATION_AUTHENTICATION_FAILURES, RuleSeverity.MANDATORY, 7, "fingerprint"),
            new DefaultOwaspRule("A08", "Software and Data Integrity Failures",
                    "Protect software and data integrity against tampering and unauthorized changes.",
                    OwaspCategory.A08_SOFTWARE_DATA_INTEGRITY_FAILURES, RuleSeverity.MANDATORY, 8, "shield-check"),
            new DefaultOwaspRule("A09", "Security Logging and Monitoring Failures",
                    "Enable logging and monitoring to detect and respond to attacks.",
                    OwaspCategory.A09_SECURITY_LOGGING_MONITORING_FAILURES, RuleSeverity.MANDATORY, 9, "monitor"),
            new DefaultOwaspRule("A10", "Server-Side Request Forgery",
                    "Prevent server-side request forgery through proper outbound request controls.",
                    OwaspCategory.A10_SERVER_SIDE_REQUEST_FORGERY, RuleSeverity.MANDATORY, 10, "network")
    );
}
