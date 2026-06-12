package com.minimarket.msreportes.service;

import com.minimarket.msreportes.dto.ReporteRequestDTO;
import com.minimarket.msreportes.dto.ReporteResponseDTO;
import com.minimarket.msreportes.exception.RecursoNoEncontradoException;
import com.minimarket.msreportes.model.ReporteGenerado;
import com.minimarket.msreportes.model.TipoReporte;
import com.minimarket.msreportes.repository.ReporteGeneradoRepository;
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

@ExtendWith(MockitoExtension.class)
@DisplayName("ReporteService - Tests unitarios")
class ReporteServiceTest {

    @Mock private ReporteGeneradoRepository reporteRepository;
    @InjectMocks private ReporteService reporteService;

    @Nested @DisplayName("Consultas")
    class Consultas {
        @Test @DisplayName("listarTodos mapea")
        void listarTodos() {
            when(reporteRepository.findAll()).thenReturn(List.of(crear(1L, TipoReporte.VENTAS_DIARIAS, 5L)));
            assertEquals(1, reporteService.listarTodos().size());
        }

        @Test @DisplayName("obtenerPorId existente")
        void porIdOk() {
            when(reporteRepository.findById(1L)).thenReturn(Optional.of(crear(1L, TipoReporte.STOCK_BAJO, 5L)));
            assertEquals(1L, reporteService.obtenerPorId(1L).getId());
        }

        @Test @DisplayName("obtenerPorId no existe → RecursoNoEncontradoException")
        void porIdNo() {
            when(reporteRepository.findById(99L)).thenReturn(Optional.empty());
            assertThrows(RecursoNoEncontradoException.class, () -> reporteService.obtenerPorId(99L));
        }

        @Test @DisplayName("listarPorTipo filtra")
        void porTipo() {
            when(reporteRepository.findByTipoReporteOrderByFechaGeneracionDesc(TipoReporte.VENTAS_DIARIAS))
                    .thenReturn(List.of(crear(1L, TipoReporte.VENTAS_DIARIAS, 5L)));
            assertEquals(1, reporteService.listarPorTipo(TipoReporte.VENTAS_DIARIAS).size());
        }

        @Test @DisplayName("listarPorEmpleado filtra")
        void porEmpleado() {
            when(reporteRepository.findByGeneradoPorOrderByFechaGeneracionDesc(5L))
                    .thenReturn(List.of(crear(1L, TipoReporte.STOCK_BAJO, 5L)));
            assertEquals(1, reporteService.listarPorEmpleado(5L).size());
        }
    }

    @Nested @DisplayName("crear()")
    class Crear {
        @Test @DisplayName("Datos válidos → crea reporte")
        void ok() {
            ReporteRequestDTO dto = crearDTO(TipoReporte.VENTAS_DIARIAS, 5L);
            when(reporteRepository.save(any(ReporteGenerado.class)))
                    .thenAnswer(inv -> { ReporteGenerado r = inv.getArgument(0); r.setId(7L); return r; });
            ReporteResponseDTO r = reporteService.crear(dto);
            assertEquals(7L, r.getId());
        }

        @Test @DisplayName("Asigna resultadoJson placeholder")
        void asignaPlaceholder() {
            ReporteRequestDTO dto = crearDTO(TipoReporte.STOCK_BAJO, 5L);
            ArgumentCaptor<ReporteGenerado> captor = ArgumentCaptor.forClass(ReporteGenerado.class);
            when(reporteRepository.save(captor.capture())).thenAnswer(inv -> inv.getArgument(0));
            reporteService.crear(dto);
            assertNotNull(captor.getValue().getResultadoJson());
            assertTrue(captor.getValue().getResultadoJson().contains("pendiente"));
        }
    }

    @Nested @DisplayName("eliminar()")
    class Eliminar {
        @Test @DisplayName("Existe → delete")
        void ok() {
            ReporteGenerado r = crear(1L, TipoReporte.VENTAS_DIARIAS, 5L);
            when(reporteRepository.findById(1L)).thenReturn(Optional.of(r));
            reporteService.eliminar(1L);
            verify(reporteRepository).delete(r);
        }
        @Test @DisplayName("No existe → RecursoNoEncontradoException")
        void noExiste() {
            when(reporteRepository.findById(99L)).thenReturn(Optional.empty());
            assertThrows(RecursoNoEncontradoException.class, () -> reporteService.eliminar(99L));
            verify(reporteRepository, never()).delete(any());
        }
    }

    // Helpers
    private ReporteGenerado crear(Long id, TipoReporte tipo, Long generadoPor) {
        ReporteGenerado r = new ReporteGenerado();
        r.setId(id);
        r.setTipoReporte(tipo);
        r.setDescripcion("desc");
        r.setParametros("{}");
        r.setResultadoJson("{}");
        r.setGeneradoPor(generadoPor);
        return r;
    }

    private ReporteRequestDTO crearDTO(TipoReporte tipo, Long generadoPor) {
        ReporteRequestDTO dto = new ReporteRequestDTO();
        dto.setTipoReporte(tipo);
        dto.setDescripcion("desc");
        dto.setParametros("{}");
        dto.setGeneradoPor(generadoPor);
        return dto;
    }
}
