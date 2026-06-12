package com.minimarket.mscategorias.service;

import com.minimarket.mscategorias.dto.CategoriaRequestDTO;
import com.minimarket.mscategorias.dto.CategoriaResponseDTO;
import com.minimarket.mscategorias.exception.CategoriaDuplicadaException;
import com.minimarket.mscategorias.exception.RecursoNoEncontradoException;
import com.minimarket.mscategorias.model.Categoria;
import com.minimarket.mscategorias.repository.CategoriaRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Tests unitarios de {@link CategoriaService}.
 * Mockea CategoriaRepository — sin BD, sin contexto Spring.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("CategoriaService - Tests unitarios")
class CategoriaServiceTest {

    @Mock
    private CategoriaRepository categoriaRepository;

    @InjectMocks
    private CategoriaService categoriaService;

    @Nested
    @DisplayName("listarTodas()")
    class ListarTodas {
        @Test
        @DisplayName("Debe mapear todas las categorías a DTO")
        void listarTodas_devuelveLista() {
            // GIVEN
            when(categoriaRepository.findAll())
                    .thenReturn(List.of(crearCategoria(1L, "Bebidas", "BEB-001", true)));
            // WHEN
            List<CategoriaResponseDTO> resultado = categoriaService.listarTodas();
            // THEN
            assertEquals(1, resultado.size());
            verify(categoriaRepository).findAll();
        }
    }

    @Nested
    @DisplayName("listarActivas()")
    class ListarActivas {
        @Test
        @DisplayName("Debe devolver solo categorías activas")
        void listarActivas_filtraInactivas() {
            // GIVEN
            when(categoriaRepository.findByActivaTrue())
                    .thenReturn(List.of(crearCategoria(1L, "Lácteos", "LAC-001", true)));
            // WHEN
            List<CategoriaResponseDTO> resultado = categoriaService.listarActivas();
            // THEN
            assertEquals(1, resultado.size());
            assertTrue(resultado.get(0).getActiva());
            verify(categoriaRepository).findByActivaTrue();
            verify(categoriaRepository, never()).findAll();
        }
    }

    @Nested
    @DisplayName("obtenerPorId()")
    class ObtenerPorId {
        @Test
        @DisplayName("Debe devolver DTO cuando la categoría existe")
        void obtenerPorId_existente() {
            // GIVEN
            when(categoriaRepository.findById(1L))
                    .thenReturn(Optional.of(crearCategoria(1L, "Snacks", "SNA-001", true)));
            // WHEN
            CategoriaResponseDTO resultado = categoriaService.obtenerPorId(1L);
            // THEN
            assertEquals("Snacks", resultado.getNombre());
            verify(categoriaRepository).findById(1L);
        }

        @Test
        @DisplayName("Debe lanzar RecursoNoEncontradoException cuando no existe")
        void obtenerPorId_inexistente() {
            // GIVEN
            when(categoriaRepository.findById(99L)).thenReturn(Optional.empty());
            // WHEN + THEN
            assertThrows(RecursoNoEncontradoException.class,
                    () -> categoriaService.obtenerPorId(99L));
        }
    }

    @Nested
    @DisplayName("crear()")
    class Crear {
        @Test
        @DisplayName("Debe crear categoría cuando nombre y código son únicos")
        void crear_datosValidos() {
            // GIVEN
            CategoriaRequestDTO dto = crearDTO("Limpieza", "LIM-001", true);
            when(categoriaRepository.existsByNombreIgnoreCase("Limpieza")).thenReturn(false);
            when(categoriaRepository.existsByCodigo("LIM-001")).thenReturn(false);
            when(categoriaRepository.save(any(Categoria.class)))
                    .thenAnswer(inv -> { Categoria c = inv.getArgument(0); c.setId(5L); return c; });
            // WHEN
            CategoriaResponseDTO resultado = categoriaService.crear(dto);
            // THEN
            assertEquals(5L, resultado.getId());
            verify(categoriaRepository).save(any(Categoria.class));
        }

        @Test
        @DisplayName("Debe marcar activa=true cuando el DTO no especifica activa")
        void crear_activaNull_defaultTrue() {
            // GIVEN
            CategoriaRequestDTO dto = crearDTO("X", "X-001", null);
            when(categoriaRepository.existsByNombreIgnoreCase("X")).thenReturn(false);
            when(categoriaRepository.existsByCodigo("X-001")).thenReturn(false);

            ArgumentCaptor<Categoria> captor = ArgumentCaptor.forClass(Categoria.class);
            when(categoriaRepository.save(captor.capture())).thenAnswer(inv -> inv.getArgument(0));
            // WHEN
            categoriaService.crear(dto);
            // THEN
            assertTrue(captor.getValue().getActiva());
        }

        @Test
        @DisplayName("Debe lanzar CategoriaDuplicadaException si el nombre ya existe")
        void crear_nombreDuplicado() {
            // GIVEN
            CategoriaRequestDTO dto = crearDTO("Bebidas", "BEB-002", true);
            when(categoriaRepository.existsByNombreIgnoreCase("Bebidas")).thenReturn(true);
            // WHEN + THEN
            assertThrows(CategoriaDuplicadaException.class,
                    () -> categoriaService.crear(dto));
            verify(categoriaRepository, never()).save(any());
        }

        @Test
        @DisplayName("Debe lanzar CategoriaDuplicadaException si el código ya existe")
        void crear_codigoDuplicado() {
            // GIVEN
            CategoriaRequestDTO dto = crearDTO("Frutas", "FRU-001", true);
            when(categoriaRepository.existsByNombreIgnoreCase("Frutas")).thenReturn(false);
            when(categoriaRepository.existsByCodigo("FRU-001")).thenReturn(true);
            // WHEN + THEN
            assertThrows(CategoriaDuplicadaException.class,
                    () -> categoriaService.crear(dto));
            verify(categoriaRepository, never()).save(any());
        }
    }

    @Nested
    @DisplayName("actualizar()")
    class Actualizar {
        @Test
        @DisplayName("Debe actualizar campos cuando nombre y código no cambian")
        void actualizar_sinCambioDeUnicos() {
            // GIVEN
            Categoria existente = crearCategoria(1L, "Bebidas", "BEB-001", true);
            CategoriaRequestDTO dto = crearDTO("Bebidas", "BEB-001", true);
            dto.setDescripcion("Nueva descripcion");
            when(categoriaRepository.findById(1L)).thenReturn(Optional.of(existente));
            when(categoriaRepository.save(any(Categoria.class))).thenAnswer(inv -> inv.getArgument(0));
            // WHEN
            CategoriaResponseDTO resultado = categoriaService.actualizar(1L, dto);
            // THEN: nunca consulta existsByNombre/Codigo si no cambiaron
            assertEquals("Nueva descripcion", resultado.getDescripcion());
            verify(categoriaRepository, never()).existsByNombreIgnoreCase(any());
            verify(categoriaRepository, never()).existsByCodigo(any());
        }

        @Test
        @DisplayName("Debe lanzar excepción si el nuevo nombre ya pertenece a otra categoría")
        void actualizar_nombreNuevoDuplicado() {
            // GIVEN
            Categoria existente = crearCategoria(1L, "Bebidas", "BEB-001", true);
            CategoriaRequestDTO dto = crearDTO("Lácteos", "BEB-001", true);
            when(categoriaRepository.findById(1L)).thenReturn(Optional.of(existente));
            when(categoriaRepository.existsByNombreIgnoreCase("Lácteos")).thenReturn(true);
            // WHEN + THEN
            assertThrows(CategoriaDuplicadaException.class,
                    () -> categoriaService.actualizar(1L, dto));
        }

        @Test
        @DisplayName("Debe lanzar excepción si el nuevo código ya pertenece a otra categoría")
        void actualizar_codigoNuevoDuplicado() {
            // GIVEN
            Categoria existente = crearCategoria(1L, "Bebidas", "BEB-001", true);
            CategoriaRequestDTO dto = crearDTO("Bebidas", "OTRO-999", true);
            when(categoriaRepository.findById(1L)).thenReturn(Optional.of(existente));
            when(categoriaRepository.existsByCodigo("OTRO-999")).thenReturn(true);
            // WHEN + THEN
            assertThrows(CategoriaDuplicadaException.class,
                    () -> categoriaService.actualizar(1L, dto));
        }

        @Test
        @DisplayName("Debe lanzar RecursoNoEncontradoException si la categoría no existe")
        void actualizar_inexistente() {
            // GIVEN
            when(categoriaRepository.findById(404L)).thenReturn(Optional.empty());
            // WHEN + THEN
            assertThrows(RecursoNoEncontradoException.class,
                    () -> categoriaService.actualizar(404L, crearDTO("X", "X-001", true)));
        }

        @Test
        @DisplayName("Debe conservar activa actual cuando el DTO trae activa=null")
        void actualizar_activaNullConservaValor() {
            // GIVEN
            Categoria existente = crearCategoria(1L, "Bebidas", "BEB-001", true);
            CategoriaRequestDTO dto = crearDTO("Bebidas", "BEB-001", null);
            when(categoriaRepository.findById(1L)).thenReturn(Optional.of(existente));
            when(categoriaRepository.save(any(Categoria.class))).thenAnswer(inv -> inv.getArgument(0));
            // WHEN
            CategoriaResponseDTO resultado = categoriaService.actualizar(1L, dto);
            // THEN
            assertTrue(resultado.getActiva());
        }
    }

    @Nested
    @DisplayName("darDeBaja()")
    class DarDeBaja {
        @Test
        @DisplayName("Debe marcar activa=false (borrado lógico)")
        void darDeBaja_existente() {
            // GIVEN
            Categoria existente = crearCategoria(1L, "Bebidas", "BEB-001", true);
            when(categoriaRepository.findById(1L)).thenReturn(Optional.of(existente));
            when(categoriaRepository.save(any(Categoria.class))).thenAnswer(inv -> inv.getArgument(0));
            // WHEN
            categoriaService.darDeBaja(1L);
            // THEN
            assertFalse(existente.getActiva());
            verify(categoriaRepository).save(existente);
            verify(categoriaRepository, never()).deleteById(any());
        }

        @Test
        @DisplayName("Debe lanzar RecursoNoEncontradoException si no existe")
        void darDeBaja_inexistente() {
            when(categoriaRepository.findById(999L)).thenReturn(Optional.empty());
            assertThrows(RecursoNoEncontradoException.class, () -> categoriaService.darDeBaja(999L));
            verify(categoriaRepository, never()).save(any());
        }
    }

    @Nested
    @DisplayName("reactivar()")
    class Reactivar {
        @Test
        @DisplayName("Debe marcar activa=true")
        void reactivar_existente() {
            // GIVEN
            Categoria existente = crearCategoria(1L, "Bebidas", "BEB-001", false);
            when(categoriaRepository.findById(1L)).thenReturn(Optional.of(existente));
            when(categoriaRepository.save(any(Categoria.class))).thenAnswer(inv -> inv.getArgument(0));
            // WHEN
            CategoriaResponseDTO resultado = categoriaService.reactivar(1L);
            // THEN
            assertTrue(resultado.getActiva());
        }

        @Test
        @DisplayName("Debe lanzar RecursoNoEncontradoException si no existe")
        void reactivar_inexistente() {
            when(categoriaRepository.findById(999L)).thenReturn(Optional.empty());
            assertThrows(RecursoNoEncontradoException.class, () -> categoriaService.reactivar(999L));
        }
    }

    // ─── Helpers ───
    private Categoria crearCategoria(Long id, String nombre, String codigo, Boolean activa) {
        Categoria c = new Categoria();
        c.setId(id);
        c.setNombre(nombre);
        c.setDescripcion("desc");
        c.setCodigo(codigo);
        c.setActiva(activa);
        return c;
    }

    private CategoriaRequestDTO crearDTO(String nombre, String codigo, Boolean activa) {
        CategoriaRequestDTO dto = new CategoriaRequestDTO();
        dto.setNombre(nombre);
        dto.setDescripcion("desc");
        dto.setCodigo(codigo);
        dto.setActiva(activa);
        return dto;
    }
}
