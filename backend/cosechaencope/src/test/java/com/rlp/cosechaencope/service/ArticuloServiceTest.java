package com.rlp.cosechaencope.service;

import java.math.BigDecimal;
import java.util.List;
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

import com.rlp.cosechaencope.dto.request.ArticuloRequest;
import com.rlp.cosechaencope.dto.response.ArticuloResponse;
import com.rlp.cosechaencope.exception.ResourceNotFoundException;
import com.rlp.cosechaencope.model.Articulo;
import com.rlp.cosechaencope.model.Categoria;
import com.rlp.cosechaencope.model.Productor;
import com.rlp.cosechaencope.repository.ArticuloRepository;
import com.rlp.cosechaencope.repository.CategoriaRepository;
import com.rlp.cosechaencope.repository.ProductorRepository;

@ExtendWith(MockitoExtension.class)
public class ArticuloServiceTest {

    @Mock
    private ArticuloRepository articuloRepository;
    @Mock
    private CategoriaRepository categoriaRepository;
    @Mock
    private ProductorRepository productorRepository;

    @InjectMocks
    private ArticuloService articuloService;

    private ArticuloRequest articuloRequest;
    private Categoria categoria;
    private Productor productor;
    private Articulo articulo;

    @BeforeEach
    void setUp() {
        articuloRequest = new ArticuloRequest();
        articuloRequest.setNombre("Tomate");
        articuloRequest.setDescripcion("Fruta fresca");
        articuloRequest.setPrecio(new BigDecimal("10.00"));
        articuloRequest.setStock(100);
        articuloRequest.setImagenUrl("http://img.com/tomate.jpg");
        articuloRequest.setIdCategoria(1L);
        articuloRequest.setIdProductor(2L);

        categoria = new Categoria();
        categoria.setIdCategoria(1L);
        categoria.setNombre("Frutas");

        productor = new Productor();
        productor.setIdProductor(2L);
        productor.setNombre("Productor X");

        articulo = new Articulo();
        articulo.setIdArticulo(1L);
        articulo.setNombre("Tomate");
        articulo.setDescripcion("Fruta fresca");
        articulo.setPrecio(new BigDecimal("10.00"));
        articulo.setStock(100);
        articulo.setImagenUrl("http://img.com/tomate.jpg");
        articulo.setCategoria(categoria);
        articulo.setProductor(productor);
    }

    @Test
    void crearArticulos_deberiaCrearArticuloCuandoDatosValidos() {
        // Arrange
        when(categoriaRepository.findById(1L)).thenReturn(Optional.of(categoria));
        when(productorRepository.findById(2L)).thenReturn(Optional.of(productor));
        when(articuloRepository.save(any(Articulo.class))).thenAnswer(invocation -> {
            Articulo a = invocation.getArgument(0);
            a.setIdArticulo(10L); // Simula ID generado
            return a;
        });

        // Act
        ArticuloResponse response = articuloService.crearArticulos(articuloRequest);

        // Assert
        assertThat(response.getNombre()).isEqualTo("Tomate");
        assertThat(response.getIdArticulo()).isEqualTo(10L);
        verify(categoriaRepository).findById(1L);
        verify(productorRepository).findById(2L);
        verify(articuloRepository).save(any(Articulo.class));
    }

    @Test
    void crearArticulos_deberiaLanzarExcepcionSiCategoriaNoExiste() {
        // Arrange
        when(categoriaRepository.findById(1L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> articuloService.crearArticulos(articuloRequest))
            .isInstanceOf(ResourceNotFoundException.class)
            .hasMessageContaining("Categoría no encontrada");

        verify(categoriaRepository).findById(1L);
        verifyNoMoreInteractions(productorRepository, articuloRepository);
    }

    @Test
    void obtenerArticuloPorId_deberiaRetornarArticuloCuandoExiste() {
        // 2. Configuramos el mock para simular que el artículo existe
        when(articuloRepository.findById(1L)).thenReturn(Optional.of(articulo));

        // 3. Ejecutamos el método a testear
        ArticuloResponse response = articuloService.obtenerArticuloPorId(1L);

        // 4. Verificamos el resultado esperado
        assertThat(response.getIdArticulo()).isEqualTo(1L);
        assertThat(response.getNombre()).isEqualTo("Tomate");

        // 5. Verificamos que el repositorio fue llamado correctamente
        verify(articuloRepository).findById(1L);
    }

    @Test
    void obtenerArticuloPorId_deberiaLanzarExcepcionSiNoExiste() {
        // 1. Simulamos que el artículo no existe
        when(articuloRepository.findById(99L)).thenReturn(Optional.empty());

        // 2. Ejecutamos el método esperando una excepción
        assertThatThrownBy(() -> articuloService.obtenerArticuloPorId(99L))
            .isInstanceOf(ResourceNotFoundException.class)
            .hasMessageContaining("Artículo no encontrado");

        // 3. Verificamos que el repositorio fue llamado correctamente
        verify(articuloRepository).findById(99L);
    }

    @Test
    void obtenerTodosLosArticulos_deberiaRetornarListaDeArticulos() {
        // Arrange: el mock devuelve una lista con un artículo de ejemplo
        when(articuloRepository.findAll()).thenReturn(List.of(articulo));

        // Act: llama al método del servicio
        List<ArticuloResponse> articulos = articuloService.obtenerTodosLosArticulos();

        // Assert: la lista no está vacía y el primer artículo tiene el nombre esperado
        assertThat(articulos).isNotEmpty();
        assertThat(articulos.get(0).getNombre()).isEqualTo("Tomate");

        // Verifica que el repositorio fue consultado
        verify(articuloRepository).findAll();
    }

    @Test
    void obtenerTodosLosArticulos_deberiaRetornarListaVaciaSiNoHayArticulos() {
        // Arrange
        when(articuloRepository.findAll()).thenReturn(List.of());

        // Act
        List<ArticuloResponse> articulos = articuloService.obtenerTodosLosArticulos();

        // Assert
        assertThat(articulos).isEmpty();
        verify(articuloRepository).findAll();
    }

    @Test
    void actualizarArticulo_deberiaActualizarArticuloExistente() {
        // Arrange
        ArticuloRequest updateRequest = new ArticuloRequest();
        updateRequest.setNombre("Tomate Orgánico");
        updateRequest.setDescripcion("Fruta fresca orgánica");
        updateRequest.setPrecio(new BigDecimal("12.00"));
        updateRequest.setStock(80);
        updateRequest.setImagenUrl("http://img.com/tomate_organico.jpg");
        updateRequest.setIdCategoria(1L);

        when(articuloRepository.findById(1L)).thenReturn(Optional.of(articulo));
        when(categoriaRepository.findById(1L)).thenReturn(Optional.of(categoria));
        when(articuloRepository.save(any(Articulo.class))).thenReturn(articulo);

        // Act
        ArticuloResponse response = articuloService.actualizarArticulo(1L, updateRequest);

        // Assert
        assertThat(response.getNombre()).isEqualTo("Tomate Orgánico");
        assertThat(response.getPrecio()).isEqualTo(new BigDecimal("12.00"));
        assertThat(response.getStock()).isEqualTo(80);
        verify(articuloRepository).findById(1L);
        verify(categoriaRepository).findById(1L);
        verify(articuloRepository).save(any(Articulo.class));
    }

    @Test
    void actualizarArticulo_deberiaLanzarExcepcionSiArticuloNoExiste() {
                // Arrange
        ArticuloRequest updateRequest = new ArticuloRequest();
        updateRequest.setNombre("Tomate Orgánico");
        updateRequest.setDescripcion("Fruta fresca orgánica");
        updateRequest.setPrecio(new BigDecimal("12.00"));
        updateRequest.setStock(80);
        updateRequest.setImagenUrl("http://img.com/tomate_organico.jpg");
        updateRequest.setIdCategoria(1L);

        when(articuloRepository.findById(1L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> articuloService.actualizarArticulo(1L, updateRequest))
            .isInstanceOf(ResourceNotFoundException.class)
            .hasMessageContaining("Artículo no encontrado");
    }

    @Test
    void eliminarArticulo_deberiaEliminarArticuloExistente() {
        // Arrange
        when(articuloRepository.findById(1L)).thenReturn(Optional.of(articulo));

        // Act
        articuloService.eliminarArticulo(1L);

        // Assert
        verify(articuloRepository).findById(1L);
        verify(articuloRepository).delete(articulo);
    }

    @Test
    void eliminarArticulo_deberiaLanzarExcepcionSiArticuloNoExiste() {
        // Arrange
        when(articuloRepository.findById(1L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> articuloService.eliminarArticulo(1L))
            .isInstanceOf(ResourceNotFoundException.class)
            .hasMessageContaining("Artículo no encontrado");

        verify(articuloRepository).findById(1L);
    }
}

