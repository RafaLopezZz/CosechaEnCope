package com.rlp.cosechaencope.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * DTO de respuesta para representar los datos públicos de un usuario.
 *
 * <p>Este objeto es devuelto por el backend cuando se consulta información de
 * un usuario, por ejemplo después del registro, login o en el perfil. Contiene
 * datos básicos sin exponer información sensible como la contraseña.</p>
 *
 * <p>Se utiliza comúnmente en respuestas HTTP para mantener la separación entre
 * el modelo de entidad y lo que se expone al cliente.</p>
 * 
 * Ejemplo de respuesta:
 * <pre>
 * {
 *   "idUsuario": 1,
 *   "email": "usuario@ejemplo.com",
 *   "rol": "USER",
 *   "tipoUsuario": "CLIENTE"
 * }
 * </pre>
 * 
 * @author rafalopezzz
 */
@Data
@Schema(description = "Datos de respuesta de un usuario")
public class UsuarioResponse {

    @Schema(description = "ID del usuario", example = "1")
    private Long idUsuario;
    
    @Schema(description = "Correo electrónico del usuario", example = "rlp@example.com")
    private String email;

    @Schema(description = "Rol del usuario", example = "USER")
    private String rol;

    @Schema(description = "Tipo de usuario", example = "CLIENTE")
    private String tipoUsuario;

}
