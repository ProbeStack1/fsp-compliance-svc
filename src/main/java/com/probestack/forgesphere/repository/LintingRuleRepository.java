package com.probestack.forgesphere.repository;

import com.probestack.forgesphere.document.LintingRuleDocument;
import com.probestack.forgesphere.model.AssetType;
import com.probestack.forgesphere.model.RuleStatus;
import org.springframework.data.mongodb.repository.MongoRepository;
import java.util.List;
import java.util.Optional;

public interface LintingRuleRepository extends MongoRepository<LintingRuleDocument, String> {
    Optional<LintingRuleDocument> findByRuleId(String ruleId);
    boolean existsByRuleId(String ruleId);
    List<LintingRuleDocument> findAllByAssetTypeOrderByDisplayOrderAsc(AssetType assetType);
    List<LintingRuleDocument> findAllByAssetTypeAndStatusOrderByDisplayOrderAsc(AssetType assetType, RuleStatus status);
}