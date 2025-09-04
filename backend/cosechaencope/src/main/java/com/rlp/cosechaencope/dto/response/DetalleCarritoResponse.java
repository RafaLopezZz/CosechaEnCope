package com.rlp.cosechaencope.dto.response;

import java.math.BigDecimal;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "Datos de respuesta de un detalle del carrito de compras")
public class DetalleCarritoResponse {

    @Schema(description = "ID del detalle del carrito", example = "1")
    private Long id;

    @Schema(description = "ID del artículo en el carrito", example = "1")
    private Long idArticulo;

    @Schema(description = "Nombre del artículo en el carrito", example = "Tomate Raf")
    private String nombreArticulo;

    @Schema(description = "Descripción del artículo en el carrito", example = "Tomate de variedad Raf, muy sabroso y jugoso")
    private int cantidad;

    @Schema(description = "Precio unitario del artículo en el carrito", example = "4.99")
    private BigDecimal precioUnitario;

    @Schema(description = "Total de la línea del carrito", example = "19.96")
    private BigDecimal totalLinea;
}
