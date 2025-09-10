package com.rlp.cosechaencope.service;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.rlp.cosechaencope.dto.request.CategoriaRequest;
import com.rlp.cosechaencope.dto.response.CategoriaResponse;
import com.rlp.cosechaencope.exception.CategoryNameAlreadyExistsException;
import com.rlp.cosechaencope.exception.ResourceNotFoundException;
import com.rlp.cosechaencope.model.Categoria;
import com.rlp.cosechaencope.repository.CategoriaRepository;

import lombok.extern.slf4j.Slf4j;

/**
 * Servicio para gestionar las operaciones relacionadas con las categorías.
 * <p>
 * Incluye operaciones de creación, obtención, listado, actualización y
 * eliminación de categorías. Utiliza {@link CategoriaRepository} como capa de
 * acceso a datos.
 * </p>
 * 
 * <p>
 * Incluye validación para evitar nombres duplicados de categorías,
 * manteniendo la unicidad en el sistema.
 * </p>
 * 
 * @author rafalopezzz
 */
@Slf4j
@Service
public class CategoriaService {

    @Autowired
    private CategoriaRepository categoriasRepository;

    /**
     * Crea una nueva categoría en el sistema.
     *
     * @param dto DTO con los datos de la categoría a crear.
     * @return DTO de respuesta con los datos de la categoría creada.
     * @throws CategoryNameAlreadyExistsException si ya existe una categoría con el mismo nombre.
     */
    public CategoriaResponse crearCategoria(CategoriaRequest dto) {
        log.info("Creando categoría: {}", dto.getNombre());
        
        // Validar que no exista una categoría con el mismo nombre
        validarNombreUnico(dto.getNombre());
        
        Categoria categoria = new Categoria();
        categoria.setNombre(dto.getNombre());
        categoria.setDescripcion(dto.getDescripcion());
        Categoria guardado = categoriasRepository.save(categoria);
        log.info("Categoría creada, id: {}", guardado.getIdCategoria());
        return mapearResponseDTO(guardado);
    }

    /**
     * Obtiene una categoría por su ID.
     *
     * @param id ID de la categoría a buscar.
     * @return DTO de respuesta con los datos de la categoría encontrada.
     * @throws ResourceNotFoundException si no se encuentra la categoría.
     */
    public CategoriaResponse obtenerCategoriaPorId(Long id) {
        log.info("Obteniendo categoría por id: {}", id);
        Categoria categoria = categoriasRepository.findById(id)
                .orElseThrow(() -> {
                    log.error("Categoría no encontrada para el id: {}", id);
                    return new ResourceNotFoundException("Categoría no encontrada");
                });
        return mapearResponseDTO(categoria);
    }

    /**
     * Obtiene todas las categorías del sistema.
     *
     * @return Lista de DTOs de respuesta con los datos de todas las categorías.
     */
    public List<CategoriaResponse> obtenerTodasLasCategorias() {
        log.info("Obteniendo todas las categorías");
        List<Categoria> categoria = categoriasRepository.findAll();
        log.debug("Cantidad de categorías encontradas: {}", categoria.size());
        return categoria.stream()
                .map(this::mapearResponseDTO)
                .collect(Collectors.toList());
    }

    /**
     * Actualiza una categoría existente.
     *
     * @param id ID de la categoría a actualizar.
     * @param dto DTO con los nuevos datos de la categoría.
     * @return DTO de respuesta con los datos de la categoría actualizada.
     * @throws ResourceNotFoundException si no se encuentra la categoría.
     * @throws CategoryNameAlreadyExistsException si ya existe otra categoría con el mismo nombre.
     */
    public CategoriaResponse actualizarCategoria(Long id, CategoriaRequest dto) {
        log.info("Actualizando categoría id: {}", id);
        Categoria categoria = categoriasRepository.findById(id)
                .orElseThrow(() -> {
                    log.error("Categoría no encontrada para actualizar, id: {}", id);
                    return new ResourceNotFoundException("Categoría no encontrada");
                });
        
        // Validar que no exista otra categoría con el mismo nombre (excluyendo la actual)
        validarNombreUnicoParaActualizacion(dto.getNombre(), id);
        
        categoria.setNombre(dto.getNombre());
        categoria.setDescripcion(dto.getDescripcion());
        categoria.setImagenUrl(dto.getImagenUrl());

        Categoria actualizada = categoriasRepository.save(categoria);
        log.info("Categoría actualizada, id: {}", actualizada.getIdCategoria());
        return mapearResponseDTO(actualizada);
    }

    /**
     * Elimina una categoría por su ID.
     *
     * @param id ID de la categoría a eliminar.
     * @throws ResourceNotFoundException si no se encuentra la categoría.
     */
    public void eliminarCategoria(Long id) {
        log.info("Eliminando categoría id: {}", id);
        Categoria categoria = categoriasRepository.findById(id)
                .orElseThrow(() -> {
                    log.error("Categoría no encontrada para eliminar, id: {}", id);
                    return new ResourceNotFoundException("Categoría no encontrada");
                });
        categoriasRepository.delete(categoria);
        log.info("Categoría eliminada, id: {}", id);
    }

    /**
     * Valida que no exista una categoría con el nombre especificado.
     * 
     * @param nombre Nombre de la categoría a validar.
     * @throws CategoryNameAlreadyExistsException si ya existe una categoría con el mismo nombre.
     */
    private void validarNombreUnico(String nombre) {
        if (nombre == null || nombre.trim().isEmpty()) {
            throw new IllegalArgumentException("El nombre de la categoría no puede estar vacío");
        }
        
        if (categoriasRepository.existsByNombreIgnoreCase(nombre.trim())) {
            log.warn("Intento de crear categoría con nombre duplicado: {}", nombre);
            throw new CategoryNameAlreadyExistsException(
                "Ya existe una categoría con el nombre: " + nombre.trim());
        }
    }

    /**
     * Valida que no exista otra categoría con el nombre especificado,
     * excluyendo la categoría con el ID dado (útil para actualizaciones).
     * 
     * @param nombre Nombre de la categoría a validar.
     * @param idCategoria ID de la categoría a excluir de la validación.
     * @throws CategoryNameAlreadyExistsException si ya existe otra categoría con el mismo nombre.
     */
    private void validarNombreUnicoParaActualizacion(String nombre, Long idCategoria) {
        if (nombre == null || nombre.trim().isEmpty()) {
            throw new IllegalArgumentException("El nombre de la categoría no puede estar vacío");
        }
        
        if (categoriasRepository.existsByNombreIgnoreCaseAndIdCategoriaNotEqual(nombre.trim(), idCategoria)) {
            log.warn("Intento de actualizar categoría con nombre duplicado: {}", nombre);
            throw new CategoryNameAlreadyExistsException(
                "Ya existe otra categoría con el nombre: " + nombre.trim());
        }
    }

    /**
     * Convierte una entidad {@link Categoria} en un DTO de respuesta.
     *
     * @param categoria La entidad a convertir.
     * @return DTO de respuesta con los datos de la categoría.
     */
    private CategoriaResponse mapearResponseDTO(Categoria categoria) {
        CategoriaResponse dto = new CategoriaResponse();
        dto.setIdCategoria(categoria.getIdCategoria());
        dto.setNombre(categoria.getNombre());
        dto.setDescripcion(categoria.getDescripcion());
        dto.setImagenUrl(categoria.getImagenUrl());
        return dto;
    }
}
