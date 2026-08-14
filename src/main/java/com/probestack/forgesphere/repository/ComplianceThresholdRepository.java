package com.probestack.forgesphere.repository;

import com.probestack.forgesphere.document.ComplianceThresholdDocument;
import com.probestack.forgesphere.model.AssetType;
import com.probestack.forgesphere.model.ScanKind;
import java.util.List;
import java.util.Optional;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface ComplianceThresholdRepository extends MongoRepository<ComplianceThresholdDocument, String> {

    Optional<ComplianceThresholdDocument> findByScanKindAndAssetType(ScanKind scanKind, AssetType assetType);

    List<ComplianceThresholdDocument> findAllByAssetType(AssetType assetType);

    List<ComplianceThresholdDocument> findAllByScanKind(ScanKind scanKind);
}
