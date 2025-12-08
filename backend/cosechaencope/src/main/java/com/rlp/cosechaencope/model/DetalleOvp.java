package com.rlp.cosechaencope.model;

import java.math.BigDecimal;

import com.fasterxml.jackson.annotation.JsonBackReference;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.validation.constraints.Min;
import lombok.Data;

/**
 * Representa una línea individual dentro de una Orden de Venta a productor (OVP).
 * 
 * Cada instancia de esta clase indica un artículo específico, la cantidad solicitada
 * y el precio unitario acordado. Esta clase está asociada a una OVP concreta.
 * 
 * Es útil para registrar la lista detallada de artículos que un productor debe entregar.
 * 
 * Mapeada a la tabla "detalle_ovp"
 */
@Data
@Entity
@Table(name = "detalle_ovp")
public class DetalleOvp {
  
  /**
   * Identificador único del detalle de OVP. Se genera automáticamente.
   */
  @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long idDetalleOvp;

  /**
   * OVP a la que pertenece este detalle. Se establece una relación de muchos a uno
   * con la entidad OrdenVentaproductor.
   */
  @JsonBackReference
  @ManyToOne
  @JoinColumn(name="id_ovp")
  private OrdenVentaProductor ordenVenta;

  /**
   * Artículo que forma parte de este detalle de OVP. Se establece una relación de
   * muchos a uno con la entidad Articulos.
   */
  @ManyToOne
  @JoinColumn(name="id_articulo")
  private Articulo articulo;

  /**
   * Cantidad del artículo solicitada en este detalle de OVP.
   */
  @Min(1)
  private Integer cantidad;
  
  /**
   * Precio unitario del artículo en este detalle de OVP.
   */
  private BigDecimal precioUnitario;

}

