package com.rlp.cosechaencope.service;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.rlp.cosechaencope.dto.request.ClienteRequest;
import com.rlp.cosechaencope.dto.request.ProductorRequest;
import com.rlp.cosechaencope.dto.request.UsuarioRequest;
import com.rlp.cosechaencope.dto.response.UsuarioResponse;
import com.rlp.cosechaencope.exception.EmailAlreadyExistsException;
import com.rlp.cosechaencope.exception.ResourceNotFoundException;
import com.rlp.cosechaencope.model.Usuario;
import com.rlp.cosechaencope.repository.UsuarioRepository;

import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;

/**
 * Servicio que encapsula la lógica de negocio para la gestión de usuarios.
 *
 * <p>
 * Permite crear, obtener, listar, actualizar y eliminar usuarios, así como
 * mapear entre entidades JPA y DTOs de petición/respuesta. Incluye validaciones
 * como comprobación de email duplicado y manejo de tipos de usuario.</p>
 *
 * <p>
 * En la creación de usuarios, delega la creación de registros en las tablas
 * correspondientes de Cliente o productor según el tipo de usuario, permitiendo
 * registros con datos mínimos para ambos tipos.</p>
 *
 * @author rafalopezzz
 */
@Slf4j
@Service
public class UsuarioService {

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private ClienteService clienteService;

    @Autowired
    private ProductorService productorService;

    /**
     * Crea un nuevo usuario en la base de datos.
     *
     * <p>
     * Valida que el email no exista previamente, asigna un rol por defecto o
     * usa el proporcionado, codifica la contraseña y crea el registro adicional
     * en Cliente o productor según el tipo de usuario.</p>
     *
     * <p>
     * Para clientes y productores, permite la creación con datos mínimos si no
     * se proporcionan detalles específicos en el DTO.</p>
     *
     * @param uDto DTO de petición con los datos del usuario a crear.
     * @return DTO de respuesta con los datos del usuario recién creado.
     * @throws EmailAlreadyExistsException si el email ya está registrado.
     * @throws IllegalArgumentException si el tipo de usuario no es válido.
     */
    @Transactional
    public UsuarioResponse crearUsuario(UsuarioRequest uDto) {
        // Normalizar el email
        String email = uDto.getEmail().trim().toLowerCase();
        // Comprobar si el email ya existe
        if (usuarioRepository.findByEmail(email).isPresent()) {
            throw new EmailAlreadyExistsException("El email ya está en uso");
        }

        // Validación y asignación del tipo de usuario
        String tipoUsuario = uDto.getTipoUsuario();
        if (tipoUsuario == null) {
            tipoUsuario = "CLIENTE"; // Valor por defecto
        } else if (!tipoUsuario.equals("CLIENTE") && !tipoUsuario.equals("PRODUCTOR")) {
            throw new IllegalArgumentException("Tipo de usuario no válido. Debe ser CLIENTE o PRODUCTOR");
        }
        // Crear y guardar el usuario
        Usuario usuario = new Usuario();
        usuario.setEmail(email);
        usuario.setPassword(passwordEncoder.encode(uDto.getPassword()));

        // Usar el rol proporcionado o USER por defecto
        usuario.setRol(uDto.getRol() != null ? uDto.getRol() : "USER");

        // Importante: usar el tipoUsuario validado, no un valor fijo
        usuario.setTipoUsuario(tipoUsuario);

        Usuario usuarioGuardado = usuarioRepository.save(usuario);

        // Dependiendo del tipo, crea los registros en la tabla correspondiente
        if (tipoUsuario.equals("CLIENTE")) {
            // Para clientes, verificar que existan datos
            if (uDto.getClienteData() != null) {
                clienteService.crearCliente(usuarioGuardado, uDto.getClienteData());
            } else {
                // Crear un cliente con datos mínimos si es necesario
                ClienteRequest clienteMinimo = new ClienteRequest();
                clienteMinimo.setIdUsuario(usuarioGuardado.getIdUsuario());
                clienteService.crearCliente(usuarioGuardado, clienteMinimo);
            }
        } else if (tipoUsuario.equals("PRODUCTOR")) {
            // Para productores, permitir datos mínimos
            if (uDto.getProductorData() != null) {
                productorService.crearProductor(usuarioGuardado, uDto.getProductorData());
            } else {
                // Crear un productor con datos mínimos
                ProductorRequest productorMinimo = new ProductorRequest();
                productorMinimo.setIdUsuario(usuarioGuardado.getIdUsuario());
                productorService.crearProductor(usuarioGuardado, productorMinimo);
            }
        }

        return mapearResponseDTO(usuarioGuardado);
    }

    /**
     * Obtiene un usuario por su ID.
     *
     * @param id ID del usuario a buscar.
     * @return DTO de respuesta con los datos del usuario encontrado.
     * @throws ResourceNotFoundException si el usuario no se encuentra.
     */
    public UsuarioResponse obtenerUsuarioPorId(Long id) {
        Usuario usuario = usuarioRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado"));
        return mapearResponseDTO(usuario);
    }

    /**
     * Obtiene un usuario por su email.
     *
     * @param email Email del usuario a buscar.
     * @return DTO de respuesta con los datos del usuario encontrado.
     * @throws ResourceNotFoundException si el usuario no se encuentra.
     */
    public List<UsuarioResponse> listarUsuarios() {
        return usuarioRepository.findAll()
                .stream()
                .map(this::mapearResponseDTO)
                .collect(Collectors.toList());
    }

    /**
     * Actualiza un usuario existente.
     *
     * @param id ID del usuario a actualizar.
     * @param uDto DTO de petición con los nuevos datos del usuario.
     * @return DTO de respuesta con los datos del usuario actualizado.
     * @throws ResourceNotFoundException si el usuario no se encuentra.
     */
    public UsuarioResponse actualizarUsuario(Long id, UsuarioRequest uDto) {
        Usuario usuario = usuarioRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado"));

        // Actualizar campos (evitando por ejemplo la actualización de la contraseña si así se requiere)
        usuario.setEmail(uDto.getEmail());
        usuario.setPassword(uDto.getPassword());
        uDto.getRol();
        uDto.getTipoUsuario();

        Usuario actualizado = usuarioRepository.save(usuario);
        return mapearResponseDTO(actualizado);
    }

    /**
     * Elimina un usuario por su ID.
     *
     * @param id ID del usuario a eliminar.
     * @throws ResourceNotFoundException si el usuario no se encuentra.
     */
    public void eliminarUsuario(Long id) {
        Usuario usuario = usuarioRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado"));
        usuarioRepository.delete(usuario);
    }

    @PostConstruct
    public void crearSuperAdmin() {
        // Verificar si ya existe el usuario
        if (!usuarioRepository.existsByEmail("ra@cec.com")) {

            // Crear el usuario
            Usuario superAdmin = new Usuario();
            superAdmin.setEmail("ra@cec.com");
            superAdmin.setPassword(passwordEncoder.encode("12345678"));
            superAdmin.setRol("SUPERADMIN");
            superAdmin.setTipoUsuario("PRODUCTOR");

            // Guardar el usuario
            Usuario usuarioGuardado = usuarioRepository.save(superAdmin);
            
            // Crear el registro de productor asociado
            ProductorRequest productorData = new ProductorRequest();
            productorData.setIdUsuario(usuarioGuardado.getIdUsuario());
            productorData.setNombre("Super Admin Productor");
            productorData.setDireccion("Dirección administrativa");
            productorData.setTelefono("000000000");
            productorService.crearProductor(usuarioGuardado, productorData);
            
            log.info("SUPERADMIN creado correctamente con registro de productor asociado");
        } else {
            log.info("SUPERADMIN ya existe en el sistema");
        }
    }

    /**
     * Mapea un objeto {@link Usuario} a un {@link UsuarioResponse}.
     *
     * @param usuario Entidad JPA de usuario
     * @return DTO de respuesta con los datos del usuario.
     */
    private UsuarioResponse mapearResponseDTO(Usuario usuario) {
        UsuarioResponse dto = new UsuarioResponse();
        dto.setIdUsuario(usuario.getIdUsuario());
        dto.setEmail(usuario.getEmail());
        dto.setRol(usuario.getRol());
        dto.setTipoUsuario(usuario.getTipoUsuario());
        return dto;
    }
}
