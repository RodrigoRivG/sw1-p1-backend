package com.rodrigo.sw1.app_sw1.controllers;

import com.rodrigo.sw1.app_sw1.services.DeepLearningService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.Map;

@RestController
@RequestMapping("/api/predictions")
public class DeepLearningController {

    @Autowired
    private DeepLearningService deepLearningService;

    @PostMapping("/delay-risk")
    public ResponseEntity<?> predictDelayRisk(@RequestBody Map<String, Object> body) {
        try {
            int numNodes = (int) body.get("num_nodes");
            int numParallel = (int) body.get("num_parallel");
            double avgNodeTime = Double.parseDouble(body.get("avg_node_time").toString());
            double departmentLoad = Double.parseDouble(body.get("department_load").toString());
            
            LocalDateTime now = LocalDateTime.now();
            int hourOfDay = now.getHour();
            int dayOfWeek = now.getDayOfWeek().getValue() - 1;

            return ResponseEntity.ok(deepLearningService.predictDelayRisk(
                numNodes, numParallel, avgNodeTime, departmentLoad, hourOfDay, dayOfWeek
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PostMapping("/anomaly")
    public ResponseEntity<?> predictAnomaly(@RequestBody Map<String, Object> body) {
        try {
            int numNodes = (int) body.get("num_nodes");
            int numParallel = (int) body.get("num_parallel");
            double avgNodeTime = Double.parseDouble(body.get("avg_node_time").toString());
            double departmentLoad = Double.parseDouble(body.get("department_load").toString());

            LocalDateTime now = LocalDateTime.now();
            int hourOfDay = now.getHour();
            int dayOfWeek = now.getDayOfWeek().getValue() - 1;

            return ResponseEntity.ok(deepLearningService.predictAnomaly(
                numNodes, numParallel, avgNodeTime, departmentLoad, hourOfDay, dayOfWeek
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PostMapping("/best-route")
    public ResponseEntity<?> predictBestRoute(@RequestBody Map<String, Object> body) {
        try {
            double routeAAvgTime = Double.parseDouble(body.get("route_a_avg_time").toString());
            double routeBAvgTime = Double.parseDouble(body.get("route_b_avg_time").toString());
            double routeALoad = Double.parseDouble(body.get("route_a_load").toString());
            double routeBLoad = Double.parseDouble(body.get("route_b_load").toString());
            int routeANodes = (int) body.get("route_a_nodes");
            int routeBNodes = (int) body.get("route_b_nodes");

            return ResponseEntity.ok(deepLearningService.predictBestRoute(
                routeAAvgTime, routeBAvgTime, routeALoad, routeBLoad, routeANodes, routeBNodes
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}
