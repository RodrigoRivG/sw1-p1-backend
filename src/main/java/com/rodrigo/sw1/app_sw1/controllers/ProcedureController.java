package com.rodrigo.sw1.app_sw1.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.rodrigo.sw1.app_sw1.services.ProcedureService;
import com.rodrigo.sw1.app_sw1.dto.ProcedureRequest;
import com.rodrigo.sw1.app_sw1.models.Procedure;

import java.util.List;

@RestController
@RequestMapping("/api/procedures")
public class ProcedureController {
    
    @Autowired
    private ProcedureService procedureService;

    @PostMapping
    public ResponseEntity<?> createProcedure(@RequestBody ProcedureRequest request, 
        Authentication authentication) {
        try{
            String userId = authentication.getName();
            return ResponseEntity.ok(procedureService.createProcedure(request, userId));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @GetMapping 
    public ResponseEntity<List<Procedure>> getAll() {
        return ResponseEntity.ok(procedureService.getAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getById(@PathVariable String id) {
        try {
            return ResponseEntity.ok(procedureService.getById(id));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PutMapping("/{id}/cancel")
    public ResponseEntity<?> cancelProcedure(@PathVariable String id) {
        try {
            return ResponseEntity.ok(procedureService.cancelProcedure(id));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}
