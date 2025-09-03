package com.rlp.cosechaencope.model;

import java.util.List;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.Data;

/**
 * Entidad que representa una categoría de artículos dentro del sistema Cosecha-en-Cope.
 *
 * Cada categoría puede contener múltiples artículos, un nombre y una descripción.
 *
 * <p>
 * Está mapeada a la tabla {@code categoria} en la base de datos.
 * </p>
 *
 * <p>
 * Se usa {@code @Data} de Lombok para generar automáticamente los métodos
 * estándar como getters, setters, equals, hashCode y toString.</p>
 *
 * @author rafalopezzz
 */
@Data
@Entity
@Table(name = "categoria")
public class Categoria {

    /** Identificador único de la categoría. Se genera automáticamente. */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_categoria")
    private Long idCategoria;

    /**
     * Descripción larga de la categoría.
     * Se permite contenido extenso gracias a {@code columnDefinition = "TEXT"}.
     */
    @Column(name = "nombre", nullable = false)
    private String nombre;

    /**
     * Descripción larga de la categoría.
     * Se permite contenido extenso gracias a {@code columnDefinition = "TEXT"}.
     */
    @Column(columnDefinition = "TEXT")
    private String descripcion;

    /**
     * Lista de artículos que pertenecen a esta categoría.
     * 
     * Relación uno a muchos: una categoría puede tener muchos artículos.
     * El atributo {@code mappedBy = "categoria"} indica que la relación es gestionada desde la entidad Articulos.
     */
    @OneToMany(mappedBy = "categoria")
    private List<Articulo> articulos;

    @Column(name = "imagen_url")
    private String imagenUrl;
}
