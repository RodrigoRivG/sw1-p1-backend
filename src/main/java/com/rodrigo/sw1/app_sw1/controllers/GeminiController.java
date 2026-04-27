package com.rodrigo.sw1.app_sw1.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.rodrigo.sw1.app_sw1.services.GeminiService;

import java.util.Map;

@RestController
@RequestMapping("/api/ia")
public class GeminiController {
    
    @Autowired
    private GeminiService geminiService;

    @PostMapping("/prompt")
    public ResponseEntity<?> prompt(@RequestBody Map<String, String> body) {
        try {
            String prompt = body.get("prompt");
            String response = geminiService.generateContent(prompt);
            return ResponseEntity.ok(Map.of("response", response));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PostMapping("/desing-flow")
    public ResponseEntity<?> desingFlow(@RequestBody Map<String, Object> body) {
        try {
            String prompt = """
                Eres un asistente experto en diseño de flujos de trabajo.
                El usuario tiene un diagrama existente y quiere modificarlo con una instrucción.
                
                Diagrama actual:
                %s
                
                Instrucción del usuario: %s
                
                Reglas:
                - Devuelve el diagrama completo modificado en JSON
                - Los IDs de nodos nuevos deben ser únicos, usa el formato "n-{timestamp}"
                - Los IDs de edges nuevos deben ser únicos, usa el formato "e-{timestamp}"
                - Los tipos de nodo son: start, end, task, decision, fork, join
                - Los tipos de edge son: sequential, conditional, parallel
                - Si el usuario pide agregar una tarea, agrégala con userId y departmentId vacíos
                - Mantén todos los nodos y edges existentes a menos que el usuario pida eliminar algo
                - Responde SOLO con el JSON puro, sin bloques de markdown ni explicaciones
                
                La estructura EXACTA de cada nodo debe ser:
                {
                    "id": "n-123456",
                    "data": {
                        "label": "nombre de la tarea",
                        "type": "task",
                        "userId": "",
                        "departmentId": "",
                        "color": "#a27ea8"
                    }
                }

                Estructura del JSON de respuesta:
                {
                    "swimlanes": [...],
                    "nodes": [...],
                    "edges": [...]
                }
                """.formatted(body.get("diagram").toString(), body.get("instruction"));
            
            String response = geminiService.generateContent(prompt);
            String clean = response.replaceAll("```json", "").replaceAll("```", "").trim();
            return ResponseEntity.ok(Map.of("diagram", clean));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PostMapping("/generate-report")
    public ResponseEntity<?> generateReport(@RequestBody Map<String, String> body) {
        try {
            String prompt = """
                Eres un asistente que ayuda a funcionarios a redactar reportes de trabajo.
                El funcionario te describe lo que hizo en su tarea y tú debes generar un reporte
                profesional, claro y conciso en español.
                Tarea: %s
                Descripción del funcionario: %s
                Responde SOLO con el texto del reporte, sin títulos ni explicaciones adicionales.
                """.formatted(body.get("taskLabel"), body.get("description"));
            String response = geminiService.generateContent(prompt);
            return ResponseEntity.ok(Map.of("report", response));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

}
