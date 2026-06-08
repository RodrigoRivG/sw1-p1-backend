package com.rodrigo.sw1.app_sw1.services;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.Map;

@Service
public class DeepLearningService {

    @Value("${deeplearning.api.url}")
    private String apiUrl;

    private final WebClient webClient = WebClient.create();

    public Map predict(String endpoint, Map<String, Object> features) {
        return webClient.post()
                .uri(apiUrl + endpoint)
                .header("Content-Type", "application/json")
                .bodyValue(features)
                .retrieve()
                .bodyToMono(Map.class)
                .block();
    }

    public Map predictDelayRisk(int numNodes, int numParallel, double avgNodeTime,
                                 double departmentLoad, int hourOfDay, int dayOfWeek) {
        Map<String, Object> features = Map.of(
            "num_nodes", numNodes,
            "num_parallel", numParallel,
            "avg_node_time", avgNodeTime,
            "department_load", departmentLoad,
            "hour_of_day", hourOfDay,
            "day_of_week", dayOfWeek
        );
        return predict("/predict/delay-risk", features);
    }

    public Map predictAnomaly(int numNodes, int numParallel, double avgNodeTime,
                               double departmentLoad, int hourOfDay, int dayOfWeek) {
        Map<String, Object> features = Map.of(
            "num_nodes", numNodes,
            "num_parallel", numParallel,
            "avg_node_time", avgNodeTime,
            "department_load", departmentLoad,
            "hour_of_day", hourOfDay,
            "day_of_week", dayOfWeek
        );
        return predict("/predict/anomaly", features);
    }

    public Map predictBestRoute(double routeAAvgTime, double routeBAvgTime,
                                 double routeALoad, double routeBLoad,
                                 int routeANodes, int routeBNodes) {
        Map<String, Object> features = Map.of(
            "route_a_avg_time", routeAAvgTime,
            "route_b_avg_time", routeBAvgTime,
            "route_a_load", routeALoad,
            "route_b_load", routeBLoad,
            "route_a_nodes", routeANodes,
            "route_b_nodes", routeBNodes
        );
        return predict("/predict/best-route", features);
    }
}