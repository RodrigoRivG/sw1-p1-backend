package com.rodrigo.sw1.app_sw1.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.rodrigo.sw1.app_sw1.dto.DepartmentRequest;
import com.rodrigo.sw1.app_sw1.services.DepartmentService;
import com.rodrigo.sw1.app_sw1.models.Department;
import org.springframework.web.bind.annotation.GetMapping;
//import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

//Nota: De momento solo lo voy a usar de forma interna, 
// pero se puede usar para exponer una API REST si es necesario

@RestController
@RequestMapping("/api/departments")
public class DepartmentController {
    
    @Autowired
    private DepartmentService departmentService;

    @PostMapping
    public ResponseEntity<?> createDepartment(@RequestBody DepartmentRequest request) {
        try {
            Department department = departmentService.createDepartment(request);
            return ResponseEntity.ok(department);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @GetMapping
    public ResponseEntity<List<Department>> getAllDepartments() {
        List<Department> departments = departmentService.getAll();
        return ResponseEntity.ok(departments);
    }
    
}
