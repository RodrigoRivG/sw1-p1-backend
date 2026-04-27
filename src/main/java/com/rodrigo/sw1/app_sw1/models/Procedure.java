package com.rodrigo.sw1.app_sw1.models;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.Map;

// Nota: modelo para los trámites

@Data
@Document(collection = "procedures")
public class Procedure {
    
    @Id
    private String id;
    private String policyId; // Relación con la política de negocio
    private String currentNodeId;
    private String name;
    private String description;
    private String clientName; // Lo que menos me importa de momento
    private String clientEmail;
    private Map<String, Object> clientInfo; // Lo que menos me importa de momento
    private String status = "in_progress"; // Ej: "pending", "in_progress", "completed"
    private String startedBy; // Usuario que inició el trámite
    
    // Para los cuellos de botella
    private LocalDateTime createdAt = LocalDateTime.now();
    private LocalDateTime updatedAt = LocalDateTime.now();
}
