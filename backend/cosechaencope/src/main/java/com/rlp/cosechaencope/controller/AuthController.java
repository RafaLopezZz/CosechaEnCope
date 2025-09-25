package com.rlp.cosechaencope.controller;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.rlp.cosechaencope.dto.request.LoginRequest;
import com.rlp.cosechaencope.dto.request.UsuarioRequest;
import com.rlp.cosechaencope.dto.response.JwtResponse;
import com.rlp.cosechaencope.dto.response.MessageResponse;
import com.rlp.cosechaencope.dto.response.UsuarioResponse;
import com.rlp.cosechaencope.exception.EmailAlreadyExistsException;
import com.rlp.cosechaencope.security.JwtUtils;
import com.rlp.cosechaencope.security.UserDetailsImpl;
import com.rlp.cosechaencope.service.UsuarioService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

/**
 * Controlador de autenticación para usuarios públicos (clientes y productores).
 * <p>
 * Este controlador maneja los endpoints de acceso público que no requieren
 * autenticación previa: inicio de sesión y registro diferenciado por tipo de
 * usuario. Implementa JWT para la autenticación de usuarios.
 * </p>
 *
 * <p>
 * Endpoints disponibles:</p>
 * <ul>
 * <li>POST /cosechaencope/auth/login               → Autenticación y generación de token JWT</li>
 * <li>POST /cosechaencope/auth/registro/cliente    → Registro exclusivo para clientes</li>
 * <li>POST /cosechaencope/auth/registro/productor  → Registro exclusivo para productores</li>
 * </ul>
 *
 * @author rafalopezzz
 */
@CrossOrigin(origins = "*")
@RestController
@RequestMapping("/cosechaencope/auth")
@Tag(name = "Autenticación", description = "Endpoints públicos para autenticación y registro de usuarios")
public class AuthController {

    @Autowired
    AuthenticationManager authenticationManager;

    @Autowired
    private UsuarioService usuarioService;

    @Autowired
    JwtUtils jwtUtils;

    /**
     * Autentica un usuario y genera un token JWT.
     * <p>
     * Proceso:
     * <ol>
     * <li>Valida las credenciales del usuario contra la base de datos</li>
     * <li>Genera un token JWT si las credenciales son válidas</li>
     * <li>Devuelve el token junto con información básica del usuario</li>
     * </ol>
     * </p>
     *
     * @param loginRequest DTO con email y password del usuario
     * @return ResponseEntity con JWT e información del usuario autenticado
     */
    @Operation(
            summary = "Iniciar sesión",
            description = "Autentica las credenciales del usuario y devuelve un token JWT válido para acceder a endpoints protegidos"
    )
    @ApiResponses(value = {
        @ApiResponse(
                responseCode = "200",
                description = "Autenticación exitosa",
                content = @Content(
                        mediaType = "application/json",
                        schema = @Schema(implementation = JwtResponse.class)
                )
        ),
        @ApiResponse(
                responseCode = "401",
                description = "Credenciales inválidas",
                content = @Content(
                        mediaType = "application/json",
                        schema = @Schema(implementation = MessageResponse.class)
                )
        ),
        @ApiResponse(
                responseCode = "400",
                description = "Datos de entrada inválidos"
        )
    })
    @PostMapping("/login")
    public ResponseEntity<?> authenticateUser(@Valid @RequestBody LoginRequest loginRequest) {
        // Autenticar credenciales del usuario
        Authentication authentication = authenticationManager
                .authenticate(new UsernamePasswordAuthenticationToken(loginRequest.getEmail(), loginRequest.getPassword()));

        // Establecer autenticación en el contexto de seguridad
        SecurityContextHolder.getContext().setAuthentication(authentication);
        // Generar token JWT
        String jwt = jwtUtils.generateJwtToken(authentication);

        // Obtener detalles del usuario autenticado
        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
        List<String> roles = userDetails.getAuthorities().stream()
                .map(item -> item.getAuthority())
                .collect(Collectors.toList());

        // Devolver respuesta con el token y detalles del usuario
        return ResponseEntity.ok(new JwtResponse(jwt,
                userDetails.getId(),
                userDetails.getEmail(),
                userDetails.getTipoUsuario(),
                roles));
    }

    @Operation(
            summary = "Iniciar sesión como productor",
            description = "Autentica específicamente a usuarios de tipo productor y devuelve un token JWT"
    )
    @ApiResponses(value = {
        @ApiResponse(
                responseCode = "200",
                description = "Autenticación exitosa para productor",
                content = @Content(
                        mediaType = "application/json",
                        schema = @Schema(implementation = JwtResponse.class)
                )
        ),
        @ApiResponse(
                responseCode = "401",
                description = "Credenciales inválidas"
        ),
        @ApiResponse(
                responseCode = "403",
                description = "Usuario no es de tipo productor",
                content = @Content(
                        mediaType = "text/plain",
                        schema = @Schema(type = "string", example = "Acceso sólo permitido a usuarios productor")
                )
        )
    })
    @PostMapping("/login/productores")
    public ResponseEntity<?> authenticateproductor(@Valid @RequestBody LoginRequest loginRequest) {

        // Autenticar usuario
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        loginRequest.getEmail(),
                        loginRequest.getPassword()
                )
        );
        SecurityContextHolder.getContext().setAuthentication(authentication);

        // Obtener detalles
        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();

        // Comprobar tipo de usuario
        if (!"PRODUCTOR".equalsIgnoreCase(userDetails.getTipoUsuario())) {
            return ResponseEntity
                    .status(HttpStatus.FORBIDDEN)
                    .body("Acceso sólo permitido a usuarios PRODUCTORES");
        }

        // Generar JWT y roles
        String jwt = jwtUtils.generateJwtToken(authentication);
        List<String> roles = userDetails.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.toList());

        // Responder igual que en /login
        JwtResponse body = new JwtResponse(
                jwt,
                userDetails.getId(),
                userDetails.getEmail(),
                userDetails.getTipoUsuario(),
                roles
        );
        return ResponseEntity.ok(body);
    }

    /**
     * Registra un nuevo cliente en el sistema.
     * <p>
     * Este endpoint está optimizado para el registro público de clientes. El
     * tipo de usuario se establece automáticamente como "CLIENTE".
     * </p>
     *
     * @param usuarioRequestDTO DTO con datos del usuario y cliente a registrar
     * @return ResponseEntity con mensaje de éxito (200) o error (400)
     * @throws EmailAlreadyExistsException Si el email ya está registrado
     */
    @Operation(
            summary = "Registrar cliente",
            description = "Crea una nueva cuenta de usuario con tipo CLIENTE. El tipo de usuario se establece automáticamente."
    )
    @ApiResponses(value = {
        @ApiResponse(
                responseCode = "200",
                description = "Cliente registrado exitosamente",
                content = @Content(
                        mediaType = "application/json",
                        schema = @Schema(implementation = UsuarioResponse.class)
                )
        ),
        @ApiResponse(
                responseCode = "400",
                description = "Error en los datos de entrada o email ya existe",
                content = @Content(
                        mediaType = "application/json",
                        schema = @Schema(implementation = MessageResponse.class)
                )
        )
    })
    @PostMapping("/registro/clientes")
    public ResponseEntity<?> registrarCliente(@Valid
            @RequestBody UsuarioRequest usuarioRequest) {
        try {
            // Forzar tipo CLIENTE independientemente de lo que envíe el usuario
            usuarioRequest.setTipoUsuario("CLIENTE");
            usuarioRequest.setRol("USER");
            UsuarioResponse response = usuarioService.crearUsuario(usuarioRequest);
            return ResponseEntity.ok(response);

        } catch (EmailAlreadyExistsException ex) {
            return ResponseEntity.badRequest()
                    .body("Error: " + ex.getMessage());
        } catch (Exception ex) {
            return ResponseEntity.badRequest()
                    .body(new MessageResponse("Error en el registro: " + ex.getMessage()));
        }
    }

    /**
     * Registra un nuevo productor en el sistema.
     * <p>
     * Este endpoint está optimizado para el registro público de productores. El
     * tipo de usuario se establece automáticamente como "productor".
     * </p>
     *
     * @param usuarioRequestDTO DTO con datos del usuario y productor a
     * registrar
     * @return ResponseEntity con mensaje de éxito (200) o error (400)
     * @throws EmailAlreadyExistsException Si el email ya está registrado
     */
    @Operation(
            summary = "Registrar productor",
            description = "Crea una nueva cuenta de usuario con tipo PRODUCTOR. El tipo de usuario se establece automáticamente."
    )
    @ApiResponses(value = {
        @ApiResponse(
                responseCode = "200",
                description = "Productor registrado",
                content = @Content(
                        mediaType = "application/json",
                        schema = @Schema(implementation = UsuarioResponse.class)
                )
        ),
        @ApiResponse(
                responseCode = "400",
                description = "Error en los datos de entrada o email ya existe",
                content = @Content(
                        mediaType = "application/json",
                        schema = @Schema(implementation = MessageResponse.class)
                )
        )
    })
    @PostMapping("/registro/productores")
    public ResponseEntity<?> registrarproductor(@Valid
            @RequestBody UsuarioRequest usuarioRequest) {
        try {
            // Forzar tipo productor independientemente de lo que envíe el usuario
            usuarioRequest.setTipoUsuario("PRODUCTOR");

            // Forzar rol USER para registros públicos
            usuarioRequest.setRol("USER");
            UsuarioResponse response = usuarioService.crearUsuario(usuarioRequest);
            return ResponseEntity.ok(response);

        } catch (EmailAlreadyExistsException ex) {
            return ResponseEntity.badRequest()
                    .body(new MessageResponse("Error: " + ex.getMessage()));
        } catch (Exception ex) {
            return ResponseEntity.badRequest()
                    .body(new MessageResponse("Error en el registro: " + ex.getMessage()));
        }
    }
}
