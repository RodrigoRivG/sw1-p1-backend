package com.rodrigo.sw1.app_sw1.repository;

import com.rodrigo.sw1.app_sw1.models.Department;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface DepartmentRepository extends MongoRepository<Department, String> {

}
