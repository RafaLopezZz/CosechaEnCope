package com.rlp.cosechaencope.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.rlp.cosechaencope.dto.request.ProductorRequest;
import com.rlp.cosechaencope.dto.response.ProductorResponse;
import com.rlp.cosechaencope.dto.response.UsuarioResponse;
import com.rlp.cosechaencope.exception.ResourceNotFoundException;
import com.rlp.cosechaencope.model.Productor;
import com.rlp.cosechaencope.model.Usuario;
import com.rlp.cosechaencope.repository.ProductorRepository;


/**
 * Servicio para la gestión de productores.
 * 
 * <p>Proporciona operaciones como crear, obtener, actualizar, eliminar y listar
 * productores. Además, mapea entidades {@link Productor} a DTOs y viceversa.</p>
 * 
 * Este servicio se utiliza principalmente en conjunto con {@link UsuarioService}
 * cuando se registran nuevos usuarios del tipo productor.
 * 
 * @author rafalopezzz
 */
@Service
public class ProductorService {

    @Autowired
    private ProductorRepository productorRepository;



    /**
     * Crea y guarda un nuevo productor en base a los datos proporcionados.
     * 
     * @param usuario El usuario asociado al productor.
     * @param pDto DTO con los datos del productor.
     * @return DTO de respuesta con los datos del productor creado.
     */
    public void crearProductor(Usuario usuario, ProductorRequest pDto) {
        if (pDto == null) {
            pDto = new ProductorRequest(); // Inicializar si es nulo para evitar errores
        }

        Productor productor = new Productor();
        productor.setUsuario(usuario);
        productor.setNombre(pDto.getNombre());
        productor.setDireccion(pDto.getDireccion());
        productor.setTelefono(pDto.getTelefono());
        productor.setFechaRegistro(LocalDateTime.now());
        productor.setImagenUrl(pDto.getImagenUrl());

        productorRepository.save(productor);
    }

    /**
     * Busca un productor por el ID del usuario asociado.
     * 
     * @param idUsuario ID del usuario relacionado.
     * @return DTO con los datos del productor.
     * @throws ResourceNotFoundException si no se encuentra el productor.
     */
    public ProductorResponse obtenerProductorPorUsuarioId(Long idUsuario) {
        Productor productor = productorRepository.findByUsuario_IdUsuario(idUsuario)
                .orElseThrow(() -> new ResourceNotFoundException("Productor no encontrado para el usuario con ID: " + idUsuario));
        
        return mapearResponseDTO(productor);
    }

    /**
     * Lista todos los productores registrados en el sistema.
     * 
     * @return Lista de DTOs con los datos de todos los productores.
     */
    public List<ProductorResponse> listarProductores() {
        return productorRepository.findAll()
                .stream()
                .map(this::mapearResponseDTO)
                .collect(Collectors.toList());
    }

    /**
     * Actualiza los datos de un productor existente.
     * 
     * @param id ID del productor a actualizar.
     * @param dto DTO con los nuevos datos del productor.
     * @return DTO actualizado del productor.
     * @throws RuntimeException si el productor no existe.
     */
    public ProductorResponse actualizarProductor(Long idUsuario, ProductorRequest dto) {
        Productor productor = productorRepository.findByUsuario_IdUsuario(idUsuario)
                .orElseThrow(() -> new ResourceNotFoundException("Productor no encontrado"));

        productor.setNombre(dto.getNombre());
        productor.setDireccion(dto.getDireccion());
        productor.setTelefono(dto.getTelefono());
        productor.setImagenUrl(dto.getImagenUrl());
        
        Productor actualizado = productorRepository.save(productor);
        return mapearResponseDTO(actualizado);
    }

    /**
     * Elimina un productor por su ID.
     * 
     * @param id ID del productor a eliminar.
     * @throws RuntimeException si el productor no existe.
     */
    public void eliminarProductor(Long idUsuario) {
        Productor productor = productorRepository.findByUsuario_IdUsuario(idUsuario)
                .orElseThrow(() -> new ResourceNotFoundException("productor no encontrado"));
        productorRepository.delete(productor);
    }
    
    /**
     * Convierte una entidad {@link Productor} a un {@link ProductorResponse}.
     * 
     * @param productor Entidad a convertir.
     * @return DTO de respuesta con los datos del productor.
     */
    private ProductorResponse mapearResponseDTO(Productor productor) {
        ProductorResponse dto = new ProductorResponse();
        dto.setIdProductor(productor.getIdProductor());
        dto.setNombre(productor.getNombre());
        dto.setDireccion(productor.getDireccion());
        dto.setTelefono(productor.getTelefono());
        dto.setFechaRegistro(productor.getFechaRegistro());
        dto.setImagenUrl(productor.getImagenUrl());
        
        Usuario usuario = productor.getUsuario();
        if (usuario != null) {
            UsuarioResponse usuarioDTO = new UsuarioResponse();
            usuarioDTO.setIdUsuario(usuario.getIdUsuario());
            usuarioDTO.setEmail(usuario.getEmail());
            usuarioDTO.setRol(usuario.getRol());
            usuarioDTO.setTipoUsuario(usuario.getTipoUsuario());
            dto.setUsuario(usuarioDTO);
        }
        return dto;
    }
}
