package com.rlp.cosechaencope.model;

import java.math.BigDecimal;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Data;

/**
 * Entidad que representa un artículo o producto dentro del sistema Cosecha-en-Cope.
 *
 * Cada artículo está relacionado con una categoría, y contiene información
 * relevante para su visualización y comercialización, como nombre, descripción,
 * precio, stock y graduación.
 *
 * <p>
 * Está mapeada a la tabla {@code articulos} en la base de datos.
 *
 * <p>
 * Se usa {@code @Data} de Lombok para generar automáticamente los métodos
 * estándar como getters, setters, equals, hashCode y toString.</p>
 *
 * @author rafalopezzz
 */
@Data
@Entity
@Table(name = "articulo")
public class Articulo {


    /**
     * Identificador único del artículo. Se genera automáticamente.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_articulo")
    private Long idArticulo;

    /**
     * Nombre del artículo. Este campo es obligatorio.
     */
    @Column(name = "nombre", nullable = false)
    private String nombre;

    /**
     * Precio del artículo. Campo obligatorio.
     */
    @Column(name = "precio", nullable = false)
    private BigDecimal precio;

    /**
     * Descripción larga del artículo. Se permite texto extenso gracias a
     * {@code columnDefinition = "TEXT"}.
     */
    @Column(name = "descripcion", columnDefinition = "TEXT")
    private String descripcion;

    /**
     * Stock disponible del artículo. Campo obligatorio.
     */
    @Column(name = "stock", nullable = false)
    private Integer stock;

   /**
     * URL asociada al artículo, normalmente usada para imágenes del producto.
     */
    @Column(name = "imagen_url")
    private String imagenUrl;

    /**
     * Relación muchos a uno con la categoría del artículo. Un artículo
     * pertenece a una única categoría, pero una categoría puede tener muchos
     * artículos.
     */
    @ManyToOne
    @JoinColumn(name = "id_categoria")
    private Categoria categoria;

    /**
     * El productor que suministra este artículo. Obligatorio: cada artículo
     * debe pertenecer a un productor.
     */
    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "id_productor", nullable = false)
    private Productor productor;
}
