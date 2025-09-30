package com.rlp.cosechaencope.service;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.rlp.cosechaencope.dto.response.DetallePedidoResponse;
import com.rlp.cosechaencope.dto.response.PedidoResponse;
import com.rlp.cosechaencope.exception.ResourceNotFoundException;
import com.rlp.cosechaencope.exception.StockInsuficienteException;
import com.rlp.cosechaencope.model.Articulo;
import com.rlp.cosechaencope.model.Carrito;
import com.rlp.cosechaencope.model.Cliente;
import com.rlp.cosechaencope.model.DetalleCarrito;
import com.rlp.cosechaencope.model.DetallePedido;
import com.rlp.cosechaencope.model.Pedido;
import com.rlp.cosechaencope.repository.CarritoRepository;
import com.rlp.cosechaencope.repository.ClienteRepository;
import com.rlp.cosechaencope.repository.PedidoRepository;
import com.rlp.cosechaencope.repository.ArticuloRepository;

@Service
public class PedidoService {

    private final ArticuloRepository articuloRepository;
    private final PedidoRepository pedidoRepository;
    private final ClienteRepository clienteRepository;
    private final CarritoRepository carritoRepository;
    private final CarritoService carritoService;
    private final OrdenVentaProductorService ordenVentaProductorService;

    public PedidoService(
            ArticuloRepository articuloRepository,
            PedidoRepository pedidoRepository,
            CarritoRepository carritoRepository,
            CarritoService carritoService,
            ClienteRepository clienteRepository,
            OrdenVentaProductorService ordenVentaProductorService
    ) {
        this.articuloRepository = articuloRepository;
        this.clienteRepository = clienteRepository;
        this.pedidoRepository = pedidoRepository;
        this.carritoRepository = carritoRepository;
        this.carritoService = carritoService;
        this.ordenVentaProductorService = ordenVentaProductorService;
    }

    @Transactional
    public PedidoResponse crearPedido(Long idUsuario, String metodoPago) {

        // Obtener el cliente asociado al usuario
        Cliente cliente = clienteRepository.findByUsuario_IdUsuario(idUsuario)
                .orElseThrow(() -> new ResourceNotFoundException("Cliente no encontrado"));

        // Obtener el carrito del cliente
        Carrito carrito = carritoRepository.findByCliente(cliente)
                .orElseThrow(() -> new ResourceNotFoundException("Carrito no encontrado"));

        // Verificar que el carrito no esté vacío
        if (carrito.getDetalleList().isEmpty()) {
            throw new IllegalStateException("No se puede crear un pedido con un carrito vacío");
        }

        // Verificar stock para cada artículo en el carrito
        for (DetalleCarrito detalle : carrito.getDetalleList()) {
            Articulo articulo = detalle.getArticulo();
            if (articulo.getStock() < detalle.getCantidad()) {
                throw new StockInsuficienteException(
                        "Stock insuficiente para: " + articulo.getNombre()
                );
            }
        }

        // Asegurar que los totales estén actualizados
        carrito.recalcularTotales();

        // Crear el pedido
        Pedido pedido = new Pedido();
        pedido.setCliente(cliente);
        pedido.setEstadoPedido("PENDIENTE");
        pedido.setFechaPedido(Instant.now());
        pedido.setSubtotal(carrito.getSubtotal());
        pedido.setIva(carrito.getImpuestos());
        pedido.setGastosEnvio(carrito.getGastosEnvio());
        pedido.setTotal(carrito.getTotal());
        pedido.setMetodoPago(metodoPago);

        // Generar ID de transacción único (simulado)
        pedido.setIdTransaccion(generarNumeroPedido());

        // Crear detalles del pedido a partir del carrito
        for (DetalleCarrito detalleCarrito : carrito.getDetalleList()) {
            DetallePedido detallePedido = new DetallePedido();
            detallePedido.setPedido(pedido);
            detallePedido.setArticulo(detalleCarrito.getArticulo());
            detallePedido.setCantidad(detalleCarrito.getCantidad());
            detallePedido.setPrecioUnitario(detalleCarrito.getPrecioUnitario());
            detallePedido.setTotalLinea(detalleCarrito.getTotalLinea());
            pedido.getDetallePedido().add(detallePedido);
        }

        // Guardar pedido
        Pedido pedidoGuardado = pedidoRepository.save(pedido);

        // Actualizar stock de los artículos
        for (DetallePedido detallePedido : pedidoGuardado.getDetallePedido()) {
            Articulo articulo = detallePedido.getArticulo();
            articulo.setStock(articulo.getStock() - detallePedido.getCantidad());
            articuloRepository.save(articulo);
        }

        // Generar órdenes de venta a productores
        ordenVentaProductorService.generarOrdenesVentaDesdePedido(pedidoGuardado);

        // Vaciar el carrito
        carritoService.vaciarCarrito(idUsuario);

        return mapearResponseDTO(pedido);
    }

    public List<PedidoResponse> listarPorCliente(Long idUsuario) {
        Cliente cliente = clienteRepository.findByUsuario_IdUsuario(idUsuario)
                .orElseThrow(() -> new ResourceNotFoundException("Cliente no encontrado"));
        List<Pedido> pedidos = pedidoRepository.findByCliente(cliente);
        return pedidos.stream()
                .map(this::mapearResponseDTO)
                .collect(Collectors.toList());
    }

    /**
     * Genera un número de pedido único Formato: PED-YYYYMMDD-XXXX
     */
    private String generarNumeroPedido() {
        LocalDateTime ahora = LocalDateTime.now();
        String fecha = ahora.format(DateTimeFormatter.ofPattern("yyyyMMdd"));

        // Obtener contador del día
        Long countHoy = pedidoRepository.countByFechaPedidoAfter(
                Instant.now().minus(1, ChronoUnit.DAYS)
        );

        String secuencial = String.format("%04d", (countHoy % 10000) + 1);
        return "PED-" + fecha + "-" + secuencial;
    }

    private PedidoResponse mapearResponseDTO(Pedido pedido) {
        PedidoResponse response = new PedidoResponse();
        //BigDecimal total = BigDecimal.valueOf(pedido.getTotal());

        response.setIdPedido(pedido.getIdPedido());
        response.setIdUsuario(pedido.getCliente().getUsuario().getIdUsuario());
        response.setIdCliente(pedido.getCliente().getIdCliente());
        response.setEmailUsuario(pedido.getCliente().getUsuario().getEmail());
        response.setNombreCliente(pedido.getCliente().getNombre());
        response.setDireccionCliente(pedido.getCliente().getDireccion());
        response.setFechaPedido(pedido.getFechaPedido());
        response.setEstadoPedido(pedido.getEstadoPedido());
        response.setSubTotal(pedido.getSubtotal());
        response.setIva(pedido.getIva());
        response.setGastosEnvio(pedido.getGastosEnvio());
        response.setTotal(pedido.getTotal());
        response.setMetodoPago(pedido.getMetodoPago());
        response.setIdTransaccion(pedido.getIdTransaccion());

        List<DetallePedidoResponse> detalles = pedido.getDetallePedido().stream()
                .map(dp -> {
                    DetallePedidoResponse dto = new DetallePedidoResponse();
                    dto.setIdPedido(dp.getIdDetallePedido());

                    Articulo art = dp.getArticulo();
                    dto.setIdArticulo(art.getIdArticulo());
                    dto.setNombreArticulo(art.getNombre());
                    dto.setCantidad(dp.getCantidad());
                    dto.setPrecioUnitario(dp.getPrecioUnitario());
                    dto.setTotalLinea(dp.getTotalLinea());

                    return dto;
                })
                .collect(Collectors.toList());

        response.setDetalles(detalles);
        return response;
    }
}
