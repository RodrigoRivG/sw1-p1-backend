package com.rodrigo.sw1.app_sw1.repository;

import org.springframework.data.mongodb.repository.MongoRepository;

import com.rodrigo.sw1.app_sw1.models.Procedure;
import java.util.List;

public interface ProcedureRepository extends MongoRepository<Procedure, String>{
    List<Procedure> findByPolicyId(String policyId);
    List<Procedure> findByStartedBy(String startedBy);
    List<Procedure> findByStatus(String status);
}
