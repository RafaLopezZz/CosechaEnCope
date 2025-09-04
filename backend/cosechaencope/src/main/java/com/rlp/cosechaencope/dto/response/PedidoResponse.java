package com.rlp.cosechaencope.dto.response;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(
        description = "Datos de respuesta de un pedido"
)
public class PedidoResponse {

    @Schema(
            description = "ID del pedido",
            example = "1"
    )
    private Long idPedido;

    @Schema(
            description = "ID del usuario que realizó el pedido",
            example = "2"
    )
    private Long idUsuario;

    @Schema(
            description = "ID del cliente que realizó el pedido",
            example = "3"
    )
    private Long idCliente;

    @Schema(
            description = "Nombre del cliente que realizó el pedido",
            example = "Rafael López"
    )
    private String nombreCliente;

    @Schema(
            description = "Email del usuario que realizó el pedido",
            example = "rlp@example.com"
    )
    private String emailUsuario;

    @Schema(
            description = "Dirección del cliente que realizó el pedido",
            example = "Calle Tal 69"
    )
    private String direccionCliente;

    @Schema(
            description = "Teléfono del cliente que realizó el pedido",
            example = "666223344"
    )
    private String telefonoCliente;

    @Schema(
            description = "Fecha en la que se realizó el pedido",
            example = "2023-03-15T10:00:00Z"
    )
    private Instant fechaPedido;

    @Schema(
            description = "Estado del pedido",
            example = "PENDIENTE"
    )
    private String estadoPedido;

    @Schema(
            description = "Sub total del pedido",
            example = "100.00"
    )
    private BigDecimal subTotal;

    @Schema(
            description = "IVA aplicado al pedido",
            example = "21.00"
    )
    private BigDecimal iva;

    @Schema(
            description = "Gastos de envío del pedido",
            example = "5.00"
    )
    private BigDecimal gastosEnvio;

    @Schema(
            description = "Total del pedido",
            example = "126.00"
    )
    private BigDecimal total;

    @Schema(
            description = "Método de pago utilizado para el pedido",
            example = "Tarjeta de crédito"
    )
    private String metodoPago;

    @Schema(
            description = "ID de la transacción asociada al pedido",
            example = "PED-1234567890"
    )
    private String idTransaccion;

    @Schema(
            description = "Detalles del pedido"
    )
    
    private List<DetallePedidoResponse> detalles;

    public List<DetallePedidoResponse> getDetalles() {
        return detalles;
    }

    public void setDetalles(List<DetallePedidoResponse> detalles) {
        this.detalles = detalles;
    }

}
