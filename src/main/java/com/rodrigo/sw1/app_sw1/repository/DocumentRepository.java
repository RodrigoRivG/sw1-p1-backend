package com.rodrigo.sw1.app_sw1.repository;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;
import com.rodrigo.sw1.app_sw1.models.DocumentModel;
import java.util.List;

@Repository
public interface DocumentRepository extends MongoRepository<DocumentModel, String> {
    List<DocumentModel> findByPolicyId(String policyId);
    DocumentModel findByIdAndPolicyId(String id, String policyId);
}
