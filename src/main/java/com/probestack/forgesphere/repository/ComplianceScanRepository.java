package com.probestack.forgesphere.repository;

import com.probestack.forgesphere.document.ComplianceScanDocument;
import com.probestack.forgesphere.model.AssetType;
import java.util.List;
import java.util.Optional;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface ComplianceScanRepository extends MongoRepository<ComplianceScanDocument, String> {

    Optional<ComplianceScanDocument> findByScanId(String scanId);

    List<ComplianceScanDocument> findTop12ByOrderByCreateDateDesc();

    List<ComplianceScanDocument> findTop12ByProjectNameOrderByCreateDateDesc(String projectName);

    List<ComplianceScanDocument> findTop12ByAssetTypeOrderByCreateDateDesc(AssetType assetType);

    List<ComplianceScanDocument> findTop12ByProjectNameAndAssetTypeOrderByCreateDateDesc(
            String projectName, AssetType assetType);
}
