package com.rlp.cosechaencope.service;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import static org.mockito.ArgumentMatchers.any;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;

import com.rlp.cosechaencope.model.Articulo;
import com.rlp.cosechaencope.model.Categoria;
import com.rlp.cosechaencope.model.Cliente;
import com.rlp.cosechaencope.model.DetalleOvp;
import com.rlp.cosechaencope.model.DetallePedido;
import com.rlp.cosechaencope.model.EstadoOrdenVenta;
import com.rlp.cosechaencope.model.OrdenVentaProductor;
import com.rlp.cosechaencope.model.Pedido;
import com.rlp.cosechaencope.model.Productor;
import com.rlp.cosechaencope.model.Usuario;
import com.rlp.cosechaencope.repository.DetalleOvpRepository;
import com.rlp.cosechaencope.repository.OrdenVentaProductorRepository;

@ExtendWith(MockitoExtension.class)
public class OrdenVentaProductorServiceTest {

    @Mock
    private OrdenVentaProductorRepository ordenVentaproductorRepository;
    @Mock
    private DetalleOvpRepository detalleOvpRepository;

    @InjectMocks
    private OrdenVentaProductorService ordenVentaProductorService;

    private Pedido pedido;
    private Cliente cliente;
    private Usuario usuario;
    private Productor productor1;
    private Productor productor2;
    private Articulo articulo1;
    private Articulo articulo2;
    private Articulo articulo3;
    private DetallePedido detallePedido1;
    private DetallePedido detallePedido2;
    private DetallePedido detallePedido3;
    private OrdenVentaProductor ovp1;
    private OrdenVentaProductor ovp2;

    @BeforeEach
    void setUp() {
        // Configurar usuario
        usuario = new Usuario();
        usuario.setIdUsuario(1L);
        usuario.setEmail("cliente@test.com");
        usuario.setTipoUsuario("CLIENTE");

        // Configurar cliente
        cliente = new Cliente();
        cliente.setIdCliente(1L);
        cliente.setNombre("Cliente Test");
        cliente.setUsuario(usuario);

        // Configurar categoría
        Categoria categoria = new Categoria();
        categoria.setIdCategoria(1L);
        categoria.setNombre("Frutas");

        // Configurar productores
        productor1 = new Productor();
        productor1.setIdProductor(1L);
        productor1.setNombre("Productor 1");

        productor2 = new Productor();
        productor2.setIdProductor(2L);
        productor2.setNombre("Productor 2");

        // Configurar artículos
        articulo1 = new Articulo();
        articulo1.setIdArticulo(1L);
        articulo1.setNombre("Tomate");
        articulo1.setPrecio(new BigDecimal("5.50"));
        articulo1.setCategoria(categoria);
        articulo1.setProductor(productor1);

        articulo2 = new Articulo();
        articulo2.setIdArticulo(2L);
        articulo2.setNombre("Lechuga");
        articulo2.setPrecio(new BigDecimal("3.00"));
        articulo2.setCategoria(categoria);
        articulo2.setProductor(productor1);

        articulo3 = new Articulo();
        articulo3.setIdArticulo(3L);
        articulo3.setNombre("Zanahoria");
        articulo3.setPrecio(new BigDecimal("2.50"));
        articulo3.setCategoria(categoria);
        articulo3.setProductor(productor2);

        // Configurar detalles de pedido
        detallePedido1 = new DetallePedido();
        detallePedido1.setIdDetallePedido(1L);
        detallePedido1.setArticulo(articulo1);
        detallePedido1.setCantidad(5);
        detallePedido1.setPrecioUnitario(new BigDecimal("5.50"));

        detallePedido2 = new DetallePedido();
        detallePedido2.setIdDetallePedido(2L);
        detallePedido2.setArticulo(articulo2);
        detallePedido2.setCantidad(3);
        detallePedido2.setPrecioUnitario(new BigDecimal("3.00"));

        detallePedido3 = new DetallePedido();
        detallePedido3.setIdDetallePedido(3L);
        detallePedido3.setArticulo(articulo3);
        detallePedido3.setCantidad(2);
        detallePedido3.setPrecioUnitario(new BigDecimal("2.50"));

        // Configurar pedido
        pedido = new Pedido();
        pedido.setIdPedido(1L);
        pedido.setCliente(cliente);
        pedido.setFechaPedido(Instant.now());
        pedido.setDetallePedido(List.of(detallePedido1, detallePedido2, detallePedido3));

        // Configurar órdenes de venta
        ovp1 = new OrdenVentaProductor();
        ovp1.setIdOvp(1L);
        ovp1.setProductor(productor1);
        ovp1.setFechaCreacion(Instant.now());
        ovp1.setEstado(EstadoOrdenVenta.PENDIENTE);

        ovp2 = new OrdenVentaProductor();
        ovp2.setIdOvp(2L);
        ovp2.setProductor(productor2);
        ovp2.setFechaCreacion(Instant.now());
        ovp2.setEstado(EstadoOrdenVenta.PENDIENTE);
    }

    @Test
    void generarOrdenesVentaDesdePedido_deberiaCrearOrdenesParaCadaProductor() {
        // Arrange
        when(ordenVentaproductorRepository.save(any(OrdenVentaProductor.class)))
            .thenReturn(ovp1, ovp2);

        // Act
        ordenVentaProductorService.generarOrdenesVentaDesdePedido(pedido);

        // Assert
        // Verificar que se guarden 2 órdenes de venta (una por cada productor)
        verify(ordenVentaproductorRepository, times(2)).save(any(OrdenVentaProductor.class));
        
        // Verificar que se guarden 3 detalles OVP (uno por cada detalle del pedido)
        verify(detalleOvpRepository, times(3)).save(any(DetalleOvp.class));
    }

    @Test
    void generarOrdenesVentaDesdePedido_deberiaAgruparArticulosPorProductor() {
        // Arrange
        when(ordenVentaproductorRepository.save(any(OrdenVentaProductor.class)))
            .thenAnswer(invocation -> {
                OrdenVentaProductor ovp = invocation.getArgument(0);
                if (ovp.getProductor().equals(productor1)) {
                    ovp.setIdOvp(1L);
                    return ovp1;
                } else {
                    ovp.setIdOvp(2L);
                    return ovp2;
                }
            });

        // Act
        ordenVentaProductorService.generarOrdenesVentaDesdePedido(pedido);

        // Assert
        // Verificar que se crean órdenes de venta para cada productor
        verify(ordenVentaproductorRepository, times(2)).save(any(OrdenVentaProductor.class));
        
        // Verificar que se crean detalles para todos los artículos
        verify(detalleOvpRepository, times(3)).save(any(DetalleOvp.class));
    }

    @Test
    void generarOrdenesVentaDesdePedido_deberiaAsignarDatosCorrectosAOrdenVenta() {
        // Arrange
        when(ordenVentaproductorRepository.save(any(OrdenVentaProductor.class)))
            .thenAnswer(invocation -> {
                OrdenVentaProductor ovp = invocation.getArgument(0);
                
                // Verificar que los datos básicos están asignados correctamente
                assertThat(ovp.getProductor()).isNotNull();
                assertThat(ovp.getFechaCreacion()).isNotNull();
                
                if (ovp.getProductor().equals(productor1)) {
                    ovp.setIdOvp(1L);
                    return ovp1;
                } else {
                    ovp.setIdOvp(2L);
                    return ovp2;
                }
            });

        // Act
        ordenVentaProductorService.generarOrdenesVentaDesdePedido(pedido);

        // Assert
        verify(ordenVentaproductorRepository, times(2)).save(any(OrdenVentaProductor.class));
    }

    @Test
    void generarOrdenesVentaDesdePedido_deberiaManejarPedidoVacio() {
        // Arrange
        pedido.setDetallePedido(List.of()); // Pedido sin detalles

        // Act
        ordenVentaProductorService.generarOrdenesVentaDesdePedido(pedido);

        // Assert
        // No se debería guardar ninguna orden ni detalle
        verify(ordenVentaproductorRepository, times(0)).save(any(OrdenVentaProductor.class));
        verify(detalleOvpRepository, times(0)).save(any(DetalleOvp.class));
    }

    @Test
    void generarOrdenesVentaDesdePedido_deberiaCrearSoloUnaOrdenParaUnProductor() {
        // Arrange - Todos los artículos del mismo productor
        articulo2.setProductor(productor1); // Cambiar productor del segundo artículo
        articulo3.setProductor(productor1); // Cambiar productor del tercer artículo
        
        when(ordenVentaproductorRepository.save(any(OrdenVentaProductor.class)))
            .thenReturn(ovp1);

        // Act
        ordenVentaProductorService.generarOrdenesVentaDesdePedido(pedido);

        // Assert
        // Solo se debería crear una orden de venta (todos los artículos son del mismo productor)
        verify(ordenVentaproductorRepository, times(1)).save(any(OrdenVentaProductor.class));
        
        // Pero se deberían crear 3 detalles (uno por cada artículo)
        verify(detalleOvpRepository, times(3)).save(any(DetalleOvp.class));
    }

    @Test
    void generarOrdenesVentaDesdePedido_deberiaAsignarEstadoPendienteInicialmente() {
        // Arrange
        when(ordenVentaproductorRepository.save(any(OrdenVentaProductor.class)))
            .thenAnswer(invocation -> {
                OrdenVentaProductor ovp = invocation.getArgument(0);
                
                // Verificar que el estado inicial es PENDIENTE
                assertThat(ovp.getEstado()).isEqualTo(EstadoOrdenVenta.PENDIENTE);
                assertThat(ovp.getFechaCreacion()).isNotNull();
                
                if (ovp.getProductor().equals(productor1)) {
                    ovp.setIdOvp(1L);
                    return ovp1;
                } else {
                    ovp.setIdOvp(2L);
                    return ovp2;
                }
            });

        // Act
        ordenVentaProductorService.generarOrdenesVentaDesdePedido(pedido);

        // Assert
        verify(ordenVentaproductorRepository, times(2)).save(any(OrdenVentaProductor.class));
    }

    @Test
    void generarOrdenesVentaDesdePedido_deberiaCrearDetallesConDatosCorrectos() {
        // Arrange
        when(ordenVentaproductorRepository.save(any(OrdenVentaProductor.class)))
            .thenAnswer(invocation -> {
                OrdenVentaProductor ovp = invocation.getArgument(0);
                if (ovp.getProductor().equals(productor1)) {
                    ovp.setIdOvp(1L);
                    return ovp1;
                } else {
                    ovp.setIdOvp(2L);
                    return ovp2;
                }
            });

        when(detalleOvpRepository.save(any(DetalleOvp.class)))
            .thenAnswer(invocation -> {
                DetalleOvp detalle = invocation.getArgument(0);
                
                // Verificar que los detalles tienen los datos correctos
                assertThat(detalle.getOrdenVenta()).isNotNull();
                assertThat(detalle.getArticulo()).isNotNull();
                assertThat(detalle.getCantidad()).isGreaterThan(0);
                assertThat(detalle.getPrecioUnitario()).isNotNull();
                
                return detalle;
            });

        // Act
        ordenVentaProductorService.generarOrdenesVentaDesdePedido(pedido);

        // Assert
        verify(ordenVentaproductorRepository, times(2)).save(any(OrdenVentaProductor.class));
        verify(detalleOvpRepository, times(3)).save(any(DetalleOvp.class));
    }
}