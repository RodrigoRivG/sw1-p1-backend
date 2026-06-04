package com.rodrigo.sw1.app_sw1.dto;

import lombok.Data;
import java.util.List;

@Data
public class DocumentRequest {
    private String policyId;              // A qué política pertenece
    private String name;                  // Nombre del archivo
    private String type;                  // pdf, docx, txt, jpg, png, etc.
    private String content;               // Para archivos de texto
    private List<DocumentPermissionRequest> permissions; // Permisos por nodo
}
