package com.rlp.cosechaencope.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.rlp.cosechaencope.model.Carrito;
import com.rlp.cosechaencope.model.Cliente;

/**
 * Repositorio para la entidad {@link Carrito}.
 *
 * <p>
 * Proporciona operaciones CRUD básicas heredadas de {@link JpaRepository}, así
 * como consultas personalizadas para gestionar carritos de compra de clientes.
 * </p>
 * 
 * <p>
 * <b>Optimización:</b> Las queries utilizan {@code LEFT JOIN FETCH} para cargar
 * eagerly los detalles del carrito y evitar el problema N+1 de lazy loading.
 * </p>
 *
 * @author rafalopezzz
 */
@Repository
public interface CarritoRepository extends JpaRepository<Carrito, Long> {

    /**
     * Busca el carrito activo (no finalizado) de un cliente, cargando todos
     * sus detalles de forma eager.
     * 
     * <p>
     * <b>Uso:</b> Este método se usa cuando el cliente está navegando/editando su carrito.
     * Solo debe haber un carrito activo por cliente.
     * </p>
     *
     * @param cliente El cliente cuyo carrito activo se desea buscar.
     * @return {@link Optional} con el carrito si existe, vacío si no tiene carrito activo.
     */
    @Query("""
        SELECT c FROM Carrito c
        LEFT JOIN FETCH c.detalleList d
        LEFT JOIN FETCH d.articulo
        WHERE c.cliente = :cliente AND c.finalizado = FALSE
        """)
    Optional<Carrito> findActivoConDetalles(@Param("cliente") Cliente cliente);

    /**
     * Busca cualquier carrito (activo o finalizado) de un cliente por su ID de usuario.
     * 
     * <p>
     * <b>Uso:</b> Útil cuando solo tienes el ID del usuario autenticado y necesitas
     * buscar su carrito sin tener la entidad {@link Cliente} completa.
     * </p>
     *
     * @param idUsuario El ID del usuario asociado al cliente.
     * @return {@link Optional} con el carrito si existe, vacío si no tiene ningún carrito.
     */
    @Query("""
        SELECT c FROM Carrito c
        LEFT JOIN FETCH c.detalleList d
        LEFT JOIN FETCH d.articulo
        WHERE c.cliente.usuario.idUsuario = :idUsuario AND c.finalizado = FALSE
        """)
    Optional<Carrito> findActivoByUsuarioIdConDetalles(@Param("idUsuario") Long idUsuario);

    /**
     * Busca el carrito activo (no finalizado) de un cliente sin cargar los detalles.
     * 
     * <p>
     * <b>Uso:</b> Utilizar cuando solo necesitas verificar si existe un carrito activo
     * sin necesidad de acceder a sus ítems (más eficiente).
     * </p>
     *
     * @param cliente El cliente cuyo carrito se desea buscar.
     * @return {@link Optional} con el carrito si existe, vacío si no tiene carrito activo.
     */
    Optional<Carrito> findByClienteAndFinalizadoFalse(Cliente cliente);

    /**
     * Busca cualquier carrito de un cliente (activo o finalizado) sin cargar detalles.
     * 
     * <p>
     * <b>Advertencia:</b> Puede devolver un carrito ya finalizado. Usar con precaución.
     * </p>
     *
     * @param cliente El cliente cuyo carrito se desea buscar.
     * @return {@link Optional} con el carrito si existe, vacío si no tiene ningún carrito.
     */
    Optional<Carrito> findByCliente(Cliente cliente);

    /**
     * Elimina todos los carritos asociados a un cliente.
     * 
     * <p>
     * <b>Uso:</b> Útil para limpieza de datos cuando se elimina un cliente del sistema.
     * </p>
     *
     * @param cliente El cliente cuyos carritos se desean eliminar.
     */
    void deleteByCliente(Cliente cliente);

}
