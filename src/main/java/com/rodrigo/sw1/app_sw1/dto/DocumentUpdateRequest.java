package com.rodrigo.sw1.app_sw1.dto;

import lombok.Data;

@Data
public class DocumentUpdateRequest {
    private String content;               // Nuevo contenido del documento
    private String changeDescription;     // Descripción del cambio
}
