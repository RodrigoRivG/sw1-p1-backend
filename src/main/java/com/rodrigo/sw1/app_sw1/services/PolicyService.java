package com.rodrigo.sw1.app_sw1.services;

import java.time.LocalDateTime;
import java.util.List;
import java.util.ArrayList;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.rodrigo.sw1.app_sw1.repository.PolicyRepository;
import com.rodrigo.sw1.app_sw1.dto.PolicyRequest;
import com.rodrigo.sw1.app_sw1.models.Policy;

@Service
public class PolicyService {
    // Crear, actualizar, eliminar y consultar políticas de negocio

    @Autowired
    private PolicyRepository policyRepository;

    public Policy createPolicy(PolicyRequest request, String userId) {
        Policy policy = new Policy();
        policy.setName(request.getName());
        policy.setDescription(request.getDescription());
        policy.setCreatedBy(userId);
        policy.setDiagram(request.getDiagram());
        policy.setStatus(request.getStatus() != null ? request.getStatus() : "active");
        policy.setCollaborators(request.getCollaborators());

        return policyRepository.save(policy);
    }

    public Policy updatePolicy(String id, PolicyRequest request) {
        Policy policy = getById(id);

        policy.setName(request.getName());
        policy.setDescription(request.getDescription());
        policy.setDiagram(request.getDiagram());
        policy.setStatus(request.getStatus() != null ? request.getStatus() : "active");
        policy.setCollaborators(request.getCollaborators());
        policy.setUpdatedAt(LocalDateTime.now());
        
        return policyRepository.save(policy);
    }

    public Policy addCollaborator(String policyId, String userId) {
        Policy policy = getById(policyId);
        List<String> collaborators = policy.getCollaborators() != null 
            ? policy.getCollaborators() 
            : new ArrayList<>();

        if (!collaborators.contains(userId)) {
            collaborators.add(userId);
            policy.setCollaborators(collaborators);
            policyRepository.save(policy);
        }
        
        return policy;
    }

    public Policy removeCollaborator(String policyId, String userId) {
        Policy policy = getById(policyId);
        List<String> collaborators = policy.getCollaborators();

        if (collaborators != null) {
            collaborators.remove(userId);
            policy.setCollaborators(collaborators);
            policyRepository.save(policy);
        }

        return policy;
    }

    public List<Policy> getAsCollaborator(String userId) {
        return policyRepository.findByCollaboratorsContaining(userId);
    }

    public void deletePolicy(String id) {
        Policy policy = getById(id);

        policyRepository.delete(policy);
    }

    public List<Policy> getAllPolicies() {
        return policyRepository.findAll();
    }

    public Policy getById(String id) {
        return policyRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Política no encontrada"));
    }

    public List<Policy> getByUser(String userId) {
        return policyRepository.findByCreatedBy(userId);
    }

}