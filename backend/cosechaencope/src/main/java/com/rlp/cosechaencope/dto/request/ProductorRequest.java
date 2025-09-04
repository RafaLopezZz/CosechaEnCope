package com.rlp.cosechaencope.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "Datos de petición para registrar un nuevo productor")
public class ProductorRequest {
        @Schema(
            description = "ID único del usuario asociado al productor",
            example = "7",
            required = false
    )
    private Long idUsuario;

    @Schema(
            description = "Nombre comercial del productor",
            example = "Cervezas Artesanales S.A.",
            required = true
    )
    private String nombre;

    @Schema(
            description = "Dirección física del productor",
            example = "Calle Tal 69",
            required = true
    )
    private String direccion;

    @Schema(
            description = "Número de teléfono de contacto del productor",
            example = "666223344",
            required = true
    )
    private String telefono;

    @Schema(
            description = "URL del sitio web del provductor",
            example = "https://tomatesorganicos.com",
            required = false
    )
    private String imagenUrl;

}
