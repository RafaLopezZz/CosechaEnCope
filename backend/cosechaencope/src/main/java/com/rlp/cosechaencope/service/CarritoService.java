package com.rlp.cosechaencope.service;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.rlp.cosechaencope.dto.request.AddToCarritoRequest;
import com.rlp.cosechaencope.dto.response.CarritoResponse;
import com.rlp.cosechaencope.dto.response.DetalleCarritoResponse;
import com.rlp.cosechaencope.exception.ResourceNotFoundException;
import com.rlp.cosechaencope.exception.StockInsuficienteException;
import com.rlp.cosechaencope.model.Articulo;
import com.rlp.cosechaencope.model.Carrito;
import com.rlp.cosechaencope.model.Cliente;
import com.rlp.cosechaencope.model.DetalleCarrito;
import com.rlp.cosechaencope.repository.ArticuloRepository;
import com.rlp.cosechaencope.repository.CarritoRepository;
import com.rlp.cosechaencope.repository.ClienteRepository;
import com.rlp.cosechaencope.repository.DetalleCarritoRepository;

@Service
public class CarritoService {

    private final DetalleCarritoRepository detalleCarritoRepository;
    private final CarritoRepository carritoRepository;
    private final ArticuloRepository articuloRepository;
    private final ClienteRepository clienteRepository;

    public CarritoService(CarritoRepository carritoRepository,
            ArticuloRepository articuloRepository, DetalleCarritoRepository detalleCarritoRepository,
            ClienteRepository clienteRepository) {
        this.clienteRepository = clienteRepository;
        this.carritoRepository = carritoRepository;
        this.articuloRepository = articuloRepository;
        this.detalleCarritoRepository = detalleCarritoRepository;
    }

    @Transactional
    public CarritoResponse agregarACarrito(Long idUsuario, AddToCarritoRequest request) {
        Cliente cliente = clienteRepository.findByUsuario_IdUsuario(idUsuario)
                .orElseThrow(() -> new ResourceNotFoundException("Cliente no encontrado"));
        // Validación de cantidad
        if (request.getCantidad() <= 0) {
            throw new IllegalArgumentException("La cantidad debe ser mayor que cero.");
        }

        Carrito carrito = carritoRepository
                .findByClienteConDetalles(cliente)
                .map(c -> {
                    // Si ya existía pero estaba finalizado, lo reseteamos
                    if (Boolean.TRUE.equals(c.getFinalizado())) {
                        c.setFinalizado(false);
                        c.getDetalleList().clear();
                        c.setSubtotal(BigDecimal.ZERO);
                        c.setImpuestos(BigDecimal.ZERO);
                        c.setGastosEnvio(BigDecimal.ZERO);
                        c.setTotal(BigDecimal.ZERO);
                    }
                    return c;
                })
                .orElseGet(() -> crearCarritoNuevo(cliente));

        // Verificar existencia del artículo
        Articulo articulo = articuloRepository.findById(request.getIdArticulo())
                .orElseThrow(() -> new ResourceNotFoundException("Artículo no existe"));

        // Verificar stock disponible
        if (articulo.getStock() < request.getCantidad()) {
            throw new StockInsuficienteException("Stock insuficiente para el artículo: " + articulo.getNombre());
        }

        // Buscar si ya existe el artículo en el carrito o crear nuevo detalle
        DetalleCarrito detalle = carrito.getDetalleList().stream()
                .filter(d -> d.getArticulo().getIdArticulo()
                .equals(articulo.getIdArticulo()))
                .findFirst()
                .orElseGet(() -> {
                    DetalleCarrito d = new DetalleCarrito();
                    d.setCarrito(carrito);
                    d.setArticulo(articulo);
                    d.setCantidad(0);
                    d.setPrecioUnitario(articulo.getPrecio());
                    // guardamos para forzar ID (cascade=ALL, no haría falta, pero nos aseguramos)
                    DetalleCarrito saved = detalleCarritoRepository.save(d);
                    carrito.getDetalleList().add(saved);
                    return saved;
                });

        // Verificar stock para cantidad total
        int nuevaCantidad = detalle.getCantidad() + request.getCantidad();
        if (articulo.getStock() < nuevaCantidad) {
            throw new StockInsuficienteException(
                    "Stock insuficiente. Tienes " + detalle.getCantidad()
                    + " unidades en el carrito y solo hay " + articulo.getStock()
                    + " disponibles en total.");
        }

        // Actualizar cantidad y precios
        detalle.setCantidad(nuevaCantidad);
        detalle.setPrecioUnitario(articulo.getPrecio()); // Asegurarse de que el precio esté actualizado

        // Verificar si el método calcularTotalLinea existe
        try {
            detalle.calcularTotalLinea();
        } catch (Exception ex) {
            // Si no existe, calcular manualmente
            detalle.setTotalLinea(detalle.getPrecioUnitario().multiply(new BigDecimal(detalle.getCantidad())));
        }

        // Recalcular totales del carrito
        try {
            carrito.recalcularTotales();
        } catch (Exception ex) {
            // Si el método no existe, implementar cálculo básico
            BigDecimal subtotal = carrito.getDetalleList().stream()
                    .map(DetalleCarrito::getTotalLinea)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
            carrito.setSubtotal(subtotal);
            carrito.setImpuestos(subtotal.multiply(new BigDecimal("0.21")));
            carrito.setTotal(subtotal.add(carrito.getImpuestos()).add(carrito.getGastosEnvio()));
        }
        // Actualizar stock del artículo
        articulo.setStock(articulo.getStock() - request.getCantidad());
        articuloRepository.save(articulo);

        carritoRepository.save(carrito);

        return mapearCarritoResponseDTO(carrito);
    }

    private Carrito crearCarritoNuevo(Cliente cliente) {
        Carrito nuevo = new Carrito();
        nuevo.setCliente(cliente);
        nuevo.setFechaCreacion(Instant.now());
        nuevo.setSubtotal(BigDecimal.ZERO);
        nuevo.setImpuestos(BigDecimal.ZERO);
        nuevo.setGastosEnvio(BigDecimal.ZERO);
        nuevo.setTotal(BigDecimal.ZERO);
        // la lista detalleList ya viene inicializada en el constructor de la entidad
        return carritoRepository.save(nuevo);
    }

    @Transactional
    public CarritoResponse decrementarArticulo(Long idUsuario, Long articuloId) {
        Cliente cliente = clienteRepository.findByUsuario_IdUsuario(idUsuario)
                .orElseThrow(() -> new ResourceNotFoundException("Cliente no encontrado"));
        // Buscar carrito del cliente
        Carrito carrito = carritoRepository.findByCliente(cliente)
                .orElseThrow(() -> new ResourceNotFoundException("Carrito no encontrado"));

        // Buscar el detalle correspondiente al artículo
        DetalleCarrito detalle = carrito.getDetalleList().stream()
                .filter(d -> d.getArticulo().getIdArticulo().equals(articuloId))
                .findFirst()
                .orElseThrow(() -> new ResourceNotFoundException("Artículo no encontrado en el carrito"));

        // Obtener referencia al artículo
        Articulo articulo = detalle.getArticulo();

        // Si solo hay una unidad, eliminar el detalle
        if (detalle.getCantidad() == 1) {
            carrito.getDetalleList().remove(detalle);
            detalleCarritoRepository.delete(detalle);
        } else {
            // Si hay más unidades, decrementar
            detalle.setCantidad(detalle.getCantidad() - 1);
            detalle.calcularTotalLinea();
        }

        // Recalcular totales del carrito
        carrito.recalcularTotales();
        carritoRepository.save(carrito);
        // Devolver stock
        articulo.setStock(articulo.getStock() + 1);
        articuloRepository.save(articulo);

        // Guardar cambios
        Carrito carritoActualizado = carritoRepository.save(carrito);

        return mapearCarritoResponseDTO(carritoActualizado);
    }

    public CarritoResponse verCarrito(Long idUsuario) {
        Cliente cliente = clienteRepository.findByUsuario_IdUsuario(idUsuario)
                .orElseThrow(() -> new ResourceNotFoundException("Cliente no encontrado"));
        Carrito carrito = carritoRepository.findByCliente(cliente)
                .orElseGet(() -> {
                    Carrito nuevoCarrito = new Carrito();
                    nuevoCarrito.setCliente(cliente);
                    nuevoCarrito.setFechaCreacion(Instant.now());
                    nuevoCarrito.setSubtotal(BigDecimal.ZERO);
                    nuevoCarrito.setImpuestos(BigDecimal.ZERO);
                    nuevoCarrito.setGastosEnvio(BigDecimal.ZERO);
                    nuevoCarrito.setTotal(BigDecimal.ZERO);
                    return carritoRepository.save(nuevoCarrito);
                });

        return mapearCarritoResponseDTO(carrito);
    }

    @Transactional
    public void vaciarCarrito(Long idUsuario) {
        Cliente cliente = clienteRepository.findByUsuario_IdUsuario(idUsuario)
                .orElseThrow(() -> new ResourceNotFoundException("Cliente no encontrado"));
        // Recuperar carrito junto con sus detalles
        Carrito carrito = carritoRepository
                .findByClienteConDetalles(cliente)
                .orElse(null);

        if (carrito != null && !carrito.getDetalleList().isEmpty()) {
            // Devolver stock de cada artículo
            for (DetalleCarrito detalle : carrito.getDetalleList()) {
                Articulo articulo = detalle.getArticulo();
                articulo.setStock(articulo.getStock() + detalle.getCantidad());
                articuloRepository.save(articulo);
            }

            // Limpiar la lista de detalles (orphanRemoval eliminará en BD)
            carrito.getDetalleList().clear();

            // Recalcular totales (quedarán a cero)
            carrito.recalcularTotales();

            // Persistir el carrito vacío
            carritoRepository.save(carrito);
        }
    }

    @Transactional
    public Carrito obtenerOCrearCarritoActivo(Long idCliente) {
        Cliente cliente = clienteRepository.findById(idCliente)
                .orElseThrow(() -> new ResourceNotFoundException("Cliente no encontrado"));

        return carritoRepository
                .findByClienteConDetalles(cliente)
                .map(c -> {
                    // Si está finalizado, crear uno nuevo
                    if (Boolean.TRUE.equals(c.getFinalizado())) {
                        Carrito carritoNuevo = new Carrito();
                        carritoNuevo.setCliente(cliente);
                        carritoNuevo.setFechaCreacion(Instant.now());
                        carritoNuevo.setFinalizado(false);
                        return carritoRepository.save(carritoNuevo);
                    }
                    return c;
                })
                .orElseGet(() -> crearCarritoNuevo(cliente));
    }

    private CarritoResponse mapearCarritoResponseDTO(Carrito carrito) {
        CarritoResponse dto = new CarritoResponse();
        dto.setId(carrito.getIdCarrito());
        dto.setFechaCreacion(carrito.getFechaCreacion());

        List<DetalleCarritoResponse> items = carrito.getDetalleList() != null
                ? carrito.getDetalleList().stream()
                        .map(this::mapearDetalleCarritoDTO)
                        .collect(Collectors.toList())
                : new ArrayList<>();

        dto.setItems(items);
        dto.setSubtotal(carrito.getSubtotal() != null ? carrito.getSubtotal() : BigDecimal.ZERO);
        dto.setImpuestos(carrito.getImpuestos() != null ? carrito.getImpuestos() : BigDecimal.ZERO);
        dto.setGastosEnvio(carrito.getGastosEnvio() != null ? carrito.getGastosEnvio() : BigDecimal.ZERO);
        dto.setTotal(carrito.getTotal() != null ? carrito.getTotal() : BigDecimal.ZERO);

        return dto;
    }

    private DetalleCarritoResponse mapearDetalleCarritoDTO(DetalleCarrito detalle) {
        DetalleCarritoResponse dto = new DetalleCarritoResponse();
        dto.setId(detalle.getIdDetalleCarrito());
        dto.setIdArticulo(detalle.getArticulo().getIdArticulo());
        dto.setNombreArticulo(detalle.getArticulo().getNombre());
        dto.setCantidad(detalle.getCantidad());
        dto.setPrecioUnitario(detalle.getPrecioUnitario());
        dto.setTotalLinea(detalle.getTotalLinea());
        return dto;
    }
}
