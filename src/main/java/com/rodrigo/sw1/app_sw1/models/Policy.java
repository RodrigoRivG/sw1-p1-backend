package com.rodrigo.sw1.app_sw1.models;

import java.util.List;
import java.util.Map;
import java.time.LocalDateTime;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import lombok.Data;

@Data
@Document(collection = "bussiness_policies")
public class Policy {

    @Id
    private String id;
    private String name;
    private String description;
    private Map<String, Object> diagram;
    private String status = "active";
    private String createdBy;
    private List<String> collaborators; // Lista de colaboradores
    private LocalDateTime createdAt = LocalDateTime.now();
    private LocalDateTime updatedAt = LocalDateTime.now();
}