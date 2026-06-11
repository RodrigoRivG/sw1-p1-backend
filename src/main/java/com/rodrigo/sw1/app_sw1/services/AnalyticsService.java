package com.rodrigo.sw1.app_sw1.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.rodrigo.sw1.app_sw1.repository.ProcedureRepository;
import com.rodrigo.sw1.app_sw1.repository.TaskRepository;
import com.rodrigo.sw1.app_sw1.repository.PolicyRepository;
import com.rodrigo.sw1.app_sw1.repository.UserRepository;

import com.rodrigo.sw1.app_sw1.models.Task;
import com.rodrigo.sw1.app_sw1.models.Policy;
import com.rodrigo.sw1.app_sw1.models.Procedure;
import com.rodrigo.sw1.app_sw1.models.User;

import java.util.*;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Service
public class AnalyticsService {
    
    @Autowired
    private ProcedureRepository procedureRepository;

    @Autowired
    private PolicyRepository policyRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private TaskRepository taskRepository;

    @SuppressWarnings("unchecked")
    public Map<String, Object> getAnalytics() {
        List<Task> completedTasks = taskRepository.findByStatus("completed");

        // Mapa de nodeId -> label
        Map<String, String> nodeLabelMap = buildNodeLabelMap();

        // Mapa de userId -> nombre
        Map<String, String> userNameMap = buildUserNameMap();

        // Tiempo promedio por nodo
        Map<String, List<Long>> timesByNode = new HashMap<>();
        for (Task task : completedTasks) {
            if (task.getStartedAt() != null && task.getFinishedAt() != null) {
                long minutes = Duration.between(task.getStartedAt(), task.getFinishedAt()).toMinutes();
                timesByNode.computeIfAbsent(task.getNodeId(), k -> new ArrayList<>()).add(minutes);
            }
        }

        Map<String, Double> avgTimeByNode = new HashMap<>();
        for (Map.Entry<String, List<Long>> entry : timesByNode.entrySet()) {
            double avg = entry.getValue().stream().mapToLong(Long::longValue).average().orElse(0);
            String label = nodeLabelMap.getOrDefault(entry.getKey(), entry.getKey());
            avgTimeByNode.put(label, avg);
        }

        // Nodo más lento
        String bottleneckNodeId = timesByNode.entrySet().stream()
                .max(Comparator.comparingDouble(e -> e.getValue().stream().mapToLong(Long::longValue).average().orElse(0)))
                .map(Map.Entry::getKey)
                .orElse("N/A");
        String bottleneckNode = nodeLabelMap.getOrDefault(bottleneckNodeId, bottleneckNodeId);

        // Tiempo promedio por funcionario
        Map<String, List<Long>> timesByUser = new HashMap<>();
        for (Task task : completedTasks) {
            if (task.getUserId() != null && task.getStartedAt() != null && task.getFinishedAt() != null) {
                long minutes = Duration.between(task.getStartedAt(), task.getFinishedAt()).toMinutes();
                timesByUser.computeIfAbsent(task.getUserId(), k -> new ArrayList<>()).add(minutes);
            }
        }

        Map<String, Double> avgTimeByUser = new HashMap<>();
        for (Map.Entry<String, List<Long>> entry : timesByUser.entrySet()) {
            double avg = entry.getValue().stream().mapToLong(Long::longValue).average().orElse(0);
            String name = userNameMap.getOrDefault(entry.getKey(), entry.getKey());
            avgTimeByUser.put(name, avg);
        }

        // Funcionario más eficiente
        String mostEfficientUserId = timesByUser.entrySet().stream()
                .min(Comparator.comparingDouble(e -> e.getValue().stream().mapToLong(Long::longValue).average().orElse(0)))
                .map(Map.Entry::getKey)
                .orElse("N/A");
        String mostEfficientUser = userNameMap.getOrDefault(mostEfficientUserId, mostEfficientUserId);

        // Totales de trámites
        long totalProcedures = procedureRepository.count();
        long completedProcedures = procedureRepository.findByStatus("completed").size();
        long inProgressProcedures = procedureRepository.findByStatus("in_progress").size();

        Map<String, Object> analytics = new HashMap<>();
        analytics.put("avgTimeByNode", avgTimeByNode);
        analytics.put("avgTimeByUser", avgTimeByUser);
        analytics.put("bottleneckNode", bottleneckNode);
        analytics.put("mostEfficientUser", mostEfficientUser);
        analytics.put("totalProcedures", totalProcedures);
        analytics.put("completedProcedures", completedProcedures);
        analytics.put("inProgressProcedures", inProgressProcedures);
        analytics.put("totalCompletedTasks", completedTasks.size());

        return analytics;
    }

    @SuppressWarnings("unchecked")
    private Map<String, String> buildNodeLabelMap() {
        Map<String, String> nodeLabelMap = new HashMap<>();
        List<Policy> policies = policyRepository.findAll();

        for (Policy policy : policies) {
            Map<String, Object> diagram = policy.getDiagram();
            if (diagram == null) continue;

            List<Map<String, Object>> nodes = (List<Map<String, Object>>) diagram.get("nodes");
            if (nodes == null) continue;

            for (Map<String, Object> node : nodes) {
                String nodeId = (String) node.get("id");
                Map<String, Object> data = (Map<String, Object>) node.get("data");
                if (data != null && nodeId != null) {
                    String label = (String) data.get("label");
                    if (label != null) {
                        nodeLabelMap.put(nodeId, label);
                    }
                }
            }
        }

        return nodeLabelMap;
    }

    private Map<String, String> buildUserNameMap() {
        Map<String, String> userNameMap = new HashMap<>();
        List<User> users = userRepository.findAll();

        for (User user : users) {
            userNameMap.put(user.getId(), user.getName());
        }

        return userNameMap;
    }

    /* 

    public Map<String, Object> getDynamicReport(String startDate, String endDate, 
                                              String department, String type) {
        // Convertir fechas
        LocalDateTime start = startDate != null ? 
            LocalDate.parse(startDate).atStartOfDay() : LocalDateTime.of(2000, 1, 1, 0, 0);
        LocalDateTime end = endDate != null ? 
            LocalDate.parse(endDate).atTime(23, 59, 59) : LocalDateTime.now();

        // Obtener tareas completadas en el rango de fechas
        List<Task> tasks = taskRepository.findByStatusAndCreatedAtBetween("completed", start, end);

        // Filtrar por departamento si se especificó
        if (department != null) {
            Map<String, String> nodeLabelMap = buildNodeLabelMap();
            Map<String, String> nodeDepartmentMap = buildNodeDepartmentMap();
            tasks = tasks.stream()
                    .filter(t -> {
                        String dept = nodeDepartmentMap.get(t.getNodeId());
                        return dept != null && dept.toLowerCase().contains(department.toLowerCase());
                    })
                    .collect(java.util.stream.Collectors.toList());
        }

        // Calcular KPIs
        Map<String, String> userNameMap = buildUserNameMap();
        Map<String, String> nodeLabelMap = buildNodeLabelMap();

        Map<String, List<Long>> timesByNode = new HashMap<>();
        Map<String, List<Long>> timesByUser = new HashMap<>();

        for (Task task : tasks) {
            if (task.getStartedAt() != null && task.getFinishedAt() != null) {
                long minutes = Duration.between(task.getStartedAt(), task.getFinishedAt()).toMinutes();
                String nodeLabel = nodeLabelMap.getOrDefault(task.getNodeId(), task.getNodeId());
                String userName = userNameMap.getOrDefault(task.getUserId(), task.getUserId());
                timesByNode.computeIfAbsent(nodeLabel, k -> new ArrayList<>()).add(minutes);
                timesByUser.computeIfAbsent(userName, k -> new ArrayList<>()).add(minutes);
            }
        }

        Map<String, Double> avgTimeByNode = new HashMap<>();
        timesByNode.forEach((k, v) -> avgTimeByNode.put(k, 
            v.stream().mapToLong(Long::longValue).average().orElse(0)));

        Map<String, Double> avgTimeByUser = new HashMap<>();
        timesByUser.forEach((k, v) -> avgTimeByUser.put(k, 
            v.stream().mapToLong(Long::longValue).average().orElse(0)));

        String bottleneck = avgTimeByNode.entrySet().stream()
                .max(Map.Entry.comparingByValue())
                .map(Map.Entry::getKey).orElse("N/A");

        String mostEfficient = avgTimeByUser.entrySet().stream()
                .min(Map.Entry.comparingByValue())
                .map(Map.Entry::getKey).orElse("N/A");

        Map<String, Object> report = new HashMap<>();
        report.put("totalTasks", tasks.size());
        report.put("avgTimeByNode", avgTimeByNode);
        report.put("avgTimeByUser", avgTimeByUser);
        report.put("bottleneckNode", bottleneck);
        report.put("mostEfficientUser", mostEfficient);
        report.put("period", Map.of(
            "from", startDate != null ? startDate : "sin límite",
            "to", endDate != null ? endDate : "hoy"
        ));
        report.put("department", department != null ? department : "todos");

        return report;
    }
    */

    @SuppressWarnings("unchecked")
    private Map<String, String> buildNodeDepartmentMap() {
        Map<String, String> nodeDepartmentMap = new HashMap<>();
        List<Policy> policies = policyRepository.findAll();

        for (Policy policy : policies) {
            Map<String, Object> diagram = policy.getDiagram();
            if (diagram == null) continue;

            List<Map<String, Object>> nodes = (List<Map<String, Object>>) diagram.get("nodes");
            List<Map<String, Object>> swimlanes = (List<Map<String, Object>>) diagram.get("swimlanes");
            if (nodes == null || swimlanes == null) continue;

            Map<String, String> swimlaneLabels = new HashMap<>();
            for (Map<String, Object> swimlane : swimlanes) {
                swimlaneLabels.put((String) swimlane.get("id"), (String) swimlane.get("label"));
            }

            for (Map<String, Object> node : nodes) {
                String nodeId = (String) node.get("id");
                Map<String, Object> data = (Map<String, Object>) node.get("data");
                if (data != null && nodeId != null) {
                    String deptId = (String) data.get("departmentId");
                    if (deptId != null) {
                        nodeDepartmentMap.put(nodeId, swimlaneLabels.getOrDefault(deptId, deptId));
                    }
                }
            }
        }
        return nodeDepartmentMap;
    }

    
    public Map<String, Object> getDynamicReport(String startDate, String endDate,
                                              String department, String type, String client) {
        LocalDateTime start = startDate != null ?
            LocalDate.parse(startDate).atStartOfDay() : LocalDateTime.of(2000, 1, 1, 0, 0);
        LocalDateTime end = endDate != null ?
            LocalDate.parse(endDate).atTime(23, 59, 59) : LocalDateTime.now();

        // Obtener trámites en el rango de fechas
        List<Procedure> procedures = procedureRepository.findByCreatedAtBetween(start, end);

        // Filtrar por departamento si se especificó
        Map<String, String> nodeDepartmentMap = buildNodeDepartmentMap();
        if (department != null) {
            procedures = procedures.stream()
                    .filter(p -> {
                        String dept = nodeDepartmentMap.get(p.getCurrentNodeId());
                        return dept != null && dept.toLowerCase().contains(department.toLowerCase());
                    })
                    .collect(java.util.stream.Collectors.toList());
        }

        // Filtrar por cliente (nombre o email) si se especificó
        if (client != null) {
            procedures = procedures.stream()
                    .filter(p -> (p.getClientName() != null && p.getClientName().toLowerCase().contains(client.toLowerCase()))
                              || (p.getClientEmail() != null && p.getClientEmail().toLowerCase().contains(client.toLowerCase())))
                    .collect(java.util.stream.Collectors.toList());
        }

        // Armar lista de trámites con detalles
        List<Map<String, Object>> procedureDetails = new ArrayList<>();
        for (Procedure procedure : procedures) {
            Map<String, Object> detail = new HashMap<>();
            detail.put("id", procedure.getId());
            detail.put("policyId", procedure.getPolicyId());
            detail.put("clientName", procedure.getClientName());
            detail.put("status", procedure.getStatus());
            detail.put("createdAt", procedure.getCreatedAt());

            // Nombre de la política
            policyRepository.findById(procedure.getPolicyId()).ifPresent(policy -> {
                detail.put("policyName", policy.getName());
            });

            // Tiempo total del trámite en minutos
            if (procedure.getUpdatedAt() != null && procedure.getCreatedAt() != null) {
                long minutes = Duration.between(procedure.getCreatedAt(), procedure.getUpdatedAt()).toMinutes();
                detail.put("totalMinutes", minutes);
            } else {
                detail.put("totalMinutes", "En proceso");
            }

            procedureDetails.add(detail);
        }

        Map<String, Object> report = new HashMap<>();
            report.put("procedures", procedureDetails);
            report.put("totalProcedures", procedures.size());
            report.put("period", Map.of(
            "from", startDate != null ? startDate : "sin límite",
            "to", endDate != null ? endDate : "hoy"
        ));
        report.put("department", department != null ? department : "todos");

        return report;
    }
}
