package com.rlp.cosechaencope.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.rlp.cosechaencope.dto.request.ArticuloRequest;
import com.rlp.cosechaencope.dto.request.ProductorRequest;
import com.rlp.cosechaencope.dto.response.ArticuloResponse;
import com.rlp.cosechaencope.dto.response.MessageResponse;
import com.rlp.cosechaencope.dto.response.ProductorResponse;
import com.rlp.cosechaencope.service.ArticuloService;
import com.rlp.cosechaencope.service.ProductorService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;

/**
 * Controlador REST para la gestión de datos de productores asociados a
 * usuarios.
 *
 * <p>
 * Proporciona endpoints para obtener, listar y actualizar la información del
 * productor correspondiente a un usuario determinado. Los productores están
 * vinculados a usuarios mediante una relación uno a uno y contienen información
 * específica del negocio como datos fiscales, información de contacto
 * comercial, etc.</p>
 *
 * <p>
 * Se habilita CORS con {@code @CrossOrigin(origins = "*")} para permitir
 * peticiones desde cualquier origen.</p>
 *
 * <p>
 * Endpoints disponibles:</p>
 * <ul>
 * <li>GET /cosechaencope/usuarios/productores              → Listar todos los productores</li>
 * <li>GET /cosechaencope/usuarios/productores/{idUsuario}  → Obtener datos de productor por ID de usuario</li>
 * <li>PUT /cosechaencope/usuarios/productores/{idUsuario}  → Actualizar datos de productor por ID de usuario</li>
 * </ul>
 *
 * <p>
 * <strong>Nota:</strong> Todos los endpoints requieren autenticación JWT válida
 * y permisos apropiados para acceder a la información de productores.</p>
 *
 * @author rafalopezzz
 */
@CrossOrigin(origins = "*")
@RestController
@RequestMapping("/cosechaencope/usuarios/productores")
@Tag(name = "Productor", description = "Gestión de datos de productores asociados a usuarios")
public class ProductorController {

    
    @Autowired
    private ProductorService productorService;

    @Autowired
    private ArticuloService articuloService;

    /**
     * Obtiene los datos del productor asociado al usuario indicado por su ID.
     *
     * <p>
     * Busca el productor vinculado al usuario especificado. Cada usuario de
     * tipo PRODUCTOR tiene asociado un registro de productor con información
     * comercial adicional como datos fiscales, información de contacto,
     * categorías de productos, etc.</p>
     *
     * @param idUsuario ID único del usuario cuyo productor se desea obtener
     * @return ResponseEntity con los datos del productor encontrado
     * @throws UsuarioNotFoundException Si el usuario no existe
     * @throws AccessDeniedException Si el usuario no tiene permisos para
     * acceder a este productor
     */
    @Operation(
            summary = "Obtener productor por ID de usuario",
            description = "Busca y devuelve los datos del productor asociado al usuario especificado"
    )
    @ApiResponses({
        @ApiResponse(
                responseCode = "200",
                description = "Productor encontrado exitosamente",
                content = @Content(
                        mediaType = "application/json",
                        schema = @Schema(implementation = ProductorResponse.class)
                )
        ),
        @ApiResponse(
                responseCode = "404",
                description = "Productor no encontrado para el usuario especificado",
                content = @Content(
                        mediaType = "application/json",
                        schema = @Schema(implementation = MessageResponse.class)
                )
        ),
        @ApiResponse(
                responseCode = "400",
                description = "ID de usuario inválido"
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
    public ResponseEntity<ProductorResponse> obtenerProductorPorUsuarioId(@PathVariable Long idUsuario) {
        ProductorResponse productor = productorService.obtenerProductorPorUsuarioId(idUsuario);
        return ResponseEntity.ok(productor);
    }

    /**
     * Lista todos los productores registrados en el sistema.
     *
     * <p>
     * Devuelve una lista completa de todos los productores con su información
     * comercial básica. Este endpoint está disponible para usuarios con
     * permisos administrativos y puede ser utilizado para generar directorios
     * de productores o realizar búsquedas administrativas.</p>
     *
     * @return ResponseEntity con la lista de todos los productores
     */
    @Operation(
            summary = "Listar todos los productores",
            description = "Obtiene una lista completa de todos los productores registrados en el sistema"
    )
    @ApiResponses({
        @ApiResponse(
                responseCode = "200",
                description = "Lista de productores obtenida exitosamente",
                content = @Content(
                        mediaType = "application/json",
                        array = @ArraySchema(schema = @Schema(implementation = ProductorResponse.class))
                )
        ),
        @ApiResponse(
                responseCode = "204",
                description = "No hay productores registrados en el sistema"
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
    public ResponseEntity<List<ProductorResponse>> listarProductores() {
        List<ProductorResponse> productores = productorService.listarProductores();
        return ResponseEntity.ok(productores);
    }

    /**
     * Actualiza los datos del productor asociado al usuario indicado por su ID.
     *
     * <p>
     * Modifica la información comercial del productor vinculado al usuario
     * especificado. Solo se actualizan los campos proporcionados en el DTO de
     * entrada. Los campos no incluidos mantienen sus valores actuales. Esta
     * operación puede incluir actualización de datos fiscales, información de
     * contacto, categorías de productos ofrecidos, etc.</p>
     *
     * @param idUsuario ID del usuario cuyo productor se desea actualizar
     * @param productorRequestDTO DTO con los nuevos datos del productor a
     * actualizar
     * @return ResponseEntity con los datos actualizados del productor
     * @throws UsuarioNotFoundException Si el usuario no existe
     * @throws ValidationException Si los datos de entrada son inválidos
     * @throws AccessDeniedException Si el usuario no tiene permisos para
     * modificar este productor
     */
    @Operation(
            summary = "Actualizar datos de productor",
            description = "Modifica la información comercial del productor asociado al usuario especificado"
    )
    @ApiResponses({
        @ApiResponse(
                responseCode = "200",
                description = "Productor actualizado exitosamente",
                content = @Content(
                        mediaType = "application/json",
                        schema = @Schema(implementation = ProductorResponse.class)
                )
        ),
        @ApiResponse(
                responseCode = "404",
                description = "Productor no encontrado para el usuario especificado",
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
    public ResponseEntity<ProductorResponse> actualizarProductor(
            @PathVariable Long idUsuario,
            @RequestBody ProductorRequest productorRequest) {
        ProductorResponse productorActualizado = productorService.actualizarProductor(idUsuario, productorRequest);
        return ResponseEntity.ok(productorActualizado);
    }

    /**
     * Obtiene todos los artículos de un productor específico.
     *
     * @param idProductor ID del productor
     * @return Lista de artículos del productor
     */
    @Operation(
            summary = "Obtener artículos por productor",
            description = "Obtiene todos los artículos asociados a un productor específico"
    )
    @ApiResponses({
        @ApiResponse(
                responseCode = "200",
                description = "Artículos obtenidos exitosamente",
                content = @Content(
                        mediaType = "application/json",
                        array = @ArraySchema(schema = @Schema(implementation = ArticuloResponse.class))
                )
        ),
        @ApiResponse(
                responseCode = "404",
                description = "Productor no encontrado"
        )
    })
    @GetMapping("/{idProductor}/articulos")
    public ResponseEntity<List<ArticuloResponse>> obtenerArticulosPorProductor(@PathVariable Long idProductor) {
        List<ArticuloResponse> articulos = articuloService.obtenerArticulosPorProductor(idProductor);
        return ResponseEntity.ok(articulos);
    }

    /**
     * Crea un nuevo artículo para un productor específico.
     *
     * @param idProductor ID del productor
     * @param articuloRequest Datos del artículo a crear
     * @return Artículo creado
     */
    @Operation(
            summary = "Crear artículo para productor",
            description = "Crea un nuevo artículo asociado a un productor específico"
    )
    @ApiResponses({
        @ApiResponse(
                responseCode = "201",
                description = "Artículo creado exitosamente",
                content = @Content(
                        mediaType = "application/json",
                        schema = @Schema(implementation = ArticuloResponse.class)
                )
        ),
        @ApiResponse(
                responseCode = "400",
                description = "Datos de entrada inválidos"
        ),
        @ApiResponse(
                responseCode = "404",
                description = "Productor no encontrado"
        )
    })
    @PostMapping("/{idProductor}/articulos")
    public ResponseEntity<ArticuloResponse> crearArticuloPorProductor(
            @PathVariable Long idProductor,
            @RequestBody ArticuloRequest articuloRequest) {
        ArticuloResponse articuloCreado = articuloService.crearArticuloPorProductor(idProductor, articuloRequest);
        return ResponseEntity.ok(articuloCreado);
    }

    /**
     * Actualiza un artículo específico de un productor.
     *
     * @param idProductor ID del productor
     * @param idArticulo ID del artículo a actualizar
     * @param articuloRequest Datos actualizados del artículo
     * @return Artículo actualizado
     */
    @Operation(
            summary = "Actualizar artículo de productor",
            description = "Actualiza un artículo específico asociado a un productor"
    )
    @ApiResponses({
        @ApiResponse(
                responseCode = "200",
                description = "Artículo actualizado exitosamente",
                content = @Content(
                        mediaType = "application/json",
                        schema = @Schema(implementation = ArticuloResponse.class)
                )
        ),
        @ApiResponse(
                responseCode = "400",
                description = "Datos de entrada inválidos"
        ),
        @ApiResponse(
                responseCode = "404",
                description = "Artículo o productor no encontrado"
        )
    })
    @PutMapping("/{idProductor}/articulos/{idArticulo}")
    public ResponseEntity<ArticuloResponse> actualizarArticuloPorProductor(
            @PathVariable Long idProductor,
            @PathVariable Long idArticulo,
            @RequestBody ArticuloRequest articuloRequest) {
        ArticuloResponse articuloActualizado = articuloService.actualizarArticuloPorProductor(idProductor, idArticulo, articuloRequest);
        return ResponseEntity.ok(articuloActualizado);
    }

    /**
     * Elimina un artículo específico de un productor.
     *
     * @param idProductor ID del productor
     * @param idArticulo ID del artículo a eliminar
     * @return Mensaje de confirmación
     */
    @Operation(
            summary = "Eliminar artículo de productor",
            description = "Elimina un artículo específico asociado a un productor"
    )
    @ApiResponses({
        @ApiResponse(
                responseCode = "200",
                description = "Artículo eliminado exitosamente",
                content = @Content(
                        mediaType = "application/json",
                        schema = @Schema(implementation = MessageResponse.class)
                )
        ),
        @ApiResponse(
                responseCode = "404",
                description = "Artículo o productor no encontrado"
        )
    })
    @DeleteMapping("/{idProductor}/articulos/{idArticulo}")
    public ResponseEntity<MessageResponse> eliminarArticuloPorProductor(
            @PathVariable Long idProductor,
            @PathVariable Long idArticulo) {
        articuloService.eliminarArticuloPorProductor(idProductor, idArticulo);
        return ResponseEntity.ok(new MessageResponse("Artículo eliminado exitosamente"));
    }

}
