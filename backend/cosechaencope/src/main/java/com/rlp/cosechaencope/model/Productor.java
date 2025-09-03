package com.rlp.cosechaencope.model;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

/**
 * Entidad que representa un productor en el sistema Cosecha-en-Cope.
 * 
 * Esta clase está mapeada a la tabla {@code productor}.
 * Cada productor está vinculado de forma única a un usuario.
 * 
 * <p>Contiene datos básicos como nombre, dirección, teléfono y la fecha en que se registró.</p>
 * 
 * <p>Se utiliza {@code @Data} de Lombok para generar automáticamente getters, setters, 
 * toString, equals y hashCode.</p>
 * 
 * @author rafalopezzz
 */
@Data
@Entity
@Table(name = "productor")
public class Productor {

    /** Identificador único del productor. Se genera automáticamente. */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long idProductor;

    /** Nombre del productor. Opcional. */
    @Column(name = "nombre", nullable = true)
    private String nombre;

    /** Dirección física del productor. */
    @Column(name = "direccion")
    private String direccion;

    /** Número de teléfono del productor. */
    @Column(name = "telefono")
    private String telefono;

    /**
     * Fecha y hora en la que el productor se registró.
     * 
     * Se inicializa automáticamente con la fecha y hora actual.
     */
    @Column(name = "fecha_registro")
    private LocalDateTime fechaRegistro = LocalDateTime.now();

    /**
     * URL a la imagen asociada al productor. 
     */
    @Column(name = "imagen_url")
    private String imagenUrl;

    /**
     * Relación uno a uno con la entidad Usuario.
     * 
     * El productor está asociado a un único usuario. La columna {@code id_usuario} es única,
     * lo que garantiza que un mismo usuario no pueda ser productor más de una vez.
     */
    @OneToOne
    @JoinColumn(name = "id_usuario", unique = true)
    @EqualsAndHashCode.Exclude
    @ToString.Exclude
    private Usuario usuario;
}
