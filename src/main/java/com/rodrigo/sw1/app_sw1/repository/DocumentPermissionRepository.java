package com.rodrigo.sw1.app_sw1.repository;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;
import com.rodrigo.sw1.app_sw1.models.DocumentPermission;
import java.util.List;

@Repository
public interface DocumentPermissionRepository extends MongoRepository<DocumentPermission, String> {
    List<DocumentPermission> findByDocumentId(String documentId);
    DocumentPermission findByDocumentIdAndNodeId(String documentId, String nodeId);
    List<DocumentPermission> findByNodeId(String nodeId);
}
