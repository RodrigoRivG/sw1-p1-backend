package com.rodrigo.sw1.app_sw1.models;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Data
@Document(collection = "documents")
public class DocumentModel {
    
    @Id
    private String id;
    private String policyId;              // A qué política pertenece
    private String name;                  // Nombre del archivo (ej: "Contrato.docx")
    private String type;                  // Tipo: pdf, docx, txt, jpg, png, etc.
    private String backblazeUrl;          // URL del archivo actual en Backblaze
    private String currentVersionId;      // ID de la versión actual (FK a DocumentVersion)
    private String createdBy;             // userId que creó el documento
    private LocalDateTime createdAt = LocalDateTime.now();
    private LocalDateTime updatedAt = LocalDateTime.now();
    private List<String> versionIds = new ArrayList<>();  // IDs de todas las versiones del documento
}
