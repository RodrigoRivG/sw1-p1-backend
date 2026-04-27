package com.rodrigo.sw1.app_sw1.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.rodrigo.sw1.app_sw1.repository.ProcedureRepository;
import com.rodrigo.sw1.app_sw1.repository.TaskRepository;
import com.rodrigo.sw1.app_sw1.repository.PolicyRepository;
import com.rodrigo.sw1.app_sw1.repository.UserRepository;

import com.rodrigo.sw1.app_sw1.models.Task;
import com.rodrigo.sw1.app_sw1.models.Policy;
import com.rodrigo.sw1.app_sw1.models.User;

import java.util.*;
import java.time.Duration;

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
}
