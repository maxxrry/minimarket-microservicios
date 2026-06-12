package com.minimarket.msventas.service;

import com.minimarket.msventas.client.CatalogoClient;
import com.minimarket.msventas.client.ClienteClient;
import com.minimarket.msventas.client.EmpleadoClient;
import com.minimarket.msventas.client.InventarioClient;
import com.minimarket.msventas.client.PromocionClient;
import com.minimarket.msventas.client.dto.ClienteDTO;
import com.minimarket.msventas.client.dto.EmpleadoDTO;
import com.minimarket.msventas.client.dto.ProductoDTO;
import com.minimarket.msventas.client.dto.PromocionDTO;
import com.minimarket.msventas.client.dto.StockDTO;
import com.minimarket.msventas.client.dto.MovimientoStockRequestDTO;
import com.minimarket.msventas.dto.DetalleVentaRequestDTO;
import com.minimarket.msventas.dto.VentaRequestDTO;
import com.minimarket.msventas.dto.VentaResponseDTO;
import com.minimarket.msventas.exception.EstadoVentaInvalidoException;
import com.minimarket.msventas.exception.RecursoNoEncontradoException;
import com.minimarket.msventas.exception.ServicioNoDisponibleException;
import com.minimarket.msventas.exception.VentaInvalidaException;
import com.minimarket.msventas.model.DetalleVenta;
import com.minimarket.msventas.model.EstadoVenta;
import com.minimarket.msventas.model.Venta;
import com.minimarket.msventas.repository.VentaRepository;
import feign.FeignException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
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
 * Tests unitarios de {@link VentaService}.
 *
 * Mockea TODOS los colaboradores externos:
 *   - VentaRepository (BD local)
 *   - CatalogoClient, ClienteClient, EmpleadoClient,
 *     PromocionClient, InventarioClient (microservicios remotos via Feign)
 *
 * Cubre tanto el camino feliz como cada rama de excepción:
 *   - FeignException.NotFound (recurso remoto inexistente)
 *   - FeignException genérica (servicio caído)
 *   - VentaInvalidaException (reglas de negocio locales)
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("VentaService - Tests unitarios con Feign mockeado")
class VentaServiceTest {

    @Mock private VentaRepository ventaRepository;
    @Mock private CatalogoClient catalogoClient;
    @Mock private ClienteClient clienteClient;
    @Mock private EmpleadoClient empleadoClient;
    @Mock private PromocionClient promocionClient;
    @Mock private InventarioClient inventarioClient;

    @InjectMocks private VentaService ventaService;

    // ════════════════════════════════════════════════════════════
    // CONSULTAS (sin Feign)
    // ════════════════════════════════════════════════════════════

    @Nested @DisplayName("Consultas")
    class Consultas {
        @Test @DisplayName("listarTodas mapea ventas")
        void listarTodas() {
            when(ventaRepository.findAll()).thenReturn(List.of(crearVenta(1L, "VTA-001", EstadoVenta.PENDIENTE)));
            assertEquals(1, ventaService.listarTodas().size());
        }

        @Test @DisplayName("obtenerPorId existente")
        void obtenerPorIdOk() {
            when(ventaRepository.findById(1L)).thenReturn(Optional.of(crearVenta(1L, "VTA-001", EstadoVenta.PENDIENTE)));
            assertEquals(1L, ventaService.obtenerPorId(1L).getId());
        }

        @Test @DisplayName("obtenerPorId no existe → RecursoNoEncontradoException")
        void obtenerPorIdNo() {
            when(ventaRepository.findById(99L)).thenReturn(Optional.empty());
            assertThrows(RecursoNoEncontradoException.class, () -> ventaService.obtenerPorId(99L));
        }

        @Test @DisplayName("obtenerPorNumeroVenta existente")
        void obtenerPorNumeroOk() {
            when(ventaRepository.findByNumeroVenta("VTA-001"))
                    .thenReturn(Optional.of(crearVenta(1L, "VTA-001", EstadoVenta.PENDIENTE)));
            assertEquals("VTA-001", ventaService.obtenerPorNumeroVenta("VTA-001").getNumeroVenta());
        }

        @Test @DisplayName("obtenerPorNumeroVenta no existe → excepción")
        void obtenerPorNumeroNo() {
            when(ventaRepository.findByNumeroVenta("X")).thenReturn(Optional.empty());
            assertThrows(RecursoNoEncontradoException.class, () -> ventaService.obtenerPorNumeroVenta("X"));
        }

        @Test @DisplayName("listarPorCliente delega al repo")
        void porCliente() {
            when(ventaRepository.findByClienteIdOrderByFechaVentaDesc(5L))
                    .thenReturn(List.of(crearVenta(1L, "VTA-001", EstadoVenta.PENDIENTE)));
            assertEquals(1, ventaService.listarPorCliente(5L).size());
        }

        @Test @DisplayName("listarPorEmpleado delega al repo")
        void porEmpleado() {
            when(ventaRepository.findByEmpleadoIdOrderByFechaVentaDesc(7L))
                    .thenReturn(List.of(crearVenta(1L, "VTA-001", EstadoVenta.PENDIENTE)));
            assertEquals(1, ventaService.listarPorEmpleado(7L).size());
        }

        @Test @DisplayName("listarPorEstado delega al repo")
        void porEstado() {
            when(ventaRepository.findByEstadoOrderByFechaVentaDesc(EstadoVenta.COMPLETADA))
                    .thenReturn(List.of(crearVenta(1L, "VTA-001", EstadoVenta.COMPLETADA)));
            assertEquals(1, ventaService.listarPorEstado(EstadoVenta.COMPLETADA).size());
        }
    }

    // ════════════════════════════════════════════════════════════
    // crear() — orquesta los 5 Feign clients
    // ════════════════════════════════════════════════════════════

    @Nested @DisplayName("crear() - camino feliz")
    class CrearOk {
        @Test @DisplayName("Empleado, cliente y producto válidos + stock + promoción → calcula totales correctos")
        void crearCompleto() {
            // GIVEN: empleado y cliente activos
            when(empleadoClient.obtenerEmpleadoPorId(1L)).thenReturn(empleado(1L, true));
            when(clienteClient.obtenerClientePorId(2L)).thenReturn(cliente(2L, true));
            // Producto a $1990, categoría 5
            when(catalogoClient.obtenerProductoPorId(10L)).thenReturn(producto(10L, "Coca", new BigDecimal("1990.00"), 5L));
            // Stock con 50 disponibles
            when(inventarioClient.obtenerStockPorProducto(10L)).thenReturn(stock(100L, 10L, 50));
            // Promoción del 10% para la categoría 5
            when(promocionClient.listarPorProducto(10L)).thenReturn(Collections.emptyList());
            when(promocionClient.listarPorCategoria(5L)).thenReturn(List.of(promo(10L, null, 5L, new BigDecimal("10.0"))));
            when(ventaRepository.countByNumeroVentaStartingWith(any())).thenReturn(0L);
            when(ventaRepository.save(any(Venta.class)))
                    .thenAnswer(inv -> { Venta v = inv.getArgument(0); v.setId(1L); return v; });

            // WHEN
            VentaResponseDTO r = ventaService.crear(crearVentaDTO(1L, 2L, 10L, 3, null));

            // THEN: precio venido de catalogo, descuento del 10%, IVA 19%
            // Subtotal: 1990*3 = 5970; Descuento: 199*3 = 597; Base: 5373; IVA: 1020.87; Total: 6393.87
            assertEquals(new BigDecimal("5970.00"), r.getSubtotal());
            assertEquals(new BigDecimal("597.00"), r.getDescuentoTotal());
            assertEquals(new BigDecimal("6393.87"), r.getTotal());
            // Verifica que se llamaron los 5 servicios
            verify(empleadoClient).obtenerEmpleadoPorId(1L);
            verify(clienteClient).obtenerClientePorId(2L);
            verify(catalogoClient).obtenerProductoPorId(10L);
            verify(inventarioClient).obtenerStockPorProducto(10L);
            verify(promocionClient).listarPorCategoria(5L);
        }

        @Test @DisplayName("Sin clienteId → no consulta ms-clientes")
        void sinCliente() {
            when(empleadoClient.obtenerEmpleadoPorId(1L)).thenReturn(empleado(1L, true));
            when(catalogoClient.obtenerProductoPorId(10L)).thenReturn(producto(10L, "X", new BigDecimal("1000.00"), 5L));
            when(inventarioClient.obtenerStockPorProducto(10L)).thenReturn(stock(100L, 10L, 50));
            when(promocionClient.listarPorProducto(10L)).thenReturn(Collections.emptyList());
            when(promocionClient.listarPorCategoria(5L)).thenReturn(Collections.emptyList());
            when(ventaRepository.countByNumeroVentaStartingWith(any())).thenReturn(0L);
            when(ventaRepository.save(any(Venta.class)))
                    .thenAnswer(inv -> { Venta v = inv.getArgument(0); v.setId(1L); return v; });

            ventaService.crear(crearVentaDTO(1L, null, 10L, 1, null));

            verifyNoInteractions(clienteClient);
        }

        @Test @DisplayName("Descuento manual respeta el valor enviado (no aplica promoción)")
        void descuentoManualRespetado() {
            when(empleadoClient.obtenerEmpleadoPorId(1L)).thenReturn(empleado(1L, true));
            when(catalogoClient.obtenerProductoPorId(10L)).thenReturn(producto(10L, "X", new BigDecimal("1000.00"), 5L));
            when(inventarioClient.obtenerStockPorProducto(10L)).thenReturn(stock(100L, 10L, 50));
            when(ventaRepository.countByNumeroVentaStartingWith(any())).thenReturn(0L);
            when(ventaRepository.save(any(Venta.class)))
                    .thenAnswer(inv -> { Venta v = inv.getArgument(0); v.setId(1L); return v; });

            VentaResponseDTO r = ventaService.crear(crearVentaDTO(1L, null, 10L, 1, new BigDecimal("50.00")));

            assertEquals(new BigDecimal("50.00"), r.getDescuentoTotal());
            verifyNoInteractions(promocionClient); // ← no consulta promociones si hay descuento manual
        }

        @Test @DisplayName("Promociones service caído → venta procede sin descuento")
        void promoServiceCaido() {
            when(empleadoClient.obtenerEmpleadoPorId(1L)).thenReturn(empleado(1L, true));
            when(catalogoClient.obtenerProductoPorId(10L)).thenReturn(producto(10L, "X", new BigDecimal("1000.00"), 5L));
            when(inventarioClient.obtenerStockPorProducto(10L)).thenReturn(stock(100L, 10L, 50));
            when(promocionClient.listarPorProducto(10L)).thenThrow(mock(FeignException.class));
            when(ventaRepository.countByNumeroVentaStartingWith(any())).thenReturn(0L);
            when(ventaRepository.save(any(Venta.class)))
                    .thenAnswer(inv -> { Venta v = inv.getArgument(0); v.setId(1L); return v; });

            VentaResponseDTO r = ventaService.crear(crearVentaDTO(1L, null, 10L, 1, null));

            assertEquals(BigDecimal.ZERO.setScale(2), r.getDescuentoTotal()); // sin descuento, no se rompe
        }
    }

    @Nested @DisplayName("crear() - errores")
    class CrearErrores {
        @Test @DisplayName("Lista de detalles vacía → VentaInvalidaException")
        void sinDetalles() {
            VentaRequestDTO dto = new VentaRequestDTO();
            dto.setEmpleadoId(1L);
            dto.setDetalles(Collections.emptyList());
            assertThrows(VentaInvalidaException.class, () -> ventaService.crear(dto));
            verifyNoInteractions(empleadoClient, ventaRepository);
        }

        @Test @DisplayName("Empleado inexistente → VentaInvalidaException")
        void empleadoNoExiste() {
            when(empleadoClient.obtenerEmpleadoPorId(99L)).thenThrow(mock(FeignException.NotFound.class));
            VentaInvalidaException ex = assertThrows(VentaInvalidaException.class,
                    () -> ventaService.crear(crearVentaDTO(99L, null, 10L, 1, null)));
            assertTrue(ex.getMessage().contains("empleado"));
        }

        @Test @DisplayName("Empleado inactivo → VentaInvalidaException")
        void empleadoInactivo() {
            when(empleadoClient.obtenerEmpleadoPorId(1L)).thenReturn(empleado(1L, false));
            assertThrows(VentaInvalidaException.class,
                    () -> ventaService.crear(crearVentaDTO(1L, null, 10L, 1, null)));
        }

        @Test @DisplayName("Servicio empleados caído → ServicioNoDisponibleException")
        void empleadoServiceCaido() {
            when(empleadoClient.obtenerEmpleadoPorId(1L)).thenThrow(mock(FeignException.class));
            assertThrows(ServicioNoDisponibleException.class,
                    () -> ventaService.crear(crearVentaDTO(1L, null, 10L, 1, null)));
        }

        @Test @DisplayName("Cliente inexistente → VentaInvalidaException")
        void clienteNoExiste() {
            when(empleadoClient.obtenerEmpleadoPorId(1L)).thenReturn(empleado(1L, true));
            when(clienteClient.obtenerClientePorId(99L)).thenThrow(mock(FeignException.NotFound.class));
            assertThrows(VentaInvalidaException.class,
                    () -> ventaService.crear(crearVentaDTO(1L, 99L, 10L, 1, null)));
        }

        @Test @DisplayName("Cliente inactivo → VentaInvalidaException")
        void clienteInactivo() {
            when(empleadoClient.obtenerEmpleadoPorId(1L)).thenReturn(empleado(1L, true));
            when(clienteClient.obtenerClientePorId(2L)).thenReturn(cliente(2L, false));
            assertThrows(VentaInvalidaException.class,
                    () -> ventaService.crear(crearVentaDTO(1L, 2L, 10L, 1, null)));
        }

        @Test @DisplayName("Servicio clientes caído → ServicioNoDisponibleException")
        void clienteServiceCaido() {
            when(empleadoClient.obtenerEmpleadoPorId(1L)).thenReturn(empleado(1L, true));
            when(clienteClient.obtenerClientePorId(2L)).thenThrow(mock(FeignException.class));
            assertThrows(ServicioNoDisponibleException.class,
                    () -> ventaService.crear(crearVentaDTO(1L, 2L, 10L, 1, null)));
        }

        @Test @DisplayName("Producto inexistente → VentaInvalidaException")
        void productoNoExiste() {
            when(empleadoClient.obtenerEmpleadoPorId(1L)).thenReturn(empleado(1L, true));
            when(catalogoClient.obtenerProductoPorId(99L)).thenThrow(mock(FeignException.NotFound.class));
            assertThrows(VentaInvalidaException.class,
                    () -> ventaService.crear(crearVentaDTO(1L, null, 99L, 1, null)));
        }

        @Test @DisplayName("Producto inactivo → VentaInvalidaException")
        void productoInactivo() {
            when(empleadoClient.obtenerEmpleadoPorId(1L)).thenReturn(empleado(1L, true));
            ProductoDTO p = producto(10L, "X", new BigDecimal("1000"), 5L);
            p.setActivo(false);
            when(catalogoClient.obtenerProductoPorId(10L)).thenReturn(p);
            assertThrows(VentaInvalidaException.class,
                    () -> ventaService.crear(crearVentaDTO(1L, null, 10L, 1, null)));
        }

        @Test @DisplayName("Servicio catalogo caído → ServicioNoDisponibleException")
        void catalogoServiceCaido() {
            when(empleadoClient.obtenerEmpleadoPorId(1L)).thenReturn(empleado(1L, true));
            when(catalogoClient.obtenerProductoPorId(10L)).thenThrow(mock(FeignException.class));
            assertThrows(ServicioNoDisponibleException.class,
                    () -> ventaService.crear(crearVentaDTO(1L, null, 10L, 1, null)));
        }

        @Test @DisplayName("Stock insuficiente → VentaInvalidaException")
        void stockInsuficiente() {
            when(empleadoClient.obtenerEmpleadoPorId(1L)).thenReturn(empleado(1L, true));
            when(catalogoClient.obtenerProductoPorId(10L)).thenReturn(producto(10L, "X", new BigDecimal("1000"), 5L));
            when(inventarioClient.obtenerStockPorProducto(10L)).thenReturn(stock(100L, 10L, 1));
            assertThrows(VentaInvalidaException.class,
                    () -> ventaService.crear(crearVentaDTO(1L, null, 10L, 5, null)));
        }

        @Test @DisplayName("Stock no registrado → VentaInvalidaException")
        void stockNoExiste() {
            when(empleadoClient.obtenerEmpleadoPorId(1L)).thenReturn(empleado(1L, true));
            when(catalogoClient.obtenerProductoPorId(10L)).thenReturn(producto(10L, "X", new BigDecimal("1000"), 5L));
            when(inventarioClient.obtenerStockPorProducto(10L)).thenThrow(mock(FeignException.NotFound.class));
            assertThrows(VentaInvalidaException.class,
                    () -> ventaService.crear(crearVentaDTO(1L, null, 10L, 5, null)));
        }

        @Test @DisplayName("Servicio inventario caído → ServicioNoDisponibleException")
        void inventarioServiceCaido() {
            when(empleadoClient.obtenerEmpleadoPorId(1L)).thenReturn(empleado(1L, true));
            when(catalogoClient.obtenerProductoPorId(10L)).thenReturn(producto(10L, "X", new BigDecimal("1000"), 5L));
            when(inventarioClient.obtenerStockPorProducto(10L)).thenThrow(mock(FeignException.class));
            assertThrows(ServicioNoDisponibleException.class,
                    () -> ventaService.crear(crearVentaDTO(1L, null, 10L, 1, null)));
        }
    }

    // ════════════════════════════════════════════════════════════
    // completarVenta() — descuenta stock vía Feign
    // ════════════════════════════════════════════════════════════

    @Nested @DisplayName("completarVenta()")
    class Completar {
        @Test @DisplayName("Venta PENDIENTE → registra SALIDA en inventario y marca COMPLETADA")
        void completarOk() {
            Venta v = crearVentaConDetalle(1L, "VTA-001", EstadoVenta.PENDIENTE, 10L, 3);
            when(ventaRepository.findById(1L)).thenReturn(Optional.of(v));
            when(inventarioClient.obtenerStockPorProducto(10L)).thenReturn(stock(100L, 10L, 50));
            when(ventaRepository.save(any(Venta.class))).thenAnswer(inv -> inv.getArgument(0));

            VentaResponseDTO r = ventaService.completarVenta(1L);

            assertEquals(EstadoVenta.COMPLETADA, r.getEstado());
            verify(inventarioClient).registrarMovimiento(any(MovimientoStockRequestDTO.class));
        }

        @Test @DisplayName("Venta no PENDIENTE → EstadoVentaInvalidoException")
        void completarNoPendiente() {
            Venta v = crearVenta(1L, "VTA-001", EstadoVenta.COMPLETADA);
            when(ventaRepository.findById(1L)).thenReturn(Optional.of(v));
            assertThrows(EstadoVentaInvalidoException.class, () -> ventaService.completarVenta(1L));
            verifyNoInteractions(inventarioClient);
        }

        @Test @DisplayName("Venta inexistente → RecursoNoEncontradoException")
        void completarNoExiste() {
            when(ventaRepository.findById(99L)).thenReturn(Optional.empty());
            assertThrows(RecursoNoEncontradoException.class, () -> ventaService.completarVenta(99L));
        }

        @Test @DisplayName("Stock insuficiente al completar → VentaInvalidaException")
        void completarStockInsuf() {
            Venta v = crearVentaConDetalle(1L, "VTA-001", EstadoVenta.PENDIENTE, 10L, 100);
            when(ventaRepository.findById(1L)).thenReturn(Optional.of(v));
            when(inventarioClient.obtenerStockPorProducto(10L)).thenReturn(stock(100L, 10L, 5));
            assertThrows(VentaInvalidaException.class, () -> ventaService.completarVenta(1L));
            verify(inventarioClient, never()).registrarMovimiento(any());
        }

        @Test @DisplayName("Stock no registrado al completar → VentaInvalidaException")
        void completarStockNoExiste() {
            Venta v = crearVentaConDetalle(1L, "VTA-001", EstadoVenta.PENDIENTE, 10L, 3);
            when(ventaRepository.findById(1L)).thenReturn(Optional.of(v));
            when(inventarioClient.obtenerStockPorProducto(10L)).thenThrow(mock(FeignException.NotFound.class));
            assertThrows(VentaInvalidaException.class, () -> ventaService.completarVenta(1L));
        }

        @Test @DisplayName("Servicio inventario caído al completar → ServicioNoDisponibleException")
        void completarInvCaido() {
            Venta v = crearVentaConDetalle(1L, "VTA-001", EstadoVenta.PENDIENTE, 10L, 3);
            when(ventaRepository.findById(1L)).thenReturn(Optional.of(v));
            when(inventarioClient.obtenerStockPorProducto(10L)).thenThrow(mock(FeignException.class));
            assertThrows(ServicioNoDisponibleException.class, () -> ventaService.completarVenta(1L));
        }
    }

    // ════════════════════════════════════════════════════════════
    // anularVenta()
    // ════════════════════════════════════════════════════════════

    @Nested @DisplayName("anularVenta()")
    class Anular {
        @Test @DisplayName("PENDIENTE → ANULADA")
        void anularOk() {
            Venta v = crearVenta(1L, "VTA-001", EstadoVenta.PENDIENTE);
            when(ventaRepository.findById(1L)).thenReturn(Optional.of(v));
            when(ventaRepository.save(any(Venta.class))).thenAnswer(inv -> inv.getArgument(0));
            assertEquals(EstadoVenta.ANULADA, ventaService.anularVenta(1L).getEstado());
        }

        @Test @DisplayName("Ya ANULADA → EstadoVentaInvalidoException")
        void anularYaAnulada() {
            Venta v = crearVenta(1L, "VTA-001", EstadoVenta.ANULADA);
            when(ventaRepository.findById(1L)).thenReturn(Optional.of(v));
            assertThrows(EstadoVentaInvalidoException.class, () -> ventaService.anularVenta(1L));
        }

        @Test @DisplayName("No existe → RecursoNoEncontradoException")
        void anularNoExiste() {
            when(ventaRepository.findById(99L)).thenReturn(Optional.empty());
            assertThrows(RecursoNoEncontradoException.class, () -> ventaService.anularVenta(99L));
        }
    }

    // ════════════════════════════════════════════════════════════
    // HELPERS
    // ════════════════════════════════════════════════════════════

    private VentaRequestDTO crearVentaDTO(Long empleadoId, Long clienteId, Long productoId,
                                          int cantidad, BigDecimal descuento) {
        DetalleVentaRequestDTO d = new DetalleVentaRequestDTO();
        d.setProductoId(productoId);
        d.setCantidad(cantidad);
        d.setDescuentoUnitario(descuento);

        VentaRequestDTO dto = new VentaRequestDTO();
        dto.setEmpleadoId(empleadoId);
        dto.setClienteId(clienteId);
        dto.setDetalles(List.of(d));
        return dto;
    }

    private Venta crearVenta(Long id, String numero, EstadoVenta estado) {
        Venta v = new Venta();
        v.setId(id);
        v.setNumeroVenta(numero);
        v.setEmpleadoId(1L);
        v.setEstado(estado);
        v.setSubtotal(BigDecimal.ZERO);
        v.setDescuentoTotal(BigDecimal.ZERO);
        v.setIva(BigDecimal.ZERO);
        v.setTotal(BigDecimal.ZERO);
        return v;
    }

    private Venta crearVentaConDetalle(Long id, String numero, EstadoVenta estado, Long productoId, int cantidad) {
        Venta v = crearVenta(id, numero, estado);
        DetalleVenta d = new DetalleVenta();
        d.setProductoId(productoId);
        d.setNombreProducto("Producto");
        d.setCantidad(cantidad);
        d.setPrecioUnitario(new BigDecimal("1000.00"));
        d.setDescuentoUnitario(BigDecimal.ZERO);
        d.setSubtotalLinea(new BigDecimal("1000.00").multiply(BigDecimal.valueOf(cantidad)));
        d.setVenta(v);
        v.getDetalles().add(d);
        return v;
    }

    private EmpleadoDTO empleado(Long id, boolean activo) {
        EmpleadoDTO e = new EmpleadoDTO();
        e.setId(id);
        e.setRut("11.111.111-1");
        e.setNombre("Empleado");
        e.setApellido("Test");
        e.setActivo(activo);
        return e;
    }

    private ClienteDTO cliente(Long id, boolean activo) {
        ClienteDTO c = new ClienteDTO();
        c.setId(id);
        c.setRut("22.222.222-2");
        c.setNombre("Cliente");
        c.setApellido("Test");
        c.setActivo(activo);
        return c;
    }

    private ProductoDTO producto(Long id, String nombre, BigDecimal precio, Long categoriaId) {
        ProductoDTO p = new ProductoDTO();
        p.setId(id);
        p.setNombre(nombre);
        p.setPrecio(precio);
        p.setCategoriaId(categoriaId);
        p.setActivo(true);
        return p;
    }

    private StockDTO stock(Long id, Long productoId, int cantidadActual) {
        StockDTO s = new StockDTO();
        s.setId(id);
        s.setProductoId(productoId);
        s.setCantidadActual(cantidadActual);
        s.setCantidadMinima(5);
        return s;
    }

    private PromocionDTO promo(Long id, Long productoId, Long categoriaId, BigDecimal porcentaje) {
        PromocionDTO p = new PromocionDTO();
        p.setId(id);
        p.setNombre("Promo");
        p.setProductoId(productoId);
        p.setCategoriaId(categoriaId);
        p.setPorcentajeDescuento(porcentaje);
        p.setActivo(true);
        return p;
    }
}
