package com.rodrigo.sw1.app_sw1.repository;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;
import com.rodrigo.sw1.app_sw1.models.Policy;

import java.util.List;


@Repository
public interface PolicyRepository extends MongoRepository<Policy, String> {
    List<Policy> findByCreatedBy(String createdBy);
    List<Policy> findByCollaboratorsContaining(String userId);
}
