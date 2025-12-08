package com.rlp.cosechaencope.dto.request;

import java.math.BigDecimal;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * DTO (Data Transfer Object) para recibir datos de creación o actualización de un artículo.
 *
 * <p>Este objeto encapsula la información enviada por el cliente cuando se crea o actualiza un artículo
 * en la base de datos. Se utiliza principalmente en los controladores para recibir y validar entradas.</p>
 *
 * <p>Incluye validaciones con anotaciones como {@code @NotBlank}, {@code @DecimalMin}, y {@code @Min}
 * para garantizar que los datos cumplan ciertas condiciones básicas antes de procesarlos.</p>
 * 
 * Ejemplo de uso en una petición JSON - crear artículo - nombre(NotNull)
 * <pre>
 *  {
 *   "nombre": "Tomate Orgánico",
 *   "descripcion": "Tomate cultivado sin pesticidas ni químicos",
 *   "precio": 0.99,
 *   "stock": 10,
 *   "idCategoria": 1,
 *   "nombreCategoria": "Ale",
 *   "idproductor": 1,
 *   "nombreProductor": "Productor Genérico",
 *   "imagenUrl": null
 *   }
 * </pre>
 *
 * @author rafalopezzz
 */
@Data
@Schema(description = "Datos para la creación o actualización de un artículo")
public class ArticuloRequest {

    @NotBlank
    @Schema(description = "Nombre del artículo", example = "Tomate Raf", required = true)
    private String nombre;

    @Schema(description = "Descripción detallada del artículo", example = "Tomate típico de la Marina de Cope cultivado sin pesticidas ni químicos")
    private String descripcion;

    @DecimalMin(value = "0.0")
    @Schema(description = "Precio del artículo", example = "4.99", required = true)
    private BigDecimal precio;

    @Min(0)
    @Schema(description = "Cantidad disponible en stock", example = "50", required = true)
    private Integer stock;
    
    @Schema(description = "ID de la categoría a la que pertenece el artículo", example = "1", required = true)
    private Long idCategoria;

    @Schema(description= "ID del productor del artículo", example = "1", required = true)
    private long idProductor;

    @Schema(description= "URL de la imagen del artículo", example = "https://example.com/imagen.jpg")
    private String imagenUrl;
}
