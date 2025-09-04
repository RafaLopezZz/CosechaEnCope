package com.rlp.cosechaencope.dto.response;

import java.math.BigDecimal;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "Detalles de una orden de venta a productor, incluyendo artículos y cantidades")
public class DetalleOvpResponse {

    @Schema(
            description = "ID del detalle de la orden de venta a productor",
            example = "1"
    )
    private Long idDetalleOvp;
    
    @Schema(
            description = "Artículo incluido en el detalle de la orden de venta a productor",
            example = "101"
    )
    private ArticuloResponse articulo;

    @Schema(
            description = "Cantidad del artículo en la orden de venta a productor",
            example = "50"
    )
    private Integer cantidad;

    @Schema(
            description = "Precio unitario del artículo en la orden de venta a productor",
            example = "10.99"
    )
    private BigDecimal precioUnitario;

    @Schema(
            description = "Subtotal del detalle de la orden de venta a productor",
            example = "549.50"
    )
    private BigDecimal subtotal;

}
