package com.rlp.cosechaencope.repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.rlp.cosechaencope.model.Productor;
import com.rlp.cosechaencope.model.Usuario;

public interface ProductorRepository extends JpaRepository<Productor, Long> {

    /**
     * Busca todos los productores cuyo nombre coincida exactamente con el proporcionado.
     *
     * @param nombre Nombre del productor a buscar.
     * @return Lista de productores con el nombre indicado.
     */
    List<Productor> findByNombre(String nombre);

    /**
     * Busca el productor asociado al usuario cuyo ID coincide con el proporcionado.
     *
     * @param idUsuario ID del usuario asociado al productor.
     * @return {@link Optional} con el productor si existe, o vacío si no se encuentra.
     */
    Optional<Productor> findByUsuario_IdUsuario(Long idUsuario);

    /**
     * Busca el productor asociado al usuario proporcionado.
     *
     * @param usuario Usuario asociado al productor.
     * @return {@link Optional} con el productor si existe, o vacío si no se encuentra.
     */
    Optional<Productor> findByUsuario(Usuario usuario);

    /**
     * Obtiene los productores que se registraron después de la fecha/hora indicada.
     *
     * @param fecha Fecha y hora límite.
     * @return Lista de produdoctores registrados posterior a la fecha proporcionada.
     */
    List<Productor> findByFechaRegistroAfter(LocalDateTime fecha);
    
    /**
     * Busca el productor cuyo teléfono coincide con el proporcionado.
     *
     * @param telefono Teléfono del productor a buscar.
     * @return {@link Optional} con el productor si existe, o vacío si no se encuentra.
     */
    Optional<Productor> findByTelefono(String telefono);

}
