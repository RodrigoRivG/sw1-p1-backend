package com.rodrigo.sw1.app_sw1.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ActiveUserMessage {
    private String documentId;
    private String userId;
    private String userName;
    private String action;                 // "JOIN" o "LEAVE"
    private Integer activeUsers;           // Número total de usuarios activos
    private Long timestamp;
}
