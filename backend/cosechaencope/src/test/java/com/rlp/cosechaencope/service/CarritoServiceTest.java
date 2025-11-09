package com.rlp.cosechaencope.service;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import static org.mockito.ArgumentMatchers.any;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;

import com.rlp.cosechaencope.dto.request.AddToCarritoRequest;
import com.rlp.cosechaencope.dto.response.CarritoResponse;
import com.rlp.cosechaencope.exception.ResourceNotFoundException;
import com.rlp.cosechaencope.exception.StockInsuficienteException;
import com.rlp.cosechaencope.model.Articulo;
import com.rlp.cosechaencope.model.Carrito;
import com.rlp.cosechaencope.model.Categoria;
import com.rlp.cosechaencope.model.Cliente;
import com.rlp.cosechaencope.model.DetalleCarrito;
import com.rlp.cosechaencope.model.Productor;
import com.rlp.cosechaencope.model.Usuario;
import com.rlp.cosechaencope.repository.ArticuloRepository;
import com.rlp.cosechaencope.repository.CarritoRepository;
import com.rlp.cosechaencope.repository.ClienteRepository;
import com.rlp.cosechaencope.repository.DetalleCarritoRepository;

@ExtendWith(MockitoExtension.class)
public class CarritoServiceTest {

    @Mock
    private CarritoRepository carritoRepository;
    @Mock
    private ArticuloRepository articuloRepository;
    @Mock
    private DetalleCarritoRepository detalleCarritoRepository;
    @Mock
    private ClienteRepository clienteRepository;

    @InjectMocks
    private CarritoService carritoService;

    private Cliente cliente;
    private Usuario usuario;
    private Articulo articulo;
    private Carrito carrito;
    private DetalleCarrito detalleCarrito;
    private AddToCarritoRequest addToCarritoRequest;

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

        // Configurar productor
        Productor productor = new Productor();
        productor.setIdProductor(1L);
        productor.setNombre("Productor Test");

        // Configurar artículo
        articulo = new Articulo();
        articulo.setIdArticulo(1L);
        articulo.setNombre("Tomate");
        articulo.setDescripcion("Tomate fresco");
        articulo.setPrecio(new BigDecimal("5.50"));
        articulo.setStock(100);
        articulo.setCategoria(categoria);
        articulo.setProductor(productor);

        // Configurar carrito con lista mutable
        carrito = new Carrito();
        carrito.setIdCarrito(1L);
        carrito.setCliente(cliente);
        carrito.setFechaCreacion(Instant.now());
        carrito.setDetalleList(new ArrayList<>()); // Lista mutable

        // Configurar detalle carrito
        detalleCarrito = new DetalleCarrito();
        detalleCarrito.setIdDetalleCarrito(1L);
        detalleCarrito.setCarrito(carrito);
        detalleCarrito.setArticulo(articulo);
        detalleCarrito.setCantidad(5);
        detalleCarrito.setPrecioUnitario(new BigDecimal("5.50"));

        // Configurar request
        addToCarritoRequest = new AddToCarritoRequest();
        addToCarritoRequest.setIdArticulo(1L);
        addToCarritoRequest.setCantidad(5);
    }

    @Test
    void agregarACarrito_deberiaAgregarArticuloCuandoDatosValidos() {
        // Arrange
        when(clienteRepository.findByUsuario_IdUsuario(1L)).thenReturn(Optional.of(cliente));
        when(articuloRepository.findById(1L)).thenReturn(Optional.of(articulo));
        when(carritoRepository.findByClienteConDetalles(cliente)).thenReturn(Optional.of(carrito));
        when(detalleCarritoRepository.save(any(DetalleCarrito.class))).thenReturn(detalleCarrito);
        when(carritoRepository.save(any(Carrito.class))).thenReturn(carrito);

        // Act
        CarritoResponse response = carritoService.agregarACarrito(1L, addToCarritoRequest);

        // Assert
        assertThat(response.getId()).isEqualTo(1L);
        verify(clienteRepository).findByUsuario_IdUsuario(1L);
        verify(articuloRepository).findById(1L);
        verify(carritoRepository).findByClienteConDetalles(cliente);
        verify(detalleCarritoRepository).save(any(DetalleCarrito.class));
    }

    @Test
    void agregarACarrito_deberiaLanzarExcepcionCuandoClienteNoExiste() {
        // Arrange
        when(clienteRepository.findByUsuario_IdUsuario(1L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> carritoService.agregarACarrito(1L, addToCarritoRequest))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("Cliente no encontrado");

        verify(clienteRepository).findByUsuario_IdUsuario(1L);
        verifyNoMoreInteractions(articuloRepository, carritoRepository, detalleCarritoRepository);
    }

    @Test
    void agregarACarrito_deberiaLanzarExcepcionCuandoArticuloNoExiste() {
        // Arrange
        when(clienteRepository.findByUsuario_IdUsuario(1L)).thenReturn(Optional.of(cliente));
        when(articuloRepository.findById(1L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> carritoService.agregarACarrito(1L, addToCarritoRequest))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("Artículo no existe");

        verify(clienteRepository).findByUsuario_IdUsuario(1L);
        verify(articuloRepository).findById(1L);
        verifyNoMoreInteractions(carritoRepository, detalleCarritoRepository);
    }

    @Test
    void agregarACarrito_deberiaLanzarExcepcionCuandoCantidadEsCero() {
        // Arrange
        addToCarritoRequest.setCantidad(0);

        // Act & Assert
        assertThatThrownBy(() -> carritoService.agregarACarrito(1L, addToCarritoRequest))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("La cantidad debe ser mayor que cero.");

        verifyNoMoreInteractions(clienteRepository, articuloRepository, carritoRepository, detalleCarritoRepository);
    }

    @Test
    void agregarACarrito_deberiaLanzarExcepcionCuandoStockInsuficiente() {
        // Arrange
        articulo.setStock(3); // Stock menor que la cantidad solicitada
        addToCarritoRequest.setCantidad(5);
        
        when(clienteRepository.findByUsuario_IdUsuario(1L)).thenReturn(Optional.of(cliente));
        when(carritoRepository.findByClienteConDetalles(cliente)).thenReturn(Optional.of(carrito));
        when(articuloRepository.findById(1L)).thenReturn(Optional.of(articulo));

        // Act & Assert
        assertThatThrownBy(() -> carritoService.agregarACarrito(1L, addToCarritoRequest))
                .isInstanceOf(StockInsuficienteException.class)
                .hasMessage("Stock insuficiente para el artículo: Tomate");

        verify(clienteRepository).findByUsuario_IdUsuario(1L);
        verify(articuloRepository).findById(1L);
        verifyNoMoreInteractions(detalleCarritoRepository);
    }

    @Test
    void verCarrito_deberiaRetornarCarritoCuandoExiste() {
        // Arrange
        carrito.getDetalleList().add(detalleCarrito); // Agregar detalle al carrito
        when(clienteRepository.findByUsuario_IdUsuario(1L)).thenReturn(Optional.of(cliente));
        when(carritoRepository.findByCliente(cliente)).thenReturn(Optional.of(carrito));

        // Act
        CarritoResponse response = carritoService.verCarrito(1L);

        // Assert
        assertThat(response.getId()).isEqualTo(1L);
        verify(clienteRepository).findByUsuario_IdUsuario(1L);
        verify(carritoRepository).findByCliente(cliente);
    }

    @Test
    void verCarrito_deberiaLanzarExcepcionCuandoClienteNoExiste() {
        // Arrange
        when(clienteRepository.findByUsuario_IdUsuario(1L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> carritoService.verCarrito(1L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("Cliente no encontrado");

        verify(clienteRepository).findByUsuario_IdUsuario(1L);
        verifyNoMoreInteractions(carritoRepository, detalleCarritoRepository);
    }

    @Test
    void decrementarArticulo_deberiaDecrementarCantidadCuandoExiste() {
        // Arrange
        detalleCarrito.setCantidad(3); // Cantidad mayor a 1
        carrito.getDetalleList().add(detalleCarrito); // Agregar detalle al carrito
        
        when(clienteRepository.findByUsuario_IdUsuario(1L)).thenReturn(Optional.of(cliente));
        when(carritoRepository.findByCliente(cliente)).thenReturn(Optional.of(carrito));
        when(articuloRepository.save(any(Articulo.class))).thenReturn(articulo);
        when(carritoRepository.save(any(Carrito.class))).thenReturn(carrito);

        // Act
        CarritoResponse response = carritoService.decrementarArticulo(1L, 1L);

        // Assert
        assertThat(response.getId()).isEqualTo(1L);
        verify(clienteRepository).findByUsuario_IdUsuario(1L);
        verify(carritoRepository).findByCliente(cliente);
        verify(articuloRepository).save(any(Articulo.class));
        verify(carritoRepository).save(any(Carrito.class));
    }

    @Test
    void vaciarCarrito_deberiaEliminarTodosLosArticulos() {
        // Arrange
        carrito.getDetalleList().add(detalleCarrito); // Agregar detalle al carrito
        when(clienteRepository.findByUsuario_IdUsuario(1L)).thenReturn(Optional.of(cliente));
        when(carritoRepository.findByClienteConDetalles(cliente)).thenReturn(Optional.of(carrito));
        when(articuloRepository.save(any(Articulo.class))).thenReturn(articulo);
        when(carritoRepository.save(any(Carrito.class))).thenReturn(carrito);

        // Act
        carritoService.vaciarCarrito(1L);

        // Assert
        verify(clienteRepository).findByUsuario_IdUsuario(1L);
        verify(carritoRepository).findByClienteConDetalles(cliente);
        verify(articuloRepository).save(any(Articulo.class));
        verify(carritoRepository).save(any(Carrito.class));
    }

    @Test
    void vaciarCarrito_deberiaLanzarExcepcionCuandoClienteNoExiste() {
        // Arrange
        when(clienteRepository.findByUsuario_IdUsuario(1L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> carritoService.vaciarCarrito(1L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("Cliente no encontrado");

        verify(clienteRepository).findByUsuario_IdUsuario(1L);
        verifyNoMoreInteractions(carritoRepository, detalleCarritoRepository);
    }
}