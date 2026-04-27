package com.rodrigo.sw1.app_sw1.dto;

import lombok.Data;

@Data
public class RegisterRequest {
    private String name;
    private String email;
    private String password;
    private String role;
    private String departmentId;
}
