package com.rodrigo.sw1.app_sw1.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DocumentEditMessage {
    private String documentId;
    private String userId;
    private String userName;
    private String content;                // Contenido completo del documento
    private Integer cursorPosition;        // Posición del cursor
    private String changeType;             // "INSERT", "DELETE", "REPLACE"
    private String changeDescription;      // Descripción del cambio
    private Long timestamp;
}
