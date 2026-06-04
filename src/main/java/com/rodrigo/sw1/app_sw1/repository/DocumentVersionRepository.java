package com.rodrigo.sw1.app_sw1.repository;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;
import com.rodrigo.sw1.app_sw1.models.DocumentVersion;
import java.util.Optional;
import java.util.List;

@Repository
public interface DocumentVersionRepository extends MongoRepository<DocumentVersion, String> {
    List<DocumentVersion> findByDocumentIdOrderByVersionNumberDesc(String documentId);
    Optional<DocumentVersion> findByDocumentIdAndVersionNumber(String documentId, Integer versionNumber);
}
