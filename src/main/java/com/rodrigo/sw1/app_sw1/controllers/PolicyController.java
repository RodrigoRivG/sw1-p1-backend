package com.rodrigo.sw1.app_sw1.controllers;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.core.Authentication;

import com.rodrigo.sw1.app_sw1.dto.PolicyRequest;
import com.rodrigo.sw1.app_sw1.services.PolicyService;
import com.rodrigo.sw1.app_sw1.models.Policy;

@RestController
@RequestMapping("/api/policies")
public class PolicyController {
    
    @Autowired
    private PolicyService policyService;

    @PostMapping
    public ResponseEntity<?> createPolicy(@RequestBody PolicyRequest request, 
        Authentication authentication) {
            try {
                String userId = authentication.getName();
                Policy policy = policyService.createPolicy(request, userId);
                return ResponseEntity.ok(policy);
            } catch (RuntimeException e) {
                return ResponseEntity.badRequest().body(e.getMessage());
            }
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updatePolicy(@PathVariable String id, 
        @RequestBody PolicyRequest request) {
            try {
                Policy policy = policyService.updatePolicy(id, request);
                return ResponseEntity.ok(policy);
            } catch (RuntimeException e) {
                return ResponseEntity.badRequest().body((e.getMessage()));
            }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deletePolicy(@PathVariable String id) {
        try {
            policyService.deletePolicy(id);
            return ResponseEntity.ok("Política eliminada");
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PostMapping("/{id}/collaborators")
    public ResponseEntity<?> addCollaborator(@PathVariable String id, 
                                            @RequestBody Map<String, String> body) {
            try {
                String userId = body.get("userId");
                return ResponseEntity.ok(policyService.addCollaborator(id, userId));
            } catch (RuntimeException e) {
                return ResponseEntity.badRequest().body(e.getMessage());
            }
    }

    @DeleteMapping("/{id}/collaborators/{userId}")
    public ResponseEntity<?> removeCollaborator(@PathVariable String id,
                                             @PathVariable String userId) {
        try {
            return ResponseEntity.ok(policyService.removeCollaborator(id, userId));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @GetMapping("/as-collaborator")
    public ResponseEntity<List<Policy>> getAsCollaborator(Authentication authentication) {
        String userId = authentication.getName();
        return ResponseEntity.ok(policyService.getAsCollaborator(userId));
    }

    @GetMapping
    public ResponseEntity<List<Policy>> getAll() {
        return ResponseEntity.ok(policyService.getAllPolicies());
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getById(@PathVariable String id) {
        try {
            return ResponseEntity.ok(policyService.getById(id));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @GetMapping("/my-policies")
    public ResponseEntity<List<Policy>> getByUser(Authentication authentication) {
        String userId = authentication.getName();
        return ResponseEntity.ok(policyService.getByUser(userId));
    }
}
