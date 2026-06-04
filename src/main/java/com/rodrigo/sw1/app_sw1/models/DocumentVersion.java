package com.rodrigo.sw1.app_sw1.models;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Document(collection = "document_versions")
public class DocumentVersion {
    
    @Id
    private String id;
    private String documentId;            // FK a Document
    private Integer versionNumber;        // 1, 2, 3, etc.
    private String content;               // Para archivos de texto (editables)
    private String backblazeUrl;          // Para PDFs, imágenes, etc. (no editables)
    private String modifiedBy;            // userId que realizó la modificación
    private LocalDateTime modifiedAt = LocalDateTime.now();
    private String changeDescription;     // Qué cambió ("Actualización de cláusulas legales")
    private String previousVersionId;     // ID de la versión anterior (para historial)
}
