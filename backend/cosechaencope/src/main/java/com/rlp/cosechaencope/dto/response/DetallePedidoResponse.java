package com.rlp.cosechaencope.dto.response;

import java.math.BigDecimal;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(
        description = "Detalles de un pedido, incluyendo productos y cantidades"
)
public class DetallePedidoResponse {

    @Schema(
            description = "ID del detalle del pedido",
            example = "1"
    )
    private Long idPedido;

    @Schema(
            description = "ID del artículo en el pedido",
            example = "101"
    )
    private Long idArticulo;

    @Schema(
            description = "Nombre del artículo en el pedido",
            example = "Brócoli Orgánico"
    )
    private String nombreArticulo;

    @Schema(
            description = "Cantidad de artículos en el pedido",
            example = "5"
    )
    private Integer cantidad;

    @Schema(
            description = "Precio unitario del artículo en el pedido",
            example = "2.50"
    )
    private BigDecimal precioUnitario;

    @Schema(
            description = "Total de la linea en el pedido",
            example = "3.50"
    )
    private BigDecimal totalLinea;

    public BigDecimal getTotalLinea() {
        return totalLinea;
    }

    public void setTotalLinea(BigDecimal totalLinea) {
        this.totalLinea = totalLinea;
    }

}
