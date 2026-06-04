package com.rodrigo.sw1.app_sw1.dto;

import lombok.Data;

@Data
public class DocumentPermissionResponse {
    private String id;
    private String documentId;
    private String nodeId;
    private String permissionLevel;
}
