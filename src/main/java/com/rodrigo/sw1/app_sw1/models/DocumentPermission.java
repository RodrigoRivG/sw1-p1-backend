package com.rodrigo.sw1.app_sw1.models;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import lombok.Data;

@Data
@Document(collection = "document_permissions")
public class DocumentPermission {
    
    @Id
    private String id;
    private String documentId;            // FK a Document
    private String nodeId;                // Nodo (tarea) en el diagrama de la política
    private String permissionLevel;       // "VIEW", "EDIT", "UPLOAD", "NONE"
    
    // Posibles valores para permissionLevel:
    // - "VIEW"   → Solo lectura
    // - "EDIT"   → Ver y modificar
    // - "UPLOAD" → Subir archivos
    // - "NONE"   → Sin acceso
}
