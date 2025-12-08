package com.rlp.cosechaencope.model;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonManagedReference;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;
import lombok.Data;
import lombok.ToString;

@Data
@Entity
@Table(name = "orden_venta_productor")
public class OrdenVentaProductor {
    /**
     * Identificador único de la orden de venta. Se genera automáticamente.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long idOvp;

    /**
     * Pedido del cliente asociado a esta orden de venta. Se establece una
     * relación de muchos a uno con la entidad Pedido.
     */
    @ManyToOne
    @JoinColumn(name = "id_pedido_cliente")
    private Pedido pedidoCliente;

    /**
     * productor al que se envía esta orden de venta. Se establece una relación
     * de muchos a uno con la entidad productor.
     */
    @ManyToOne
    @JoinColumn(name = "id_productor")
    private Productor productor;

    /**
     * Pedido del productor asociado a esta orden de venta. Se establece una
     * relación de muchos a uno con la entidad Pedido, que representa el pedido
     * realizado al productor.
     */
    @ManyToOne
    @JoinColumn(name = "id_pedido")
    private Pedido pedido;

    /**
     * Número único de la orden de venta (formato: OVP-YYYYMMDD-XXXX).
     */
    @Column(unique = true)
    private String numeroOrden;

    /**
     * Fecha y hora en que se creó la orden de venta. Se establece un valor por
     * defecto de la fecha y hora actual.
     */
    @Column(name = "fecha_creacion")
    private Instant fechaCreacion;

    /**
     * Estado actual de la orden de venta. Puede ser PENDIENTE, ENVIADO,
     * ENTREGADO o CANCELADO.
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "estado", nullable = false, length = 20)
    private EstadoOrdenVenta estado = EstadoOrdenVenta.PENDIENTE;

    @Column(name = "fecha_actualizacion")
    private Instant fechaActualizacion;

    /**
     * Lista de líneas o ítems que forman parte de esta orden de venta. Cada
     * línea indica qué artículo y cuánta cantidad se le solicita al productor.
     * 
     * - mappedBy = "ordenVenta": indica que la relación es bidireccional y la 
     * entidad DetalleOvp tiene la clave foránea.
     * 
     * - CascadeType.ALL: cualquier cambio en la orden (guardar, eliminar) se
     * propaga a las líneas. - orphanRemoval = true: si se elimina una línea de
     * la lista, también se elimina de la base.
     * 
     * - @JsonManagedReference: maneja la serialización JSON para evitar
     * referencias circulares con la entidad DetalleOvp.
     * 
     * - @ToString.Exclude: evita que las líneas se incluyan en el método toString
     * generado automáticamente por Lombok, previniendo posibles bucles.
     */
    @JsonManagedReference
    @ToString.Exclude
    @OneToMany(mappedBy = "ordenVenta", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<DetalleOvp> lineas = new ArrayList<>();

    /**
     * Observaciones adicionales para el productor.
     */
    private String observaciones;

    /**
     * Total calculado de la orden de venta.
     * Se calcula dinámicamente sumando los subtotales de cada línea.
     */
    @Transient
    public BigDecimal getTotal() {
        if (lineas == null || lineas.isEmpty()) {
            return BigDecimal.ZERO;
        }
        return lineas.stream()
                .map(detalle -> {
                    BigDecimal precio = detalle.getPrecioUnitario() != null 
                            ? detalle.getPrecioUnitario() 
                            : BigDecimal.ZERO;
                    int cantidad = detalle.getCantidad() != null 
                            ? detalle.getCantidad() 
                            : 0;
                    return precio.multiply(BigDecimal.valueOf(cantidad));
                })
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    /**
     * Alias para compatibilidad con el frontend que espera "detalles"
     */
    @Transient
    public List<DetalleOvp> getDetalles() {
        return this.lineas;
    }
}
