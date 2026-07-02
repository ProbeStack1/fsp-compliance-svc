package com.probestack.forgesphere.repository;

import com.probestack.forgesphere.document.AuditLog;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface AuditLogRepository extends MongoRepository<AuditLog, String> {
}
