package com.minimarket.mspromociones.service;

import com.minimarket.mspromociones.dto.PromocionRequestDTO;
import com.minimarket.mspromociones.dto.PromocionResponseDTO;
import com.minimarket.mspromociones.exception.PromocionInvalidaException;
import com.minimarket.mspromociones.exception.RecursoNoEncontradoException;
import com.minimarket.mspromociones.model.Promocion;
import com.minimarket.mspromociones.repository.PromocionRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("PromocionService - Tests unitarios")
class PromocionServiceTest {

    @Mock private PromocionRepository promocionRepository;
    @InjectMocks private PromocionService promocionService;

    private static final LocalDateTime INICIO = LocalDateTime.of(2026, 1, 1, 0, 0);
    private static final LocalDateTime FIN = LocalDateTime.of(2026, 12, 31, 23, 59);

    @Nested @DisplayName("listarTodos()")
    class ListarTodos {
        @Test @DisplayName("Mapea todas")
        void ok() {
            when(promocionRepository.findAll()).thenReturn(List.of(crear(1L, 10L, null, true)));
            assertEquals(1, promocionService.listarTodos().size());
        }
    }

    @Nested @DisplayName("listarActivas()")
    class ListarActivas {
        @Test @DisplayName("Solo activas")
        void ok() {
            when(promocionRepository.findByActivoTrue()).thenReturn(List.of(crear(1L, 10L, null, true)));
            assertEquals(1, promocionService.listarActivas().size());
        }
    }

    @Nested @DisplayName("obtenerPorId()")
    class ObtenerPorId {
        @Test @DisplayName("Existe")
        void ok() {
            when(promocionRepository.findById(1L)).thenReturn(Optional.of(crear(1L, 10L, null, true)));
            assertEquals(1L, promocionService.obtenerPorId(1L).getId());
        }
        @Test @DisplayName("No existe → excepción")
        void noExiste() {
            when(promocionRepository.findById(99L)).thenReturn(Optional.empty());
            assertThrows(RecursoNoEncontradoException.class, () -> promocionService.obtenerPorId(99L));
        }
    }

    @Nested @DisplayName("listarPorProducto() / listarPorCategoria()")
    class ListarPorReferencia {
        @Test @DisplayName("Por producto delega al repo")
        void porProducto() {
            when(promocionRepository.findByProductoIdAndActivoTrue(10L))
                    .thenReturn(List.of(crear(1L, 10L, null, true)));
            assertEquals(1, promocionService.listarPorProducto(10L).size());
        }
        @Test @DisplayName("Por categoría delega al repo")
        void porCategoria() {
            when(promocionRepository.findByCategoriaIdAndActivoTrue(5L))
                    .thenReturn(List.of(crear(2L, null, 5L, true)));
            assertEquals(1, promocionService.listarPorCategoria(5L).size());
        }
    }

    @Nested @DisplayName("crear()")
    class Crear {
        @Test @DisplayName("Datos válidos → crea como activa")
        void ok() {
            PromocionRequestDTO dto = crearDTO(INICIO, FIN, 10L, null);
            when(promocionRepository.save(any(Promocion.class)))
                    .thenAnswer(inv -> { Promocion p = inv.getArgument(0); p.setId(1L); return p; });
            PromocionResponseDTO r = promocionService.crear(dto);
            assertEquals(1L, r.getId());
        }

        @Test @DisplayName("Asigna activo=true al crear")
        void siempreActiva() {
            PromocionRequestDTO dto = crearDTO(INICIO, FIN, 10L, null);
            ArgumentCaptor<Promocion> captor = ArgumentCaptor.forClass(Promocion.class);
            when(promocionRepository.save(captor.capture())).thenAnswer(inv -> inv.getArgument(0));
            promocionService.crear(dto);
            assertTrue(captor.getValue().getActivo());
        }

        @Test @DisplayName("fechaInicio == fechaFin → PromocionInvalidaException")
        void fechasIguales() {
            PromocionRequestDTO dto = crearDTO(INICIO, INICIO, 10L, null);
            assertThrows(PromocionInvalidaException.class, () -> promocionService.crear(dto));
            verify(promocionRepository, never()).save(any());
        }

        @Test @DisplayName("fechaInicio > fechaFin → PromocionInvalidaException")
        void fechasInvertidas() {
            PromocionRequestDTO dto = crearDTO(FIN, INICIO, 10L, null);
            assertThrows(PromocionInvalidaException.class, () -> promocionService.crear(dto));
        }

        @Test @DisplayName("Sin producto ni categoría → PromocionInvalidaException")
        void sinReferencia() {
            PromocionRequestDTO dto = crearDTO(INICIO, FIN, null, null);
            assertThrows(PromocionInvalidaException.class, () -> promocionService.crear(dto));
        }
    }

    @Nested @DisplayName("actualizar()")
    class Actualizar {
        @Test @DisplayName("Existe y dto válido → actualiza")
        void ok() {
            Promocion existente = crear(1L, 10L, null, true);
            PromocionRequestDTO dto = crearDTO(INICIO, FIN, null, 5L);
            dto.setNombre("Nueva promo");
            when(promocionRepository.findById(1L)).thenReturn(Optional.of(existente));
            when(promocionRepository.save(any(Promocion.class))).thenAnswer(inv -> inv.getArgument(0));
            PromocionResponseDTO r = promocionService.actualizar(1L, dto);
            assertEquals("Nueva promo", r.getNombre());
            assertEquals(5L, r.getCategoriaId());
        }

        @Test @DisplayName("No existe → RecursoNoEncontradoException")
        void noExiste() {
            when(promocionRepository.findById(404L)).thenReturn(Optional.empty());
            assertThrows(RecursoNoEncontradoException.class,
                    () -> promocionService.actualizar(404L, crearDTO(INICIO, FIN, 10L, null)));
        }

        @Test @DisplayName("Existe pero dto inválido → PromocionInvalidaException")
        void dtoInvalido() {
            Promocion existente = crear(1L, 10L, null, true);
            when(promocionRepository.findById(1L)).thenReturn(Optional.of(existente));
            assertThrows(PromocionInvalidaException.class,
                    () -> promocionService.actualizar(1L, crearDTO(FIN, INICIO, 10L, null)));
            verify(promocionRepository, never()).save(any());
        }
    }

    @Nested @DisplayName("darDeBaja() / reactivar()")
    class CambioActivo {
        @Test @DisplayName("darDeBaja existente → activo=false")
        void darDeBajaOk() {
            Promocion p = crear(1L, 10L, null, true);
            when(promocionRepository.findById(1L)).thenReturn(Optional.of(p));
            when(promocionRepository.save(any(Promocion.class))).thenAnswer(inv -> inv.getArgument(0));
            promocionService.darDeBaja(1L);
            assertFalse(p.getActivo());
        }
        @Test @DisplayName("darDeBaja no existe → excepción")
        void darDeBajaNoExiste() {
            when(promocionRepository.findById(99L)).thenReturn(Optional.empty());
            assertThrows(RecursoNoEncontradoException.class, () -> promocionService.darDeBaja(99L));
        }
        @Test @DisplayName("reactivar existente → activo=true")
        void reactivarOk() {
            Promocion p = crear(1L, 10L, null, false);
            when(promocionRepository.findById(1L)).thenReturn(Optional.of(p));
            when(promocionRepository.save(any(Promocion.class))).thenAnswer(inv -> inv.getArgument(0));
            assertTrue(promocionService.reactivar(1L).getActivo());
        }
        @Test @DisplayName("reactivar no existe → excepción")
        void reactivarNoExiste() {
            when(promocionRepository.findById(99L)).thenReturn(Optional.empty());
            assertThrows(RecursoNoEncontradoException.class, () -> promocionService.reactivar(99L));
        }
    }

    // Helpers
    private Promocion crear(Long id, Long productoId, Long categoriaId, Boolean activo) {
        Promocion p = new Promocion();
        p.setId(id);
        p.setNombre("Promo");
        p.setDescripcion("desc");
        p.setPorcentajeDescuento(new BigDecimal("10.0"));
        p.setFechaInicio(INICIO);
        p.setFechaFin(FIN);
        p.setProductoId(productoId);
        p.setCategoriaId(categoriaId);
        p.setActivo(activo);
        return p;
    }

    private PromocionRequestDTO crearDTO(LocalDateTime inicio, LocalDateTime fin, Long productoId, Long categoriaId) {
        PromocionRequestDTO dto = new PromocionRequestDTO();
        dto.setNombre("Promo");
        dto.setDescripcion("desc");
        dto.setPorcentajeDescuento(new BigDecimal("10.0"));
        dto.setFechaInicio(inicio);
        dto.setFechaFin(fin);
        dto.setProductoId(productoId);
        dto.setCategoriaId(categoriaId);
        return dto;
    }
}
