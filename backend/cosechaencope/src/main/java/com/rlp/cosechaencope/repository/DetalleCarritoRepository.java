package com.rlp.cosechaencope.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.rlp.cosechaencope.model.Carrito;
import com.rlp.cosechaencope.model.DetalleCarrito;

/**
 * Repositorio para la entidad {@link DetalleCarrito}.
 *
 * <p>
 * Proporciona operaciones CRUD básicas heredadas de {@link JpaRepository}, así
 * como consultas personalizadas si es necesario.
 * </p>
 *
 * @author rafalopezzz
 */
@Repository
public interface DetalleCarritoRepository extends JpaRepository<DetalleCarrito, Long> {
    
    /**
     * Lista todos los detalles de un carrito.
     */
    Optional<DetalleCarrito> findByCarrito(Carrito carrito);

    /**
     * Ejemplo de consulta nativa si necesitamos unir con artículo para filtrar por stock.
     */
    @Query(
      value = "SELECT dc.* FROM beerstar_schema.detalles_carrito dc " +
              "JOIN beerstar_schema.articulos a ON dc.id_articulo = a.id_articulo " +
              "WHERE dc.id_carrito = :id AND a.stock > :minStock",
      nativeQuery = true
    )
    List<DetalleCarrito> findByCarritoAndMinStock(
        @Param("id") Long carritoId,
        @Param("minStock") int minStock
    );
}
