package com.rlp.cosechaencope.dto.response;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "Datos de respuesta de una orden de venta a productor")
public class OrdenVentaProductorResponse {

    @Schema(
            description = "ID de la orden de venta a productor"
    )
    private Long idOvp;

    @Schema(
            description = "Número de la orden de venta a productor",
            example = "OVP-2024-0001"
    )
    private String numeroOrden;

    @Schema(
            description = "Fecha de creación de la orden de venta a productor",
            example = "2024-06-01T12:00:00Z"
    )
    private Instant fechaCreacion;

    @Schema(
            description = "Estado de la orden de venta a productor",
            example = "PENDIENTE"
    )
    private String estado;

    @Schema(
            description = "Total de la orden de venta a productor",
            example = "1500.75"
    )
    private BigDecimal total;

    @Schema(
            description = "Observaciones de la orden de venta a productor",
            example = "Entregar antes del fin de mes"
    )
    private String observaciones;

    @Schema(
            description = "Datos del productor asociado a la orden de venta"
    )
    private ProductorResponse productor;

    @Schema(
            description = "Detalles de la orden de venta a productor"
    )
    private List<DetalleOvpResponse> detalles;

}
