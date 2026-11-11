package com.petsalud.backendpetsalud;

import com.petsalud.backendpetsalud.entity.Usuario;
import com.petsalud.backendpetsalud.repository.UsuarioRepository;
import com.petsalud.backendpetsalud.service.UsuarioServiceImpl;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class) // Activa Mockito en JUnit 5
class UsuarioServiceImplTest {

    @Mock
    private UsuarioRepository usuarioRepository; // Mock del repositorio

    @InjectMocks
    private UsuarioServiceImpl usuarioService; // El servicio real, pero con el mock inyectado

    private Usuario usuario;

    @BeforeEach
    void setUp() {
        // Arrange común: creamos un usuario de prueba
        usuario = new Usuario();
        usuario.setEmail("juan@petsalud.cl");
        usuario.setNombre("Juan");
        usuario.setApellido("Pérez");
        usuario.setPassword("123456");
    }

    @Test
    void registrarUsuario_cuandoEmailNoExiste_debeGuardarYRetornarUsuario() {
        // Arrange
        when(usuarioRepository.findByEmail(usuario.getEmail()))
                .thenReturn(Optional.empty()); // Simulamos que el email NO existe

        when(usuarioRepository.save(any(Usuario.class)))
                .thenReturn(usuario); // Simulamos que el save funciona

        // Act
        Usuario resultado = usuarioService.registrarUsuario(usuario);

        // Assert
        assertNotNull(resultado);
        assertEquals("juan@petsalud.cl", resultado.getEmail());
        verify(usuarioRepository).save(usuario); // Verificamos que se llamó al save
    }

    @Test
    void registrarUsuario_cuandoEmailYaExiste_debeLanzarExcepcion() {
        // Arrange
        when(usuarioRepository.findByEmail(usuario.getEmail()))
                .thenReturn(Optional.of(usuario)); // Simulamos que el email SÍ existe

        // Act & Assert
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> usuarioService.registrarUsuario(usuario));

        assertEquals("El email ya está registrado", exception.getMessage());
        verify(usuarioRepository, never()).save(any()); // Verificamos que NO se guardó
    }
}