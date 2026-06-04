package com.rodrigo.sw1.app_sw1.dto;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class DocumentVersionResponse {
    private String id;
    private String documentId;
    private Integer versionNumber;
    private String content;
    private String backblazeUrl;
    private String modifiedBy;
    private LocalDateTime modifiedAt;
    private String changeDescription;
}
