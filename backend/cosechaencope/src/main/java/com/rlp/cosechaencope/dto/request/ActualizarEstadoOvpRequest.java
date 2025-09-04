package com.rlp.cosechaencope.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * DTO de request para actualizar estado de OVP.
 */
@Data
@Schema(
        description = "Datos para actualizar el estado de una orden de venta a productor"
)
public class ActualizarEstadoOvpRequest {

    @Schema(
            description = "Nuevo estado de la orden de venta a productor",
            example = "CONFIRMADO",
            allowableValues = {"PENDIENTE", "CONFIRMADO", "ENVIADO", "ENTREGADO", "CANCELADO"},
            required = true
    )
    @NotNull
    private String estado;

    @Schema(
            description = "Observaciones adicionales sobre el cambio de estado",
            example = "El pedido ha sido confirmado y está en proceso de preparación."
    )
    private String observaciones;
}
