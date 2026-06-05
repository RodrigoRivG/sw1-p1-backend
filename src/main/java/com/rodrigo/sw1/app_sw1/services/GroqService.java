package com.rodrigo.sw1.app_sw1.services;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.List;
import java.util.Map;

@Service
public class GroqService {

    @Value("${groq.api.key}")
    private String apiKey;

    @Value("${groq.api.url}")
    private String apiUrl;

    @Value("${groq.model}")
    private String model;

    private final WebClient webClient = WebClient.create();

    public String generateContent(String prompt) {
        Map<String, Object> body = Map.of(
            "model", model,
            "messages", List.of(
                Map.of("role", "user", "content", prompt)
            )
        );

        Map response = webClient.post()
                .uri(apiUrl)
                .header("Content-Type", "application/json")
                .header("Authorization", "Bearer " + apiKey)
                .bodyValue(body)
                .retrieve()
                .bodyToMono(Map.class)
                .block();

        List<Map> choices = (List<Map>) response.get("choices");
        Map message = (Map) choices.get(0).get("message");
        return (String) message.get("content");
    }

    public Map<String, Object> extractReportFilters(String userQuery) {
        String prompt = """
            Eres un extractor de filtros para reportes de un sistema de gestión de procesos.
            El usuario describe en lenguaje natural qué reporte quiere.
            Extrae los filtros y devuelve SOLO un JSON con esta estructura exacta, sin explicaciones ni markdown:
            {
                "startDate": "YYYY-MM-DD o null",
                "endDate": "YYYY-MM-DD o null",
                "department": "nombre del departamento o null",
                "type": "tasks/procedures/analytics o null"
            }
            
            Reglas:
            - Si no menciona fecha de inicio, startDate es null
            - Si no menciona fecha de fin, endDate es null
            - Si no menciona departamento, department es null
            - Si no menciona tipo de reporte, type es null
            - Las fechas deben estar en formato YYYY-MM-DD
            - Hoy es %s

            Consulta del usuario: %s
            """.formatted(java.time.LocalDate.now(), userQuery);

        String response = generateContent(prompt);
        String clean = response.replaceAll("```json", "").replaceAll("```", "").trim();

        try {
            return new com.fasterxml.jackson.databind.ObjectMapper().readValue(clean, Map.class);
        } catch(Exception e) {
            throw new RuntimeException("Error al procesar la consulta: " + e.getMessage());
        }
    }
}
