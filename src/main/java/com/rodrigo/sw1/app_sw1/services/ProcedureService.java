package com.rodrigo.sw1.app_sw1.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.rodrigo.sw1.app_sw1.repository.ProcedureRepository;
import com.rodrigo.sw1.app_sw1.repository.TaskRepository;
import com.rodrigo.sw1.app_sw1.repository.PolicyRepository;

import com.rodrigo.sw1.app_sw1.models.Procedure;
import com.rodrigo.sw1.app_sw1.models.Policy;
import com.rodrigo.sw1.app_sw1.models.Task;

import com.rodrigo.sw1.app_sw1.dto.ProcedureRequest;

import java.util.Map;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class ProcedureService {
    
    @Autowired
    private ProcedureRepository procedureRepository;

    @Autowired
    private PolicyRepository policyRepository;

    @Autowired
    private TaskRepository taskRepository;

    public Procedure createProcedure(ProcedureRequest request, String userId) {
        Policy policy = policyRepository.findById(request.getPolicyId())
            .orElseThrow(() -> new RuntimeException("Política no encontrada"));

        Map<String, Object> diagram = policy.getDiagram();
        String firstNodeId = getFirstNodeId(diagram);

        Procedure procedure = new Procedure();
        procedure.setPolicyId(request.getPolicyId());
        procedure.setClientName(request.getClientName());
        procedure.setClientEmail(request.getClientEmail());
        procedure.setClientInfo(request.getClientInfo());
        procedure.setCurrentNodeId(firstNodeId);
        procedure.setStartedBy(userId);
        procedureRepository.save(procedure);

        //createTask(procedure.getId(), firstNodeId, diagram);
        createTask(procedure, firstNodeId, diagram);

        return procedure;
    }

    public Procedure cancelProcedure(String id) {
        Procedure procedure = getById(id);
        procedure.setStatus("cancelled");
        procedure.setUpdatedAt(LocalDateTime.now());
        return procedureRepository.save(procedure);
    }

    public List<Procedure> getAll() {
        return procedureRepository.findAll();
    }

    public Procedure getById(String id) {
        return procedureRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Trámite no encontrado"));
    }

    @SuppressWarnings("unchecked")
    private String getFirstNodeId(Map<String, Object> diagram) {
        List<Map<String, Object>> nodes = (List<Map<String, Object>>) diagram.get("nodes");
        List<Map<String, Object>> edges = (List<Map<String, Object>>) diagram.get("edges");

        if (nodes == null || nodes.isEmpty()) {
            throw new RuntimeException("Diagrama sin nodos");
        }

        //Buscamos el nodo "start"
        String startNodeId = null;
        for (Map<String, Object> node : nodes) {
            Map<String, Object> data = (Map<String, Object>) node.get("data");
            if (data != null && "start".equals(data.get("type"))) {
                startNodeId = (String) node.get("id");
                break;
            }
        }

        if (startNodeId == null) {
            throw new RuntimeException("El diagrama no tiene inicio");
        }

        //Buscamos el siguiente nodo start -> nodo-2 (task 1)
        for (Map<String, Object> edge : edges) {
            if (edge.get("source").equals(startNodeId)) {
                return (String) edge.get("target");
            }
        }

        throw new RuntimeException("El nodo de inicio no tiene conexión");
    }

    public List<Procedure> getByClientEmail(String email) {
        return procedureRepository.findByClientEmail(email);
    }

    @SuppressWarnings("unchecked")
    private void createTask(Procedure procedure, String nodeId, Map<String, Object> diagram) {
        List<Map<String, Object>> nodes = (List<Map<String, Object>>) diagram.get("nodes");
        String userId = null;

        for (Map<String, Object> node : nodes) {
            if (node.get("id").equals(nodeId)) {
                Map<String, Object> data = (Map<String, Object>) node.get("data");
                if (data != null) {
                    userId = (String) data.get("userId");
                }
                break;
            }
        }

        Task newTask = new Task();
        newTask.setProcedureId(procedure.getId());
        newTask.setNodeId(nodeId);
        newTask.setUserId(userId);
        newTask.setStartedAt(LocalDateTime.now());
        taskRepository.save(newTask);
    }
}
