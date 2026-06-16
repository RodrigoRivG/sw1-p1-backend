package com.rodrigo.sw1.app_sw1.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import com.rodrigo.sw1.app_sw1.dto.*;
import com.rodrigo.sw1.app_sw1.services.DocumentService;
import org.springframework.security.core.Authentication;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/documents")
public class DocumentController {

    @Autowired
    private DocumentService documentService;

    /**
     * Obtener todos los documentos de una política
     */
    @GetMapping("/policy/{policyId}")
    public ResponseEntity<?> getDocumentsByPolicy(@PathVariable String policyId) {
        try {
            List<DocumentResponse> documents = documentService.getDocumentsByPolicy(policyId);
            return ResponseEntity.ok(documents);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * Obtener un documento específico
     */
    @GetMapping("/{id}")
    public ResponseEntity<?> getDocument(@PathVariable String id) {
        try {
            DocumentResponse document = documentService.getDocument(id);
            return ResponseEntity.ok(document);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * Crear un nuevo documento con archivo opcional
     */
    @PostMapping(consumes = "multipart/form-data")
    public ResponseEntity<?> createDocument(
            Authentication authentication,
            @RequestPart DocumentRequest request,
            @RequestPart(required = false) MultipartFile file) {
        try {
            String userId = authentication.getName(); // Temporal
            DocumentResponse document = documentService.createDocument(request, userId, file);
            return ResponseEntity.ok(document);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * Actualizar contenido de un documento (crear nueva versión) con archivo opcional
     */
    @PutMapping(value = "/{id}", consumes = "multipart/form-data")
    public ResponseEntity<?> updateDocument(
            Authentication authentication,
            @PathVariable String id,
            @RequestPart DocumentUpdateRequest request,
            @RequestPart(required = false) MultipartFile file) {
        try {
            String userId = authentication.getName(); 
            DocumentResponse document = documentService.updateDocument(id, request, userId, file);
            return ResponseEntity.ok(document);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * Obtener historial de versiones de un documento
     */
    @GetMapping("/{id}/history")
    public ResponseEntity<?> getDocumentHistory(@PathVariable String id) {
        try {
            List<DocumentVersionResponse> history = documentService.getDocumentHistory(id);
            return ResponseEntity.ok(history);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * Restaurar una versión anterior de un documento
     */
    @PostMapping("/{id}/restore/{versionNumber}")
    public ResponseEntity<?> restoreVersion(
            Authentication authentication,
            @PathVariable String id,
            @PathVariable Integer versionNumber) {
        try {
            String userId = authentication.getName(); 
            DocumentResponse document = documentService.restoreVersion(id, versionNumber, userId);
            return ResponseEntity.ok(document);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * Obtener permisos de un documento
     */
    @GetMapping("/{id}/permissions")
    public ResponseEntity<?> getDocumentPermissions(@PathVariable String id) {
        try {
            List<DocumentPermissionResponse> permissions = documentService.getDocumentPermissions(id);
            return ResponseEntity.ok(permissions);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * Actualizar permiso en un nodo
     */
    @PutMapping("/{documentId}/permissions/{nodeId}")
    public ResponseEntity<?> updatePermission(
            @PathVariable String documentId,
            @PathVariable String nodeId,
            @RequestBody Map<String, String> request) {
        try {
            String permissionLevel = request.get("permissionLevel");
            DocumentPermissionResponse permission = documentService.updatePermission(documentId, nodeId, permissionLevel);
            return ResponseEntity.ok(permission);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * Verificar permiso del funcionario
     */
    @GetMapping("/{documentId}/check-permission")
    public ResponseEntity<?> checkPermission(
            @PathVariable String documentId,
            @RequestParam String nodeId,
            @RequestParam String requiredPermission) {
        try {
            boolean hasPermission = documentService.hasPermission(documentId, nodeId, requiredPermission);
            return ResponseEntity.ok(Map.of(
                "documentId", documentId,
                "nodeId", nodeId,
                "requiredPermission", requiredPermission,
                "hasPermission", hasPermission
            ));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * Descargar documento - genera URL firmada temporal
     */
    @GetMapping("/{id}/download")
    public ResponseEntity<?> downloadDocument(
            @PathVariable String id,
            @RequestParam(defaultValue = "60") int expirationMinutes) {
        try {
            String presignedUrl = documentService.downloadDocument(id, expirationMinutes);
            return ResponseEntity.ok(Map.of("presignedUrl", presignedUrl));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * Eliminar un documento y todos sus archivos
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteDocument(@PathVariable String id) {
        try {
            documentService.deleteDocument(id);
            return ResponseEntity.ok(Map.of("message", "Documento eliminado exitosamente"));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
}
