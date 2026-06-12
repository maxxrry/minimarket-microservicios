package com.minimarket.mspagos.service;

import com.minimarket.mspagos.dto.PagoRequestDTO;
import com.minimarket.mspagos.dto.PagoResponseDTO;
import com.minimarket.mspagos.exception.EstadoInvalidoException;
import com.minimarket.mspagos.exception.PagoDuplicadoException;
import com.minimarket.mspagos.exception.RecursoNoEncontradoException;
import com.minimarket.mspagos.model.EstadoPago;
import com.minimarket.mspagos.model.MetodoPago;
import com.minimarket.mspagos.model.Pago;
import com.minimarket.mspagos.repository.PagoRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
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
@DisplayName("PagoService - Tests unitarios")
class PagoServiceTest {

    @Mock private PagoRepository pagoRepository;
    @InjectMocks private PagoService pagoService;

    @Nested @DisplayName("Consultas")
    class Consultas {
        @Test @DisplayName("listarTodos mapea")
        void listarTodos() {
            when(pagoRepository.findAll()).thenReturn(List.of(crear(1L, "TX-001", EstadoPago.PENDIENTE)));
            assertEquals(1, pagoService.listarTodos().size());
        }
        @Test @DisplayName("buscarPorId existe")
        void buscarPorIdOk() {
            when(pagoRepository.findById(1L)).thenReturn(Optional.of(crear(1L, "TX-001", EstadoPago.PENDIENTE)));
            assertEquals(1L, pagoService.buscarPorId(1L).getId());
        }
        @Test @DisplayName("buscarPorId no existe → excepción")
        void buscarPorIdNo() {
            when(pagoRepository.findById(99L)).thenReturn(Optional.empty());
            assertThrows(RecursoNoEncontradoException.class, () -> pagoService.buscarPorId(99L));
        }
        @Test @DisplayName("buscarPorNumeroTransaccion existe")
        void buscarPorTx() {
            when(pagoRepository.findByNumeroTransaccion("TX-001"))
                    .thenReturn(Optional.of(crear(1L, "TX-001", EstadoPago.PENDIENTE)));
            assertEquals("TX-001", pagoService.buscarPorNumeroTransaccion("TX-001").getNumeroTransaccion());
        }
        @Test @DisplayName("buscarPorNumeroTransaccion no existe → excepción")
        void buscarPorTxNo() {
            when(pagoRepository.findByNumeroTransaccion("X")).thenReturn(Optional.empty());
            assertThrows(RecursoNoEncontradoException.class, () -> pagoService.buscarPorNumeroTransaccion("X"));
        }
        @Test @DisplayName("buscarPorVenta")
        void buscarPorVenta() {
            when(pagoRepository.findByVentaId(5L)).thenReturn(List.of(crear(1L, "TX-001", EstadoPago.PENDIENTE)));
            assertEquals(1, pagoService.buscarPorVenta(5L).size());
        }
        @Test @DisplayName("buscarPorEstado")
        void buscarPorEstado() {
            when(pagoRepository.findByEstado(EstadoPago.COMPLETADO))
                    .thenReturn(List.of(crear(1L, "TX-001", EstadoPago.COMPLETADO)));
            assertEquals(1, pagoService.buscarPorEstado(EstadoPago.COMPLETADO).size());
        }
        @Test @DisplayName("buscarPorMetodoPago")
        void buscarPorMetodo() {
            when(pagoRepository.findByMetodoPago(MetodoPago.EFECTIVO))
                    .thenReturn(List.of(crear(1L, "TX-001", EstadoPago.PENDIENTE)));
            assertEquals(1, pagoService.buscarPorMetodoPago(MetodoPago.EFECTIVO).size());
        }
    }

    @Nested @DisplayName("crear()")
    class Crear {
        @Test @DisplayName("Datos válidos → crea con estado PENDIENTE")
        void ok() {
            PagoRequestDTO dto = crearDTO("TX-001");
            when(pagoRepository.existsByNumeroTransaccion("TX-001")).thenReturn(false);
            ArgumentCaptor<Pago> captor = ArgumentCaptor.forClass(Pago.class);
            when(pagoRepository.save(captor.capture())).thenAnswer(inv -> {
                Pago p = inv.getArgument(0);
                p.setId(1L);
                return p;
            });
            PagoResponseDTO r = pagoService.crear(dto);
            assertEquals(1L, r.getId());
            assertEquals(EstadoPago.PENDIENTE, captor.getValue().getEstado());
        }

        @Test @DisplayName("Número de transacción duplicado → PagoDuplicadoException")
        void duplicado() {
            when(pagoRepository.existsByNumeroTransaccion("TX-001")).thenReturn(true);
            assertThrows(PagoDuplicadoException.class, () -> pagoService.crear(crearDTO("TX-001")));
            verify(pagoRepository, never()).save(any());
        }
    }

    @Nested @DisplayName("actualizar()")
    class Actualizar {
        @Test @DisplayName("PENDIENTE → actualiza")
        void ok() {
            Pago existente = crear(1L, "TX-001", EstadoPago.PENDIENTE);
            PagoRequestDTO dto = crearDTO("TX-001");
            dto.setObservaciones("Nueva obs");
            when(pagoRepository.findById(1L)).thenReturn(Optional.of(existente));
            when(pagoRepository.save(any(Pago.class))).thenAnswer(inv -> inv.getArgument(0));
            PagoResponseDTO r = pagoService.actualizar(1L, dto);
            assertEquals("Nueva obs", r.getObservaciones());
        }

        @Test @DisplayName("No PENDIENTE → EstadoInvalidoException")
        void estadoInvalido() {
            Pago existente = crear(1L, "TX-001", EstadoPago.COMPLETADO);
            when(pagoRepository.findById(1L)).thenReturn(Optional.of(existente));
            assertThrows(EstadoInvalidoException.class,
                    () -> pagoService.actualizar(1L, crearDTO("TX-001")));
        }

        @Test @DisplayName("Cambio de número a uno duplicado → PagoDuplicadoException")
        void cambioNumeroDuplicado() {
            Pago existente = crear(1L, "TX-001", EstadoPago.PENDIENTE);
            PagoRequestDTO dto = crearDTO("TX-NEW");
            when(pagoRepository.findById(1L)).thenReturn(Optional.of(existente));
            when(pagoRepository.existsByNumeroTransaccion("TX-NEW")).thenReturn(true);
            assertThrows(PagoDuplicadoException.class, () -> pagoService.actualizar(1L, dto));
        }

        @Test @DisplayName("No existe → RecursoNoEncontradoException")
        void noExiste() {
            when(pagoRepository.findById(404L)).thenReturn(Optional.empty());
            assertThrows(RecursoNoEncontradoException.class,
                    () -> pagoService.actualizar(404L, crearDTO("TX-001")));
        }
    }

    @Nested @DisplayName("cambiarEstado() - validación de transiciones")
    class CambiarEstado {
        @Test @DisplayName("PENDIENTE → COMPLETADO permitido")
        void pendienteACompletado() {
            Pago p = crear(1L, "TX-001", EstadoPago.PENDIENTE);
            when(pagoRepository.findById(1L)).thenReturn(Optional.of(p));
            when(pagoRepository.save(any(Pago.class))).thenAnswer(inv -> inv.getArgument(0));
            PagoResponseDTO r = pagoService.cambiarEstado(1L, EstadoPago.COMPLETADO);
            assertEquals(EstadoPago.COMPLETADO, r.getEstado());
        }
        @Test @DisplayName("PENDIENTE → RECHAZADO permitido")
        void pendienteARechazado() {
            Pago p = crear(1L, "TX-001", EstadoPago.PENDIENTE);
            when(pagoRepository.findById(1L)).thenReturn(Optional.of(p));
            when(pagoRepository.save(any(Pago.class))).thenAnswer(inv -> inv.getArgument(0));
            assertEquals(EstadoPago.RECHAZADO, pagoService.cambiarEstado(1L, EstadoPago.RECHAZADO).getEstado());
        }
        @Test @DisplayName("COMPLETADO → REEMBOLSADO permitido")
        void completadoAReembolsado() {
            Pago p = crear(1L, "TX-001", EstadoPago.COMPLETADO);
            when(pagoRepository.findById(1L)).thenReturn(Optional.of(p));
            when(pagoRepository.save(any(Pago.class))).thenAnswer(inv -> inv.getArgument(0));
            assertEquals(EstadoPago.REEMBOLSADO, pagoService.cambiarEstado(1L, EstadoPago.REEMBOLSADO).getEstado());
        }
        @Test @DisplayName("PENDIENTE → REEMBOLSADO NO permitido")
        void pendienteAReembolsadoNo() {
            Pago p = crear(1L, "TX-001", EstadoPago.PENDIENTE);
            when(pagoRepository.findById(1L)).thenReturn(Optional.of(p));
            assertThrows(EstadoInvalidoException.class,
                    () -> pagoService.cambiarEstado(1L, EstadoPago.REEMBOLSADO));
        }
        @Test @DisplayName("COMPLETADO → PENDIENTE NO permitido")
        void completadoAPendienteNo() {
            Pago p = crear(1L, "TX-001", EstadoPago.COMPLETADO);
            when(pagoRepository.findById(1L)).thenReturn(Optional.of(p));
            assertThrows(EstadoInvalidoException.class,
                    () -> pagoService.cambiarEstado(1L, EstadoPago.PENDIENTE));
        }
        @Test @DisplayName("RECHAZADO es estado final")
        void rechazadoEsFinal() {
            Pago p = crear(1L, "TX-001", EstadoPago.RECHAZADO);
            when(pagoRepository.findById(1L)).thenReturn(Optional.of(p));
            assertThrows(EstadoInvalidoException.class,
                    () -> pagoService.cambiarEstado(1L, EstadoPago.COMPLETADO));
        }
        @Test @DisplayName("REEMBOLSADO es estado final")
        void reembolsadoEsFinal() {
            Pago p = crear(1L, "TX-001", EstadoPago.REEMBOLSADO);
            when(pagoRepository.findById(1L)).thenReturn(Optional.of(p));
            assertThrows(EstadoInvalidoException.class,
                    () -> pagoService.cambiarEstado(1L, EstadoPago.COMPLETADO));
        }
        @Test @DisplayName("Mismo estado → excepción")
        void mismoEstado() {
            Pago p = crear(1L, "TX-001", EstadoPago.PENDIENTE);
            when(pagoRepository.findById(1L)).thenReturn(Optional.of(p));
            assertThrows(EstadoInvalidoException.class,
                    () -> pagoService.cambiarEstado(1L, EstadoPago.PENDIENTE));
        }
        @Test @DisplayName("Pago inexistente → RecursoNoEncontradoException")
        void noExiste() {
            when(pagoRepository.findById(404L)).thenReturn(Optional.empty());
            assertThrows(RecursoNoEncontradoException.class,
                    () -> pagoService.cambiarEstado(404L, EstadoPago.COMPLETADO));
        }
    }

    @Nested @DisplayName("eliminar()")
    class Eliminar {
        @Test @DisplayName("Pago PENDIENTE → elimina físicamente")
        void ok() {
            Pago p = crear(1L, "TX-001", EstadoPago.PENDIENTE);
            when(pagoRepository.findById(1L)).thenReturn(Optional.of(p));
            pagoService.eliminar(1L);
            verify(pagoRepository).delete(p);
        }
        @Test @DisplayName("Pago no PENDIENTE → EstadoInvalidoException")
        void estadoInvalido() {
            Pago p = crear(1L, "TX-001", EstadoPago.COMPLETADO);
            when(pagoRepository.findById(1L)).thenReturn(Optional.of(p));
            assertThrows(EstadoInvalidoException.class, () -> pagoService.eliminar(1L));
            verify(pagoRepository, never()).delete(any());
        }
        @Test @DisplayName("No existe → RecursoNoEncontradoException")
        void noExiste() {
            when(pagoRepository.findById(404L)).thenReturn(Optional.empty());
            assertThrows(RecursoNoEncontradoException.class, () -> pagoService.eliminar(404L));
        }
    }

    // Helpers
    private Pago crear(Long id, String tx, EstadoPago estado) {
        Pago p = new Pago();
        p.setId(id);
        p.setNumeroTransaccion(tx);
        p.setMonto(new BigDecimal("10000.00"));
        p.setMetodoPago(MetodoPago.EFECTIVO);
        p.setEstado(estado);
        p.setVentaId(5L);
        p.setObservaciones("obs");
        return p;
    }

    private PagoRequestDTO crearDTO(String tx) {
        PagoRequestDTO dto = new PagoRequestDTO();
        dto.setNumeroTransaccion(tx);
        dto.setMonto(new BigDecimal("10000.00"));
        dto.setMetodoPago(MetodoPago.EFECTIVO);
        dto.setVentaId(5L);
        dto.setObservaciones("obs");
        return dto;
    }
}
