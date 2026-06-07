package com.probestack.forgesphere.repository;

import com.probestack.forgesphere.document.OwaspRuleDocument;
import com.probestack.forgesphere.model.AssetType;
import com.probestack.forgesphere.model.OwaspCategory;
import com.probestack.forgesphere.model.RuleStatus;
import java.util.List;
import java.util.Optional;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface OwaspRuleRepository extends MongoRepository<OwaspRuleDocument, String> {

    long countByAssetType(AssetType assetType);

    boolean existsByRuleId(String ruleId);

    Optional<OwaspRuleDocument> findByRuleId(String ruleId);

    List<OwaspRuleDocument> findAllByAssetTypeOrderByDisplayOrderAsc(AssetType assetType);

    List<OwaspRuleDocument> findAllByAssetTypeAndStatusOrderByDisplayOrderAsc(AssetType assetType, RuleStatus status);

    List<OwaspRuleDocument> findAllByAssetTypeAndCategoryOrderByDisplayOrderAsc(AssetType assetType, OwaspCategory category);
}
