package com.rodrigo.sw1.app_sw1.dto;

import lombok.Data;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class DocumentResponse {
    private String id;
    private String policyId;
    private String name;
    private String type;
    private String backblazeUrl;
    private String currentVersionId;
    private String createdBy;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private List<String> versionIds;
    private List<DocumentVersionResponse> versions;
    private List<DocumentPermissionResponse> permissions;
}
