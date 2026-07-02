package com.probestack.forgesphere.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.probestack.forgesphere.document.AuditLog;
import com.probestack.forgesphere.document.ComplianceScanDocument;
import com.probestack.forgesphere.repository.AuditLogRepository;
import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.stereotype.Service;

@Service
public class AuditLogService {
    private static final String SERVICE_NAME = "compliance";
    private final AuditLogRepository auditLogRepository;
    private final MongoTemplate mongoTemplate;
    private final ObjectMapper objectMapper;

    public AuditLogService(AuditLogRepository auditLogRepository, MongoTemplate mongoTemplate, ObjectMapper objectMapper) {
        this.auditLogRepository = auditLogRepository;
        this.mongoTemplate = mongoTemplate;
        this.objectMapper = objectMapper;
    }

    public void logComplianceScanCreate(ComplianceScanDocument scan) {
        if (scan == null) {
            return;
        }
        try {
            Map<String, Object> resource = scan.getAssetId() == null
                    ? null
                    : mongoTemplate.findById(scan.getAssetId(), Map.class, "microservice");
            String onboardingContextId = resource == null ? null : asString(resource.get("onboardingId"));
            String resourceType = resource == null ? null : asString(resource.get("projectType"));

            AuditLog auditLog = new AuditLog();
            auditLog.setAuditType("ENTITY");
            auditLog.setService(SERVICE_NAME);
            auditLog.setEntityType("COMPLIANCE_SCAN");
            auditLog.setEntityId(scan.getId());
            auditLog.setOperation("CREATE");
            auditLog.setStatus("SUCCESS");
            auditLog.setMessage("Compliance scan submitted");
            auditLog.setPerformedBy(scan.getCreatedBy());
            auditLog.setOnboardingId(onboardingContextId);
            auditLog.setMicroserviceId(scan.getAssetId());
            auditLog.setOnboardingContextId(onboardingContextId);
            auditLog.setResourceCollection(scan.getAssetId() == null ? null : "microservice");
            auditLog.setResourceId(scan.getAssetId());
            auditLog.setResourceType(resourceType);
            auditLog.setStepKey("COMPLIANCE");
            auditLog.setStepCollection("governance_compliance_scans");
            auditLog.setStepObjectId(scan.getId());
            auditLog.setTargetCollection("governance_compliance_scans");
            auditLog.setTargetObjectId(scan.getId());
            auditLog.setTargetEntityType("COMPLIANCE_SCAN");
            auditLog.setAfterSnapshot(snapshot(scan));
            auditLog.setCreatedAt(Instant.now());
            auditLogRepository.save(auditLog);
        } catch (Exception ex) {
            // Best-effort audit: compliance scan submission must not fail because history logging failed.
        }
    }

    private Map<String, Object> snapshot(Object value) {
        return value == null
                ? null
                : objectMapper.convertValue(value, new TypeReference<LinkedHashMap<String, Object>>() {});
    }

    private String asString(Object value) {
        return value == null ? null : value.toString();
    }
}
