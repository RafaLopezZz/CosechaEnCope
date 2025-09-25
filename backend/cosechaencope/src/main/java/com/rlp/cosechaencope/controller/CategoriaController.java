package com.rlp.cosechaencope.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
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

import com.rlp.cosechaencope.dto.request.CategoriaRequest;
import com.rlp.cosechaencope.dto.response.ArticuloResponse;
import com.rlp.cosechaencope.dto.response.CategoriaResponse;
import com.rlp.cosechaencope.dto.response.MessageResponse;
import com.rlp.cosechaencope.service.ArticuloService;
import com.rlp.cosechaencope.service.CategoriaService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

/**
 * Controlador REST para gestionar categorías en Cosecha-en-Cope.
 *
 * <p>
 * Endpoints REST:</p>
 * <ul>
 * <li>POST /cosechaencope/categorias → Crear categoría</li>
 * <li>GET /cosechaencope/categorias/{id} → Obtener categoría por ID</li>
 * <li>GET /cosechaencope/categorias → Listar categorías</li>
 * <li>PUT /cosechaencope/categorias/{id} → Actualizar categoría</li>
 * <li>DELETE /cosechaencope/categorias/{id} → Eliminar categoría</li>
 * </ul>
 *
 * @author rafalopezzz
 */
@CrossOrigin(origins = "*")
@RestController
@RequestMapping("/cosechaencope/categorias")
@Tag(name = "Categorías", description = "Operaciones para la gestión de categorías")
public class CategoriaController {

    @Autowired
    private CategoriaService categoriaService;

    @Autowired
    private ArticuloService articuloService;

    /**
     * Crea una nueva categoría en el sistema.
     *
     * @param categoriasRequestDTO DTO con los datos de la categoría a crear.
     * @return DTO de respuesta con los datos de la categoría creada.
     */
    @Operation(
            summary = "Crear categoría",
            description = "Registra una nueva categoría en el sistema con los datos proporcionados"
    )
    @ApiResponses(value = {
        @ApiResponse(
                responseCode = "201",
                description = "Categoría creada exitosamente",
                content = @Content(
                        mediaType = "application/json",
                        schema = @Schema(implementation = CategoriaResponse.class)
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
                description = "La categoría ya existe"
        )
    })
    @PostMapping
    public ResponseEntity<CategoriaResponse> crearCategoria(@Valid @RequestBody CategoriaRequest categoriaRequest) {
        CategoriaResponse response = categoriaService.crearCategoria(categoriaRequest);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * Obtiene una categoría por su ID.
     *
     * @param idCategoría ID de la categoría a buscar.
     * @return DTO de respuesta con los datos de la categoría encontrada.
     */
    @Operation(
            summary = "Obtener categoría por ID",
            description = "Busca y devuelve una categoría específica utilizando su identificador único"
    )
    @ApiResponses(value = {
        @ApiResponse(
                responseCode = "200",
                description = "Categoría encontrada",
                content = @Content(
                        mediaType = "application/json",
                        schema = @Schema(implementation = CategoriaResponse.class)
                )
        ),
        @ApiResponse(
                responseCode = "404",
                description = "Categoría no encontrada",
                content = @Content(
                        mediaType = "application/json",
                        schema = @Schema(implementation = MessageResponse.class)
                )
        ),
        @ApiResponse(
                responseCode = "400",
                description = "ID de categoría inválido"
        )
    })
    @GetMapping("/{idCategoria}")
    public ResponseEntity<CategoriaResponse> obtenerCategoriaPorId(@PathVariable Long idCategoria) {
        CategoriaResponse response = categoriaService.obtenerCategoriaPorId(idCategoria);
        return ResponseEntity.ok(response);
    }

    /**
     * Lista todos los artículos asociados a una categoría específica.
     *
     * @param idCategoria ID de la categoría cuyos artículos se desean listar.
     * @return Lista de DTOs de respuesta con los datos de los artículos
     * encontrados.
     */
    @Operation(
            summary = "Listar artículos por categoría",
            description = "Obtiene todos los artículos que pertenecen a una categoría específica"
    )
    @ApiResponses({
        @ApiResponse(
                responseCode = "200",
                description = "Lista de artículos obtenida exitosamente",
                content = @Content(
                        mediaType = "application/json",
                        array = @ArraySchema(schema = @Schema(implementation = ArticuloResponse.class))
                )
        ),
        @ApiResponse(
                responseCode = "404",
                description = "Categoría no encontrada",
                content = @Content(
                        mediaType = "application/json",
                        schema = @Schema(implementation = MessageResponse.class)
                )
        ),
        @ApiResponse(
                responseCode = "204",
                description = "No hay artículos en esta categoría"
        )
    })
    @GetMapping("/{idCategoria}/articulos")
    public ResponseEntity<List<ArticuloResponse>> listarPorCategoria(@PathVariable Long idCategoria) {
        List<ArticuloResponse> articulos = articuloService.listarPorCategoria(idCategoria);
        return ResponseEntity.ok(articulos);
    }

    /**
     * Obtiene todas las categorías registradas en el sistema.
     *
     * @return Lista de DTOs de respuesta con los datos de todas las categorías.
     */
    @Operation(
            summary = "Listar todas las categorías",
            description = "Devuelve una lista completa de todas las categorías disponibles en el sistema"
    )
    @ApiResponses(value = {
        @ApiResponse(
                responseCode = "200",
                description = "Lista de categorías obtenida exitosamente",
                content = @Content(
                        mediaType = "application/json",
                        array = @ArraySchema(schema = @Schema(implementation = CategoriaResponse.class))
                )
        ),
        @ApiResponse(
                responseCode = "204",
                description = "No hay categorías registradas"
        )
    })
    @GetMapping
    public ResponseEntity<List<CategoriaResponse>> obtenerTodasLasCategorias() {
        List<CategoriaResponse> response = categoriaService.obtenerTodasLasCategorias();
        return ResponseEntity.ok(response);
    }

    /**
     * Actualiza una categoría existente por su ID.
     *
     * @param idCategoria ID de la categoría a actualizar.
     * @param categoriaRequest DTO con los nuevos datos de la categoría.
     * @return DTO de respuesta con los datos actualizados de la categoría.
     */
    @Operation(
            summary = "Actualizar categoría",
            description = "Modifica los datos de una categoría existente utilizando su ID"
    )
    @ApiResponses(value = {
        @ApiResponse(
                responseCode = "200",
                description = "Categoría actualizada exitosamente",
                content = @Content(
                        mediaType = "application/json",
                        schema = @Schema(implementation = CategoriaResponse.class)
                )
        ),
        @ApiResponse(
                responseCode = "404",
                description = "Categoría no encontrada",
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
    @PutMapping("/{idCategoria}")
    public ResponseEntity<CategoriaResponse> actualizarCategoria(@PathVariable Long idCategoria, @Valid @RequestBody CategoriaRequest categoriaRequest) {
        CategoriaResponse response = categoriaService.actualizarCategoria(idCategoria, categoriaRequest);
        return ResponseEntity.ok(response);
    }

    /**
     * Elimina una categoría por su ID.
     *
     * @param idCategoria ID de la categoría a eliminar.
     * @return Mensaje de confirmación de eliminación.
     */
    @Operation(
            summary = "Eliminar categoría",
            description = "Elimina permanentemente una categoría del sistema utilizando su ID"
    )
    @ApiResponses(value = {
        @ApiResponse(
                responseCode = "200",
                description = "Categoría eliminada exitosamente",
                content = @Content(
                        mediaType = "text/plain",
                        schema = @Schema(type = "string", example = "Categoría eliminada correctamente")
                )
        ),
        @ApiResponse(
                responseCode = "404",
                description = "Categoría no encontrada",
                content = @Content(
                        mediaType = "application/json",
                        schema = @Schema(implementation = MessageResponse.class)
                )
        ),
        @ApiResponse(
                responseCode = "409",
                description = "No se puede eliminar: categoría tiene productos asociados"
        )
    })
    @DeleteMapping("/{idCategoria}")
    public ResponseEntity<String> eliminarCategoria(@PathVariable("idCategoria") Long idCategoria) {
        categoriaService.eliminarCategoria(idCategoria);
        return ResponseEntity.ok("Categoria eliminada correctamente");

    }
}
