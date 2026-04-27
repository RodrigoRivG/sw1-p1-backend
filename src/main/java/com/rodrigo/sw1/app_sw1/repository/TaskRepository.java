package com.rodrigo.sw1.app_sw1.repository;

import org.springframework.data.mongodb.repository.MongoRepository;

import com.rodrigo.sw1.app_sw1.models.Task;

import java.util.List;

public interface TaskRepository extends MongoRepository<Task, String>{
    List<Task> findByProcedureId(String procedureId);
    List<Task> findByUserId(String userId);
    List<Task> findByUserIdAndStatus(String userId, String status);
    List<Task> findByStatus(String status);
}
