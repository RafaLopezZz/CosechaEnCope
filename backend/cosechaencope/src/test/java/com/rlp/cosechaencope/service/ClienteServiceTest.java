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

import com.rlp.cosechaencope.dto.request.ClienteRequest;
import com.rlp.cosechaencope.dto.response.ClienteResponse;
import com.rlp.cosechaencope.exception.ResourceNotFoundException;
import com.rlp.cosechaencope.model.Cliente;
import com.rlp.cosechaencope.model.Usuario;
import com.rlp.cosechaencope.repository.ClienteRepository;
import com.rlp.cosechaencope.repository.UsuarioRepository;

@ExtendWith(MockitoExtension.class)
public class ClienteServiceTest {

    @Mock
    private ClienteRepository clienteRepository;

    @Mock
    private UsuarioRepository usuarioRepository;

    @InjectMocks
    private ClienteService clienteService;

    private Cliente cliente;
    private Usuario usuario;
    private ClienteRequest clienteRequest;

    @BeforeEach
    void setUp() {
        usuario = new Usuario();
        usuario.setIdUsuario(1L);
        usuario.setEmail("cliente@test.com");
        usuario.setRol("USER");
        usuario.setTipoUsuario("CLIENTE");

        cliente = new Cliente();
        cliente.setIdCliente(10L);
        cliente.setNombre("Juan Cliente");
        cliente.setTelefono("600111222");
        cliente.setDireccion("Calle Prueba 123");
        cliente.setFechaRegistro(LocalDateTime.of(2023, 3, 1, 10, 0));
        cliente.setUsuario(usuario);

        clienteRequest = new ClienteRequest();
        clienteRequest.setNombre("Nuevo Nombre");
        clienteRequest.setTelefono("617000444");
        clienteRequest.setDireccion("Nueva dirección");
    }

    @Test
    void crearCliente_conDatos_deberiaGuardarCliente() {
        // Testea que se guarda el cliente correctamente cuando el request tiene datos
        clienteService.crearCliente(usuario, clienteRequest);

        verify(clienteRepository).save(any(Cliente.class));
    }

    @Test
    void crearCliente_conRequestNulo_deberiaGuardarClientePorDefecto() {
        // Testea que si el request es nulo, igual se guarda un cliente (con datos por defecto)
        clienteService.crearCliente(usuario, null);

        verify(clienteRepository).save(any(Cliente.class));
    }

    @Test
    void obtenerClientePorUsuarioId_existente_deberiaRetornarClienteResponse() {
        when(clienteRepository.findByUsuario_IdUsuario(1L)).thenReturn(Optional.of(cliente));

        ClienteResponse response = clienteService.obtenerClientePorUsuarioId(1L);

        assertThat(response).isNotNull();
        assertThat(response.getIdCliente()).isEqualTo(10L);
        assertThat(response.getNombre()).isEqualTo("Juan Cliente");
        verify(clienteRepository).findByUsuario_IdUsuario(1L);
    }

    @Test
    void obtenerClientePorUsuarioId_noExistente_deberiaLanzarResourceNotFoundException() {
        when(clienteRepository.findByUsuario_IdUsuario(2L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> clienteService.obtenerClientePorUsuarioId(2L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Cliente no encontrado");
    }

    @Test
    void listarClientes_deberiaRetornarListaDeClienteResponse() {
        when(clienteRepository.findAll()).thenReturn(List.of(cliente));

        List<ClienteResponse> lista = clienteService.listarClientes();
        assertThat(lista).hasSize(1);
        assertThat(lista.get(0).getNombre()).isEqualTo("Juan Cliente");
        verify(clienteRepository).findAll();
    }

    @Test
    void listarClientes_sinClientes_deberiaRetornarListaVacia() {
        when(clienteRepository.findAll()).thenReturn(List.of());

        List<ClienteResponse> lista = clienteService.listarClientes();
        assertThat(lista).isEmpty();
    }

    @Test
    void actualizarCliente_existente_deberiaActualizarDatosYRetornarResponse() {
        when(clienteRepository.findByUsuario_IdUsuario(1L)).thenReturn(Optional.of(cliente));
        when(clienteRepository.save(any(Cliente.class))).thenAnswer(inv -> inv.getArgument(0));

        ClienteResponse response = clienteService.actualizarCliente(1L, clienteRequest);

        assertThat(response.getNombre()).isEqualTo("Nuevo Nombre");
        assertThat(response.getTelefono()).isEqualTo("617000444");
        assertThat(response.getDireccion()).isEqualTo("Nueva dirección");
        verify(clienteRepository).findByUsuario_IdUsuario(1L);
        verify(clienteRepository).save(any(Cliente.class));
    }

    @Test
    void actualizarCliente_noExistente_deberiaLanzarResourceNotFoundException() {
        when(clienteRepository.findByUsuario_IdUsuario(99L)).thenReturn(Optional.empty());

        ClienteRequest req = new ClienteRequest();
        req.setNombre("No importa");

        assertThatThrownBy(() -> clienteService.actualizarCliente(99L, req))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void eliminarCliente_existente_deberiaEliminarCliente() {
        when(clienteRepository.findById(10L)).thenReturn(Optional.of(cliente));
        doNothing().when(clienteRepository).delete(cliente);

        clienteService.eliminarCliente(10L);

        verify(clienteRepository).findById(10L);
        verify(clienteRepository).delete(cliente);
    }

    @Test
    void eliminarCliente_noExistente_deberiaLanzarResourceNotFoundException() {
        when(clienteRepository.findById(66L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> clienteService.eliminarCliente(66L))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void findByEmail_clienteValido_deberiaRetornarCliente() {
        usuario.setCliente(cliente);
        when(usuarioRepository.findByEmail("cliente@test.com")).thenReturn(Optional.of(usuario));

        Cliente result = clienteService.findByEmail("cliente@test.com");

        assertThat(result).isEqualTo(cliente);
    }

    @Test
    void findByEmail_usuarioNoExiste_deberiaLanzarResourceNotFoundException() {
        when(usuarioRepository.findByEmail("noexiste@test.com")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> clienteService.findByEmail("noexiste@test.com"))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Usuario no encontrado");
    }

    @Test
    void findByEmail_tipoUsuarioNoCliente_deberiaLanzarResourceNotFoundException() {
        usuario.setTipoUsuario("PRODUCTOR");
        when(usuarioRepository.findByEmail("prod@test.com")).thenReturn(Optional.of(usuario));

        assertThatThrownBy(() -> clienteService.findByEmail("prod@test.com"))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("no es un cliente");
    }

    @Test
    void findByEmail_usuarioSinCliente_deberiaLanzarResourceNotFoundException() {
        usuario.setTipoUsuario("CLIENTE");
        usuario.setCliente(null);
        when(usuarioRepository.findByEmail("cliente@test.com")).thenReturn(Optional.of(usuario));

        assertThatThrownBy(() -> clienteService.findByEmail("cliente@test.com"))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("No existe un cliente asociado");
    }

}
