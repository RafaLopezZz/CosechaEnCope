package com.rlp.cosechaencope.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
@Schema(description = "Datos de petición para registrar un nuevo cliente")
public class ClienteRequest {

    
    /**
     * Identificador único del usuario asociado al cliente.
     *
     * <p>
     * Este campo establece la relación entre el cliente y su usuario
     * correspondiente. Es obligatorio para operaciones de actualización, pero
     * puede ser nulo durante la creación cuando se envía anidado en
     * UsuarioRequestDTO.</p>
     */
    @Schema(
            description = "ID único del usuario asociado al cliente",
            example = "1",
            required = false
    )
    private Long idUsuario;

    /**
     * Nombre completo del cliente.
     *
     * <p>
     * Debe incluir nombre y apellidos del cliente para identificación en
     * pedidos y comunicaciones comerciales.</p>
     */
    @Schema(
            description = "Nombre completo del cliente",
            example = "Rafael López Plana",
            minLength = 2,
            maxLength = 20
    )
    @Size(min = 2, max = 20, message = "El nombre debe tener entre 2 y 20 caracteres")
    private String nombre;

    /**
     * Dirección de entrega del cliente.
     *
     * <p>
     * Dirección completa donde se realizarán las entregas de pedidos. Debe
     * incluir calle, número, ciudad y código postal para facilitar el proceso
     * de envío.</p>
     */
    @Schema(
            description = "Dirección completa de entrega del cliente",
            example = "Calle Mayor 123, 4º B, 28001 Madrid",
            minLength = 10,
            maxLength = 100
    )
    @Size(min = 10, max = 100, message = "La dirección debe tener entre 10 y 100 caracteres")
    private String direccion;

    /**
     * Número de teléfono de contacto del cliente.
     *
     * <p>
     * Teléfono de contacto para comunicaciones relacionadas con pedidos,
     * entregas y atención al cliente. Debe ser un número válido.</p>
     */
    @Schema(
            description = "Número de teléfono de contacto del cliente",
            example = "666223344",
            pattern = "^[0-9+\\-\\s()]{9,15}$"
    )
    @Pattern(
            regexp = "^[0-9+\\-\\s()]{9,15}$",
            message = "El teléfono debe tener un formato válido (9-15 dígitos)"
    )
    private String telefono;

}
