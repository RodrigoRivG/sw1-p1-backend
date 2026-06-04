package com.rodrigo.sw1.app_sw1.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CursorPositionMessage {
    private String documentId;
    private String userId;
    private String userName;
    private Integer position;              // Posición del cursor
    private String color;                  // Color para identificar el cursor del usuario
    private Long timestamp;
}
