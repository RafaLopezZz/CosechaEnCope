package com.rlp.cosechaencope.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.rlp.cosechaencope.model.Categoria;

/**
 * Repositorio para la entidad {@link Categoria}.
 *
 * <p>
 * Proporciona métodos para buscar categorías por nombre y descripción.
 * </p>
 *
 * @author rafalopezzz
 */
@Repository
public interface CategoriaRepository extends JpaRepository<Categoria, Long> {

    /**
     * Busca categorías por su nombre.
     * 
     * @param nombre Nombre de la categoría a buscar.
     * @return Lista de categorías que coinciden con el nombre proporcionado.
     */
    List<Categoria> findByNombre(String nombre);

    /**
     * Busca una categoría por su nombre exacto.
     * 
     * @param nombre Nombre de la categoría a buscar.
     * @return Optional con la categoría si existe, vacío si no existe.
     */
    Optional<Categoria> findByNombreIgnoreCase(String nombre);

    /**
     * Verifica si existe una categoría con el nombre especificado, excluyendo el ID dado.
     * Útil para validaciones durante actualizaciones.
     * 
     * @param nombre Nombre de la categoría a verificar.
     * @param id ID de la categoría a excluir de la búsqueda.
     * @return true si existe otra categoría con el mismo nombre, false en caso contrario.
     */
    @Query("SELECT CASE WHEN COUNT(c) > 0 THEN true ELSE false END FROM Categoria c WHERE LOWER(c.nombre) = LOWER(:nombre) AND c.idCategoria != :id")
    boolean existsByNombreIgnoreCaseAndIdCategoriaNotEqual(@Param("nombre") String nombre, @Param("id") Long id);

    /**
     * Verifica si existe una categoría con el nombre especificado.
     * 
     * @param nombre Nombre de la categoría a verificar.
     * @return true si existe una categoría con el nombre, false en caso contrario.
     */
    boolean existsByNombreIgnoreCase(String nombre);

    /**
     * Busca categorías por su descripción.
     * 
     * @param descripcion Descripción de la categoría a buscar.
     * @return Lista de categorías que coinciden con la descripción proporcionada.
     */
    List<Categoria> findByDescripcionContaining(String descripcion);

}
