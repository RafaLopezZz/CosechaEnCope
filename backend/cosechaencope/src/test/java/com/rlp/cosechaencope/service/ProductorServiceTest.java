package com.rlp.cosechaencope.service;

import java.time.LocalDateTime;
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
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;

import com.rlp.cosechaencope.dto.request.ProductorRequest;
import com.rlp.cosechaencope.dto.response.ProductorResponse;
import com.rlp.cosechaencope.exception.ResourceNotFoundException;
import com.rlp.cosechaencope.model.Productor;
import com.rlp.cosechaencope.model.Usuario;
import com.rlp.cosechaencope.repository.ProductorRepository;

@ExtendWith(MockitoExtension.class)
public class ProductorServiceTest {

    @Mock
    private ProductorRepository productorRepository;

    @InjectMocks
    private ProductorService productorService;

    private Usuario usuario;
    private Productor productor;
    private ProductorRequest productorRequest;

    @BeforeEach
    void setUp() {
        usuario = new Usuario();
        usuario.setIdUsuario(1L);
        usuario.setEmail("prod@test.com");
        usuario.setRol("USER");
        usuario.setTipoUsuario("PRODUCTOR");

        productor = new Productor();
        productor.setIdProductor(22L);
        productor.setNombre("Tomás productor");
        productor.setDireccion("Avda. Gran Vía 456");
        productor.setTelefono("666888777");
        productor.setImagenUrl("http://img.com/prod.jpg");
        productor.setFechaRegistro(LocalDateTime.of(2023, 8, 20, 12, 0));
        productor.setUsuario(usuario);

        productorRequest = new ProductorRequest();
        productorRequest.setNombre("productor Actualizado");
        productorRequest.setDireccion("Calle Nueva 789");
        productorRequest.setTelefono("699888444");
        productorRequest.setImagenUrl("http://img.com/prod2.jpg");
    }

    @Test
    void crearProductor_conDatos_deberiaGuardarProductor() {
        productorService.crearProductor(usuario, productorRequest);

        verify(productorRepository).save(any(Productor.class));
    }

    @Test
    void crearProductor_conRequestNulo_deberiaGuardarProductorPorDefecto() {
        productorService.crearProductor(usuario, null);

        verify(productorRepository).save(any(Productor.class));
    }

    @Test
    void obtenerProductorPorUsuarioId_existente_deberiaRetornarProductorResponse() {
        when(productorRepository.findById(22L)).thenReturn(Optional.of(productor));

        ProductorResponse response = productorService.obtenerProductorPorUsuarioId(22L);

        assertThat(response).isNotNull();
        assertThat(response.getIdProductor()).isEqualTo(22L);
        assertThat(response.getNombre()).isEqualTo("Tomás productor");
        verify(productorRepository).findById(22L);
    }

    @Test
    void obtenerProductorPorUsuarioId_noExistente_deberiaLanzarExcepcion() {
        when(productorRepository.findById(60L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> productorService.obtenerProductorPorUsuarioId(60L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Productor no encontrado");
    }

    @Test
    void listarProductores_deberiaRetornarListaDeProductorResponse() {
        when(productorRepository.findAll()).thenReturn(List.of(productor));

        List<ProductorResponse> result = productorService.listarProductores();

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getNombre()).isEqualTo("Tomás productor");
        verify(productorRepository).findAll();
    }

    @Test
    void listarProductores_sinProductores_deberiaRetornarListaVacia() {
        when(productorRepository.findAll()).thenReturn(List.of());

        List<ProductorResponse> result = productorService.listarProductores();

        assertThat(result).isEmpty();
        verify(productorRepository).findAll();
    }

    @Test
    void actualizarProductor_existente_deberiaActualizarYRetornarProductorResponse() {
        when(productorRepository.findByUsuario_IdUsuario(1L)).thenReturn(Optional.of(productor));
        when(productorRepository.save(any(Productor.class))).thenAnswer(inv -> inv.getArgument(0));

        ProductorResponse response = productorService.actualizarProductor(1L, productorRequest);

        assertThat(response.getNombre()).isEqualTo("productor Actualizado");
        assertThat(response.getDireccion()).isEqualTo("Calle Nueva 789");
        assertThat(response.getImagenUrl()).isEqualTo("http://img.com/prod2.jpg");
        verify(productorRepository).findByUsuario_IdUsuario(1L);
        verify(productorRepository).save(any(Productor.class));
    }

    @Test
    void actualizarProductor_noExistente_deberiaLanzarExcepcion() {
        when(productorRepository.findByUsuario_IdUsuario(9L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> productorService.actualizarProductor(9L, productorRequest))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Productor no encontrado");
    }

    @Test
    void eliminarProductor_existente_deberiaEliminarProductor() {
        when(productorRepository.findByUsuario_IdUsuario(1L)).thenReturn(Optional.of(productor));
        doNothing().when(productorRepository).delete(productor);

        productorService.eliminarProductor(1L);

        verify(productorRepository).findByUsuario_IdUsuario(1L);
        verify(productorRepository).delete(productor);
    }

    @Test
    void eliminarProductor_noExistente_deberiaLanzarExcepcion() {
        when(productorRepository.findByUsuario_IdUsuario(8L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> productorService.eliminarProductor(8L))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("productor no encontrado");
    }

}
