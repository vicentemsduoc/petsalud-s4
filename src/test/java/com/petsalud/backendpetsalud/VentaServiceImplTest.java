package com.petsalud.backendpetsalud;

import com.petsalud.backendpetsalud.entity.Producto;
import com.petsalud.backendpetsalud.entity.Usuario;
import com.petsalud.backendpetsalud.entity.Venta;
import com.petsalud.backendpetsalud.repository.ProductoRepository;
import com.petsalud.backendpetsalud.repository.UsuarioRepository;
import com.petsalud.backendpetsalud.repository.VentaRepository;
import com.petsalud.backendpetsalud.service.VentaServiceImpl;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class VentaServiceImplTest {

    @Mock
    private VentaRepository ventaRepository;

    @Mock
    private UsuarioRepository usuarioRepository;

    @Mock
    private ProductoRepository productoRepository;

    @InjectMocks
    private VentaServiceImpl ventaService;

    private Usuario usuario;
    private Producto producto1;
    private Producto producto2;

    @BeforeEach
    void setUp() {
        usuario = new Usuario();
        usuario.setId(1L);
        usuario.setNombre("Juan");

        producto1 = new Producto();
        producto1.setId(10L);
        producto1.setNombre("Alimento");
        producto1.setPrecio(new BigDecimal("5000"));
        producto1.setStock(10);

        producto2 = new Producto();
        producto2.setId(20L);
        producto2.setNombre("Juguete");
        producto2.setPrecio(new BigDecimal("3000"));
        producto2.setStock(5);
    }

    @Test
    void realizarVenta_stockTrue() {
        // Arrange
        when(usuarioRepository.findById(1L)).thenReturn(Optional.of(usuario));
        when(productoRepository.findAllById(List.of(10L, 20L)))
                .thenReturn(List.of(producto1, producto2));
        when(ventaRepository.save(any(Venta.class))).thenAnswer(inv -> inv.getArgument(0));

        // Act
        Venta resultado = ventaService.realizarVenta(1L, List.of(10L, 20L));

        // Assert
        assertNotNull(resultado);
        assertEquals(new BigDecimal("8000"), resultado.getTotal());
        assertEquals(9, producto1.getStock()); // se descontó 1
        assertEquals(4, producto2.getStock());
        verify(productoRepository, times(2)).save(any(Producto.class));
        verify(ventaRepository).save(any(Venta.class));
    }

    @Test
    void realizarVenta_usuarioFalse() {
        // Arrange
        when(usuarioRepository.findById(99L)).thenReturn(Optional.empty());

        // Act & Assert
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> ventaService.realizarVenta(99L, List.of(10L)));

        assertEquals("Usuario no encontrado", ex.getMessage());
        verify(ventaRepository, never()).save(any());
    }

    @Test
    void realizarVenta_productoFalse() {
        // Arrange
        when(usuarioRepository.findById(1L)).thenReturn(Optional.of(usuario));
        when(productoRepository.findAllById(List.of(10L, 99L)))
                .thenReturn(List.of(producto1)); // solo devuelve 1 de 2

        // Act & Assert
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> ventaService.realizarVenta(1L, List.of(10L, 99L)));

        assertEquals("Algunos productos no fueron encontrados", ex.getMessage());
        verify(ventaRepository, never()).save(any());
    }

    @Test
    void realizarVenta_stockFalse() {
        // Arrange
        producto1.setStock(0);
        when(usuarioRepository.findById(1L)).thenReturn(Optional.of(usuario));
        when(productoRepository.findAllById(List.of(10L)))
                .thenReturn(List.of(producto1));

        // Act & Assert
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> ventaService.realizarVenta(1L, List.of(10L)));

        assertTrue(ex.getMessage().contains("no tiene stock disponible"));
        verify(ventaRepository, never()).save(any());
    }
}