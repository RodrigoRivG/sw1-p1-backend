package com.rodrigo.sw1.app_sw1.dto;

import lombok.Data;

import java.util.Map;
import java.util.List;

@Data
public class PolicyRequest {
    private String name;
    private String description;
    private String createdBy;
    private Map<String, Object> diagram;
    private String status;
    private List<String> collaborators;
}
