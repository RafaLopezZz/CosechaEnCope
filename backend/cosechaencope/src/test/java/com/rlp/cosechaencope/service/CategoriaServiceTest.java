package com.rlp.cosechaencope.service;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;

import com.rlp.cosechaencope.dto.request.CategoriaRequest;
import com.rlp.cosechaencope.dto.response.CategoriaResponse;
import com.rlp.cosechaencope.exception.CategoryNameAlreadyExistsException;
import com.rlp.cosechaencope.model.Categoria;
import com.rlp.cosechaencope.repository.CategoriaRepository;

@ExtendWith(MockitoExtension.class)
public class CategoriaServiceTest {

    @Mock
    private CategoriaRepository categoriaRepository;

    @InjectMocks
    private CategoriaService categoriaService;

    private CategoriaRequest categoriaRequest;
    private Categoria categoria;

    @BeforeEach
    void setUp() {
        // Inicializar el DTO de petición con datos de prueba
        categoriaRequest = new CategoriaRequest();
        categoriaRequest.setIdCategoria(1L); // Simular ID para pruebas
        categoriaRequest.setNombre("Frutas");
        categoriaRequest.setDescripcion("Frutas frescas y saludables");

        categoria = new Categoria();
        categoria.setIdCategoria(1L); // Simular ID para pruebas
        categoria.setNombre("Frutas");
        categoria.setDescripcion("Frutas frescas y saludables");
    }

    @Test
    void crearCategoria_deberiaCrearCategoriaCorrectamente() {
        // Arrange: configurar el mock para que retorne una categoría guardada
        when(categoriaRepository.existsByNombreIgnoreCase(anyString())).thenReturn(false);
        when(categoriaRepository.save(any(Categoria.class))).thenAnswer(invocation -> {
            Categoria categoriaGuardada = invocation.getArgument(0);
            categoriaGuardada.setIdCategoria(1L); // Simular ID generado
            return categoriaGuardada;
        });
        
        // Act: llamar al método del servicio
        CategoriaResponse response = categoriaService.crearCategoria(categoriaRequest);

        // Assert: verificar que la categoría fue creada con los datos correctos
        assertThat(response.getNombre()).isEqualTo("Frutas");
        assertThat(response.getDescripcion()).isEqualTo("Frutas frescas y saludables");

        verify(categoriaRepository).existsByNombreIgnoreCase("Frutas");
        verify(categoriaRepository).save(any(Categoria.class));
    }

    @Test
    void obtenerCategoriaPorId_deberiaRetornarCategoriaExistente() {
        // Arrange
        when(categoriaRepository.findById(1L)).thenReturn(Optional.of(categoria));

        // Act
        CategoriaResponse response = categoriaService.obtenerCategoriaPorId(1L);

        // Assert
        assertThat(response.getIdCategoria()).isEqualTo(1L);
        assertThat(response.getNombre()).isEqualTo("Frutas");
        verify(categoriaRepository).findById(1L);
    }

    @Test
    void obtenerTodasLasCategorias_deberiaRetornarListaDeCategorias() {
        // Arrange: simular una lista de categorías
        when(categoriaRepository.findAll()).thenReturn(List.of(categoria));

        // Act: llamar al método del servicio
        var response = categoriaService.obtenerTodasLasCategorias();

        // Assert: verificar que la lista no esté vacía y contenga la categoría esperada
        assertThat(response).isNotEmpty();
        assertThat(response.get(0).getNombre()).isEqualTo("Frutas");

        verify(categoriaRepository).findAll();
    }

    @Test
    void actualizarCategoria_deberiaActualizarCategoriaCorrectamente() {
        // Arrange: configurar el mock para que retorne una categoría existente
        categoria = new Categoria();
        categoria.setIdCategoria(1L);
        categoria.setNombre("Frutas");
        categoria.setDescripcion("Frutas frescas");

        when(categoriaRepository.findById(1L)).thenReturn(java.util.Optional.of(categoria));
        when(categoriaRepository.save(any(Categoria.class))).thenReturn(categoria);

        // Act: llamar al método del servicio
        CategoriaResponse response = categoriaService.actualizarCategoria(1L, categoriaRequest);

        // Assert: verificar que la categoría fue actualizada correctamente
        assertThat(response.getNombre()).isEqualTo("Frutas");
        assertThat(response.getDescripcion()).isEqualTo("Frutas frescas y saludables");
    }

    @Test
    void eliminarCategoria_deberiaEliminarCategoriaExistente() {
        // Arrange: configurar el mock para que retorne una categoría existente
        when(categoriaRepository.findById(1L)).thenReturn(Optional.of(categoria));

        // Act: llamar al método del servicio
        categoriaService.eliminarCategoria(1L);

        // Assert: verificar que el repositorio fue llamado para eliminar la categoría
        verify(categoriaRepository).delete(categoria);
    }

    @Test
    void crearCategoria_deberiaLanzarExcepcionCuandoNombreDuplicado() {
        // Arrange: configurar el mock para que indique que ya existe una categoría con ese nombre
        when(categoriaRepository.existsByNombreIgnoreCase("Frutas")).thenReturn(true);

        // Act & Assert: verificar que se lanza la excepción correcta
        assertThatThrownBy(() -> categoriaService.crearCategoria(categoriaRequest))
                .isInstanceOf(CategoryNameAlreadyExistsException.class)
                .hasMessage("Ya existe una categoría con el nombre: Frutas");

        verify(categoriaRepository).existsByNombreIgnoreCase("Frutas");
    }

    @Test
    void crearCategoria_deberiaLanzarExcepcionCuandoNombreVacio() {
        // Arrange: configurar categoría con nombre vacío
        categoriaRequest.setNombre("");

        // Act & Assert: verificar que se lanza la excepción correcta
        assertThatThrownBy(() -> categoriaService.crearCategoria(categoriaRequest))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("El nombre de la categoría no puede estar vacío");
    }

    @Test
    void actualizarCategoria_deberiaActualizarCuandoNombreEsUnico() {
        // Arrange: configurar mocks para actualización exitosa
        when(categoriaRepository.findById(1L)).thenReturn(Optional.of(categoria));
        when(categoriaRepository.existsByNombreIgnoreCaseAndIdCategoriaNotEqual("Frutas", 1L)).thenReturn(false);
        when(categoriaRepository.save(any(Categoria.class))).thenReturn(categoria);

        // Act: llamar al método del servicio
        CategoriaResponse response = categoriaService.actualizarCategoria(1L, categoriaRequest);

        // Assert: verificar que la categoría fue actualizada correctamente
        assertThat(response.getNombre()).isEqualTo("Frutas");
        verify(categoriaRepository).existsByNombreIgnoreCaseAndIdCategoriaNotEqual("Frutas", 1L);
        verify(categoriaRepository).save(any(Categoria.class));
    }

    @Test
    void actualizarCategoria_deberiaLanzarExcepcionCuandoNombreDuplicado() {
        // Arrange: configurar mocks para nombre duplicado
        when(categoriaRepository.findById(1L)).thenReturn(Optional.of(categoria));
        when(categoriaRepository.existsByNombreIgnoreCaseAndIdCategoriaNotEqual("Frutas", 1L)).thenReturn(true);

        // Act & Assert: verificar que se lanza la excepción correcta
        assertThatThrownBy(() -> categoriaService.actualizarCategoria(1L, categoriaRequest))
                .isInstanceOf(CategoryNameAlreadyExistsException.class)
                .hasMessage("Ya existe otra categoría con el nombre: Frutas");

        verify(categoriaRepository).findById(1L);
        verify(categoriaRepository).existsByNombreIgnoreCaseAndIdCategoriaNotEqual("Frutas", 1L);
    }

    @Test
    void actualizarCategoria_deberiaLanzarExcepcionCuandoNombreVacio() {
        // Arrange: configurar categoría existente y nombre vacío
        categoriaRequest.setNombre("   "); // Nombre con solo espacios
        when(categoriaRepository.findById(1L)).thenReturn(Optional.of(categoria));

        // Act & Assert: verificar que se lanza la excepción correcta
        assertThatThrownBy(() -> categoriaService.actualizarCategoria(1L, categoriaRequest))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("El nombre de la categoría no puede estar vacío");

        verify(categoriaRepository).findById(1L);
    }
}
