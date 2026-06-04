package com.rodrigo.sw1.app_sw1.models;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ActiveDocumentUser {
    private String userId;
    private String userName;
    private String sessionId;
    private String color;                  // Color para identificar visualmente
    private Long joinedAt;
    private Integer cursorPosition;
}
