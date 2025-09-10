package com.rlp.cosechaencope.service;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.rlp.cosechaencope.dto.request.ArticuloRequest;
import com.rlp.cosechaencope.dto.response.ArticuloResponse;
import com.rlp.cosechaencope.exception.ResourceNotFoundException;
import com.rlp.cosechaencope.model.Articulo;
import com.rlp.cosechaencope.model.Categoria;
import com.rlp.cosechaencope.model.Productor;
import com.rlp.cosechaencope.repository.ArticuloRepository;
import com.rlp.cosechaencope.repository.CategoriaRepository;
import com.rlp.cosechaencope.repository.ProductorRepository;

import lombok.extern.slf4j.Slf4j;

/**
 * Servicio que encapsula la lógica de negocio para la gestión de artículos.
 *
 * <p>
 * Permite crear, obtener, actualizar y eliminar artículos, así como mapear
 * entre entidades JPA y DTOs de petición/respuesta.</p>
 *
 * <p>
 * Utiliza repositorios para acceder a la base de datos y lanza excepciones
 * específicas cuando no se encuentran recursos.</p>
 *
 * @author rafalopezzz
 */
@Slf4j
@Service
public class ArticuloService {

    @Autowired
    private ArticuloRepository articuloRepository;

    @Autowired
    private CategoriaRepository categoriaRepository;

    @Autowired
    private ProductorRepository productorRepository;

    /**
     * Crea un nuevo artículo en la base de datos.
     *
     * @param dto DTO con los datos de petición para crear un artículo.
     * @return DTO de respuesta con los datos del artículo recién creado.
     * @throws ResourceNotFoundException si la categoría indicada no existe.
     */
    public ArticuloResponse crearArticulos(ArticuloRequest dto) {
        log.info("Creando artículo: {}", dto.getNombre());

        // Mapear DTO a entidad
        Articulo articulo = new Articulo();
        articulo.setNombre(dto.getNombre());
        articulo.setDescripcion(dto.getDescripcion());
        articulo.setPrecio(dto.getPrecio());
        articulo.setStock(dto.getStock());
        articulo.setImagenUrl(dto.getImagenUrl());

        // Buscar y asociar categoría
        Categoria categoria = categoriaRepository.findById(dto.getIdCategoria())
                .orElseThrow(() -> {
                    log.error("Categoría no encontrada para el id: {}", dto.getIdCategoria());
                    return new ResourceNotFoundException("Categoría no encontrada");
                });

        articulo.setCategoria(categoria);

        Productor productor = productorRepository.findById(dto.getIdProductor())
                .orElseThrow(() -> {
                    log.error("Productor no encontrado para el id: {}", dto.getIdProductor());
                    return new ResourceNotFoundException("Productor no encontrado");
                });

        articulo.setProductor(productor);

        // Guardar y mapear a DTO de respuesta
        Articulo guardado = articuloRepository.save(articulo);
        log.info("Artículo creado con éxito, id: {}", guardado.getIdArticulo());
        return mapearResponseDTO(guardado);
    }

    /**
     * Obtiene un artículo por su id.
     *
     * @param id ID del artículo a buscar.
     * @return DTO de respuesta con los datos del artículo encontrado.
     * @throws ResourceNotFoundException si no existe un artículo con ese ID.
     */
    public ArticuloResponse obtenerArticuloPorId(Long id) {
        log.info("Obteniendo artículo por id: {}", id);
        Articulo articulo = articuloRepository.findById(id)
                .orElseThrow(() -> {
                    log.error("Artículo no encontrado para el id: {}", id);
                    return new ResourceNotFoundException("Artículo no encontrado");
                });
        return mapearResponseDTO(articulo);
    }

    /**
     * Obtiene todos los artículos almacenados.
     *
     * @return Lista de DTOs de respuesta con todos los artículos.
     */
    public List<ArticuloResponse> obtenerTodosLosArticulos() {
        log.info("Obteniendo todos los artículos");
        List<Articulo> articulos = articuloRepository.findAll();
        log.debug("Cantidad de artículos encontrados: {}", articulos.size());
        return articulos.stream()
                .map(this::mapearResponseDTO)
                .collect(Collectors.toList());
    }

    /**
     * Actualiza un artículo existente.
     *
     * @param idArticulo ID del artículo a actualizar.
     * @param dto DTO con los nuevos datos del artículo.
     * @return DTO de respuesta con los datos actualizados.
     * @throws ResourceNotFoundException si el artículo o la categoría no
     * existen.
     */
    public ArticuloResponse actualizarArticulo(Long idArticulo, ArticuloRequest dto) {
        log.info("Actualizando artículo id: {}", idArticulo);

        // Buscar artículo por ID
        Articulo articulo = articuloRepository.findById(idArticulo)
                .orElseThrow(() -> {
                    log.error("Artículo no encontrado para actualizar, id: {}", idArticulo);
                    return new ResourceNotFoundException("Artículo no encontrado");
                });

        // Actualizar campos del artículo
        articulo.setNombre(dto.getNombre());
        articulo.setDescripcion(dto.getDescripcion());
        articulo.setPrecio(dto.getPrecio());
        articulo.setStock(dto.getStock());
        articulo.setImagenUrl(dto.getImagenUrl());

        // Buscar y asociar categoría
        Categoria categoria = categoriaRepository.findById(dto.getIdCategoria())
                .orElseThrow(() -> new ResourceNotFoundException("Categoría no encontrada"));
        articulo.setCategoria(categoria);

        // Guardar artículo actualizado y mapear a DTO de respuesta
        Articulo actualizado = articuloRepository.save(articulo);
        return mapearResponseDTO(actualizado);
    }

    /**
     * Lista los artículos que pertenecen a una categoría específica.
     *
     * @param idCategoria ID de la categoría para filtrar los artículos.
     * @return Lista de DTOs de respuesta con los artículos de la categoría.
     * @throws ResourceNotFoundException si la categoría no existe.
     */
    public List<ArticuloResponse> listarPorCategoria(Long idCategoria) {
        log.info("Buscando artículos por categoría con id: {}", idCategoria);

        // Validación
        Categoria categoria = categoriaRepository.findById(idCategoria)
                .orElseThrow(() -> {
                    log.error("Categoría no encontrada con id: {}", idCategoria);
                    return new ResourceNotFoundException("Categoría no encontrada");
                });

        // Obtención de artículos por categoría
        List<Articulo> articulos = articuloRepository.findByCategoria(categoria);

        if (articulos.isEmpty()) {
            log.warn("No se encontraron artículos para la categoría id: {}", idCategoria);
        }

        // Mapeamos a DTOs
        return articulos.stream()
                .map(this::mapearResponseDTO)
                .collect(Collectors.toList());
    }

    /**
     * Elimina un artículo por su ID.
     *
     * @param id ID del artículo a eliminar.
     * @throws ResourceNotFoundException si no existe un artículo con ese ID.
     */
    public void eliminarArticulo(Long id) {
        log.info("Eliminando artículo id: {}", id);
        Articulo articulo = articuloRepository.findById(id)
                .orElseThrow(() -> {
                    log.error("Artículo no encontrado para eliminar, id: {}", id);
                    return new ResourceNotFoundException("Artículo no encontrado");
                });
        articuloRepository.delete(articulo);
        log.info("Artículo eliminado, id: {}", id);
    }

    /**
     * Mapea una entidad {@link Articulos} a su correspondiente
     * {@link ArticulosResponseDTO}.
     *
     * @param articulo Entidad JPA de artículo.
     * @return DTO de respuesta con los datos mapeados.
     */
    private ArticuloResponse mapearResponseDTO(Articulo articulo) {
        ArticuloResponse dto = new ArticuloResponse();
        dto.setIdArticulo(articulo.getIdArticulo());
        dto.setNombre(articulo.getNombre());
        dto.setDescripcion(articulo.getDescripcion());
        dto.setPrecio(articulo.getPrecio());
        dto.setStock(articulo.getStock());
        dto.setIdProductor(articulo.getProductor().getIdProductor());
        dto.setNombreProductor(articulo.getProductor().getNombre());
        dto.setImagenUrl(articulo.getImagenUrl());

        if (articulo.getCategoria() != null) {
            dto.setIdCategoria(articulo.getCategoria().getIdCategoria());
            dto.setNombreCategoria(articulo.getCategoria().getNombre());
            if (articulo.getDescripcion() == null) {
                dto.setDescripcion(articulo.getCategoria().getDescripcion());
            }
        }

        if (articulo.getProductor() != null) {
            dto.setIdProductor(articulo.getProductor().getIdProductor());
            dto.setNombreProductor(articulo.getProductor().getNombre());
        }
        return dto;
    }

    /**
     * Obtiene todos los artículos de un productor específico.
     *
     * @param idProductor ID del productor
     * @return Lista de artículos del productor
     * @throws ResourceNotFoundException si el productor no existe
     */
    public List<ArticuloResponse> obtenerArticulosPorProductor(Long idProductor) {
        log.info("Obteniendo artículos para el productor id: {}", idProductor);
        
        // Verificar que el productor existe
        Productor productor = productorRepository.findById(idProductor)
                .orElseThrow(() -> {
                    log.error("Productor no encontrado, id: {}", idProductor);
                    return new ResourceNotFoundException("Productor no encontrado");
                });
        
        List<Articulo> articulos = articuloRepository.findByProductor(productor);
        log.info("Encontrados {} artículos para el productor {}", articulos.size(), idProductor);
        
        return articulos.stream()
                .map(this::mapearResponseDTO)
                .collect(Collectors.toList());
    }

    /**
     * Crea un nuevo artículo para un productor específico.
     *
     * @param idProductor ID del productor
     * @param dto DTO con los datos del artículo
     * @return Artículo creado
     * @throws ResourceNotFoundException si el productor no existe
     */
    public ArticuloResponse crearArticuloPorProductor(Long idProductor, ArticuloRequest dto) {
        log.info("Creando artículo para el productor id: {}", idProductor);
        
        // Verificar que el productor existe
        Productor productor = productorRepository.findById(idProductor)
                .orElseThrow(() -> {
                    log.error("Productor no encontrado, id: {}", idProductor);
                    return new ResourceNotFoundException("Productor no encontrado");
                });
        
        // Buscar la categoría
        Categoria categoria = categoriaRepository.findById(dto.getIdCategoria())
                .orElseThrow(() -> {
                    log.error("Categoría no encontrada, id: {}", dto.getIdCategoria());
                    return new ResourceNotFoundException("Categoría no encontrada");
                });
        
        // Crear el artículo
        Articulo nuevoArticulo = new Articulo();
        nuevoArticulo.setNombre(dto.getNombre());
        nuevoArticulo.setDescripcion(dto.getDescripcion());
        nuevoArticulo.setPrecio(dto.getPrecio());
        nuevoArticulo.setStock(dto.getStock());
        nuevoArticulo.setImagenUrl(dto.getImagenUrl());
        nuevoArticulo.setProductor(productor);
        nuevoArticulo.setCategoria(categoria);
        
        Articulo articuloGuardado = articuloRepository.save(nuevoArticulo);
        log.info("Artículo creado exitosamente con id: {} para el productor: {}", 
                articuloGuardado.getIdArticulo(), idProductor);
        
        return mapearResponseDTO(articuloGuardado);
    }

    /**
     * Actualiza un artículo específico de un productor.
     *
     * @param idProductor ID del productor
     * @param idArticulo ID del artículo
     * @param dto DTO con los datos actualizados
     * @return Artículo actualizado
     * @throws ResourceNotFoundException si el productor o artículo no existe
     */
    public ArticuloResponse actualizarArticuloPorProductor(Long idProductor, Long idArticulo, ArticuloRequest dto) {
        log.info("Actualizando artículo id: {} para el productor id: {}", idArticulo, idProductor);
        
        // Buscar el artículo y verificar que pertenece al productor
        Articulo articulo = articuloRepository.findById(idArticulo)
                .orElseThrow(() -> {
                    log.error("Artículo no encontrado, id: {}", idArticulo);
                    return new ResourceNotFoundException("Artículo no encontrado");
                });
        
        // Verificar que el artículo pertenece al productor
        if (!articulo.getProductor().getIdProductor().equals(idProductor)) {
            log.error("El artículo {} no pertenece al productor {}", idArticulo, idProductor);
            throw new ResourceNotFoundException("El artículo no pertenece a este productor");
        }
        
        // Buscar la categoría si se proporciona
        if (dto.getIdCategoria() != null) {
            Categoria categoria = categoriaRepository.findById(dto.getIdCategoria())
                    .orElseThrow(() -> {
                        log.error("Categoría no encontrada, id: {}", dto.getIdCategoria());
                        return new ResourceNotFoundException("Categoría no encontrada");
                    });
            articulo.setCategoria(categoria);
        }
        
        // Actualizar campos
        if (dto.getNombre() != null) {
            articulo.setNombre(dto.getNombre());
        }
        if (dto.getDescripcion() != null) {
            articulo.setDescripcion(dto.getDescripcion());
        }
        if (dto.getPrecio() != null) {
            articulo.setPrecio(dto.getPrecio());
        }
        if (dto.getStock() != null) {
            articulo.setStock(dto.getStock());
        }
        if (dto.getImagenUrl() != null) {
            articulo.setImagenUrl(dto.getImagenUrl());
        }
        
        Articulo articuloActualizado = articuloRepository.save(articulo);
        log.info("Artículo actualizado exitosamente, id: {}", idArticulo);
        
        return mapearResponseDTO(articuloActualizado);
    }

    /**
     * Elimina un artículo específico de un productor.
     *
     * @param idProductor ID del productor
     * @param idArticulo ID del artículo
     * @throws ResourceNotFoundException si el productor o artículo no existe
     */
    public void eliminarArticuloPorProductor(Long idProductor, Long idArticulo) {
        log.info("Eliminando artículo id: {} del productor id: {}", idArticulo, idProductor);
        
        // Buscar el artículo y verificar que pertenece al productor
        Articulo articulo = articuloRepository.findById(idArticulo)
                .orElseThrow(() -> {
                    log.error("Artículo no encontrado, id: {}", idArticulo);
                    return new ResourceNotFoundException("Artículo no encontrado");
                });
        
        // Verificar que el artículo pertenece al productor
        if (!articulo.getProductor().getIdProductor().equals(idProductor)) {
            log.error("El artículo {} no pertenece al productor {}", idArticulo, idProductor);
            throw new ResourceNotFoundException("El artículo no pertenece a este productor");
        }
        
        articuloRepository.delete(articulo);
        log.info("Artículo eliminado exitosamente, id: {}", idArticulo);
    }

}
