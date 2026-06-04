package com.rodrigo.sw1.app_sw1.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;
import org.springframework.context.event.EventListener;

import com.rodrigo.sw1.app_sw1.dto.*;
import com.rodrigo.sw1.app_sw1.services.CollaborativeEditService;
import com.rodrigo.sw1.app_sw1.services.DocumentService;

import java.util.Map;
import java.util.List;

@Controller
public class WebSocketController {
    
    @Autowired
    private SimpMessagingTemplate messagingTemplate;

    @Autowired
    private CollaborativeEditService collaborativeEditService;

    @Autowired
    private DocumentService documentService;

    /**
     * Políticas (diagrama de actividades)
     */
    @MessageMapping("/policy/{policyId}/update")
    @SendTo("/topic/policy/{policyId}")
    public Map<String, Object> updateDiagram(
        @DestinationVariable String policyId, 
        Map<String, Object> diagram) {
            return diagram;
    }

    @MessageMapping("/policy/{policyId}/join")
    public void joinPolicy(
        @DestinationVariable String policyId,
        Map<String, Object> user) {
            messagingTemplate.convertAndSend(
                "/topic/policy/" + policyId + "/users", 
                user
            );
    }

    /**
     * Edición colaborativa de documentos
     */
    
    /**
     * Usuario se conecta a un documento para edición colaborativa
     */
    @MessageMapping("/documents/{documentId}/join")
    public void joinDocument(
            @DestinationVariable String documentId,
            @Header("simpUser") String userId,
            StompHeaderAccessor accessor) {
        try {
            // Obtener información del usuario desde el header
            String userName = accessor.getNativeHeader("userName") != null 
                ? accessor.getNativeHeader("userName").get(0) 
                : "Usuario " + userId;
            String sessionId = accessor.getSessionId();

            // Registrar usuario en el servicio
            collaborativeEditService.joinDocument(documentId, userId, userName, sessionId);

            // Obtener contenido actual del documento
            DocumentResponse docResponse = documentService.getDocument(documentId);
            List<DocumentVersionResponse> history = documentService.getDocumentHistory(documentId);
            
            if (!history.isEmpty()) {
                String currentContent = history.get(0).getContent();
                collaborativeEditService.initializeDocumentContent(documentId, currentContent);
            }

            // Notificar a otros usuarios que se conectó uno nuevo
            ActiveUserMessage joinMessage = new ActiveUserMessage(
                documentId,
                userId,
                userName,
                "JOIN",
                collaborativeEditService.getActiveUserCount(documentId),
                System.currentTimeMillis()
            );

            messagingTemplate.convertAndSend(
                "/topic/documents/" + documentId + "/users",
                joinMessage
            );

            // Enviar lista de usuarios activos al nuevo usuario
            List activeUsers = collaborativeEditService.getActiveUsers(documentId);
            messagingTemplate.convertAndSendToUser(
                userId,
                "/queue/documents/" + documentId + "/active-users",
                activeUsers
            );

        } catch (Exception e) {
            messagingTemplate.convertAndSendToUser(
                userId,
                "/queue/errors",
                Map.of("error", "Error al conectar al documento: " + e.getMessage())
            );
        }
    }

    /**
     * Recibir edición de documento
     */
    @MessageMapping("/documents/{documentId}/edit")
    public void editDocument(
            @DestinationVariable String documentId,
            DocumentEditMessage editMessage,
            @Header("simpUser") String userId) {
        try {
            editMessage.setUserId(userId);
            editMessage.setDocumentId(documentId);
            editMessage.setTimestamp(System.currentTimeMillis());

            // Procesar la edición
            collaborativeEditService.processEdit(editMessage, userId);

            // Broadcast a todos los usuarios del documento
            messagingTemplate.convertAndSend(
                "/topic/documents/" + documentId + "/edits",
                editMessage
            );

        } catch (Exception e) {
            messagingTemplate.convertAndSendToUser(
                userId,
                "/queue/errors",
                Map.of("error", "Error en edición: " + e.getMessage())
            );
        }
    }

    /**
     * Sincronizar posición del cursor
     */
    @MessageMapping("/documents/{documentId}/cursor")
    public void updateCursor(
            @DestinationVariable String documentId,
            CursorPositionMessage cursorMessage,
            @Header("simpUser") String userId) {
        try {
            cursorMessage.setUserId(userId);
            cursorMessage.setDocumentId(documentId);
            cursorMessage.setTimestamp(System.currentTimeMillis());

            // Actualizar posición en servicio
            collaborativeEditService.updateCursorPosition(documentId, userId, cursorMessage.getPosition());

            // Broadcast a todos los usuarios (excepto el remitente)
            messagingTemplate.convertAndSend(
                "/topic/documents/" + documentId + "/cursors",
                cursorMessage
            );

        } catch (Exception e) {
            messagingTemplate.convertAndSendToUser(
                userId,
                "/queue/errors",
                Map.of("error", "Error sincronizando cursor: " + e.getMessage())
            );
        }
    }

    /**
     * Guardar documento manualmente
     */
    @MessageMapping("/documents/{documentId}/save")
    public void saveDocument(
            @DestinationVariable String documentId,
            DocumentEditMessage saveMessage,
            @Header("simpUser") String userId) {
        try {
            collaborativeEditService.saveDocument(
                documentId,
                saveMessage.getContent(),
                userId,
                saveMessage.getChangeDescription()
            );

            // Notificar a todos que se guardó
            messagingTemplate.convertAndSend(
                "/topic/documents/" + documentId + "/saved",
                Map.of(
                    "documentId", documentId,
                    "savedBy", userId,
                    "timestamp", System.currentTimeMillis()
                )
            );

        } catch (Exception e) {
            messagingTemplate.convertAndSendToUser(
                userId,
                "/queue/errors",
                Map.of("error", "Error guardando: " + e.getMessage())
            );
        }
    }

    /**
     * Desconexión automática (STOMP Disconnect)
     */
    @EventListener
    public void handleDisconnect(SessionDisconnectEvent event) {
        // Obtener la sesión y limpiar
        String sessionId = event.getSessionId();
        // TODO: Mapear sessionId a documentId y userId para limpiar
    }
}
