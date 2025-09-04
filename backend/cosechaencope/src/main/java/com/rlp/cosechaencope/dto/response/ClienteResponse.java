package com.rlp.cosechaencope.dto.response;

import java.time.LocalDateTime;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * DTO de respuesta que representa los datos completos de un cliente.
 *
 * <p>Este objeto se utiliza para devolver al cliente (frontend) la información
 * relevante de un cliente registrado, incluyendo su relación con el usuario
 * y su fecha de registro.</p>
 *
 * <p>Se puede usar, por ejemplo, al consultar el perfil de un cliente o al listar
 * clientes desde el backend. Incorpora un {@link UsuarioResponse} para representar
 * la información del usuario asociado sin exponer datos sensibles.</p>
 *
 * Ejemplo de respuesta:
 * <pre>
 * {
 *   "idCliente": 12,
 *   "usuario": {
 *     "idUsuario": 5,
 *     "email": "cliente@ejemplo.com",
 *     "rol": "USER",
 *     "tipoUsuario": "CLIENTE"
 *   },
 *   "nombre": "Rafael López",
 *   "direccion": "Calle Tal 69",
 *   "telefono": "666223344",
 *   "fechaRegistro": "2024-03-10T14:30:00"
 * }
 * </pre>
 * 
 * @author rafalopezzz
 */
@Data
@Schema(description = "Datos de respuesta de un cliente")
public class ClienteResponse {

    @Schema(description = "ID del cliente", example = "1")
    private Long idCliente;

    @Schema(description = "Información del usuario asociado al cliente")
    private UsuarioResponse usuario;
    
    @Schema(description = "Nombre del cliente", example = "Juan Pérez")
    private String nombre;

    @Schema(description = "Apellido del cliente", example = "Gómez")
    private String direccion;

    @Schema(description = "Teléfono del cliente", example = "123456789")
    private String telefono;

    @Schema(description = "Correo electrónico del cliente", example = "rlp@example.com")
    private LocalDateTime fechaRegistro;

}
