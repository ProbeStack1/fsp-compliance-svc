package com.probestack.forgesphere.repository;

import com.probestack.forgesphere.document.ComplianceRuleDocument;
import com.probestack.forgesphere.model.AssetType;
import java.util.List;
import java.util.Optional;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface ComplianceRuleRepository extends MongoRepository<ComplianceRuleDocument, String> {

    long countByAssetType(AssetType assetType);

    boolean existsByRuleId(String ruleId);

    Optional<ComplianceRuleDocument> findByRuleId(String ruleId);

    List<ComplianceRuleDocument> findAllByAssetTypeOrderByDisplayOrderAsc(AssetType assetType);
}
