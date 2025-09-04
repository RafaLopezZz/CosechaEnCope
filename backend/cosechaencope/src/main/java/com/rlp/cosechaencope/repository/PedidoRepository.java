package com.rlp.cosechaencope.repository;

import java.util.List;
import java.util.Optional;
import java.time.Instant;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.rlp.cosechaencope.model.Cliente;
import com.rlp.cosechaencope.model.Pedido;

/**
 * Repositorio para la entidad {@link Pedido}.
 *
 * <p>
 * Proporciona operaciones CRUD básicas heredadas de {@link JpaRepository}, así
 * como consultas personalizadas para buscar pedidos según diferentes criterios.
 * </p>
 *
 * @author rafalopezzz
 */
@Repository
public interface PedidoRepository extends JpaRepository<Pedido, Long> {

    /**
     * Lista todos los pedidos de un cliente.
     */
    List<Pedido> findByCliente(Cliente cliente);

    /**
     * Lista paginada de pedidos de un cliente, útil para historiales muy largos.
     */
    Page<Pedido> findByCliente(Cliente cliente, Pageable pageable);

    Optional<Pedido> findByCliente_Usuario_IdUsuario(Long idUsuario);

    Long countByFechaPedidoAfter(Instant fecha);

}
