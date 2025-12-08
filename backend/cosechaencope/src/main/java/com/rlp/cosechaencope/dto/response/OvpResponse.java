package com.rlp.cosechaencope.dto.response;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

import com.rlp.cosechaencope.model.EstadoOrdenVenta;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO de respuesta para Orden de Venta a Productor.
 * Evita referencias circulares al serializar a JSON.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OvpResponse {
    
    private Long idOvp;
    private String numeroOrden;
    private Instant fechaCreacion;
    private Instant fechaActualizacion;
    private EstadoOrdenVenta estado;
    private String observaciones;
    private BigDecimal total;
    
    // Información del productor (simplificada)
    private ProductorBasicoResponse productor;
    
    // Información del pedido cliente (simplificada)
    private PedidoClienteBasicoResponse pedidoCliente;
    
    // Detalles/líneas de la orden
    private List<DetalleOvpResponse> detalles;
    
    /**
     * Información básica del productor
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ProductorBasicoResponse {
        private Long idProductor;
        private String nombre;
        private String email;
        private String telefono;
    }
    
    /**
     * Información básica del pedido cliente
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PedidoClienteBasicoResponse {
        private Long idPedido;
        private String idTransaccion;
        private Instant fechaPedido;
        private String metodoPago;
        private BigDecimal total;
        private ClienteBasicoResponse cliente;
    }
    
    /**
     * Información básica del cliente
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ClienteBasicoResponse {
        private Long idCliente;
        private String nombre;
        private String telefono;
        private String direccion;
    }
}
