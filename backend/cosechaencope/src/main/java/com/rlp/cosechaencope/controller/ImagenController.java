package com.rlp.cosechaencope.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.rlp.cosechaencope.dto.response.ImageUploadResponse;
import com.rlp.cosechaencope.dto.response.MessageResponse;
import com.rlp.cosechaencope.exception.FileStorageException;
import com.rlp.cosechaencope.service.S3FileStorageService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;

/**
 * Controlador REST para la gestión de imágenes en AWS S3.
 *
 * <p>
 * Proporciona endpoints para subir y eliminar imágenes que serán utilizadas en
 * artículos y categorías del sistema.</p>
 *
 * @author rafalopezzz
 */
@Slf4j
@CrossOrigin(origins = "*", allowedHeaders = "*")
@RestController
@RequestMapping("/cosechaencope/imagenes")
@Tag(name = "Gestión de Imágenes", description = "Operaciones para subir y eliminar imágenes en AWS S3")
public class ImagenController {

    @Autowired
    private S3FileStorageService s3FileStorageService;

    /**
     * Sube una imagen para un artículo.
     *
     * @param file Archivo de imagen a subir
     * @param idUsuario ID del usuario que sube la imagen
     * @param tipoUsuario Tipo de usuario (PRODUCTOR, ADMIN)
     * @return URL de la imagen subida
     */
    @Operation(
            summary = "Subir imagen de artículo",
            description = "Sube una imagen al bucket S3 para ser utilizada en un artículo"
    )
    @ApiResponses({
        @ApiResponse(
                responseCode = "201",
                description = "Imagen subida exitosamente",
                content = @Content(
                        mediaType = "application/json",
                        schema = @Schema(implementation = ImageUploadResponse.class)
                )
        ),
        @ApiResponse(
                responseCode = "400",
                description = "Archivo inválido o parámetros incorrectos"
        ),
        @ApiResponse(
                responseCode = "500",
                description = "Error interno del servidor"
        )
    })
    @PostMapping("/articulos")
    public ResponseEntity<ImageUploadResponse> subirImagenArticulo(
            @Parameter(description = "Archivo de imagen", required = true)
            @RequestParam("file") MultipartFile file,
            @Parameter(description = "ID del usuario", required = true)
            @RequestParam("idUsuario") Long idUsuario,
            @Parameter(description = "Tipo de usuario", required = true)
            @RequestParam("tipoUsuario") String tipoUsuario) {

        try {
            String imageUrl = s3FileStorageService.subirImagen(file, "articulos", tipoUsuario.toLowerCase(), idUsuario);

            ImageUploadResponse response = new ImageUploadResponse();
            response.setImageUrl(imageUrl);
            response.setMessage("Imagen subida exitosamente");
            response.setFileName(file.getOriginalFilename());
            response.setFileSize(file.getSize());

            log.info("Imagen de artículo subida por usuario {}: {}", idUsuario, imageUrl);

            return ResponseEntity.status(HttpStatus.CREATED).body(response);

        } catch (FileStorageException e) {
            log.error("Error al subir imagen de artículo: {}", e.getMessage());
            return ResponseEntity.badRequest()
                    .body(ImageUploadResponse.error(e.getMessage()));
        } catch (Exception e) {
            log.error("Error inesperado al subir imagen: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ImageUploadResponse.error("Error interno del servidor"));
        }
    }

    /**
     * Sube una imagen para una categoría.
     *
     * @param file Archivo de imagen a subir
     * @param idUsuario ID del usuario que sube la imagen
     * @param tipoUsuario Tipo de usuario (PRODUCTOR, ADMIN)
     * @return URL de la imagen subida
     */
    @Operation(
            summary = "Subir imagen de categoría",
            description = "Sube una imagen al bucket S3 para ser utilizada en una categoría"
    )
    @ApiResponses({
        @ApiResponse(
                responseCode = "201",
                description = "Imagen subida exitosamente",
                content = @Content(
                        mediaType = "application/json",
                        schema = @Schema(implementation = ImageUploadResponse.class)
                )
        ),
        @ApiResponse(
                responseCode = "400",
                description = "Archivo inválido o parámetros incorrectos"
        ),
        @ApiResponse(
                responseCode = "500",
                description = "Error interno del servidor"
        )
    })
    @PostMapping("/categorias")
    public ResponseEntity<ImageUploadResponse> subirImagenCategoria(
            @Parameter(description = "Archivo de imagen", required = true)
            @RequestParam("file") MultipartFile file,
            @Parameter(description = "ID del usuario", required = true)
            @RequestParam("idUsuario") Long idUsuario,
            @Parameter(description = "Tipo de usuario", required = true)
            @RequestParam("tipoUsuario") String tipoUsuario) {

        try {
            String imageUrl = s3FileStorageService.subirImagen(file, "categorias", tipoUsuario.toLowerCase(), idUsuario);

            ImageUploadResponse response = new ImageUploadResponse();
            response.setImageUrl(imageUrl);
            response.setMessage("Imagen subida exitosamente");
            response.setFileName(file.getOriginalFilename());
            response.setFileSize(file.getSize());

            log.info("Imagen de categoría subida por usuario {}: {}", idUsuario, imageUrl);

            return ResponseEntity.status(HttpStatus.CREATED).body(response);

        } catch (FileStorageException e) {
            log.error("Error al subir imagen de categoría: {}", e.getMessage());
            return ResponseEntity.badRequest()
                    .body(ImageUploadResponse.error(e.getMessage()));
        } catch (Exception e) {
            log.error("Error inesperado al subir imagen: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ImageUploadResponse.error("Error interno del servidor"));
        }
    }

    /**
     * Sube una imagen para el perfil de un productor.
     *
     * @param file Archivo de imagen a subir
     * @param idUsuario ID del usuario que sube la imagen
     * @param tipoUsuario Tipo de usuario (PRODUCTOR, ADMIN)
     * @return URL de la imagen subida
     */
    @Operation(
            summary = "Subir imagen de perfil",
            description = "Sube una imagen al bucket S3 para ser utilizada en el perfil de un productor"
    )
    @ApiResponses({
        @ApiResponse(
                responseCode = "201",
                description = "Imagen subida exitosamente",
                content = @Content(
                        mediaType = "application/json",
                        schema = @Schema(implementation = ImageUploadResponse.class)
                )
        ),
        @ApiResponse(
                responseCode = "400",
                description = "Archivo inválido o parámetros incorrectos"
        ),
        @ApiResponse(
                responseCode = "500",
                description = "Error interno del servidor"
        )
    })
    @PostMapping("/perfiles")
    public ResponseEntity<ImageUploadResponse> subirImagenPerfil(
            @Parameter(description = "Archivo de imagen", required = true)
            @RequestParam("file") MultipartFile file,
            @Parameter(description = "ID del usuario", required = true)
            @RequestParam("idUsuario") Long idUsuario,
            @Parameter(description = "Tipo de usuario", required = true)
            @RequestParam("tipoUsuario") String tipoUsuario) {

        try {
            String imageUrl = s3FileStorageService.subirImagen(file, "perfiles", tipoUsuario.toLowerCase(), idUsuario);

            ImageUploadResponse response = new ImageUploadResponse();
            response.setImageUrl(imageUrl);
            response.setMessage("Imagen de perfil subida exitosamente");
            response.setFileName(file.getOriginalFilename());
            response.setFileSize(file.getSize());

            log.info("Imagen de perfil subida por usuario {}: {}", idUsuario, imageUrl);

            return ResponseEntity.status(HttpStatus.CREATED).body(response);

        } catch (FileStorageException e) {
            log.error("Error al subir imagen de perfil: {}", e.getMessage());
            return ResponseEntity.badRequest()
                    .body(ImageUploadResponse.error(e.getMessage()));
        } catch (Exception e) {
            log.error("Error inesperado al subir imagen: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ImageUploadResponse.error("Error interno del servidor"));
        }
    }

    /**
     * Elimina una imagen del bucket S3.
     *
     * @param imageUrl URL completa de la imagen a eliminar
     * @return Mensaje de confirmación
     */
    @Operation(
            summary = "Eliminar imagen",
            description = "Elimina una imagen del bucket S3"
    )
    @ApiResponses({
        @ApiResponse(
                responseCode = "200",
                description = "Imagen eliminada exitosamente",
                content = @Content(
                        mediaType = "application/json",
                        schema = @Schema(implementation = MessageResponse.class)
                )
        ),
        @ApiResponse(
                responseCode = "400",
                description = "URL de imagen inválida"
        ),
        @ApiResponse(
                responseCode = "500",
                description = "Error al eliminar imagen"
        )
    })
    @DeleteMapping
    public ResponseEntity<MessageResponse> eliminarImagen(
            @Parameter(description = "URL de la imagen a eliminar", required = true)
            @RequestParam("imageUrl") String imageUrl) {

        try {
            boolean eliminada = s3FileStorageService.eliminarImagen(imageUrl);

            if (eliminada) {
                log.info("Imagen eliminada exitosamente: {}", imageUrl);
                return ResponseEntity.ok(new MessageResponse("Imagen eliminada exitosamente"));
            } else {
                log.warn("No se pudo eliminar la imagen: {}", imageUrl);
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                        .body(new MessageResponse("Error al eliminar la imagen"));
            }

        } catch (Exception e) {
            log.error("Error inesperado al eliminar imagen: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new MessageResponse("Error interno del servidor"));
        }
    }
}
