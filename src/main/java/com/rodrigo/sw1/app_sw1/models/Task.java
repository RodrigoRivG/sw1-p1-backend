package com.rodrigo.sw1.app_sw1.models;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.Map;

@Data
@Document(collection = "tasks")
public class Task {
    
    @Id
    private String id;
    private String procedureId; // Relación con el trámite al que pertenece la tarea
    private String userId; // Funcionario asignado a la tarea
    private String nodeId; // Nodo del diagrama al que pertenece la tarea
    private Map<String, Object> report; // Texto, archivos, respuestas, etc. que el funcionario debe cargar para completar la tarea
    private String status = "pending"; // Ej: "pending", "completed", "in_progress", etc.
    private LocalDateTime createdAt = LocalDateTime.now();
    private LocalDateTime startedAt;
    private LocalDateTime finishedAt;
}
