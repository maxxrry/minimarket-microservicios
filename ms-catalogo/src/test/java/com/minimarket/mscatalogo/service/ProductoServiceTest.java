package com.minimarket.mscatalogo.service;

import com.minimarket.mscatalogo.dto.ProductoRequestDTO;
import com.minimarket.mscatalogo.dto.ProductoResponseDTO;
import com.minimarket.mscatalogo.exception.CodigoBarraDuplicadoException;
import com.minimarket.mscatalogo.exception.RecursoNoEncontradoException;
import com.minimarket.mscatalogo.model.Producto;
import com.minimarket.mscatalogo.repository.ProductoRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Tests unitarios de {@link ProductoService}.
 *
 * Características clave:
 *   - Aislamiento total: usa Mockito para mockear {@link ProductoRepository},
 *     por lo que NO requiere MySQL ni contexto de Spring.
 *   - Estructura Given-When-Then en cada test, comentada explícitamente.
 *   - Cubre los 8 métodos públicos del servicio, incluyendo las ramas de error.
 *   - Verifica tanto el VALOR DEVUELTO como las INTERACCIONES con el mock
 *     mediante verify(), asegurando que no se hagan llamadas inesperadas
 *     a la base de datos.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("ProductoService - Tests unitarios")
class ProductoServiceTest {

    @Mock
    private ProductoRepository productoRepository;

    @InjectMocks
    private ProductoService productoService;

    // ════════════════════════════════════════════════════════════
    // listarTodos()
    // ════════════════════════════════════════════════════════════

    @Nested
    @DisplayName("listarTodos()")
    class ListarTodos {

        @Test
        @DisplayName("Debe devolver todos los productos mapeados a DTO")
        void listarTodos_devuelveListaCompleta() {
            // ─── GIVEN ───
            // El repositorio retorna dos productos persistidos.
            Producto p1 = construirProducto(1L, "Coca Cola", "78001", true);
            Producto p2 = construirProducto(2L, "Pepsi", "78002", false);
            when(productoRepository.findAll()).thenReturn(List.of(p1, p2));

            // ─── WHEN ───
            List<ProductoResponseDTO> resultado = productoService.listarTodos();

            // ─── THEN ───
            // Se mapean los 2 productos y se invoca al repo exactamente una vez.
            assertEquals(2, resultado.size());
            assertEquals("Coca Cola", resultado.get(0).getNombre());
            assertEquals("Pepsi", resultado.get(1).getNombre());
            verify(productoRepository, times(1)).findAll();
            verifyNoMoreInteractions(productoRepository);
        }

        @Test
        @DisplayName("Debe devolver lista vacía cuando no hay productos")
        void listarTodos_listaVacia() {
            // GIVEN: el repo retorna lista vacía
            when(productoRepository.findAll()).thenReturn(Collections.emptyList());

            // WHEN
            List<ProductoResponseDTO> resultado = productoService.listarTodos();

            // THEN: lista vacía, sin errores
            assertTrue(resultado.isEmpty());
            verify(productoRepository).findAll();
        }
    }

    // ════════════════════════════════════════════════════════════
    // listarActivos()
    // ════════════════════════════════════════════════════════════

    @Nested
    @DisplayName("listarActivos()")
    class ListarActivos {

        @Test
        @DisplayName("Debe devolver solo los productos con activo=true")
        void listarActivos_devuelveSoloActivos() {
            // GIVEN: el repo retorna únicamente los activos
            Producto activo = construirProducto(1L, "Yogur", "78003", true);
            when(productoRepository.findByActivoTrue()).thenReturn(List.of(activo));

            // WHEN
            List<ProductoResponseDTO> resultado = productoService.listarActivos();

            // THEN: viene 1 producto y se llamó al método específico (no a findAll)
            assertEquals(1, resultado.size());
            assertTrue(resultado.get(0).getActivo());
            verify(productoRepository).findByActivoTrue();
            verify(productoRepository, never()).findAll();
        }
    }

    // ════════════════════════════════════════════════════════════
    // obtenerPorId()
    // ════════════════════════════════════════════════════════════

    @Nested
    @DisplayName("obtenerPorId()")
    class ObtenerPorId {

        @Test
        @DisplayName("Debe devolver el DTO cuando el producto existe")
        void obtenerPorId_existente_devuelveDTO() {
            // GIVEN
            Producto existente = construirProducto(10L, "Pan", "12345678", true);
            when(productoRepository.findById(10L)).thenReturn(Optional.of(existente));

            // WHEN
            ProductoResponseDTO resultado = productoService.obtenerPorId(10L);

            // THEN
            assertNotNull(resultado);
            assertEquals(10L, resultado.getId());
            assertEquals("Pan", resultado.getNombre());
            verify(productoRepository).findById(10L);
        }

        @Test
        @DisplayName("Debe lanzar RecursoNoEncontradoException cuando no existe")
        void obtenerPorId_inexistente_lanzaExcepcion() {
            // GIVEN: el repo retorna Optional vacío
            when(productoRepository.findById(999L)).thenReturn(Optional.empty());

            // WHEN + THEN: la excepción debe propagarse con mensaje claro
            RecursoNoEncontradoException ex = assertThrows(
                    RecursoNoEncontradoException.class,
                    () -> productoService.obtenerPorId(999L));

            assertTrue(ex.getMessage().contains("999"));
            verify(productoRepository).findById(999L);
        }
    }

    // ════════════════════════════════════════════════════════════
    // crear()
    // ════════════════════════════════════════════════════════════

    @Nested
    @DisplayName("crear()")
    class Crear {

        @Test
        @DisplayName("Debe crear producto exitosamente cuando el código de barra es único")
        void crear_codigoUnico_persisteYDevuelveDTO() {
            // GIVEN: no existe el código y el save retorna el producto con ID
            ProductoRequestDTO dto = construirRequestDTO("Leche", "11112222", true);
            when(productoRepository.existsByCodigoBarra("11112222")).thenReturn(false);
            when(productoRepository.save(any(Producto.class)))
                    .thenAnswer(invocation -> {
                        Producto p = invocation.getArgument(0);
                        p.setId(100L);
                        return p;
                    });

            // WHEN
            ProductoResponseDTO resultado = productoService.crear(dto);

            // THEN: el producto se guardó con los datos correctos
            assertNotNull(resultado);
            assertEquals(100L, resultado.getId());
            assertEquals("Leche", resultado.getNombre());
            assertTrue(resultado.getActivo());
            verify(productoRepository).existsByCodigoBarra("11112222");
            verify(productoRepository).save(any(Producto.class));
        }

        @Test
        @DisplayName("Debe asignar activo=true por defecto cuando el DTO no lo especifica")
        void crear_activoNull_marcaActivoTruePorDefecto() {
            // GIVEN: el DTO no trae activo (null)
            ProductoRequestDTO dto = construirRequestDTO("Arroz", "33334444", null);
            when(productoRepository.existsByCodigoBarra("33334444")).thenReturn(false);

            // Capturamos lo que efectivamente se persiste
            ArgumentCaptor<Producto> captor = ArgumentCaptor.forClass(Producto.class);
            when(productoRepository.save(captor.capture()))
                    .thenAnswer(invocation -> invocation.getArgument(0));

            // WHEN
            productoService.crear(dto);

            // THEN: el producto persistido tiene activo=true aunque el DTO trajera null
            Producto persistido = captor.getValue();
            assertTrue(persistido.getActivo(),
                    "El producto debe quedar activo por defecto cuando dto.activo es null");
        }

        @Test
        @DisplayName("Debe lanzar CodigoBarraDuplicadoException si el código ya existe")
        void crear_codigoDuplicado_lanzaExcepcion() {
            // GIVEN
            ProductoRequestDTO dto = construirRequestDTO("Galletas", "55556666", true);
            when(productoRepository.existsByCodigoBarra("55556666")).thenReturn(true);

            // WHEN + THEN
            CodigoBarraDuplicadoException ex = assertThrows(
                    CodigoBarraDuplicadoException.class,
                    () -> productoService.crear(dto));

            assertTrue(ex.getMessage().contains("55556666"));
            // Y nunca se llega a guardar nada
            verify(productoRepository, never()).save(any());
        }
    }

    // ════════════════════════════════════════════════════════════
    // actualizar()
    // ════════════════════════════════════════════════════════════

    @Nested
    @DisplayName("actualizar()")
    class Actualizar {

        @Test
        @DisplayName("Debe actualizar todos los campos cuando el código de barra no cambia")
        void actualizar_codigoIgual_actualizaCampos() {
            // GIVEN: producto existente y DTO con MISMO código
            Producto existente = construirProducto(1L, "Vino Viejo", "ABCDEFGH", true);
            ProductoRequestDTO dto = construirRequestDTO("Vino Nuevo", "ABCDEFGH", true);
            dto.setPrecio(new BigDecimal("9999.99"));

            when(productoRepository.findById(1L)).thenReturn(Optional.of(existente));
            when(productoRepository.save(any(Producto.class)))
                    .thenAnswer(invocation -> invocation.getArgument(0));

            // WHEN
            ProductoResponseDTO resultado = productoService.actualizar(1L, dto);

            // THEN: campos actualizados; NUNCA se consulta existsByCodigoBarra porque no cambió
            assertEquals("Vino Nuevo", resultado.getNombre());
            assertEquals(new BigDecimal("9999.99"), resultado.getPrecio());
            verify(productoRepository, never()).existsByCodigoBarra(any());
            verify(productoRepository).save(existente);
        }

        @Test
        @DisplayName("Debe actualizar exitosamente cuando el nuevo código de barra está libre")
        void actualizar_codigoCambiaYDisponible_actualiza() {
            // GIVEN: producto existente con código viejo; DTO con código nuevo libre
            Producto existente = construirProducto(1L, "Pan", "VIEJO123", true);
            ProductoRequestDTO dto = construirRequestDTO("Pan Integral", "NUEVO456", true);

            when(productoRepository.findById(1L)).thenReturn(Optional.of(existente));
            when(productoRepository.existsByCodigoBarra("NUEVO456")).thenReturn(false);
            when(productoRepository.save(any(Producto.class)))
                    .thenAnswer(invocation -> invocation.getArgument(0));

            // WHEN
            ProductoResponseDTO resultado = productoService.actualizar(1L, dto);

            // THEN: se valida el nuevo código y se persiste
            assertEquals("NUEVO456", resultado.getCodigoBarra());
            verify(productoRepository).existsByCodigoBarra("NUEVO456");
            verify(productoRepository).save(existente);
        }

        @Test
        @DisplayName("Debe lanzar CodigoBarraDuplicadoException cuando el nuevo código pertenece a otro producto")
        void actualizar_codigoNuevoDuplicado_lanzaExcepcion() {
            // GIVEN
            Producto existente = construirProducto(1L, "Queso", "VIEJO123", true);
            ProductoRequestDTO dto = construirRequestDTO("Queso", "OCUPADO99", true);

            when(productoRepository.findById(1L)).thenReturn(Optional.of(existente));
            when(productoRepository.existsByCodigoBarra("OCUPADO99")).thenReturn(true);

            // WHEN + THEN
            assertThrows(CodigoBarraDuplicadoException.class,
                    () -> productoService.actualizar(1L, dto));

            verify(productoRepository, never()).save(any());
        }

        @Test
        @DisplayName("Debe lanzar RecursoNoEncontradoException cuando el producto no existe")
        void actualizar_productoInexistente_lanzaExcepcion() {
            // GIVEN
            ProductoRequestDTO dto = construirRequestDTO("X", "11111111", true);
            when(productoRepository.findById(404L)).thenReturn(Optional.empty());

            // WHEN + THEN
            assertThrows(RecursoNoEncontradoException.class,
                    () -> productoService.actualizar(404L, dto));

            verify(productoRepository, never()).save(any());
        }

        @Test
        @DisplayName("Debe conservar el activo original cuando el DTO trae activo=null")
        void actualizar_activoNullEnDTO_noSobrescribeActivo() {
            // GIVEN: producto activo=true; DTO con activo=null debería NO tocar ese campo
            Producto existente = construirProducto(1L, "Aceite", "AAA11111", true);
            ProductoRequestDTO dto = construirRequestDTO("Aceite Premium", "AAA11111", null);

            when(productoRepository.findById(1L)).thenReturn(Optional.of(existente));
            when(productoRepository.save(any(Producto.class)))
                    .thenAnswer(invocation -> invocation.getArgument(0));

            // WHEN
            ProductoResponseDTO resultado = productoService.actualizar(1L, dto);

            // THEN: activo SIGUE siendo true (no se sobrescribió a null)
            assertTrue(resultado.getActivo(),
                    "Si dto.activo es null, el activo previo debe conservarse");
        }
    }

    // ════════════════════════════════════════════════════════════
    // darDeBaja()
    // ════════════════════════════════════════════════════════════

    @Nested
    @DisplayName("darDeBaja()")
    class DarDeBaja {

        @Test
        @DisplayName("Debe marcar activo=false (borrado lógico) sin eliminar de BD")
        void darDeBaja_existente_marcaInactivo() {
            // GIVEN
            Producto existente = construirProducto(5L, "Galletas", "BBB22222", true);
            when(productoRepository.findById(5L)).thenReturn(Optional.of(existente));
            when(productoRepository.save(any(Producto.class)))
                    .thenAnswer(invocation -> invocation.getArgument(0));

            // WHEN
            productoService.darDeBaja(5L);

            // THEN: el activo del objeto persistido es false; nunca se llamó a deleteById
            assertFalse(existente.getActivo());
            verify(productoRepository).save(existente);
            verify(productoRepository, never()).deleteById(any());
        }

        @Test
        @DisplayName("Debe lanzar RecursoNoEncontradoException cuando el producto no existe")
        void darDeBaja_inexistente_lanzaExcepcion() {
            // GIVEN
            when(productoRepository.findById(999L)).thenReturn(Optional.empty());

            // WHEN + THEN
            assertThrows(RecursoNoEncontradoException.class,
                    () -> productoService.darDeBaja(999L));

            verify(productoRepository, never()).save(any());
        }
    }

    // ════════════════════════════════════════════════════════════
    // reactivar()
    // ════════════════════════════════════════════════════════════

    @Nested
    @DisplayName("reactivar()")
    class Reactivar {

        @Test
        @DisplayName("Debe marcar activo=true en un producto previamente dado de baja")
        void reactivar_existente_marcaActivo() {
            // GIVEN: producto dado de baja
            Producto existente = construirProducto(7L, "Yogur Light", "CCC33333", false);
            when(productoRepository.findById(7L)).thenReturn(Optional.of(existente));
            when(productoRepository.save(any(Producto.class)))
                    .thenAnswer(invocation -> invocation.getArgument(0));

            // WHEN
            ProductoResponseDTO resultado = productoService.reactivar(7L);

            // THEN
            assertTrue(resultado.getActivo());
            verify(productoRepository).save(existente);
        }

        @Test
        @DisplayName("Debe lanzar RecursoNoEncontradoException cuando el producto no existe")
        void reactivar_inexistente_lanzaExcepcion() {
            // GIVEN
            when(productoRepository.findById(404L)).thenReturn(Optional.empty());

            // WHEN + THEN
            assertThrows(RecursoNoEncontradoException.class,
                    () -> productoService.reactivar(404L));

            verify(productoRepository, never()).save(any());
        }
    }

    // ════════════════════════════════════════════════════════════
    // listarPorCategoria()
    // ════════════════════════════════════════════════════════════

    @Nested
    @DisplayName("listarPorCategoria()")
    class ListarPorCategoria {

        @Test
        @DisplayName("Debe delegar al repositorio y mapear los productos de la categoría")
        void listarPorCategoria_delegaYMapea() {
            // GIVEN
            Producto p1 = construirProducto(1L, "Detergente", "DET00001", true);
            Producto p2 = construirProducto(2L, "Cloro", "DET00002", true);
            when(productoRepository.findByCategoriaId(5L)).thenReturn(List.of(p1, p2));

            // WHEN
            List<ProductoResponseDTO> resultado = productoService.listarPorCategoria(5L);

            // THEN
            assertEquals(2, resultado.size());
            assertEquals("Detergente", resultado.get(0).getNombre());
            verify(productoRepository).findByCategoriaId(5L);
        }
    }

    // ════════════════════════════════════════════════════════════
    // HELPERS PRIVADOS (no son tests, solo factorías para reducir ruido)
    // ════════════════════════════════════════════════════════════

    private Producto construirProducto(Long id, String nombre, String codigo, Boolean activo) {
        Producto p = new Producto();
        p.setId(id);
        p.setNombre(nombre);
        p.setDescripcion("Descripcion de " + nombre);
        p.setPrecio(new BigDecimal("1990.00"));
        p.setCodigoBarra(codigo);
        p.setCategoriaId(1L);
        p.setProveedorId(1L);
        p.setActivo(activo);
        return p;
    }

    private ProductoRequestDTO construirRequestDTO(String nombre, String codigo, Boolean activo) {
        ProductoRequestDTO dto = new ProductoRequestDTO();
        dto.setNombre(nombre);
        dto.setDescripcion("Descripcion de " + nombre);
        dto.setPrecio(new BigDecimal("1990.00"));
        dto.setCodigoBarra(codigo);
        dto.setCategoriaId(1L);
        dto.setProveedorId(1L);
        dto.setActivo(activo);
        return dto;
    }
}
