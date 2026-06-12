package com.minimarket.msinventario.service;

import com.minimarket.msinventario.dto.MovimientoStockRequestDTO;
import com.minimarket.msinventario.dto.MovimientoStockResponseDTO;
import com.minimarket.msinventario.dto.StockRequestDTO;
import com.minimarket.msinventario.dto.StockResponseDTO;
import com.minimarket.msinventario.exception.RecursoNoEncontradoException;
import com.minimarket.msinventario.exception.StockDuplicadoException;
import com.minimarket.msinventario.exception.StockInsuficienteException;
import com.minimarket.msinventario.model.MovimientoStock;
import com.minimarket.msinventario.model.Stock;
import com.minimarket.msinventario.model.TipoMovimiento;
import com.minimarket.msinventario.repository.MovimientoStockRepository;
import com.minimarket.msinventario.repository.StockRepository;
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
@DisplayName("StockService - Tests unitarios")
class StockServiceTest {

    @Mock private StockRepository stockRepository;
    @Mock private MovimientoStockRepository movimientoStockRepository;
    @InjectMocks private StockService stockService;

    @Nested @DisplayName("Consultas de stock")
    class Consultas {
        @Test @DisplayName("listarTodos mapea")
        void listarTodos() {
            when(stockRepository.findAll()).thenReturn(List.of(crearStock(1L, 10L, 50, 10, 100)));
            assertEquals(1, stockService.listarTodos().size());
        }
        @Test @DisplayName("obtenerPorId existente")
        void obtenerPorId() {
            when(stockRepository.findById(1L)).thenReturn(Optional.of(crearStock(1L, 10L, 50, 10, 100)));
            assertEquals(1L, stockService.obtenerPorId(1L).getId());
        }
        @Test @DisplayName("obtenerPorId no existe → excepción")
        void obtenerPorIdNo() {
            when(stockRepository.findById(99L)).thenReturn(Optional.empty());
            assertThrows(RecursoNoEncontradoException.class, () -> stockService.obtenerPorId(99L));
        }
        @Test @DisplayName("obtenerPorProductoId existente")
        void obtenerPorProductoId() {
            when(stockRepository.findByProductoId(10L)).thenReturn(Optional.of(crearStock(1L, 10L, 50, 10, 100)));
            assertEquals(10L, stockService.obtenerPorProductoId(10L).getProductoId());
        }
        @Test @DisplayName("obtenerPorProductoId no existe → excepción")
        void obtenerPorProductoIdNo() {
            when(stockRepository.findByProductoId(99L)).thenReturn(Optional.empty());
            assertThrows(RecursoNoEncontradoException.class, () -> stockService.obtenerPorProductoId(99L));
        }
        @Test @DisplayName("listarStockBajo filtra cantidad<=minima")
        void stockBajo() {
            when(stockRepository.findAll()).thenReturn(List.of(
                    crearStock(1L, 10L, 5, 10, 100),     // bajo
                    crearStock(2L, 20L, 80, 10, 100)));  // ok
            List<StockResponseDTO> r = stockService.listarStockBajo();
            assertEquals(1, r.size());
            assertEquals(1L, r.get(0).getId());
        }
    }

    @Nested @DisplayName("crearStock()")
    class CrearStock {
        @Test @DisplayName("Datos válidos → crea")
        void ok() {
            StockRequestDTO dto = crearStockDTO(10L, 50, 10, 100);
            when(stockRepository.existsByProductoId(10L)).thenReturn(false);
            when(stockRepository.save(any(Stock.class)))
                    .thenAnswer(inv -> { Stock s = inv.getArgument(0); s.setId(7L); return s; });
            StockResponseDTO r = stockService.crearStock(dto);
            assertEquals(7L, r.getId());
        }

        @Test @DisplayName("Producto ya tiene stock → StockDuplicadoException")
        void duplicado() {
            when(stockRepository.existsByProductoId(10L)).thenReturn(true);
            assertThrows(StockDuplicadoException.class,
                    () -> stockService.crearStock(crearStockDTO(10L, 50, 10, 100)));
            verify(stockRepository, never()).save(any());
        }

        @Test @DisplayName("Cantidad mínima > máxima → IllegalArgumentException")
        void minimaMayorMaxima() {
            when(stockRepository.existsByProductoId(10L)).thenReturn(false);
            assertThrows(IllegalArgumentException.class,
                    () -> stockService.crearStock(crearStockDTO(10L, 50, 200, 100)));
        }

        @Test @DisplayName("Cantidad actual > máxima → IllegalArgumentException")
        void actualMayorMaxima() {
            when(stockRepository.existsByProductoId(10L)).thenReturn(false);
            assertThrows(IllegalArgumentException.class,
                    () -> stockService.crearStock(crearStockDTO(10L, 200, 10, 100)));
        }
    }

    @Nested @DisplayName("actualizarStock()")
    class ActualizarStock {
        @Test @DisplayName("Existe y datos válidos → actualiza")
        void ok() {
            Stock existente = crearStock(1L, 10L, 50, 10, 100);
            StockRequestDTO dto = crearStockDTO(10L, 999, 15, 150);
            dto.setUbicacion("Nueva ubicacion");
            when(stockRepository.findById(1L)).thenReturn(Optional.of(existente));
            when(stockRepository.save(any(Stock.class))).thenAnswer(inv -> inv.getArgument(0));
            StockResponseDTO r = stockService.actualizarStock(1L, dto);
            assertEquals("Nueva ubicacion", r.getUbicacion());
            assertEquals(150, r.getCantidadMaxima());
        }
        @Test @DisplayName("No existe → RecursoNoEncontradoException")
        void noExiste() {
            when(stockRepository.findById(404L)).thenReturn(Optional.empty());
            assertThrows(RecursoNoEncontradoException.class,
                    () -> stockService.actualizarStock(404L, crearStockDTO(10L, 50, 10, 100)));
        }
        @Test @DisplayName("Nueva máxima menor a actual → IllegalArgumentException")
        void nuevaMaximaInvalida() {
            Stock existente = crearStock(1L, 10L, 80, 10, 100);
            when(stockRepository.findById(1L)).thenReturn(Optional.of(existente));
            assertThrows(IllegalArgumentException.class,
                    () -> stockService.actualizarStock(1L, crearStockDTO(10L, 0, 10, 50)));
        }
    }

    @Nested @DisplayName("eliminarStock()")
    class EliminarStock {
        @Test @DisplayName("Existe → elimina")
        void ok() {
            Stock s = crearStock(1L, 10L, 50, 10, 100);
            when(stockRepository.findById(1L)).thenReturn(Optional.of(s));
            stockService.eliminarStock(1L);
            verify(stockRepository).delete(s);
        }
        @Test @DisplayName("No existe → excepción")
        void noExiste() {
            when(stockRepository.findById(99L)).thenReturn(Optional.empty());
            assertThrows(RecursoNoEncontradoException.class, () -> stockService.eliminarStock(99L));
        }
    }

    @Nested @DisplayName("registrarMovimiento()")
    class RegistrarMovimiento {
        @Test @DisplayName("ENTRADA suma al stock")
        void entrada() {
            Stock s = crearStock(1L, 10L, 50, 10, 100);
            when(stockRepository.findById(1L)).thenReturn(Optional.of(s));
            when(stockRepository.save(any(Stock.class))).thenAnswer(inv -> inv.getArgument(0));
            when(movimientoStockRepository.save(any(MovimientoStock.class)))
                    .thenAnswer(inv -> { MovimientoStock m = inv.getArgument(0); m.setId(1L); return m; });
            stockService.registrarMovimiento(crearMov(1L, TipoMovimiento.ENTRADA, 20));
            assertEquals(70, s.getCantidadActual());
        }

        @Test @DisplayName("ENTRADA que excede máximo → IllegalArgumentException")
        void entradaExcede() {
            Stock s = crearStock(1L, 10L, 90, 10, 100);
            when(stockRepository.findById(1L)).thenReturn(Optional.of(s));
            assertThrows(IllegalArgumentException.class,
                    () -> stockService.registrarMovimiento(crearMov(1L, TipoMovimiento.ENTRADA, 50)));
            verify(movimientoStockRepository, never()).save(any());
        }

        @Test @DisplayName("SALIDA resta del stock")
        void salida() {
            Stock s = crearStock(1L, 10L, 50, 10, 100);
            when(stockRepository.findById(1L)).thenReturn(Optional.of(s));
            when(stockRepository.save(any(Stock.class))).thenAnswer(inv -> inv.getArgument(0));
            when(movimientoStockRepository.save(any(MovimientoStock.class))).thenAnswer(inv -> inv.getArgument(0));
            stockService.registrarMovimiento(crearMov(1L, TipoMovimiento.SALIDA, 10));
            assertEquals(40, s.getCantidadActual());
        }

        @Test @DisplayName("SALIDA sin stock suficiente → StockInsuficienteException")
        void salidaInsuficiente() {
            Stock s = crearStock(1L, 10L, 5, 10, 100);
            when(stockRepository.findById(1L)).thenReturn(Optional.of(s));
            assertThrows(StockInsuficienteException.class,
                    () -> stockService.registrarMovimiento(crearMov(1L, TipoMovimiento.SALIDA, 10)));
            verify(stockRepository, never()).save(any());
        }

        @Test @DisplayName("AJUSTE establece cantidad")
        void ajuste() {
            Stock s = crearStock(1L, 10L, 50, 10, 100);
            when(stockRepository.findById(1L)).thenReturn(Optional.of(s));
            when(stockRepository.save(any(Stock.class))).thenAnswer(inv -> inv.getArgument(0));
            when(movimientoStockRepository.save(any(MovimientoStock.class))).thenAnswer(inv -> inv.getArgument(0));
            stockService.registrarMovimiento(crearMov(1L, TipoMovimiento.AJUSTE, 75));
            assertEquals(75, s.getCantidadActual());
        }

        @Test @DisplayName("AJUSTE que excede máximo → IllegalArgumentException")
        void ajusteExcede() {
            Stock s = crearStock(1L, 10L, 50, 10, 100);
            when(stockRepository.findById(1L)).thenReturn(Optional.of(s));
            assertThrows(IllegalArgumentException.class,
                    () -> stockService.registrarMovimiento(crearMov(1L, TipoMovimiento.AJUSTE, 200)));
        }

        @Test @DisplayName("Stock inexistente → RecursoNoEncontradoException")
        void stockNoExiste() {
            when(stockRepository.findById(404L)).thenReturn(Optional.empty());
            assertThrows(RecursoNoEncontradoException.class,
                    () -> stockService.registrarMovimiento(crearMov(404L, TipoMovimiento.ENTRADA, 10)));
        }

        @Test @DisplayName("Movimiento registrado captura motivo")
        void capturaMotivo() {
            Stock s = crearStock(1L, 10L, 50, 10, 100);
            when(stockRepository.findById(1L)).thenReturn(Optional.of(s));
            when(stockRepository.save(any(Stock.class))).thenAnswer(inv -> inv.getArgument(0));
            ArgumentCaptor<MovimientoStock> captor = ArgumentCaptor.forClass(MovimientoStock.class);
            when(movimientoStockRepository.save(captor.capture())).thenAnswer(inv -> inv.getArgument(0));
            MovimientoStockRequestDTO dto = crearMov(1L, TipoMovimiento.ENTRADA, 10);
            dto.setMotivo("Compra a proveedor");
            stockService.registrarMovimiento(dto);
            assertEquals("Compra a proveedor", captor.getValue().getMotivo());
        }
    }

    @Nested @DisplayName("Listados de movimientos")
    class Listados {
        @Test @DisplayName("listarMovimientosPorStock")
        void porStock() {
            MovimientoStock m = new MovimientoStock();
            m.setId(1L);
            m.setStock(crearStock(1L, 10L, 50, 10, 100));
            m.setTipoMovimiento(TipoMovimiento.ENTRADA);
            m.setCantidad(5);
            when(movimientoStockRepository.findByStockIdOrderByFechaMovimientoDesc(1L))
                    .thenReturn(List.of(m));
            assertEquals(1, stockService.listarMovimientosPorStock(1L).size());
        }
        @Test @DisplayName("listarMovimientosPorTipo")
        void porTipo() {
            MovimientoStock m = new MovimientoStock();
            m.setId(1L);
            m.setStock(crearStock(1L, 10L, 50, 10, 100));
            m.setTipoMovimiento(TipoMovimiento.SALIDA);
            m.setCantidad(5);
            when(movimientoStockRepository.findByTipoMovimiento(TipoMovimiento.SALIDA))
                    .thenReturn(List.of(m));
            List<MovimientoStockResponseDTO> r = stockService.listarMovimientosPorTipo(TipoMovimiento.SALIDA);
            assertEquals(1, r.size());
            assertEquals(TipoMovimiento.SALIDA, r.get(0).getTipoMovimiento());
        }
    }

    // Helpers
    private Stock crearStock(Long id, Long productoId, int actual, int minima, int maxima) {
        Stock s = new Stock();
        s.setId(id);
        s.setProductoId(productoId);
        s.setCantidadActual(actual);
        s.setCantidadMinima(minima);
        s.setCantidadMaxima(maxima);
        s.setUbicacion("Bodega");
        return s;
    }

    private StockRequestDTO crearStockDTO(Long productoId, int actual, int minima, int maxima) {
        StockRequestDTO dto = new StockRequestDTO();
        dto.setProductoId(productoId);
        dto.setCantidadActual(actual);
        dto.setCantidadMinima(minima);
        dto.setCantidadMaxima(maxima);
        dto.setUbicacion("Bodega");
        return dto;
    }

    private MovimientoStockRequestDTO crearMov(Long stockId, TipoMovimiento tipo, int cantidad) {
        MovimientoStockRequestDTO dto = new MovimientoStockRequestDTO();
        dto.setStockId(stockId);
        dto.setTipoMovimiento(tipo);
        dto.setCantidad(cantidad);
        dto.setMotivo("test");
        return dto;
    }
}
