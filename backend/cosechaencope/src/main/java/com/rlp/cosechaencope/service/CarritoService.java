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

    /**
     * Agrega un artículo al carrito del usuario autenticado.
     * 
     * <p>Si el artículo ya existe en el carrito, incrementa su cantidad.
     * Si el carrito no existe, lo crea automáticamente.</p>
     * 
     * @param idUsuario ID del usuario autenticado (obtenido del JWT)
     * @param request Datos del artículo a agregar (idArticulo, cantidad)
     * @return {@link CarritoResponse} con el estado actualizado del carrito
     * @throws ResourceNotFoundException Si el artículo no existe
     * @throws StockInsuficienteException Si no hay suficiente stock
     */
    @Transactional
    public CarritoResponse agregarACarrito(Long idUsuario, AddToCarritoRequest request) {
        // Validación de cantidad (aunque ya viene validada por @Min en el DTO)
        if (request.getCantidad() <= 0) {
            throw new IllegalArgumentException("La cantidad debe ser mayor que cero.");
        }

        // Buscar o crear carrito activo del usuario (1 sola query optimizada)
        Carrito carrito = carritoRepository
                .findActivoByUsuarioIdConDetalles(idUsuario)
                .orElseGet(() -> crearCarritoNuevoPorUsuarioId(idUsuario));

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

        // Actualizar cantidad y precio del detalle
        detalle.setCantidad(nuevaCantidad);
        detalle.setPrecioUnitario(articulo.getPrecio());
        detalle.calcularTotalLinea();

        // Recalcular totales del carrito (subtotal, impuestos, envío, total)
        carrito.recalcularTotales();

        // Actualizar stock del artículo (restar la cantidad agregada)
        articulo.setStock(articulo.getStock() - request.getCantidad());
        articuloRepository.save(articulo);

        carritoRepository.save(carrito);

        return mapearCarritoResponseDTO(carrito);
    }

    /**
     * Crea un carrito nuevo vacío para un usuario (usado cuando buscamos por ID de usuario).
     * 
     * @param idUsuario ID del usuario
     * @return Carrito nuevo persistido
     */
    private Carrito crearCarritoNuevoPorUsuarioId(Long idUsuario) {
        Cliente cliente = clienteRepository.findByUsuario_IdUsuario(idUsuario)
                .orElseThrow(() -> new ResourceNotFoundException("Cliente no encontrado para el usuario: " + idUsuario));
        return crearCarritoNuevo(cliente);
    }

    /**
     * Crea un carrito nuevo vacío para un cliente.
     * 
     * @param cliente Cliente propietario del carrito
     * @return Carrito nuevo persistido
     */
    private Carrito crearCarritoNuevo(Cliente cliente) {
        Carrito nuevo = new Carrito();
        nuevo.setCliente(cliente);
        nuevo.setFechaCreacion(Instant.now());
        nuevo.setFinalizado(false);
        // Los valores numéricos ya se inicializan en BigDecimal.ZERO en la entidad
        // la lista detalleList ya viene inicializada en el constructor de la entidad
        return carritoRepository.save(nuevo);
    }

    /**
     * Decrementa en 1 la cantidad de un artículo en el carrito.
     * Si la cantidad llega a 0, elimina el artículo del carrito.
     * 
     * @param idUsuario ID del usuario autenticado
     * @param articuloId ID del artículo a decrementar
     * @return {@link CarritoResponse} con el estado actualizado del carrito
     * @throws ResourceNotFoundException Si el carrito o artículo no existe
     */
    @Transactional
    public CarritoResponse decrementarArticulo(Long idUsuario, Long articuloId) {
        // Buscar carrito activo con detalles (1 query optimizada)
        Carrito carrito = carritoRepository.findActivoByUsuarioIdConDetalles(idUsuario)
                .orElseThrow(() -> new ResourceNotFoundException("Carrito no encontrado para el usuario"));

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
        
        // Devolver stock
        articulo.setStock(articulo.getStock() + 1);
        articuloRepository.save(articulo);

        // Guardar cambios del carrito
        Carrito carritoActualizado = carritoRepository.save(carrito);

        return mapearCarritoResponseDTO(carritoActualizado);
    }

    /**
     * Obtiene el carrito actual del usuario autenticado.
     * Si no existe carrito, crea uno nuevo vacío.
     * 
     * @param idUsuario ID del usuario autenticado (del JWT)
     * @return {@link CarritoResponse} con el contenido del carrito
     */
    @Transactional(readOnly = true)
    public CarritoResponse verCarrito(Long idUsuario) {
        // Buscar carrito activo con detalles, o crear uno nuevo si no existe
        Carrito carrito = carritoRepository.findActivoByUsuarioIdConDetalles(idUsuario)
                .orElseGet(() -> crearCarritoNuevoPorUsuarioId(idUsuario));

        return mapearCarritoResponseDTO(carrito);
    }

    /**
     * Vacía completamente el carrito del usuario, devolviendo el stock de todos los artículos.
     * 
     * @param idUsuario ID del usuario autenticado
     */
    @Transactional
    public void vaciarCarrito(Long idUsuario) {
        // Recuperar carrito activo junto con sus detalles
        Carrito carrito = carritoRepository
                .findActivoByUsuarioIdConDetalles(idUsuario)
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

    /**
     * Obtiene el carrito activo de un cliente o crea uno nuevo si no existe.
     * 
     * <p><b>Uso interno:</b> Método usado por otros servicios (ej: PedidoService)
     * que necesitan acceder al carrito usando el ID del cliente directamente.</p>
     * 
     * @param idCliente ID del cliente
     * @return Carrito activo con detalles cargados
     */
    @Transactional
    public Carrito obtenerOCrearCarritoActivo(Long idCliente) {
        Cliente cliente = clienteRepository.findById(idCliente)
                .orElseThrow(() -> new ResourceNotFoundException("Cliente no encontrado"));

        // Buscar carrito activo con detalles (ya filtra por finalizado=false)
        return carritoRepository
                .findActivoConDetalles(cliente)
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
        dto.setImagenUrl(detalle.getArticulo().getImagenUrl());
        dto.setStockDisponible(detalle.getArticulo().getStock());
        return dto;
    }
}
