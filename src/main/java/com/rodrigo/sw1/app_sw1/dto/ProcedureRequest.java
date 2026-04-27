package com.rodrigo.sw1.app_sw1.dto;

import java.util.Map;

import lombok.Data;

@Data
public class ProcedureRequest {
    private String policyId;
    private String clientName;
    private String clientEmail;
    private Map<String, Object> clientInfo;
}
