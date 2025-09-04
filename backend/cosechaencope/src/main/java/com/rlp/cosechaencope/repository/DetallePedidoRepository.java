package com.rlp.cosechaencope.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.rlp.cosechaencope.model.DetallePedido;

/**
 * Repositorio para la entidad {@link DetallePedido}.
 *
 * <p>
 * Proporciona operaciones CRUD básicas heredadas de {@link JpaRepository}, así
 * como consultas personalizadas para buscar detalles de pedidos por ID de pedido
 * o ID de artículo.
 * </p>
 *
 * @author rafalopezzz
 */
@Repository
public interface DetallePedidoRepository extends JpaRepository<DetallePedido, Long> {

    /**
     * Busca todos los detalles de un pedido por su ID.
     *
     * @param idPedido ID del pedido cuyos detalles se desean obtener.
     * @return Lista de {@link DetallePedido} asociados al pedido especificado.
     */
    List<DetallePedido> findByPedidoIdPedido(Long idPedido);
    
    /**
     * Busca todos los detalles de un pedido que contienen un artículo específico.
     *
     * @param idArticulo ID del artículo cuyos detalles se desean obtener.
     * @return Lista de {@link DetallePedido} que contienen el artículo especificado.
     */
    List<DetallePedido> findByArticuloIdArticulo(Long idArticulo);

}
