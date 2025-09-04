package com.rlp.cosechaencope.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * DTO de petición para registrar un nuevo usuario.
 * 
 * <p>Este objeto contiene los datos necesarios para crear un usuario en el sistema,
 * incluyendo email, contraseña, rol y tipo de usuario. Además, puede contener información
 * adicional asociada a un cliente o productor.</p>
 *
 * <p>Las validaciones aseguran que los campos obligatorios como el correo y la
 * contraseña cumplan con los requisitos mínimos definidos.</p>
 *
 * <p>Dependiendo del valor de {@code tipoUsuario}, uno de los objetos {@code clienteData}
 * o {@code productorData} debe estar presente.</p>
 * 
 * Ejemplo de uso (JSON):
 * <pre>
 * {
 *   "email": "ejemplo@correo.com",
 *   "password": "12345678",
 *   "rol": "USER",
 *   "tipoUsuario": "CLIENTE",
 *   "clienteData": {
 *     "nombre": "Rafael",
 *     "direccion": "Calle Tal 69",
 *     "telefono": "666223344"
 *   }
 * }
 * </pre>
 * 
 * @author TuNombre
 */
@Data
public class UsuarioRequest {

    @Email
    @NotBlank
    private String email;

    @NotBlank
    @Size(min = 8, max = 20)
    private String password;

    private String rol;
    private String tipoUsuario;

    private ClienteRequest clienteData;
    private ProductorRequest productorData;
}
