package com.probestack.forgesphere.repository;

import com.probestack.forgesphere.document.OwaspScanDocument;
import com.probestack.forgesphere.model.AssetType;
import java.util.List;
import java.util.Optional;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface OwaspScanRepository extends MongoRepository<OwaspScanDocument, String> {

    Optional<OwaspScanDocument> findByScanId(String scanId);

    List<OwaspScanDocument> findTop12ByOrderByCreateDateDesc();

    List<OwaspScanDocument> findTop12ByProjectNameOrderByCreateDateDesc(String projectName);

    List<OwaspScanDocument> findTop12ByAssetTypeOrderByCreateDateDesc(AssetType assetType);

    List<OwaspScanDocument> findTop12ByProjectNameAndAssetTypeOrderByCreateDateDesc(
            String projectName, AssetType assetType);
}
