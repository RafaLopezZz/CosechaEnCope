package com.rlp.cosechaencope.dto.response;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "Datos de respuesta del carrito de compras")
public class CarritoResponse {

    @Schema(description = "ID del carrito", example = "1")
    private Long id;

    @Schema(description = "ID del usuario propietario del carrito", example = "1")
    private Instant fechaCreacion;

    @Schema(description = "ID del usuario propietario del carrito", example = "1")
    private List<DetalleCarritoResponse> items;

    @Schema(description = "Subtotal del carrito", example = "100.00")
    private BigDecimal subtotal;

    @Schema(description = "Impuestos aplicados al carrito", example = "10.00")
    private BigDecimal impuestos;

    @Schema(description = "Gastos de envío del carrito", example = "5.00")
    private BigDecimal gastosEnvio;

    @Schema(description = "Total del carrito", example = "115.00")
    private BigDecimal total;


}
