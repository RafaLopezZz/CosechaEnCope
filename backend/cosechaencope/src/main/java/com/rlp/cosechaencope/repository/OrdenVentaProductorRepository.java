package com.rlp.cosechaencope.repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.rlp.cosechaencope.model.OrdenVentaProductor;
import com.rlp.cosechaencope.model.Pedido;

/**
 * Repositorio de acceso a datos para la entidad OrdenVentaProductor.
 *
 * Permite realizar operaciones CRUD y consultas personalizadas sobre las
 * órdenes que se generan hacia productores a partir de pedidos de clientes.
 */
public interface OrdenVentaProductorRepository extends JpaRepository<OrdenVentaProductor, Long> {

    /**
     * Busca órdenes de venta por productor.
     */
    List<OrdenVentaProductor> findByProductor_IdProductorOrderByFechaCreacionDesc(Long idProductor);

    /**
     * Busca órdenes de venta por pedido.
     */
    List<OrdenVentaProductor> findByPedido_IdPedido(Long idPedido);

    /**
     * Busca todas las órdenes de venta relacionadas con un pedido específico.
     * Se basa en la relación entre los detalles del pedido y los productores.
     */
    @Query("SELECT DISTINCT ovp FROM OrdenVentaProductor ovp "
            + "JOIN DetalleOvp dovp ON dovp.ordenVenta = ovp "
            + "JOIN dovp.articulo a "
            + "JOIN DetallePedido dp ON dp.articulo = a "
            + "WHERE dp.pedido = :pedido")
    List<OrdenVentaProductor> findByPedido(@Param("pedido") Pedido pedido);

    /**
     * Busca órdenes por estado.
     */
    List<OrdenVentaProductor> findByEstadoOrderByFechaCreacionDesc(String estado);

    /**
     * Busca por número de orden.
     */
    Optional<OrdenVentaProductor> findByNumeroOrden(String numeroOrden);

    /**
     * Cuenta órdenes por fecha para generar número único.
     */
    long countByFechaCreacionBetween(LocalDateTime inicio, LocalDateTime fin);
}
