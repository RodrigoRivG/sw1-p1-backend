package com.rodrigo.sw1.app_sw1.services;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.GetObjectPresignRequest;
import software.amazon.awssdk.services.s3.presigner.model.PresignedGetObjectRequest;

import java.io.IOException;
import java.time.Duration;

@Service
public class BackblazeService {

    @Value("${blackbaze.key.id}")
    private String keyId;

    @Value("${blackbaze.application.key}")
    private String applicationKey;

    @Value("${blackbaze.bucket.name}")
    private String bucketName;

    @Value("${blackbaze.endpoint}")
    private String endpoint;

    private S3Client s3Client;
    private S3Presigner presigner;

    /**
     * Inicializar cliente S3 (lazy initialization)
     */
    private void initializeS3Client() {
        if (s3Client == null) {
            AwsBasicCredentials credentials = AwsBasicCredentials.create(keyId, applicationKey);
            
            s3Client = S3Client.builder()
                    .credentialsProvider(StaticCredentialsProvider.create(credentials))
                    .endpointOverride(java.net.URI.create("https://" + endpoint))
                    .region(Region.US_EAST_1)
                    .build();
        }
    }

    /**
     * Inicializar presigner para URLs firmadas
     */
    private void initializePresigner() {
        if (presigner == null) {
            AwsBasicCredentials credentials = AwsBasicCredentials.create(keyId, applicationKey);
            
            presigner = S3Presigner.builder()
                    .credentialsProvider(StaticCredentialsProvider.create(credentials))
                    .endpointOverride(java.net.URI.create("https://" + endpoint))
                    .region(Region.US_EAST_1)
                    .build();
        }
    }

    /**
     * Subir archivo organizado por política
     * Formato: /policyId/filename
     */
    public String uploadFile(MultipartFile file, String policyId) {
        try {
            initializeS3Client();

            // Crear clave: /policyId/filename
            String fileKey = policyId + "/" + file.getOriginalFilename();

            // Subir archivo a Backblaze
            PutObjectRequest putRequest = PutObjectRequest.builder()
                    .bucket(bucketName)
                    .key(fileKey)
                    .contentType(file.getContentType())
                    .build();

            s3Client.putObject(putRequest, software.amazon.awssdk.core.sync.RequestBody.fromInputStream(
                    file.getInputStream(),
                    file.getSize()
            ));

            // Retornar URL pública del archivo
            return "https://" + endpoint + "/" + bucketName + "/" + fileKey;

        } catch (IOException e) {
            throw new RuntimeException("Error al subir archivo: " + e.getMessage());
        }
    }

    /**
     * Eliminar archivo de Backblaze
     */
    public void deleteFile(String fileUrl) {
        try {
            initializeS3Client();

            // Extraer la clave del URL
            // URL formato: https://s3.us-east-005.backblazeb2.com/bucket-name/policyId/filename
            String fileKey = extractFileKeyFromUrl(fileUrl);

            DeleteObjectRequest deleteRequest = DeleteObjectRequest.builder()
                    .bucket(bucketName)
                    .key(fileKey)
                    .build();

            s3Client.deleteObject(deleteRequest);

        } catch (Exception e) {
            throw new RuntimeException("Error al eliminar archivo: " + e.getMessage());
        }
    }

    /**
     * Generar URL firmada temporal para acceso privado
     */
    public String generatePresignedUrl(String fileKey, int expirationMinutes) {
        try {
            initializePresigner();

            GetObjectRequest getRequest = GetObjectRequest.builder()
                    .bucket(bucketName)
                    .key(fileKey)
                    .build();

            GetObjectPresignRequest presignRequest = GetObjectPresignRequest.builder()
                    .signatureDuration(Duration.ofMinutes(expirationMinutes))
                    .getObjectRequest(getRequest)
                    .build();

            PresignedGetObjectRequest presignedRequest = presigner.presignGetObject(presignRequest);
            return presignedRequest.url().toString();

        } catch (Exception e) {
            throw new RuntimeException("Error al generar URL firmada: " + e.getMessage());
        }
    }

    /**
     * Extraer la clave del archivo desde la URL
     */
    private String extractFileKeyFromUrl(String fileUrl) {
        // URL formato: https://s3.us-east-005.backblazeb2.com/bucket-name/policyId/filename
        try {
            String[] parts = fileUrl.split(bucketName + "/");
            if (parts.length == 2) {
                return parts[1];
            }
        } catch (Exception e) {
            // Fallback: asumir que es el formato correcto
        }
        return fileUrl;
    }

    /**
     * Limpiar recursos
     */
    public void cleanup() {
        if (s3Client != null) {
            s3Client.close();
        }
        if (presigner != null) {
            presigner.close();
        }
    }
}
