package com.rlp.cosechaencope.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
@Schema(
        description = "Request para cambiar el estado de un pedido"
)
public class CambiarEstadoRequest {

    @NotBlank(message = "El estado es requerido")
    @Schema(
            example = "EN_PREPARACION",
            description = "Nuevo estado: PENDIENTE, EN_PREPARACION, LISTO, ENVIADO, ENTREGADO"
    )
    private String estado;

    @Schema(
            example = "Iniciando preparación del pedido",
            description = "Nota adicional (opcional)"
    )
    private String nota;

}
