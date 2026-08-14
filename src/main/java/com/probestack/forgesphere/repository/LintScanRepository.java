package com.probestack.forgesphere.repository;

import com.probestack.forgesphere.document.LintScanDocument;
import com.probestack.forgesphere.model.AssetType;
import java.util.List;
import java.util.Optional;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface LintScanRepository extends MongoRepository<LintScanDocument, String> {

    Optional<LintScanDocument> findByScanId(String scanId);

    List<LintScanDocument> findAllByAssetTypeOrderByCreateDateDesc(AssetType assetType);
}
