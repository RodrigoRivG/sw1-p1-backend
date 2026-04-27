package com.rodrigo.sw1.app_sw1.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.rodrigo.sw1.app_sw1.repository.TaskRepository;
import com.rodrigo.sw1.app_sw1.repository.PolicyRepository;
import com.rodrigo.sw1.app_sw1.repository.ProcedureRepository;

import com.rodrigo.sw1.app_sw1.dto.TaskRequest;

import com.rodrigo.sw1.app_sw1.models.Procedure;
import com.rodrigo.sw1.app_sw1.models.Policy;
import com.rodrigo.sw1.app_sw1.models.Task;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class TaskService {
    
    @Autowired
    private TaskRepository taskRepository;

    @Autowired
    private PolicyRepository policyRepository;

    @Autowired
    private ProcedureRepository procedureRepository;

    public List<Task> getByUser(String userId) {
        return taskRepository.findByUserId(userId);
    }

    public List<Task> getPendingByUser(String userId) {
        return taskRepository.findByUserIdAndStatus(userId, "pending");
    }

    public Task completeTask(String taskId, TaskRequest request) {
        Task task = taskRepository.findById(taskId)
            .orElseThrow(() -> new RuntimeException("Tarea no encontrada"));

        task.setReport(request.getReport());
        task.setStatus("completed");
        task.setFinishedAt(LocalDateTime.now());
        taskRepository.save(task);

        advanceWorkflow(task);

        return task;
    }

    public List<Map<String, Object>> getEnrichedTasksByUser(String userId) {
        List<Task> tasks = taskRepository.findByUserId(userId);
        List<Map<String, Object>> enriched = new ArrayList<>();

        for (Task task : tasks) {
            Map<String, Object> taskMap = new HashMap<>();
            taskMap.put("id", task.getId());
            taskMap.put("procedureId", task.getProcedureId());
            taskMap.put("userId", task.getUserId());
            taskMap.put("nodeId", task.getNodeId());
            taskMap.put("report", task.getReport());
            taskMap.put("status", task.getStatus());
            taskMap.put("startedAt", task.getStartedAt());
            taskMap.put("finishedAt", task.getFinishedAt());
            taskMap.put("createdAt", task.getCreatedAt());

            try {
                Procedure procedure = procedureRepository.findById(task.getProcedureId()).orElse(null);
                if (procedure != null) {
                    taskMap.put("clientName", procedure.getClientName());
                    Policy policy = policyRepository.findById(procedure.getPolicyId()).orElse(null);
                    if (policy != null) {
                        Map<String, Object> diagram = policy.getDiagram();
                        List<Map<String, Object>> nodes = (List<Map<String, Object>>) diagram.get("nodes");
                        for (Map<String, Object> node : nodes) {
                            if (node.get("id").equals(task.getNodeId())) {
                                Map<String, Object> data = (Map<String, Object>) node.get("data");
                                taskMap.put("nodeLabel", data.get("label"));
                                break;
                            }
                        }
                    }
                }
            } catch (Exception e) {
                taskMap.put("nodeLabel", task.getNodeId());
            }

            enriched.add(taskMap);
        }

        return enriched;
    }

    @SuppressWarnings("unchecked")
    private void advanceWorkflow(Task task) {
        Procedure procedure = procedureRepository.findById(task.getProcedureId())
            .orElseThrow(() -> new RuntimeException("Trámite no encontrado"));

        Policy policy = policyRepository.findById(procedure.getPolicyId())
            .orElseThrow(() -> new RuntimeException("Política no encontrada"));

        Map<String, Object> diagram = policy.getDiagram();
        List<Map<String, Object>> edges = (List<Map<String, Object>>) diagram.get("edges");
        List<Map<String, Object>> nodes = (List<Map<String, Object>>) diagram.get("nodes");

        String nextNodeId = null;
        for (Map<String, Object> edge : edges) {
            if (edge.get("source").equals(task.getNodeId())) {
                nextNodeId = (String) edge.get("target");
                break;
            }
        }

        if (nextNodeId == null) {
            procedure.setStatus("completed");
            procedure.setUpdatedAt(LocalDateTime.now());
            procedureRepository.save(procedure);

            return;
        }

        //Verificar si el siguiente nodo es "end"
        for (Map<String, Object> node : nodes) {
            if (node.get("id").equals(nextNodeId)) {
                Map<String, Object> data = (Map<String, Object>) node.get("data");
                if (data != null && "end".equals(data.get("type"))) {
                    procedure.setStatus("completed");
                    procedure.setUpdatedAt(java.time.LocalDateTime.now());
                    procedureRepository.save(procedure);
                    return;
                }
                break;
            }
        }

        procedure.setCurrentNodeId(nextNodeId);
        procedure.setUpdatedAt(LocalDateTime.now());
        procedureRepository.save(procedure);

        String userId = null;
        for (Map<String, Object> node : nodes) {
            if (node.get("id").equals(nextNodeId)) {
                Map<String, Object> data = (Map<String, Object>) node.get("data");
                if (data != null) {
                    userId = (String) data.get("userId");
                }
                break;
            }
        }

        Task newTask = new Task();
        newTask.setProcedureId(procedure.getId());
        newTask.setNodeId(nextNodeId);
        newTask.setUserId(userId);
        newTask.setCreatedAt(LocalDateTime.now());
        taskRepository.save(newTask);
    }
}
