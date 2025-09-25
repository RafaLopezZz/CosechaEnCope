package com.rlp.cosechaencope.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.rlp.cosechaencope.dto.request.UsuarioRequest;
import com.rlp.cosechaencope.dto.response.MessageResponse;
import com.rlp.cosechaencope.dto.response.UsuarioResponse;
import com.rlp.cosechaencope.service.UsuarioService;

import io.swagger.v3.oas.annotations.Hidden;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

/**
 * Controlador REST para la gestión administrativa de usuarios en Cosecha-en-Cope.
 *
 * <p>Proporciona endpoints para operaciones CRUD de usuarios, accesibles solo para administradores.
 * Utiliza {@link UsuarioService} para delegar la lógica de negocio.</p>
 *
 * <p>Características principales:</p>
 * <ul>
 *   <li>Habilita CORS para todos los orígenes</li>
 *   <li>Requiere autenticación para todas las operaciones</li>
 *   <li>Algunos endpoints requieren rol ADMIN (especificado en cada método)</li>
 * </ul>
 *
 * <p>Endpoints disponibles:</p>
 * <ul>
 *   <li>POST   /cosechaencope/usuario/admin        → Registrar nuevo usuario (requiere rol ADMIN)</li>
 *   <li>GET    /cosechaencope/usuario/{idUsuario}  → Obtener usuario por ID</li>
 *   <li>GET    /cosechaencope/usuario              → Listar todos los usuarios</li>
 *   <li>PUT    /cosechaencope/usuario/{idUsuario}  → Actualizar usuario existente</li>
 *   <li>DELETE /cosechaencope/usuario/{idUsuario}  → Eliminar usuario por ID</li>
 * </ul>
 * 
 * @author rafalopezzz
 */
@Hidden
@CrossOrigin(origins = "*")
@RestController
@RequestMapping("/cosechaencope/usuarios")
@Tag(name = "Usuario", description = "Operaciones administrativas para la gestión de usuarios")
public class UsuarioController {

    @Autowired
    private UsuarioService usuarioService;

    /**
     * Registra un nuevo usuario en el sistema.
     *
     * @param usuarioRequest DTO con los datos del usuario a registrar. Debe pasar las validaciones de {@link Valid}.
     * @return {@code ResponseEntity<UsuarioResponseDTO>} con los datos del usuario creado y HTTP 200.
     */
    @Operation(
            summary = "Registrar nuevo usuario",
            description = "Crea un nuevo usuario en el sistema con los datos proporcionados"
    )
    @ApiResponses({
        @ApiResponse(
                responseCode = "200",
                description = "Usuario registrado exitosamente",
                content = @Content(
                        mediaType = "application/json",
                        schema = @Schema(implementation = UsuarioResponse.class)
                )
        ),
        @ApiResponse(
                responseCode = "400",
                description = "Datos de entrada inválidos",
                content = @Content(
                        mediaType = "application/json",
                        schema = @Schema(implementation = MessageResponse.class)
                )
        ),
        @ApiResponse(
                responseCode = "409",
                description = "Email ya existe en el sistema",
                content = @Content(
                        mediaType = "application/json",
                        schema = @Schema(implementation = MessageResponse.class)
                )
        )
    })
    @PostMapping("/registro")
    public ResponseEntity<UsuarioResponse> registrarUsuario(@Valid @RequestBody UsuarioRequest usuarioRequest) {
        UsuarioResponse response = usuarioService.crearUsuario(usuarioRequest);
        return ResponseEntity.ok(response);
    }

    /**
     * Obtiene un usuario por su ID.
     *
     * @param idUsuario ID del usuario a obtener.
     * @return {@code ResponseEntity<UsuarioResponse>} con los datos del usuario encontrado y HTTP 200.
     */
    @Operation(
            summary = "Obtener usuario por ID",
            description = "Busca y devuelve los datos de un usuario específico por su ID"
    )
    @ApiResponses({
        @ApiResponse(
                responseCode = "200",
                description = "Usuario encontrado exitosamente",
                content = @Content(
                        mediaType = "application/json",
                        schema = @Schema(implementation = UsuarioResponse.class)
                )
        ),
        @ApiResponse(
                responseCode = "404",
                description = "Usuario no encontrado",
                content = @Content(
                        mediaType = "application/json",
                        schema = @Schema(implementation = MessageResponse.class)
                )
        ),
        @ApiResponse(
                responseCode = "401",
                description = "Usuario no autenticado"
        ),
        @ApiResponse(
                responseCode = "403",
                description = "Sin permisos para acceder a este recurso"
        )
    })
    @GetMapping("/{idUsuario}")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPERADMIN')")
    public ResponseEntity<UsuarioResponse> obtenerUsuario(@PathVariable("idUsuario") Long idUsuario) {
        UsuarioResponse response = usuarioService.obtenerUsuarioPorId(idUsuario);
        return ResponseEntity.ok(response);
    }

    /**
     * Lista todos los usuarios registrados en el sistema.
     *
     * @return {@code ResponseEntity<List<UsuarioResponse>>} con la lista de usuarios y HTTP 200.
     */
    @Operation(
            summary = "Listar todos los usuarios",
            description = "Obtiene una lista completa de todos los usuarios registrados en el sistema"
    )
    @ApiResponses({
        @ApiResponse(
                responseCode = "200",
                description = "Lista de usuarios obtenida exitosamente",
                content = @Content(
                        mediaType = "application/json",
                        array = @ArraySchema(schema = @Schema(implementation = UsuarioResponse.class))
                )
        ),
        @ApiResponse(
                responseCode = "204",
                description = "No hay usuarios registrados en el sistema"
        ),
        @ApiResponse(
                responseCode = "401",
                description = "Usuario no autenticado"
        ),
        @ApiResponse(
                responseCode = "403",
                description = "Sin permisos para acceder a este recurso"
        )
    })
    @GetMapping
    public ResponseEntity<List<UsuarioResponse>> listarUsuarios() {
        List<UsuarioResponse> lista = usuarioService.listarUsuarios();
        return ResponseEntity.ok(lista);
    }


    /**
     * Actualiza un usuario existente.
     *
     * @param idUsuario ID del usuario a actualizar.
     * @param usuarioRequest DTO con los nuevos datos del usuario. Debe pasar las validaciones de {@link Valid}.
     * @return {@code ResponseEntity<UsuarioResponse>} con los datos del usuario actualizado y HTTP 200.
     */
    @Operation(
            summary = "Actualizar usuario existente",
            description = "Modifica los datos de un usuario existente con la información proporcionada"
    )
    @ApiResponses({
        @ApiResponse(
                responseCode = "200",
                description = "Usuario actualizado exitosamente",
                content = @Content(
                        mediaType = "application/json",
                        schema = @Schema(implementation = UsuarioResponse.class)
                )
        ),
        @ApiResponse(
                responseCode = "404",
                description = "Usuario no encontrado",
                content = @Content(
                        mediaType = "application/json",
                        schema = @Schema(implementation = MessageResponse.class)
                )
        ),
        @ApiResponse(
                responseCode = "400",
                description = "Datos de entrada inválidos",
                content = @Content(
                        mediaType = "application/json",
                        schema = @Schema(implementation = MessageResponse.class)
                )
        ),
        @ApiResponse(
                responseCode = "401",
                description = "Usuario no autenticado"
        ),
        @ApiResponse(
                responseCode = "403",
                description = "Sin permisos para modificar este recurso"
        )
    })
    @PutMapping("/{idUsuario}")
    public ResponseEntity<UsuarioResponse> actualizarUsuario(@PathVariable("idUsuario") Long idUsuario,
                                                                  @Valid @RequestBody UsuarioRequest usuarioRequest) {
        UsuarioResponse response = usuarioService.actualizarUsuario(idUsuario, usuarioRequest);
        return ResponseEntity.ok(response);
    }

    /**
     * Elimina un usuario por su ID.
     *
     * @param idUsuario ID del usuario a eliminar.
     * @return {@code ResponseEntity<String>} con un mensaje de éxito y HTTP 200.
     */
    @Operation(
            summary = "Eliminar usuario",
            description = "Elimina permanentemente un usuario del sistema por su ID"
    )
    @ApiResponses({
        @ApiResponse(
                responseCode = "200",
                description = "Usuario eliminado exitosamente",
                content = @Content(
                        mediaType = "text/plain",
                        schema = @Schema(type = "string", example = "Usuario eliminado correctamente")
                )
        ),
        @ApiResponse(
                responseCode = "404",
                description = "Usuario no encontrado",
                content = @Content(
                        mediaType = "application/json",
                        schema = @Schema(implementation = MessageResponse.class)
                )
        ),
        @ApiResponse(
                responseCode = "401",
                description = "Usuario no autenticado"
        ),
        @ApiResponse(
                responseCode = "403",
                description = "Sin permisos para eliminar este usuario"
        )
    })
    @DeleteMapping("/{idUsuario}")
    @PreAuthorize("hasRole('ADMIN','SUPERADMIN')")
    public ResponseEntity<String> eliminarUsuario(@PathVariable("idUsuario") Long idUsuario) {
        usuarioService.eliminarUsuario(idUsuario);
        return ResponseEntity.ok("Usuario eliminado correctamente");
    }
}
