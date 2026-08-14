package com.probestack.forgesphere.repository;

import com.probestack.forgesphere.document.ResourceExemptionDocument;
import com.probestack.forgesphere.model.AssetType;
import com.probestack.forgesphere.model.ScanKind;
import java.util.List;
import java.util.Optional;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface ResourceExemptionRepository extends MongoRepository<ResourceExemptionDocument, String> {

    Optional<ResourceExemptionDocument> findByScanKindAndAssetTypeAndAssetName(
            ScanKind scanKind, AssetType assetType, String assetName);

    List<ResourceExemptionDocument> findAllByAssetType(AssetType assetType);

    List<ResourceExemptionDocument> findAllByScanKindAndAssetType(ScanKind scanKind, AssetType assetType);
}
