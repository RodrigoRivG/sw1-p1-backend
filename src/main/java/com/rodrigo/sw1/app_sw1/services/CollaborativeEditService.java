package com.rodrigo.sw1.app_sw1.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import com.rodrigo.sw1.app_sw1.models.ActiveDocumentUser;
import com.rodrigo.sw1.app_sw1.dto.DocumentEditMessage;
import com.rodrigo.sw1.app_sw1.dto.DocumentUpdateRequest;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class CollaborativeEditService {

    @Autowired
    private DocumentService documentService;

    // Estructura: documentId -> Map<userId, ActiveDocumentUser>
    private final ConcurrentHashMap<String, ConcurrentHashMap<String, ActiveDocumentUser>> activeDocuments = 
        new ConcurrentHashMap<>();

    // Estructura: documentId -> último contenido guardado (para comparación)
    private final ConcurrentHashMap<String, String> documentContents = new ConcurrentHashMap<>();

    // Estructura: documentId -> temporizador de auto-guardado
    private final ConcurrentHashMap<String, Long> lastSaveTime = new ConcurrentHashMap<>();

    private static final long AUTO_SAVE_INTERVAL_MS = 30000; // 30 segundos
    private static final String[] CURSOR_COLORS = {
        "#FF6B6B", "#4ECDC4", "#45B7D1", "#FFA07A", "#98D8C8",
        "#F7DC6F", "#BB8FCE", "#85C1E2", "#F8B88B", "#82E0AA"
    };

    /**
     * Usuario se une a un documento para editar
     */
    public void joinDocument(String documentId, String userId, String userName, String sessionId) {
        activeDocuments.putIfAbsent(documentId, new ConcurrentHashMap<>());
        
        String color = CURSOR_COLORS[new Random().nextInt(CURSOR_COLORS.length)];
        ActiveDocumentUser user = new ActiveDocumentUser(userId, userName, sessionId, color, System.currentTimeMillis(), 0);
        
        activeDocuments.get(documentId).put(userId, user);
        lastSaveTime.putIfAbsent(documentId, System.currentTimeMillis());
    }

    /**
     * Usuario se desconecta del documento
     */
    public void leaveDocument(String documentId, String userId) {
        if (activeDocuments.containsKey(documentId)) {
            activeDocuments.get(documentId).remove(userId);
            
            // Si no hay más usuarios, eliminar documento de la memoria
            if (activeDocuments.get(documentId).isEmpty()) {
                activeDocuments.remove(documentId);
                documentContents.remove(documentId);
                lastSaveTime.remove(documentId);
            }
        }
    }

    /**
     * Obtener usuarios activos en un documento
     */
    public List<ActiveDocumentUser> getActiveUsers(String documentId) {
        if (!activeDocuments.containsKey(documentId)) {
            return new ArrayList<>();
        }
        return new ArrayList<>(activeDocuments.get(documentId).values());
    }

    /**
     * Procesar edición de documento
     */
    public void processEdit(DocumentEditMessage editMessage, String userId) {
        String documentId = editMessage.getDocumentId();

        // Validar que el usuario está activo en el documento
        if (!isUserActive(documentId, userId)) {
            throw new RuntimeException("Usuario no autorizado para editar este documento");
        }

        // Actualizar contenido local
        documentContents.put(documentId, editMessage.getContent());

        // Actualizar posición del cursor del usuario
        if (activeDocuments.containsKey(documentId)) {
            ActiveDocumentUser user = activeDocuments.get(documentId).get(userId);
            if (user != null) {
                user.setCursorPosition(editMessage.getCursorPosition());
            }
        }

        // Verificar si necesita auto-guardado
        checkAndAutoSave(documentId, editMessage.getContent(), userId);
    }

    /**
     * Actualizar posición del cursor de un usuario
     */
    public void updateCursorPosition(String documentId, String userId, Integer position) {
        if (activeDocuments.containsKey(documentId)) {
            ActiveDocumentUser user = activeDocuments.get(documentId).get(userId);
            if (user != null) {
                user.setCursorPosition(position);
            }
        }
    }

    /**
     * Verificar si el usuario está activo en el documento
     */
    public boolean isUserActive(String documentId, String userId) {
        return activeDocuments.containsKey(documentId) && 
               activeDocuments.get(documentId).containsKey(userId);
    }

    /**
     * Obtener número de usuarios activos
     */
    public int getActiveUserCount(String documentId) {
        if (!activeDocuments.containsKey(documentId)) {
            return 0;
        }
        return activeDocuments.get(documentId).size();
    }

    /**
     * Verificar y guardar automáticamente si han pasado los intervalos
     */
    @Async
    protected void checkAndAutoSave(String documentId, String content, String userId) {
        long currentTime = System.currentTimeMillis();
        long lastSave = lastSaveTime.getOrDefault(documentId, currentTime);

        if (currentTime - lastSave >= AUTO_SAVE_INTERVAL_MS) {
            autoSaveDocument(documentId, content, userId);
            lastSaveTime.put(documentId, currentTime);
        }
    }

    /**
     * Auto-guardar documento en la BD
     */
    private void autoSaveDocument(String documentId, String content, String userId) {
        try {
            DocumentUpdateRequest updateRequest = new DocumentUpdateRequest();
            updateRequest.setContent(content);
            updateRequest.setChangeDescription("Auto-guardado desde edición colaborativa");

            documentService.updateDocument(documentId, updateRequest, userId);
        } catch (Exception e) {
            // Log del error pero no lanzar excepción
            System.err.println("Error auto-guardando documento " + documentId + ": " + e.getMessage());
        }
    }

    /**
     * Guardar documento manualmente
     */
    public void saveDocument(String documentId, String content, String userId, String description) {
        try {
            DocumentUpdateRequest updateRequest = new DocumentUpdateRequest();
            updateRequest.setContent(content);
            updateRequest.setChangeDescription(description != null ? description : "Guardado manual");

            documentService.updateDocument(documentId, updateRequest, userId);
            lastSaveTime.put(documentId, System.currentTimeMillis());
        } catch (Exception e) {
            throw new RuntimeException("Error guardando documento: " + e.getMessage());
        }
    }

    /**
     * Obtener contenido actual en memoria del documento
     */
    public String getDocumentContent(String documentId) {
        return documentContents.getOrDefault(documentId, "");
    }

    /**
     * Inicializar contenido del documento desde la BD
     */
    public void initializeDocumentContent(String documentId, String content) {
        documentContents.put(documentId, content);
    }
}
