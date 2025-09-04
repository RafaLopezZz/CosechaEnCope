package com.rlp.cosechaencope.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * DTO de respuesta para la subida de imágenes.
 * 
 * <p>Contiene la información de respuesta cuando se sube una imagen al sistema,
 * incluyendo la URL generada, el nombre del archivo y metadatos adicionales.</p>
 * 
 * @author rafalopezzz
 */
@Data
@Schema(description = "Respuesta de subida de imagen")
public class ImageUploadResponse {

    @Schema(description = "URL pública de la imagen subida", example = "https://bucket.s3.region.amazonaws.com/articulos/productor/1/20231005_143022_abc123def.jpg")
    private String imageUrl;

    @Schema(description = "Mensaje de respuesta", example = "Imagen subida exitosamente")
    private String message;

    @Schema(description = "Nombre original del archivo", example = "imagen_producto.jpg")
    private String fileName;

    @Schema(description = "Tamaño del archivo en bytes", example = "1048576")
    private Long fileSize;

    @Schema(description = "Indica si la operación fue exitosa", example = "true")
    private boolean success;

    /**
     * Constructor vacío.
     */
    public ImageUploadResponse() {
        this.success = true;
    }

    /**
     * Constructor para respuestas exitosas.
     *
     * @param imageUrl URL de la imagen
     * @param message Mensaje de éxito
     */
    public ImageUploadResponse(String imageUrl, String message) {
        this.imageUrl = imageUrl;
        this.message = message;
        this.success = true;
    }

    /**
     * Método estático para crear respuestas de error.
     *
     * @param errorMessage Mensaje de error
     * @return Respuesta de error
     */
    public static ImageUploadResponse error(String errorMessage) {
        ImageUploadResponse response = new ImageUploadResponse();
        response.setMessage(errorMessage);
        response.setSuccess(false);
        return response;
    }

    /**
     * Método estático para crear respuestas exitosas.
     *
     * @param imageUrl URL de la imagen
     * @param message Mensaje de éxito
     * @param fileName Nombre del archivo
     * @param fileSize Tamaño del archivo
     * @return Respuesta exitosa
     */
    public static ImageUploadResponse success(String imageUrl, String message, String fileName, Long fileSize) {
        ImageUploadResponse response = new ImageUploadResponse();
        response.setImageUrl(imageUrl);
        response.setMessage(message);
        response.setFileName(fileName);
        response.setFileSize(fileSize);
        response.setSuccess(true);
        return response;
    }
}