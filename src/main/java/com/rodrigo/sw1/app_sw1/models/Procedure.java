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
    private String policyId; 
    private String currentNodeId;
    private String name;
    private String description;
    private String clientName; 
    private String clientEmail;
    private Map<String, Object> clientInfo; 
    private String status = "in_progress"; // Ej: "pending", "in_progress", "completed"
    private int pendingParallelTasks = 0; //para políticas en paralelo
    private String startedBy; // Usuario que inició el trámite
    
    // Para los cuellos de botella
    private LocalDateTime createdAt = LocalDateTime.now();
    private LocalDateTime updatedAt = LocalDateTime.now();
}
