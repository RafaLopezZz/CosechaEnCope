package com.rlp.cosechaencope.service;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.rlp.cosechaencope.dto.response.ArticuloResponse;
import com.rlp.cosechaencope.dto.response.DetalleOvpResponse;
import com.rlp.cosechaencope.dto.response.OvpResponse;
import com.rlp.cosechaencope.model.Articulo;
import com.rlp.cosechaencope.model.Cliente;
import com.rlp.cosechaencope.model.DetalleOvp;
import com.rlp.cosechaencope.model.DetallePedido;
import com.rlp.cosechaencope.model.EstadoOrdenVenta;
import com.rlp.cosechaencope.model.OrdenVentaProductor;
import com.rlp.cosechaencope.model.Pedido;
import com.rlp.cosechaencope.model.Productor;
import com.rlp.cosechaencope.repository.OrdenVentaProductorRepository;

import lombok.extern.slf4j.Slf4j;

/**
 * Servicio para gestionar órdenes de venta a productores.
 *
 * <p>
 * Maneja la creación automática de OVPs cuando se procesan pedidos, así como la
 * consulta y actualización de estados de las órdenes.</p>
 */
@Service
@Slf4j
public class OrdenVentaProductorService {

    private final OrdenVentaProductorRepository ordenVentaProductorRepository;

    public OrdenVentaProductorService(
        OrdenVentaProductorRepository ordenVentaProductorRepository
    ) {
        this.ordenVentaProductorRepository = ordenVentaProductorRepository;
    }    

    /**
     * Genera órdenes de venta a productores a partir de un pedido.
     *
     * <p>
     * Este método agrupa los detalles del pedido por productor y crea una orden
     * de venta para cada productor con los artículos correspondientes.</p>
     *
     * @param pedido El pedido del cual se generarán las órdenes de venta.
     * @return Lista de órdenes de venta creadas
     */
    @Transactional
    public List<OrdenVentaProductor> generarOrdenesVentaDesdePedido(Pedido pedido) {
        
        List<OrdenVentaProductor> ordenesCreadas = new ArrayList<>();

        // Agrupar detalles del pedido por productor
        Map<Productor, List<DetallePedido>> detallesPorProductor = new HashMap<>();

        for (DetallePedido detalle : pedido.getDetallePedido()) {
            Articulo articulo = detalle.getArticulo();
            Productor productor = articulo.getProductor();
            
            if (productor == null) {
                log.warn("Artículo {} no tiene productor asignado, saltando...", articulo.getIdArticulo());
                continue;
            }

            detallesPorProductor
                    .computeIfAbsent(productor, k -> new ArrayList<>())
                    .add(detalle);
        }

        log.info("Generando OVPs para {} productor(es) desde pedido #{}", 
                 detallesPorProductor.size(), pedido.getIdPedido());

        // Crear una OVP por cada productor
        for (Map.Entry<Productor, List<DetallePedido>> entry : detallesPorProductor.entrySet()) {
            Productor productor = entry.getKey();
            List<DetallePedido> detalles = entry.getValue();

            // Crear la orden de venta
            OrdenVentaProductor ovp = new OrdenVentaProductor();
            ovp.setProductor(productor);
            ovp.setPedidoCliente(pedido);  // ¡IMPORTANTE! Vincular con el pedido original
            ovp.setFechaCreacion(Instant.now());
            ovp.setEstado(EstadoOrdenVenta.PENDIENTE);
            ovp.setNumeroOrden(generarNumeroOrden(productor.getIdProductor()));
            
            // Crear los detalles de la OVP y agregarlos a la lista
            BigDecimal totalOvp = BigDecimal.ZERO;
            
            for (DetallePedido detallePedido : detalles) {
                DetalleOvp detalleOvp = new DetalleOvp();
                detalleOvp.setOrdenVenta(ovp);  // Vincular con la OVP
                detalleOvp.setArticulo(detallePedido.getArticulo());
                detalleOvp.setCantidad(detallePedido.getCantidad());
                detalleOvp.setPrecioUnitario(detallePedido.getPrecioUnitario());
                
                // Calcular subtotal de la línea
                BigDecimal subtotalLinea = detallePedido.getPrecioUnitario()
                        .multiply(BigDecimal.valueOf(detallePedido.getCantidad()));
                totalOvp = totalOvp.add(subtotalLinea);
                
                // Agregar a la lista de líneas de la OVP (CascadeType.ALL se encarga de persistir)
                ovp.getLineas().add(detalleOvp);
            }

            // Guardar la OVP (cascade guardará también los detalles)
            OrdenVentaProductor ovpGuardada = ordenVentaProductorRepository.save(ovp);
            ordenesCreadas.add(ovpGuardada);
            
            log.info("OVP {} creada para productor {} con {} artículo(s), total: {}", 
                     ovpGuardada.getNumeroOrden(), 
                     productor.getNombre(),
                     detalles.size(),
                     totalOvp);
        }

        return ordenesCreadas;
    }

    /**
     * Genera un número de orden único con formato OVP-YYYYMMDD-XXXX
     */
    private String generarNumeroOrden(Long idProductor) {
        String fecha = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        String aleatorio = String.format("%04d", (int) (Math.random() * 10000));
        return String.format("OVP-%s-%s-%s", fecha, idProductor, aleatorio);
    }

    /**
     * Lista todas las OVP de un productor y las convierte a DTO.
     * Esto evita referencias circulares en la serialización JSON.
     */
    @Transactional(readOnly = true)
    public List<OvpResponse> listarOrdenesPorProductor(Long idProductor) {
        log.info("Obteniendo órdenes de venta para productor ID: {}", idProductor);
        
        List<OrdenVentaProductor> ordenes = ordenVentaProductorRepository
                .findByProductor_IdProductorOrderByFechaCreacionDesc(idProductor);
        
        log.info("Encontradas {} órdenes para productor {}", ordenes.size(), idProductor);
        
        return ordenes.stream()
                .map(this::mapToOvpResponse)
                .collect(Collectors.toList());
    }

    /**
     * Convierte una entidad OrdenVentaProductor a DTO OvpResponse
     */
    private OvpResponse mapToOvpResponse(OrdenVentaProductor ovp) {
        return OvpResponse.builder()
                .idOvp(ovp.getIdOvp())
                .numeroOrden(ovp.getNumeroOrden())
                .fechaCreacion(ovp.getFechaCreacion())
                .fechaActualizacion(ovp.getFechaActualizacion())
                .estado(ovp.getEstado())
                .observaciones(ovp.getObservaciones())
                .total(ovp.getTotal())
                .productor(mapProductor(ovp.getProductor()))
                .pedidoCliente(mapPedidoCliente(ovp.getPedidoCliente()))
                .detalles(mapDetalles(ovp.getLineas()))
                .build();
    }

    private OvpResponse.ProductorBasicoResponse mapProductor(Productor productor) {
        if (productor == null) return null;
        
        return OvpResponse.ProductorBasicoResponse.builder()
                .idProductor(productor.getIdProductor())
                .nombre(productor.getNombre())
                .email(productor.getUsuario() != null ? productor.getUsuario().getEmail() : null)
                .telefono(productor.getTelefono())
                .build();
    }

    private OvpResponse.PedidoClienteBasicoResponse mapPedidoCliente(Pedido pedido) {
        if (pedido == null) return null;
        
        return OvpResponse.PedidoClienteBasicoResponse.builder()
                .idPedido(pedido.getIdPedido())
                .idTransaccion(pedido.getIdTransaccion())
                .fechaPedido(pedido.getFechaPedido())
                .metodoPago(pedido.getMetodoPago())
                .total(pedido.getTotal())
                .cliente(mapCliente(pedido.getCliente()))
                .build();
    }

    private OvpResponse.ClienteBasicoResponse mapCliente(Cliente cliente) {
        if (cliente == null) return null;
        
        return OvpResponse.ClienteBasicoResponse.builder()
                .idCliente(cliente.getIdCliente())
                .nombre(cliente.getNombre())
                .telefono(cliente.getTelefono())
                .direccion(cliente.getDireccion())
                .build();
    }

    private List<DetalleOvpResponse> mapDetalles(List<DetalleOvp> lineas) {
        if (lineas == null) return new ArrayList<>();
        
        return lineas.stream()
                .map(this::mapDetalle)
                .collect(Collectors.toList());
    }

    private DetalleOvpResponse mapDetalle(DetalleOvp detalle) {
        DetalleOvpResponse response = new DetalleOvpResponse();
        response.setIdDetalleOvp(detalle.getIdDetalleOvp());
        response.setCantidad(detalle.getCantidad());
        response.setPrecioUnitario(detalle.getPrecioUnitario());
        
        // Calcular subtotal
        if (detalle.getPrecioUnitario() != null && detalle.getCantidad() != null) {
            response.setSubtotal(detalle.getPrecioUnitario()
                    .multiply(BigDecimal.valueOf(detalle.getCantidad())));
        }
        
        // Mapear artículo
        if (detalle.getArticulo() != null) {
            response.setArticulo(mapArticulo(detalle.getArticulo()));
        }
        
        return response;
    }

    private ArticuloResponse mapArticulo(Articulo articulo) {
        ArticuloResponse response = new ArticuloResponse();
        response.setIdArticulo(articulo.getIdArticulo());
        response.setNombre(articulo.getNombre());
        response.setDescripcion(articulo.getDescripcion());
        response.setPrecio(articulo.getPrecio());
        response.setStock(articulo.getStock());
        response.setImagenUrl(articulo.getImagenUrl());
        
        // Mapear categoría si existe
        if (articulo.getCategoria() != null) {
            response.setIdCategoria(articulo.getCategoria().getIdCategoria());
            response.setNombreCategoria(articulo.getCategoria().getNombre());
        }
        
        // Mapear productor si existe
        if (articulo.getProductor() != null) {
            response.setIdProductor(articulo.getProductor().getIdProductor());
            response.setNombreProductor(articulo.getProductor().getNombre());
        }
        
        return response;
    }
}
