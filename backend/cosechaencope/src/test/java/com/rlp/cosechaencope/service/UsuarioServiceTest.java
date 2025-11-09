package com.rlp.cosechaencope.service;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import static org.mockito.ArgumentMatchers.any;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.rlp.cosechaencope.dto.request.ClienteRequest;
import com.rlp.cosechaencope.dto.request.ProductorRequest;
import com.rlp.cosechaencope.dto.request.UsuarioRequest;
import com.rlp.cosechaencope.dto.response.UsuarioResponse;
import com.rlp.cosechaencope.exception.ResourceNotFoundException;
import com.rlp.cosechaencope.model.Usuario;
import com.rlp.cosechaencope.repository.UsuarioRepository;

@ExtendWith(MockitoExtension.class)
class UsuarioServiceTest {

    @Mock
    private UsuarioRepository usuarioRepository;

    @Mock
    private ClienteService clienteService;

    @Mock
    private ProductorService productorService;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UsuarioService usuarioService;


    @Test
    void crearUsuario_clienteNuevo_deberiaCrearUsuarioYDelegarEnClienteService() {
        UsuarioRequest usuarioRequest = new UsuarioRequest();
        usuarioRequest.setEmail("test@cliente.com");
        usuarioRequest.setPassword("12345678");
        usuarioRequest.setTipoUsuario("CLIENTE");

        when(usuarioRepository.findByEmail("test@cliente.com")).thenReturn(Optional.empty());
        when(passwordEncoder.encode("12345678")).thenReturn("encrypted");
        when(usuarioRepository.save(any(Usuario.class))).thenAnswer(inv -> {
            Usuario u = inv.getArgument(0);
            u.setIdUsuario(1L);
            return u;
        });

        doNothing().when(clienteService).crearCliente(any(Usuario.class), any(ClienteRequest.class));

        UsuarioResponse usuarioResponse = usuarioService.crearUsuario(usuarioRequest);

        assertThat(usuarioResponse).isNotNull();
        assertThat(usuarioResponse.getEmail()).isEqualTo("test@cliente.com");
        assertThat(usuarioResponse.getIdUsuario()).isEqualTo(1L);

        verify(usuarioRepository).findByEmail("test@cliente.com");
        verify(passwordEncoder).encode("12345678");
        verify(usuarioRepository).save(any(Usuario.class));
        verify(clienteService).crearCliente(any(Usuario.class), any(ClienteRequest.class));
    }

    @Test
    void crearUsuario_productorNuevo_deberiaCrearUsuarioYDelegarEnProductorService() {
        UsuarioRequest usuarioRequest = new UsuarioRequest();
        usuarioRequest.setEmail("test@productor.es");
        usuarioRequest.setPassword("12345678");
        usuarioRequest.setTipoUsuario("PRODUCTOR");

        when(usuarioRepository.findByEmail("test@productor.es")).thenReturn(Optional.empty());
        when(passwordEncoder.encode("12345678")).thenReturn("encrypted");
        when(usuarioRepository.save(any(Usuario.class))).thenAnswer(inv -> {
            Usuario u = inv.getArgument(0);
            u.setIdUsuario(2L);
            return u;
        });

        doNothing().when(productorService).crearProductor(any(Usuario.class), any(ProductorRequest.class));

        UsuarioResponse response = usuarioService.crearUsuario(usuarioRequest);

        assertThat(response).isNotNull();
        assertThat(response.getTipoUsuario()).isEqualTo("PRODUCTOR");
        assertThat(response.getIdUsuario()).isEqualTo(2L);

        verify(productorService).crearProductor(any(Usuario.class), any(ProductorRequest.class));
    }

    @Test
    void obtenerUsuarioPorId_existente_deberiaRetornarUsuarioResponse() {
        // Preparamos un usuario simulado como estaría en la base de datos
        Usuario usuario = new Usuario();
        usuario.setIdUsuario(10L);
        usuario.setEmail("test@usuario.com");
        usuario.setRol("USER");
        usuario.setTipoUsuario("CLIENTE");

        // Configuramos el mock para devolver ese usuario cuando se consulta por id
        when(usuarioRepository.findById(10L)).thenReturn(Optional.of(usuario));

        // Llamamos al método a probar
        UsuarioResponse response = usuarioService.obtenerUsuarioPorId(10L);

        // Verificamos que la respuesta tiene los datos correctos
        assertThat(response).isNotNull();
        assertThat(response.getIdUsuario()).isEqualTo(10L);
        assertThat(response.getEmail()).isEqualTo("test@usuario.com");

        // Comprobamos que se haya hecho la consulta al repositorio una sola vez
        verify(usuarioRepository).findById(10L);
    }

    @Test
    void obtenerUsuarioPorId_noExistente_deberiaLanzarExcepcion() {
        // Configuramos el mock para simular que no existe ese usuario
        when(usuarioRepository.findById(11L)).thenReturn(Optional.empty());

        // Esperamos que se lance una excepción al intentar buscarlo
        assertThatThrownBy(() -> usuarioService.obtenerUsuarioPorId(11L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Usuario no encontrado");

        // Verificamos que realmente se intentó consultar al repositorio
        verify(usuarioRepository).findById(11L);
    }

    @Test
    void listarUsuarios_deberiaRetornarListaDeUsuarioResponse() {
        // Creamos dos usuarios simulados
        Usuario usuario1 = new Usuario();
        usuario1.setIdUsuario(1L);
        usuario1.setEmail("usuario1@test.com");
        usuario1.setRol("USER");
        usuario1.setTipoUsuario("CLIENTE");
        Usuario usuario2 = new Usuario();
        usuario2.setIdUsuario(2L);
        usuario2.setEmail("usuario2@test.com");
        usuario2.setRol("ADMIN");
        usuario2.setTipoUsuario("PRODUCTOR");

        // El mock devuelve la lista simulada al pedir todos los usuarios
        when(usuarioRepository.findAll()).thenReturn(java.util.List.of(usuario1, usuario2));

        // Llamamos al método de listar
        List<UsuarioResponse> result = usuarioService.listarUsuarios();

        // Comprobamos que la lista tiene dos elementos y que los datos coinciden
        assertThat(result).hasSize(2);
        assertThat(result.get(0).getEmail()).isEqualTo("usuario1@test.com");
        assertThat(result.get(1).getRol()).isEqualTo("ADMIN");
        verify(usuarioRepository).findAll();
    }

    @Test
    void listarUsuarios_sinUsuarios_deberiaRetornarListaVacia() {
        // Simulamos que la base de datos está vacía
        when(usuarioRepository.findAll()).thenReturn(java.util.List.of());

        // Ejecutamos el método y esperamos una lista vacía
        List<UsuarioResponse> result = usuarioService.listarUsuarios();

        // Verificamos el resultado
        assertThat(result).isEmpty();
        verify(usuarioRepository).findAll();
    }

    @Test
    void actualizarUsuario_existente_deberiaActualizarYRetornarUsuarioResponse() {
        // Usuario existente simulado
        Usuario usuario = new Usuario();
        usuario.setIdUsuario(22L);
        usuario.setEmail("old@test.com");
        usuario.setPassword("oldpass");
        usuario.setRol("USER");
        usuario.setTipoUsuario("CLIENTE");

        // Nueva información para actualizar
        UsuarioRequest actualizacion = new UsuarioRequest();
        actualizacion.setEmail("new@test.com");
        actualizacion.setPassword("newpass");
        actualizacion.setRol("ADMIN");
        actualizacion.setTipoUsuario("PRODUCTOR");

        // El mock devuelve el usuario existente al buscar
        when(usuarioRepository.findById(22L)).thenReturn(Optional.of(usuario));
        // El mock simula un guardado exitoso
        when(usuarioRepository.save(any(Usuario.class))).thenAnswer(inv -> inv.getArgument(0));

        // Ejecutamos la actualización
        UsuarioResponse response = usuarioService.actualizarUsuario(22L, actualizacion);

        // Comprobamos que los datos se hayan actualizado
        assertThat(response.getEmail()).isEqualTo("new@test.com");
        assertThat(response.getRol()).isEqualTo("USER"); // El servicio podría o no actualizar esto
        verify(usuarioRepository).findById(22L);
        verify(usuarioRepository).save(any(Usuario.class));
    }

    @Test
    void actualizarUsuario_noExistente_deberiaLanzarExcepcion() {
        UsuarioRequest actualizacion = new UsuarioRequest();
        actualizacion.setEmail("new@test.com");
        actualizacion.setPassword("newpass");

        // Simulamos que no existe el usuario
        when(usuarioRepository.findById(33L)).thenReturn(Optional.empty());

        // Esperamos una excepción
        assertThatThrownBy(() -> usuarioService.actualizarUsuario(33L, actualizacion))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Usuario no encontrado");
    }

    @Test
    void eliminarUsuario_existente_deberiaEliminarUsuario() {
        Usuario usuario = new Usuario();
        usuario.setIdUsuario(77L);
        usuario.setEmail("borrar@test.com");

        when(usuarioRepository.findById(77L)).thenReturn(Optional.of(usuario));
        doNothing().when(usuarioRepository).delete(usuario);

        usuarioService.eliminarUsuario(77L);

        verify(usuarioRepository).findById(77L);
        verify(usuarioRepository).delete(usuario);
    }

    @Test
    void eliminarUsuario_noExistente_deberiaLanzarExcepcion() {
        when(usuarioRepository.findById(88L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> usuarioService.eliminarUsuario(88L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Usuario no encontrado");
    }

    @Test
    void crearSuperAdmin_noExiste_deberiaCrearSuperAdmin() {
        // Simulamos que el superadmin aún no existe
        when(usuarioRepository.existsByEmail("ra@cec.com")).thenReturn(false);
        when(passwordEncoder.encode("12345678")).thenReturn("encrypted");
        when(usuarioRepository.save(any(Usuario.class))).thenReturn(new Usuario());

        usuarioService.crearSuperAdmin();

        verify(usuarioRepository).existsByEmail("ra@cec.com");
        verify(passwordEncoder).encode("12345678");
        verify(usuarioRepository).save(any(Usuario.class));
    }

    @Test
    void crearSuperAdmin_yaExiste_noDebeCrearDeNuevo() {
        // Simulamos que el superadmin ya existe
        when(usuarioRepository.existsByEmail("ra@cec.com")).thenReturn(true);

        usuarioService.crearSuperAdmin();

        // Verificamos que no se intente guardar un nuevo usuario
        verify(usuarioRepository).existsByEmail("ra@cec.com");
         verify(usuarioRepository, never()).save(any(Usuario.class));
    }

}
