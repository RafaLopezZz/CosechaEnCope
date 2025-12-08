package com.rlp.cosechaencope.service;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.rlp.cosechaencope.dto.response.DetallePedidoResponse;
import com.rlp.cosechaencope.dto.response.PedidoResponse;
import com.rlp.cosechaencope.exception.ResourceNotFoundException;
import com.rlp.cosechaencope.exception.StockInsuficienteException;
import com.rlp.cosechaencope.exception.UnauthorizedException;
import com.rlp.cosechaencope.model.Articulo;
import com.rlp.cosechaencope.model.Carrito;
import com.rlp.cosechaencope.model.Cliente;
import com.rlp.cosechaencope.model.DetalleCarrito;
import com.rlp.cosechaencope.model.DetallePedido;
import com.rlp.cosechaencope.model.EstadoOrdenVenta;
import com.rlp.cosechaencope.model.EstadoPedido;
import com.rlp.cosechaencope.model.OrdenVentaProductor;
import com.rlp.cosechaencope.model.Pedido;
import com.rlp.cosechaencope.repository.ArticuloRepository;
import com.rlp.cosechaencope.repository.CarritoRepository;
import com.rlp.cosechaencope.repository.ClienteRepository;
import com.rlp.cosechaencope.repository.OrdenVentaProductorRepository;
import com.rlp.cosechaencope.repository.PedidoRepository;

@Service
public class PedidoService {

    private final ArticuloRepository articuloRepository;
    private final PedidoRepository pedidoRepository;
    private final ClienteRepository clienteRepository;
    private final CarritoRepository carritoRepository;
    private final OrdenVentaProductorRepository ordenVentaProductorRepository;
    private final CarritoService carritoService;
    private final OrdenVentaProductorService ordenVentaProductorService;

    public PedidoService(
            ArticuloRepository articuloRepository,
            PedidoRepository pedidoRepository,
            CarritoRepository carritoRepository,
            OrdenVentaProductorRepository ordenVentaProductorRepository,
            CarritoService carritoService,
            ClienteRepository clienteRepository,
            OrdenVentaProductorService ordenVentaProductorService
    ) {
        this.articuloRepository = articuloRepository;
        this.clienteRepository = clienteRepository;
        this.pedidoRepository = pedidoRepository;
        this.carritoRepository = carritoRepository;
        this.ordenVentaProductorRepository = ordenVentaProductorRepository;
        this.carritoService = carritoService;
        this.ordenVentaProductorService = ordenVentaProductorService;
    }

    /**
     * Crea un nuevo pedido para el cliente asociado al ID de usuario proporcionado.
     * 
     * @param idUsuario
     * @param metodoPago
     * @return {@link PedidoResponse} con los detalles del pedido creado.
     * @throws ResourceNotFoundException si el cliente o el carrito activo no se encuentran.
     * @throws IllegalStateException si el carrito está vacío o si el cliente no tiene
     *         dirección o teléfono configurados.
     * @throws StockInsuficienteException si algún artículo en el carrito no tiene stock suficiente.
     */
    @Transactional
    public PedidoResponse crearPedido(Long idUsuario, String metodoPago) {

        // Obtener el cliente asociado al usuario
        Cliente cliente = clienteRepository.findByUsuario_IdUsuario(idUsuario)
                .orElseThrow(() -> new ResourceNotFoundException("Cliente no encontrado"));

        // Validar que el cliente tenga dirección configurada
        if (cliente.getDireccion() == null || cliente.getDireccion().trim().isEmpty()) {
            throw new IllegalStateException("El cliente debe tener una dirección configurada para realizar un pedido");
        }

        // Validar que el cliente tenga teléfono configurado
        if (cliente.getTelefono() == null || cliente.getTelefono().trim().isEmpty()) {
            throw new IllegalStateException("El cliente debe tener un teléfono configurado para realizar un pedido");
        }

        // Obtener el carrito ACTIVO del cliente (no finalizado)
        Carrito carrito = carritoRepository.findActivoConDetalles(cliente)
                .orElseThrow(() -> new ResourceNotFoundException("Carrito activo no encontrado"));

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
        pedido.setEstado(EstadoPedido.PENDIENTE);
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

        carrito.setFinalizado(true);
        carritoRepository.save(carrito);

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

    public PedidoResponse obtenerPedidoPorId(Long idUsuario, Long idPedido) {
        Cliente cliente = clienteRepository.findByUsuario_IdUsuario(idUsuario)
                .orElseThrow(() -> new ResourceNotFoundException("Cliente no encontrado"));

        Pedido pedido = pedidoRepository.findById(idPedido)
                .orElseThrow(() -> new ResourceNotFoundException("Pedido no encontrado"));

        if (!pedido.getCliente().getIdCliente().equals(cliente.getIdCliente())) {
            throw new ResourceNotFoundException("El pedido no pertenece al usuario autenticado");
        }

        return mapearResponseDTO(pedido);
    }

    /**
     * Actualiza el estado de una orden de venta a productor específica dentro
     * de un pedido.
     *
     * @param idCliente ID del cliente autenticado
     * @param idPedido ID del pedido
     * @param idOvp ID de la orden de venta al productor
     * @param nuevoEstado Nuevo estado a establecer
     * @return PedidoResponse actualizado
     * @throws ResourceNotFoundException si no se encuentra el pedido o la OVP
     * @throws IllegalArgumentException si el estado no es válido
     * @throws UnauthorizedException si el pedido no pertenece al cliente
     */
    @Transactional
    public PedidoResponse actualizarEstadoOrdenVentaProductor(
            Long idCliente,
            Long idPedido,
            Long idOvp,
            String nuevoEstado) {
        // 1. Validar que el pedido existe y pertenece al cliente
        Pedido pedido = pedidoRepository.findById(idPedido)
                .orElseThrow(() -> new ResourceNotFoundException("Pedido no encontrado con ID: " + idPedido));

        if (!pedido.getCliente().getIdCliente().equals(idCliente)) {
            throw new UnauthorizedException("No tiene permisos para modificar este pedido");
        }

        // 2. Buscar la orden de venta al productor
        OrdenVentaProductor ovp = ordenVentaProductorRepository.findById(idOvp)
                .orElseThrow(() -> new ResourceNotFoundException("Orden de venta no encontrada con ID: " + idOvp));

        // 3. Validar que la OVP está relacionada con el pedido
        boolean ovpPerteneceAlPedido = pedido.getDetallePedido().stream()
                .anyMatch(detalle -> detalle.getArticulo().getProductor().getIdProductor()
                .equals(ovp.getProductor().getIdProductor()));

        if (!ovpPerteneceAlPedido) {
            throw new IllegalArgumentException(
                    "La orden de venta no está relacionada con este pedido");
        }

        // 4. Validar que el nuevo estado es válido
        EstadoOrdenVenta estadoEnum;
        try {
            estadoEnum = EstadoOrdenVenta.valueOf(nuevoEstado.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Estado inválido: " + nuevoEstado
                    + ". Estados válidos: " + Arrays.toString(EstadoOrdenVenta.values()));
        }

        // 5. Validar transición de estado (lógica de negocio)
        validarTransicionEstado(ovp.getEstado(), estadoEnum);

        // 6. Actualizar el estado
        ovp.setEstado(estadoEnum);
        ovp.setFechaActualizacion(Instant.now());
        ordenVentaProductorRepository.save(ovp);

        // 7. Actualizar el estado del pedido si es necesario
        actualizarEstadoPedidoSegunOVPs(pedido);

        // 8. Retornar el pedido actualizado
        return obtenerPedidoPorId(idCliente, idPedido);
    }

    /**
     * Valida que la transición de estado sea permitida según reglas de negocio.
     *
     * @param estadoActual Estado actual de la OVP
     * @param nuevoEstado Nuevo estado solicitado
     * @throws IllegalStateException si la transición no es válida
     */
    private void validarTransicionEstado(EstadoOrdenVenta estadoActual, EstadoOrdenVenta nuevoEstado) {
        // Definir transiciones válidas
        Map<EstadoOrdenVenta, List<EstadoOrdenVenta>> transicionesPermitidas = Map.of(
                EstadoOrdenVenta.PENDIENTE, List.of(
                        EstadoOrdenVenta.EN_PROCESO,
                        EstadoOrdenVenta.CANCELADA
                ),
                EstadoOrdenVenta.EN_PROCESO, List.of(
                        EstadoOrdenVenta.ENVIADA,
                        EstadoOrdenVenta.CANCELADA
                ),
                EstadoOrdenVenta.ENVIADA, List.of(
                        EstadoOrdenVenta.ENTREGADA
                ),
                EstadoOrdenVenta.ENTREGADA, List.of(),
                EstadoOrdenVenta.CANCELADA, List.of()
        );

        List<EstadoOrdenVenta> estadosPermitidos = transicionesPermitidas.get(estadoActual);

        if (estadosPermitidos == null || !estadosPermitidos.contains(nuevoEstado)) {
            throw new IllegalStateException(
                    String.format("No se puede cambiar de estado %s a %s",
                            estadoActual, nuevoEstado));
        }
    }

    /**
     * Actualiza el estado general del pedido basándose en los estados de todas
     * sus OVPs.
     *
     * @param pedido El pedido a actualizar
     */
    private void actualizarEstadoPedidoSegunOVPs(Pedido pedido) {
        // Obtener todas las OVPs relacionadas con este pedido
        List<OrdenVentaProductor> ovps = ordenVentaProductorRepository
                .findByPedido(pedido);

        if (ovps.isEmpty()) {
            return;
        }

        // Contar estados
        long totalOvps = ovps.size();
        long ovpsEntregadas = ovps.stream()
                .filter(ovp -> ovp.getEstado() == EstadoOrdenVenta.ENTREGADA)
                .count();
        long ovpsCanceladas = ovps.stream()
                .filter(ovp -> ovp.getEstado() == EstadoOrdenVenta.CANCELADA)
                .count();
        long ovpsEnviadas = ovps.stream()
                .filter(ovp -> ovp.getEstado() == EstadoOrdenVenta.ENVIADA)
                .count();

        // Determinar el nuevo estado del pedido
        EstadoPedido nuevoEstadoPedido;

        if (ovpsEntregadas == totalOvps) {
            nuevoEstadoPedido = EstadoPedido.ENTREGADO;
        } else if (ovpsCanceladas == totalOvps) {
            nuevoEstadoPedido = EstadoPedido.CANCELADO;
        } else if (ovpsEnviadas + ovpsCanceladas + ovpsEntregadas > 0) {
            nuevoEstadoPedido = EstadoPedido.EN_PREPARACION;
        } else if (ovpsEnviadas > 0) {
            nuevoEstadoPedido = EstadoPedido.ENVIADO;
        } else {
            nuevoEstadoPedido = EstadoPedido.PENDIENTE;
        }

        // Actualizar solo si hay cambio
        if (!pedido.getEstado().equals(nuevoEstadoPedido)) {
            pedido.setEstado(nuevoEstadoPedido);
            pedidoRepository.save(pedido);
        }
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
        response.setEstadoPedido(pedido.getEstado().getNombre());
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
