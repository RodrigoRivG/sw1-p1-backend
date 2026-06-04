package com.rodrigo.sw1.app_sw1.dto;

import lombok.Data;

@Data
public class DocumentPermissionRequest {
    private String nodeId;
    private String permissionLevel;  // "VIEW", "EDIT", "UPLOAD", "NONE"
}
