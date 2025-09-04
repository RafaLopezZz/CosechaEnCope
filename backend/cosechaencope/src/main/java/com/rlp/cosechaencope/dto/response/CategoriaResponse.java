package com.rlp.cosechaencope.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * DTO de respuesta para representar una categoría de artículos.
 * 
 * <p>Se utiliza para enviar al cliente información sobre las categorías disponibles
 * dentro del sistema, incluyendo su identificador, nombre y descripción.</p>
 * 
 * <p>Este DTO es útil, por ejemplo, para poblar menús desplegables o listas de categorías
 * en el frontend, sin necesidad de exponer directamente la entidad JPA.</p>
 * 
 * @author rafalopezzz
 */
@Data
@Schema(description = "Datos de respuesta de una categoría")
public class CategoriaResponse {

    @Schema(description = "ID de la categoría", example = "1")
    private Long idCategoria;

    @Schema(description = "Nombre de la categoría", example = "Frutas")
    private String nombre;
    
    @Schema(description = "Descripción de la categoría", example = "Categoría que agrupa todos los tipos de frutas")
    private String descripcion;

    @Schema(description = "URL de la imagen representativa de la categoría", example = "http://example.com/imagen.jpg")
    private String imagenUrl;
}
