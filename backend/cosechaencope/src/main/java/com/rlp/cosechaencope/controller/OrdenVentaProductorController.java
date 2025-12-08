package com.rlp.cosechaencope.controller;

import java.util.List;

import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.rlp.cosechaencope.dto.response.MessageResponse;
import com.rlp.cosechaencope.dto.response.OvpResponse;
import com.rlp.cosechaencope.service.OrdenVentaProductorService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

/**
 * Controlador REST para la gestión de órdenes de venta asociadas a productores.
 *
 * <p>Proporciona endpoints para consultar las órdenes de venta generadas por
 * productores específicos. Este controlador permite a los productores revisar
 * el historial de sus ventas y el estado de sus órdenes.</p>
 *
 * <p>Se habilita CORS con {@code @CrossOrigin(origins = "*")} para permitir
 * peticiones desde cualquier origen.</p>
 *
 * <p>Endpoints disponibles:</p>
 * <ul>
 *   <li>GET /cosechaencope/productores/{idProductor}/ordenes → Listar órdenes de venta del productor</li>
 * </ul>
 *
 * <p><strong>Nota:</strong> Todos los endpoints requieren autenticación JWT válida.</p>
 * 
 * @author rafalopezzz
 */
@RestController
@RequestMapping("/cosechaencope/productores/{idProductor}/ordenes")
@CrossOrigin(origins = "*")
@RequiredArgsConstructor
@Tag(name = "Órdenes de Venta Productor", description = "Gestión de órdenes de venta asociadas a productores")
public class OrdenVentaProductorController {

    private final OrdenVentaProductorService ordenVentaProductorService;

    /**
     * Lista todas las órdenes de venta asociadas a un productor específico.
     *
     * <p>Devuelve un historial completo de todas las órdenes de venta generadas
     * por el productor especificado, ordenadas por fecha de creación descendente
     * (más recientes primero). Incluye información detallada de cada orden como
     * artículos vendidos, cantidades, precios, estado actual y método de pago.</p>
     *
     * @param idProductor ID del productor cuyas órdenes se desean consultar
     * @return Lista de órdenes de venta del productor especificado
     * @throws ProductorNotFoundException Si el productor no existe
     * @throws AccessDeniedException Si el usuario no tiene permisos para acceder a las órdenes de este productor
     */
    @Operation(
            summary = "Listar órdenes de venta del productor",
            description = "Obtiene el historial completo de órdenes de venta del productor especificado ordenadas por fecha"
    )
    @ApiResponses({
        @ApiResponse(
                responseCode = "200",
                description = "Lista de órdenes obtenida exitosamente",
                content = @Content(
                        mediaType = "application/json",
                        array = @ArraySchema(schema = @Schema(implementation = OvpResponse.class))
                )
        ),
        @ApiResponse(
                responseCode = "204",
                description = "El productor no tiene órdenes de venta registradas"
        ),
        @ApiResponse(
                responseCode = "404",
                description = "Productor no encontrado",
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
                description = "Sin permisos para acceder a las órdenes de este productor"
        )
    })
    @GetMapping
    public List<OvpResponse> listarPorProductor(@PathVariable Long idProductor) {
        return ordenVentaProductorService.listarOrdenesPorProductor(idProductor);
    }
}
