package com.probestack.forgesphere.service;

import com.probestack.forgesphere.document.ComplianceRuleDocument;
import com.probestack.forgesphere.document.LintingRuleDocument;
import com.probestack.forgesphere.model.*;
import com.probestack.forgesphere.repository.ComplianceRuleRepository;
import com.probestack.forgesphere.repository.LintingRuleRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

@Service
public class RuleRequestService {

    private final ComplianceRuleRepository complianceRuleRepository;
    private final LintingRuleRepository lintingRuleRepository;
    private final MailService mailService;

    public RuleRequestService(ComplianceRuleRepository complianceRuleRepository,
                              LintingRuleRepository lintingRuleRepository,
                              MailService mailService) {
        this.complianceRuleRepository = complianceRuleRepository;
        this.lintingRuleRepository = lintingRuleRepository;
        this.mailService = mailService;
    }

    /**
     * Submit a new rule request (user).
     * Creates a rule document with status = REQUESTED.
     * Sends email to approver.
     */
    public SubmitRuleRequestResponse submitRequest(SubmitRuleRequest request) {
        String ruleId = generateRuleId(request.getRuleType());
        Instant now = Instant.now();

        // Create rule with REQUESTED status
        if ("COMPLIANCE".equalsIgnoreCase(request.getRuleType())) {
            ComplianceRuleDocument doc = new ComplianceRuleDocument();
            doc.setRuleId(ruleId);
            doc.setAssetType(request.getAssetType());
            doc.setRuleName(request.getRuleName());
            doc.setRuleDescription(request.getRuleDescription());
            doc.setCategory(request.getCategory());
            doc.setSeverity(request.getSeverity());
            doc.setMandatory(request.getMandatory() != null ? request.getMandatory() : false);
            doc.setIcon(request.getIcon());
            doc.setStatus(RuleStatus.REQUESTED);
            doc.setEnabled(false);
            doc.setImplementationKey("PENDING");
            doc.setCreateDate(now);
            doc.setCreatedBy(request.getCreatedBy());
            doc.setUpdatedDate(now);
            doc.setUpdatedBy(request.getCreatedBy());
            doc.setApproverEmail(request.getApproverEmail());
            doc.setDisplayOrder(0);
            complianceRuleRepository.save(doc);
        } else if ("LINTING".equalsIgnoreCase(request.getRuleType())) {
            LintingRuleDocument doc = new LintingRuleDocument();
            doc.setRuleId(ruleId);
            doc.setAssetType(request.getAssetType());
            doc.setRuleName(request.getRuleName());
            doc.setRuleDescription(request.getRuleDescription());
            doc.setCategory(request.getCategory());
            doc.setSeverity(request.getSeverity());
            doc.setMandatory(request.getMandatory() != null ? request.getMandatory() : false);
            doc.setIcon(request.getIcon());
            doc.setStatus(RuleStatus.REQUESTED);
            doc.setEnabled(false);
            doc.setImplementationKey("PENDING");
            doc.setCreateDate(now);
            doc.setCreatedBy(request.getCreatedBy());
            doc.setUpdatedDate(now);
            doc.setUpdatedBy(request.getCreatedBy());
            doc.setApproverEmail(request.getApproverEmail());
            doc.setDisplayOrder(0);
            lintingRuleRepository.save(doc);
        } else {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid ruleType. Use COMPLIANCE or LINTING.");
        }

        // Send email to approver
        MailService.SendOutcome emailOutcome = mailService.sendRuleRequestEmail(
                request.getApproverEmail(),
                ruleId,
                request.getRuleName(),
                request.getCreatedBy()
        );

        return new SubmitRuleRequestResponse(
                ruleId,
                RuleStatus.REQUESTED,
                emailOutcome.delivered(),
                emailOutcome.message(),
                "Rule request submitted successfully."
        );
    }

    /**
     * Approve or reject a rule request (admin).
     */
    public ApproveRuleRequestResponse approveRequest(String ruleId, ApproveRuleRequest approveReq) {
        // Check Compliance rules first
        var complianceOpt = complianceRuleRepository.findByRuleId(ruleId);
        if (complianceOpt.isPresent()) {
            return updateComplianceRule(complianceOpt.get(), approveReq);
        }

        // Then check Linting rules
        var lintingOpt = lintingRuleRepository.findByRuleId(ruleId);
        if (lintingOpt.isPresent()) {
            return updateLintingRule(lintingOpt.get(), approveReq);
        }

        throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Rule not found: " + ruleId);
    }

    private ApproveRuleRequestResponse updateComplianceRule(ComplianceRuleDocument doc, ApproveRuleRequest req) {
        if (req.getStatus() == RuleStatus.REJECTED) {
            doc.setStatus(RuleStatus.REJECTED);
            doc.setUpdatedDate(Instant.now());
            doc.setUpdatedBy(req.getUpdatedBy());
            complianceRuleRepository.save(doc);
            return new ApproveRuleRequestResponse(doc.getRuleId(), RuleStatus.REJECTED, "Rule request rejected.");
        }

        if (req.getStatus() != RuleStatus.READY) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid status. Use READY or REJECTED.");
        }

        // ✅ implementationKey is MANDATORY for Compliance
        if (req.getImplementationKey() == null || req.getImplementationKey().isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "implementationKey is required to activate a compliance rule.");
        }

        // Apply overrides
        if (req.getRuleName() != null) doc.setRuleName(req.getRuleName());
        if (req.getRuleDescription() != null) doc.setRuleDescription(req.getRuleDescription());
        if (req.getCategory() != null) doc.setCategory(req.getCategory());
        if (req.getSeverity() != null) doc.setSeverity(req.getSeverity());
        if (req.getMandatory() != null) doc.setMandatory(req.getMandatory());
        if (req.getIcon() != null) doc.setIcon(req.getIcon());

        doc.setImplementationKey(req.getImplementationKey());
        doc.setStatus(RuleStatus.READY);
        doc.setEnabled(true);
        doc.setUpdatedDate(Instant.now());
        doc.setUpdatedBy(req.getUpdatedBy());
        doc.setApprovedBy(req.getUpdatedBy());
        doc.setApprovedDate(Instant.now());
        if (doc.getDisplayOrder() == null || doc.getDisplayOrder() == 0) {
            doc.setDisplayOrder((int) complianceRuleRepository.count() + 1);
        }

        complianceRuleRepository.save(doc);
        return new ApproveRuleRequestResponse(doc.getRuleId(), RuleStatus.READY, "Compliance rule approved and ready.");
    }

    private ApproveRuleRequestResponse updateLintingRule(LintingRuleDocument doc, ApproveRuleRequest req) {
        if (req.getStatus() == RuleStatus.REJECTED) {
            doc.setStatus(RuleStatus.REJECTED);
            doc.setUpdatedDate(Instant.now());
            doc.setUpdatedBy(req.getUpdatedBy());
            lintingRuleRepository.save(doc);
            return new ApproveRuleRequestResponse(doc.getRuleId(), RuleStatus.REJECTED, "Rule request rejected.");
        }

        if (req.getStatus() != RuleStatus.READY) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid status. Use READY or REJECTED.");
        }

        // ✅ implementationKey is OPTIONAL for Linting – set default if not provided
        if (req.getImplementationKey() != null && !req.getImplementationKey().isBlank()) {
            doc.setImplementationKey(req.getImplementationKey());
        } else {
            doc.setImplementationKey("EXTERNAL_LINTING");
        }

        // Apply overrides
        if (req.getRuleName() != null) doc.setRuleName(req.getRuleName());
        if (req.getRuleDescription() != null) doc.setRuleDescription(req.getRuleDescription());
        if (req.getCategory() != null) doc.setCategory(req.getCategory());
        if (req.getSeverity() != null) doc.setSeverity(req.getSeverity());
        if (req.getMandatory() != null) doc.setMandatory(req.getMandatory());
        if (req.getIcon() != null) doc.setIcon(req.getIcon());

        doc.setStatus(RuleStatus.READY);
        doc.setEnabled(true);
        doc.setUpdatedDate(Instant.now());
        doc.setUpdatedBy(req.getUpdatedBy());
        doc.setApprovedBy(req.getUpdatedBy());
        doc.setApprovedDate(Instant.now());
        if (doc.getDisplayOrder() == null || doc.getDisplayOrder() == 0) {
            doc.setDisplayOrder((int) lintingRuleRepository.count() + 1);
        }

        lintingRuleRepository.save(doc);
        return new ApproveRuleRequestResponse(doc.getRuleId(), RuleStatus.READY, "Linting rule approved and ready.");
    }

    private String generateRuleId(String ruleType) {
        String prefix = "COMPLIANCE".equalsIgnoreCase(ruleType) ? "CR" : "LR";
        int next = 1;
        if ("COMPLIANCE".equalsIgnoreCase(ruleType)) {
            next = (int) complianceRuleRepository.count() + 1;
        } else {
            next = (int) lintingRuleRepository.count() + 1;
        }
        return prefix + String.format("%03d", next);
    }
}