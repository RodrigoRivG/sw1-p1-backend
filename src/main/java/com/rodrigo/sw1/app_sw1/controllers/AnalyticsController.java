package com.rodrigo.sw1.app_sw1.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.rodrigo.sw1.app_sw1.services.AnalyticsService;
import com.rodrigo.sw1.app_sw1.services.GeminiService;

import java.util.Map;

@RestController
@RequestMapping("/api/analytics")
public class AnalyticsController {

    @Autowired
    private AnalyticsService analyticsService;

    @Autowired
    private GeminiService geminiService;

    @GetMapping
    public ResponseEntity<Map<String, Object>> getAnalytics() {
        return ResponseEntity.ok(analyticsService.getAnalytics());
    } 

    @GetMapping("/ai-analysis")
    public ResponseEntity<?> getAiAnalysis() {
        try {
            Map<String, Object> analytics = analyticsService.getAnalytics();

            String prompt = """
                    Eres un experto en análisis de procesos de negocio.
                    Analiza estos datos de un sistema de workflow y detecta cuellos de botella.
                    
                    Datos:
                    - Tiempo promedio por nodo (en minutos): %s
                    - Tiempo promedio por funcionario (en minutos): %s
                    - Nodo con mayor tiempo de atención: %s
                    - Funcionario más eficiente: %s
                    - Total de trámites: %s
                    - Trámites completados: %s
                    - Trámites en proceso: %s
                    - Total de tareas completadas: %s
                    
                    Responde en español con:
                    1. Resumen general del rendimiento
                    2. Cuellos de botella identificados
                    3. Recomendaciones concretas para mejorar
                    """.formatted(
                        analytics.get("avgTimeByNode"),
                        analytics.get("avgTimeByUser"),
                        analytics.get("bottleneckNode"),
                        analytics.get("mostEfficientUser"),
                        analytics.get("totalProcedures"),
                        analytics.get("completedProcedures"),
                        analytics.get("inProgressProcedures"),
                        analytics.get("totalCompletedTasks")
                    );
            
            String aiResponse = geminiService.generateContent(prompt);
            return ResponseEntity.ok(Map.of(
                "analytics", analytics,
                "aiAnalysis", aiResponse
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    } 
}