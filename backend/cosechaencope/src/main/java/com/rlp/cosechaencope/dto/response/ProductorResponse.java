package com.rlp.cosechaencope.dto.response;
import java.time.LocalDateTime;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * DTO de respuesta que representa los datos completos de un productor.
 *
 * <p>Este objeto es utilizado para devolver al cliente (frontend) la información
 * relevante sobre un productor, incluyendo su relación con el usuario y detalles
 * como la fecha de registro y la URL asociada.</p>
 *
 * <p>Contiene un {@link UsuarioResponse} para representar la información del
 * usuario asociado al productor sin exponer datos sensibles, como la contraseña.</p>
 *
 * Ejemplo de respuesta en formato JSON:
 * <pre>
 * {
 *   "idProductor": 3,
 *   "usuario": {
 *     "idUsuario": 8,
 *     "email": "tomate@cope.com",
 *     "rol": "USER",
 *     "tipoUsuario": "PRODUCTOR"
 *   },
 *   "nombre": "Tomates Cope S.A.",
 *   "direccion": "Calle Tal 69",
 *   "telefono": "666223344",
 *   "fechaRegistro": "2024-05-01T09:00:00",
 *   "url": "https://tomatescope.com"
 * }
 * </pre>
 * 
 * @author rafalopezzz
 */
@Data
@Schema(description = "Datos de respuesta de un productor")
public class ProductorResponse {

    @Schema(description = "ID del productor", example = "1")
    private Long idProductor;

    @Schema(description = "Información del usuario asociado al productor")
    private UsuarioResponse usuario;

    @Schema(description = "Nombre del productor", example = "Rafa López")
    private String nombre;

    @Schema(description = "Dirección del productor", example = "Calle Tal 69")
    private String direccion;

    @Schema(description = "Teléfono del productor", example = "123456789")
    private String telefono;

    @Schema(description = "Fecha de registro del productor", example = "2024-03-10T14:30:00")
    private LocalDateTime fechaRegistro;

    @Schema(description = "URL de la imagen del productor", example = "https://example.com/imagen-productor.jpg")
    private String imagenUrl;    

}
