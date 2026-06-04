package com.rodrigo.sw1.app_sw1.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import com.rodrigo.sw1.app_sw1.models.DocumentModel;
import com.rodrigo.sw1.app_sw1.models.DocumentVersion;
import com.rodrigo.sw1.app_sw1.models.DocumentPermission;
import com.rodrigo.sw1.app_sw1.dto.*;
import com.rodrigo.sw1.app_sw1.repository.DocumentRepository;
import com.rodrigo.sw1.app_sw1.repository.DocumentVersionRepository;
import com.rodrigo.sw1.app_sw1.repository.DocumentPermissionRepository;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class DocumentService {

    @Autowired
    private DocumentRepository documentRepository;

    @Autowired
    private DocumentVersionRepository documentVersionRepository;

    @Autowired
    private DocumentPermissionRepository documentPermissionRepository;

    @Autowired
    private BackblazeService backblazeService;

    /**
     * Crear un nuevo documento en una política con archivo
     */
    public DocumentResponse createDocument(DocumentRequest request, String userId, MultipartFile file) {
        // Crear documento
        DocumentModel document = new DocumentModel();
        document.setPolicyId(request.getPolicyId());
        document.setName(request.getName());
        document.setType(request.getType());
        document.setCreatedBy(userId);
        document.setCreatedAt(LocalDateTime.now());
        document.setUpdatedAt(LocalDateTime.now());

        // Subir archivo a Backblaze si existe
        String fileUrl = null;
        if (file != null && !file.isEmpty()) {
            fileUrl = backblazeService.uploadFile(file, request.getPolicyId());
            document.setBackblazeUrl(fileUrl);
        }

        // Guardar documento inicial
        DocumentModel savedDocument = documentRepository.save(document);

        // Crear versión inicial
        DocumentVersion initialVersion = new DocumentVersion();
        initialVersion.setDocumentId(savedDocument.getId());
        initialVersion.setVersionNumber(1);
        initialVersion.setContent(request.getContent());
        initialVersion.setBackblazeUrl(fileUrl);
        initialVersion.setModifiedBy(userId);
        initialVersion.setModifiedAt(LocalDateTime.now());
        initialVersion.setChangeDescription("Creación inicial del documento");

        DocumentVersion savedVersion = documentVersionRepository.save(initialVersion);

        // Actualizar documento con versión actual
        savedDocument.setCurrentVersionId(savedVersion.getId());
        documentRepository.save(savedDocument);

        // Crear permisos si existen
        if (request.getPermissions() != null) {
            for (DocumentPermissionRequest perm : request.getPermissions()) {
                DocumentPermission permission = new DocumentPermission();
                permission.setDocumentId(savedDocument.getId());
                permission.setNodeId(perm.getNodeId());
                permission.setPermissionLevel(perm.getPermissionLevel());
                documentPermissionRepository.save(permission);
            }
        }

        return mapToResponse(savedDocument);
    }

    /**
     * Crear documento sin archivo (sobrecarga)
     */
    public DocumentResponse createDocument(DocumentRequest request, String userId) {
        return createDocument(request, userId, null);
    }

    /**
     * Obtener todos los documentos de una política
     */
    public List<DocumentResponse> getDocumentsByPolicy(String policyId) {
        List<DocumentModel> documents = documentRepository.findByPolicyId(policyId);
        return documents.stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    /**
     * Obtener un documento específico
     */
    public DocumentResponse getDocument(String documentId) {
        DocumentModel document = documentRepository.findById(documentId)
                .orElseThrow(() -> new RuntimeException("Documento no encontrado: " + documentId));
        return mapToResponse(document);
    }

    /**
     * Actualizar contenido de un documento (crea nueva versión con archivo)
     */
    public DocumentResponse updateDocument(String documentId, DocumentUpdateRequest request, String userId, MultipartFile file) {
        DocumentModel document = documentRepository.findById(documentId)
                .orElseThrow(() -> new RuntimeException("Documento no encontrado: " + documentId));

        // Obtener última versión
        Integer latestVersionNumber = getLatestVersionNumber(documentId);
        if (latestVersionNumber == 0) {
            throw new RuntimeException("Documento no tiene versiones iniciales");
        }
        
        DocumentVersion lastVersion = documentVersionRepository
                .findByDocumentIdAndVersionNumber(documentId, latestVersionNumber)
                .orElseThrow(() -> new RuntimeException("Versión no encontrada"));

        // Subir nuevo archivo a Backblaze si existe
        String fileUrl = null;
        if (file != null && !file.isEmpty()) {
            fileUrl = backblazeService.uploadFile(file, document.getPolicyId());
        }

        // Crear nueva versión
        DocumentVersion newVersion = new DocumentVersion();
        newVersion.setDocumentId(documentId);
        newVersion.setVersionNumber(lastVersion.getVersionNumber() + 1);
        newVersion.setContent(request.getContent());
        newVersion.setBackblazeUrl(fileUrl);
        newVersion.setModifiedBy(userId);
        newVersion.setModifiedAt(LocalDateTime.now());
        newVersion.setChangeDescription(request.getChangeDescription());
        newVersion.setPreviousVersionId(lastVersion.getId());

        DocumentVersion savedVersion = documentVersionRepository.save(newVersion);

        // Actualizar documento
        document.setCurrentVersionId(savedVersion.getId());
        document.setUpdatedAt(LocalDateTime.now());
        if (fileUrl != null) {
            document.setBackblazeUrl(fileUrl);
        }
        documentRepository.save(document);

        return mapToResponse(document);
    }

    /**
     * Actualizar documento sin archivo (sobrecarga)
     */
    public DocumentResponse updateDocument(String documentId, DocumentUpdateRequest request, String userId) {
        return updateDocument(documentId, request, userId, null);
    }

    /**
     * Obtener historial de versiones de un documento
     */
    public List<DocumentVersionResponse> getDocumentHistory(String documentId) {
        List<DocumentVersion> versions = documentVersionRepository
                .findByDocumentIdOrderByVersionNumberDesc(documentId);
        return versions.stream()
                .map(this::mapVersionToResponse)
                .collect(Collectors.toList());
    }

    /**
     * Restaurar una versión anterior de un documento
     */
    public DocumentResponse restoreVersion(String documentId, Integer versionNumber, String userId) {
        DocumentModel document = documentRepository.findById(documentId)
                .orElseThrow(() -> new RuntimeException("Documento no encontrado: " + documentId));

        DocumentVersion targetVersion = documentVersionRepository
                .findByDocumentIdAndVersionNumber(documentId, versionNumber)
                .orElseThrow(() -> new RuntimeException("Versión no encontrada: " + versionNumber));

        // Crear nueva versión con el contenido de la versión anterior
        Integer latestVersionNumber = getLatestVersionNumber(documentId);
        DocumentVersion restoredVersion = new DocumentVersion();
        restoredVersion.setDocumentId(documentId);
        restoredVersion.setVersionNumber(latestVersionNumber + 1);
        restoredVersion.setContent(targetVersion.getContent());
        restoredVersion.setModifiedBy(userId);
        restoredVersion.setModifiedAt(LocalDateTime.now());
        restoredVersion.setChangeDescription("Restauración de versión " + versionNumber);
        
        // Obtener versión anterior a la nueva
        DocumentVersion previousVersion = documentVersionRepository
                .findByDocumentIdAndVersionNumber(documentId, latestVersionNumber)
                .orElse(null);
        if (previousVersion != null) {
            restoredVersion.setPreviousVersionId(previousVersion.getId());
        }

        DocumentVersion savedVersion = documentVersionRepository.save(restoredVersion);

        // Actualizar documento
        document.setCurrentVersionId(savedVersion.getId());
        document.setUpdatedAt(LocalDateTime.now());
        //document.getVersionIds().add(savedVersion.getId());
        documentRepository.save(document);

        return mapToResponse(document);
    }

    /**
     * Verificar permiso del funcionario en un documento
     */
    public boolean hasPermission(String documentId, String nodeId, String requiredPermission) {
        DocumentPermission permission = documentPermissionRepository
                .findByDocumentIdAndNodeId(documentId, nodeId);

        if (permission == null) {
            return false;
        }

        String userPermission = permission.getPermissionLevel();

        // Validar permisos jerárquicamente
        if (userPermission.equals("NONE")) {
            return false;
        }
        if (requiredPermission.equals("VIEW")) {
            return !userPermission.equals("NONE");
        }
        if (requiredPermission.equals("EDIT")) {
            return userPermission.equals("EDIT") || userPermission.equals("UPLOAD");
        }
        if (requiredPermission.equals("UPLOAD")) {
            return userPermission.equals("UPLOAD");
        }

        return false;
    }

    /**
     * Obtener permisos de un documento
     */
    public List<DocumentPermissionResponse> getDocumentPermissions(String documentId) {
        List<DocumentPermission> permissions = documentPermissionRepository.findByDocumentId(documentId);
        return permissions.stream()
                .map(this::mapPermissionToResponse)
                .collect(Collectors.toList());
    }

    /**
     * Actualizar permiso en un nodo
     */
    public DocumentPermissionResponse updatePermission(String documentId, String nodeId, String permissionLevel) {
        DocumentPermission permission = documentPermissionRepository
                .findByDocumentIdAndNodeId(documentId, nodeId);

        if (permission == null) {
            permission = new DocumentPermission();
            permission.setDocumentId(documentId);
            permission.setNodeId(nodeId);
        }

        permission.setPermissionLevel(permissionLevel);
        DocumentPermission saved = documentPermissionRepository.save(permission);
        return mapPermissionToResponse(saved);
    }

    /**
     * Mapeos auxiliares
     */
    private DocumentResponse mapToResponse(DocumentModel document) {
        DocumentResponse response = new DocumentResponse();
        response.setId(document.getId());
        response.setPolicyId(document.getPolicyId());
        response.setName(document.getName());
        response.setType(document.getType());
        response.setBackblazeUrl(document.getBackblazeUrl());
        response.setCurrentVersionId(document.getCurrentVersionId());
        response.setCreatedBy(document.getCreatedBy());
        response.setCreatedAt(document.getCreatedAt());
        response.setUpdatedAt(document.getUpdatedAt());
        //response.setVersionIds(document.getVersionIds());
        response.setPermissions(getDocumentPermissions(document.getId()));
        return response;
    }

    private DocumentVersionResponse mapVersionToResponse(DocumentVersion version) {
        DocumentVersionResponse response = new DocumentVersionResponse();
        response.setId(version.getId());
        response.setDocumentId(version.getDocumentId());
        response.setVersionNumber(version.getVersionNumber());
        response.setContent(version.getContent());
        response.setBackblazeUrl(version.getBackblazeUrl());
        response.setModifiedBy(version.getModifiedBy());
        response.setModifiedAt(version.getModifiedAt());
        response.setChangeDescription(version.getChangeDescription());
        return response;
    }

    private DocumentPermissionResponse mapPermissionToResponse(DocumentPermission permission) {
        DocumentPermissionResponse response = new DocumentPermissionResponse();
        response.setId(permission.getId());
        response.setDocumentId(permission.getDocumentId());
        response.setNodeId(permission.getNodeId());
        response.setPermissionLevel(permission.getPermissionLevel());
        return response;
    }

    private Integer getLatestVersionNumber(String documentId) {
        List<DocumentVersion> versions = documentVersionRepository
                .findByDocumentIdOrderByVersionNumberDesc(documentId);
        return versions.isEmpty() ? 0 : versions.get(0).getVersionNumber();
    }

    /**
     * Descargar documento - genera URL firmada temporal
     */
    public String downloadDocument(String documentId, int expirationMinutes) {
        DocumentModel document = documentRepository.findById(documentId)
                .orElseThrow(() -> new RuntimeException("Documento no encontrado: " + documentId));

        if (document.getBackblazeUrl() == null || document.getBackblazeUrl().isEmpty()) {
            throw new RuntimeException("Documento no tiene archivo asociado");
        }

        // Extraer clave del documento
        String fileKey = document.getPolicyId() + "/" + document.getName();
        
        // Generar y retornar URL firmada
        return backblazeService.generatePresignedUrl(fileKey, expirationMinutes);
    }

    /**
     * Eliminar documento y sus archivos de Backblaze
     */
    public void deleteDocument(String documentId) {
        DocumentModel document = documentRepository.findById(documentId)
                .orElseThrow(() -> new RuntimeException("Documento no encontrado: " + documentId));

        // Eliminar archivo de Backblaze si existe
        if (document.getBackblazeUrl() != null && !document.getBackblazeUrl().isEmpty()) {
            try {
                backblazeService.deleteFile(document.getBackblazeUrl());
            } catch (Exception e) {
                System.err.println("Error eliminando archivo de Backblaze: " + e.getMessage());
            }
        }

        // Eliminar versiones
        List<DocumentVersion> versions = documentVersionRepository
                .findByDocumentIdOrderByVersionNumberDesc(documentId);
        for (DocumentVersion version : versions) {
            if (version.getBackblazeUrl() != null && !version.getBackblazeUrl().isEmpty()) {
                try {
                    backblazeService.deleteFile(version.getBackblazeUrl());
                } catch (Exception e) {
                    System.err.println("Error eliminando versión de Backblaze: " + e.getMessage());
                }
            }
        }
        documentVersionRepository.deleteAll(versions);

        // Eliminar permisos
        List<DocumentPermission> permissions = documentPermissionRepository.findByDocumentId(documentId);
        documentPermissionRepository.deleteAll(permissions);

        // Eliminar documento
        documentRepository.deleteById(documentId);
    }
}
