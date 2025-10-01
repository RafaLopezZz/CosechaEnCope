package com.rlp.cosechaencope.service;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.rlp.cosechaencope.config.S3Config;
import com.rlp.cosechaencope.exception.FileStorageException;

import lombok.extern.slf4j.Slf4j;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.model.S3Exception;

/**
 * Servicio para la gestión de archivos en AWS S3.
 * 
 * <p>Proporciona funcionalidades para subir, eliminar y generar URLs de archivos
 * almacenados en Amazon S3, específicamente para imágenes de artículos y categorías.</p>
 * 
 * @author rafalopezzz
 */
@Slf4j
@Service
public class S3FileStorageService {

    @Autowired
    private S3Client s3Client;

    @Autowired
    private S3Config s3Config;

    /**
     * Sube una imagen al bucket S3.
     *
     * @param file El archivo a subir
     * @param folder La carpeta donde almacenar el archivo (ej: "articulos", "categorias")
     * @param tipoUsuario Tipo de usuario para organización ("productor", "admin")
     * @param idUsuario ID del usuario para organización
     * @return URL pública del archivo subido
     * @throws FileStorageException si ocurre un error durante la subida
     */
    public String subirImagen(MultipartFile file, String folder, String tipoUsuario, Long idUsuario) {
        validarArchivo(file);
        
        String fileName = generarNombreArchivo(file.getOriginalFilename(), folder, tipoUsuario, idUsuario);
        
        try {
            PutObjectRequest putObjectRequest = PutObjectRequest.builder()
                    .bucket(s3Config.getBucketName())
                    .key(fileName)
                    .contentType(file.getContentType())
                    .contentLength(file.getSize())
                    .build();

            s3Client.putObject(putObjectRequest, RequestBody.fromInputStream(file.getInputStream(), file.getSize()));
            
            String imageUrl = s3Config.getBaseUrl() + "/" + fileName;
            log.info("Imagen subida exitosamente: {}", imageUrl);
            
            return imageUrl;

        } catch (S3Exception e) {
            log.error("Error al subir archivo a S3: {}", e.getMessage(), e);
            throw new FileStorageException("Error al subir archivo a S3: " + e.getMessage());
        } catch (IOException e) {
            log.error("Error al leer archivo: {}", e.getMessage(), e);
            throw new FileStorageException("Error al leer archivo: " + e.getMessage());
        }
    }

    /**
     * Elimina una imagen del bucket S3.
     *
     * @param imageUrl URL completa de la imagen a eliminar
     * @return true si se eliminó exitosamente
     */
    public boolean eliminarImagen(String imageUrl) {
        if (imageUrl == null || imageUrl.isEmpty()) {
            return true; // No hay nada que eliminar
        }

        try {
            String fileName = extraerNombreArchivoDeUrl(imageUrl);
            
            DeleteObjectRequest deleteObjectRequest = DeleteObjectRequest.builder()
                    .bucket(s3Config.getBucketName())
                    .key(fileName)
                    .build();

            s3Client.deleteObject(deleteObjectRequest);
            log.info("Imagen eliminada exitosamente: {}", fileName);
            
            return true;

        } catch (S3Exception e) {
            log.error("Error al eliminar archivo de S3: {}", e.getMessage(), e);
            return false;
        } catch (Exception e) {
            log.error("Error inesperado al eliminar archivo: {}", e.getMessage(), e);
            return false;
        }
    }

    /**
     * Valida que el archivo cumpla con los requisitos.
     *
     * @param file Archivo a validar
     * @throws FileStorageException si el archivo no es válido
     */
    private void validarArchivo(MultipartFile file) {
        if (file.isEmpty()) {
            throw new FileStorageException("El archivo está vacío");
        }

        // Validar tipo de archivo
        String contentType = file.getContentType();
        if (contentType == null || !contentType.startsWith("image/")) {
            throw new FileStorageException("Solo se permiten archivos de imagen");
        }

        // Validar tamaño (máximo 10MB)
        long maxSize = 10 * 1024 * 1024; // 10MB
        if (file.getSize() > maxSize) {
            throw new FileStorageException("El archivo no puede exceder 10MB");
        }

        // Validar extensiones permitidas
        String fileName = file.getOriginalFilename();
        if (fileName != null) {
            String extension = fileName.substring(fileName.lastIndexOf(".") + 1).toLowerCase();
            if (!extension.matches("^(jpg|jpeg|png|gif|webp)$")) {
                throw new FileStorageException("Solo se permiten archivos JPG, JPEG, PNG, GIF y WEBP");
            }
        }
    }

    /**
     * Genera un nombre único para el archivo.
     *
     * @param originalFileName Nombre original del archivo
     * @param folder Carpeta de destino
     * @param tipoUsuario Tipo de usuario
     * @param idUsuario ID del usuario
     * @return Nombre de archivo único
     */
    private String generarNombreArchivo(String originalFileName, String folder, String tipoUsuario, Long idUsuario) {
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
        String uuid = UUID.randomUUID().toString().substring(0, 8);
        String extension = "";
        
        if (originalFileName != null && originalFileName.contains(".")) {
            extension = originalFileName.substring(originalFileName.lastIndexOf("."));
        }

        return String.format("%s/%s/%s/%s_%s%s", 
                folder, tipoUsuario, idUsuario, timestamp, uuid, extension);
    }

    /**
     * Extrae el nombre del archivo de una URL completa.
     *
     * @param imageUrl URL completa de la imagen
     * @return Nombre del archivo (key) en S3
     */
    private String extraerNombreArchivoDeUrl(String imageUrl) {
        if (imageUrl.startsWith(s3Config.getBaseUrl())) {
            return imageUrl.substring(s3Config.getBaseUrl().length() + 1);
        }
        
        // Si la URL no coincide con nuestro formato, intentar extraer la parte final
        return imageUrl.substring(imageUrl.lastIndexOf("/") + 1);
    }
}