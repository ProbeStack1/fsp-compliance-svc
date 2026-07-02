package com.probestack.forgesphere.document;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "audit_logs")
public class AuditLog {
    @Id private String id;
    @Indexed private String auditType;
    @Indexed private String service;
    @Indexed private String entityType;
    @Indexed private String entityId;
    @Indexed private String operation;
    @Indexed private String status;
    private String message;
    private String performedBy;
    @Indexed private String onboardingId;
    @Indexed private String microserviceId;
    @Indexed private String onboardingContextId;
    private String resourceCollection;
    @Indexed private String resourceId;
    @Indexed private String resourceType;
    @Indexed private String stepKey;
    @Indexed private String stepCollection;
    @Indexed private String stepObjectId;
    @Indexed private String targetCollection;
    @Indexed private String targetObjectId;
    @Indexed private String targetEntityType;
    private Map<String, Object> beforeSnapshot;
    private Map<String, Object> afterSnapshot;
    private List<Map<String, Object>> changes;
    private String errorMessage;
    private Map<String, Object> metadata;
    @Indexed private Instant createdAt;

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getAuditType() { return auditType; }
    public void setAuditType(String auditType) { this.auditType = auditType; }
    public String getService() { return service; }
    public void setService(String service) { this.service = service; }
    public String getEntityType() { return entityType; }
    public void setEntityType(String entityType) { this.entityType = entityType; }
    public String getEntityId() { return entityId; }
    public void setEntityId(String entityId) { this.entityId = entityId; }
    public String getOperation() { return operation; }
    public void setOperation(String operation) { this.operation = operation; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }
    public String getPerformedBy() { return performedBy; }
    public void setPerformedBy(String performedBy) { this.performedBy = performedBy; }
    public String getOnboardingId() { return onboardingId; }
    public void setOnboardingId(String onboardingId) { this.onboardingId = onboardingId; }
    public String getMicroserviceId() { return microserviceId; }
    public void setMicroserviceId(String microserviceId) { this.microserviceId = microserviceId; }
    public String getOnboardingContextId() { return onboardingContextId; }
    public void setOnboardingContextId(String onboardingContextId) { this.onboardingContextId = onboardingContextId; }
    public String getResourceCollection() { return resourceCollection; }
    public void setResourceCollection(String resourceCollection) { this.resourceCollection = resourceCollection; }
    public String getResourceId() { return resourceId; }
    public void setResourceId(String resourceId) { this.resourceId = resourceId; }
    public String getResourceType() { return resourceType; }
    public void setResourceType(String resourceType) { this.resourceType = resourceType; }
    public String getStepKey() { return stepKey; }
    public void setStepKey(String stepKey) { this.stepKey = stepKey; }
    public String getStepCollection() { return stepCollection; }
    public void setStepCollection(String stepCollection) { this.stepCollection = stepCollection; }
    public String getStepObjectId() { return stepObjectId; }
    public void setStepObjectId(String stepObjectId) { this.stepObjectId = stepObjectId; }
    public String getTargetCollection() { return targetCollection; }
    public void setTargetCollection(String targetCollection) { this.targetCollection = targetCollection; }
    public String getTargetObjectId() { return targetObjectId; }
    public void setTargetObjectId(String targetObjectId) { this.targetObjectId = targetObjectId; }
    public String getTargetEntityType() { return targetEntityType; }
    public void setTargetEntityType(String targetEntityType) { this.targetEntityType = targetEntityType; }
    public Map<String, Object> getBeforeSnapshot() { return beforeSnapshot; }
    public void setBeforeSnapshot(Map<String, Object> beforeSnapshot) { this.beforeSnapshot = beforeSnapshot; }
    public Map<String, Object> getAfterSnapshot() { return afterSnapshot; }
    public void setAfterSnapshot(Map<String, Object> afterSnapshot) { this.afterSnapshot = afterSnapshot; }
    public List<Map<String, Object>> getChanges() { return changes; }
    public void setChanges(List<Map<String, Object>> changes) { this.changes = changes; }
    public String getErrorMessage() { return errorMessage; }
    public void setErrorMessage(String errorMessage) { this.errorMessage = errorMessage; }
    public Map<String, Object> getMetadata() { return metadata; }
    public void setMetadata(Map<String, Object> metadata) { this.metadata = metadata; }
    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
}
