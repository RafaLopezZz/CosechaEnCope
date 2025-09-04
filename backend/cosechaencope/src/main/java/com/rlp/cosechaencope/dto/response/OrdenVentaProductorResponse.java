package com.rlp.cosechaencope.dto.response;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "Datos de respuesta de una orden de venta a productor")
public class OrdenVentaProductorResponse {
    
    private Long idOvp;
    private String numeroOrden;
    private Instant fechaCreacion;
    private String estado;
    private BigDecimal total;
    private String observaciones;
    private ProductorResponse productor;
    private List<DetalleOvpResponse> detalles;

}
