package com.rodrigo.sw1.app_sw1.models;

import lombok.Data; //Para Constructores, Getters y Setters
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.index.Indexed;

@Data
@Document(collection = "users")
public class User {
    
    @Id
    private String id;
    private String name;

    @Indexed(unique = true)
    private String email;

    private String password;
    private String role;
    private String departmentId;
    private Boolean active = true;
    private java.time.LocalDate createdAt = java.time.LocalDate.now();
}
