package com.rodrigo.sw1.app_sw1.services;

import org.springframework.stereotype.Service;
import java.io.InputStream;

@Service
public class BackblazeService {

    /**
     * Subir archivo a Backblaze
     */
    public String uploadFile(String fileName, InputStream fileContent, String contentType) {
        // TODO: Implementar integración con Backblaze B2 API
        // Por ahora retornar URL simulada
        return "https://backblaze.example.com/files/" + fileName;
    }

    /**
     * Descargar archivo desde Backblaze
     */
    public InputStream downloadFile(String backblazeUrl) {
        // TODO: Implementar descarga desde Backblaze
        return null;
    }

    /**
     * Eliminar archivo desde Backblaze
     */
    public void deleteFile(String backblazeUrl) {
        // TODO: Implementar eliminación desde Backblaze
    }
}
