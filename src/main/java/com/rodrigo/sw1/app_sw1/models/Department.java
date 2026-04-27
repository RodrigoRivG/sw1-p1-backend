package com.rodrigo.sw1.app_sw1.models;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Data
@Document(collection = "departments")
public class Department {
    
    @Id
    private String id;
    private String name;
}
