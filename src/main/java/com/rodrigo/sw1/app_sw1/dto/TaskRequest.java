package com.rodrigo.sw1.app_sw1.dto;

import lombok.Data;

import java.util.Map;

@Data
public class TaskRequest {
    private Map<String, Object> report;
}
