package com.rodrigo.sw1.app_sw1.services;

import org.springframework.stereotype.Service;
import com.rodrigo.sw1.app_sw1.dto.DepartmentRequest;
import com.rodrigo.sw1.app_sw1.models.Department;
import com.rodrigo.sw1.app_sw1.repository.DepartmentRepository;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

@Service
public class DepartmentService {
    
    @Autowired
    private DepartmentRepository departmentRepository;

    public Department createDepartment(DepartmentRequest request) {
        Department department = new Department();
        department.setName(request.getName());

        return departmentRepository.save(department);
    }

    public List<Department> getAll() {
        return departmentRepository.findAll();
    }
}
