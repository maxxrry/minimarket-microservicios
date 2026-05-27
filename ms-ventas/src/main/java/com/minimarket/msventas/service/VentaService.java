package com.minimarket.msventas.service;

import com.minimarket.msventas.client.CatalogoClient;
import com.minimarket.msventas.client.ClienteClient;
import com.minimarket.msventas.client.EmpleadoClient;
import com.minimarket.msventas.client.InventarioClient;
import com.minimarket.msventas.client.PromocionClient;
import com.minimarket.msventas.client.dto.ClienteDTO;
import com.minimarket.msventas.client.dto.EmpleadoDTO;
import com.minimarket.msventas.client.dto.MovimientoStockRequestDTO;
import com.minimarket.msventas.client.dto.ProductoDTO;
import com.minimarket.msventas.client.dto.PromocionDTO;
import com.minimarket.msventas.client.dto.StockDTO;
import com.minimarket.msventas.dto.DetalleVentaRequestDTO;
import com.minimarket.msventas.dto.DetalleVentaResponseDTO;
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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Capa de servicio del microservicio ms-ventas.
 * Orquesta la creación de ventas integrando vía Feign Client con:
 *   - ms-catalogo    (validar producto y obtener nombre/precio reales)
 *   - ms-clientes    (validar cliente si se especifica)
 *   - ms-empleados   (validar empleado vendedor)
 *   - ms-promociones (aplicar descuentos automáticos)
 *   - ms-inventario  (validar stock disponible y descontar al completar)
 *
 * Reglas de negocio principales:
 *   - El número de venta se genera automáticamente (VTA-YYYYMM-NNNNN)
 *   - El subtotal, IVA y total se calculan automáticamente
 *   - El IVA en Chile es 19%
 *   - Solo se pueden modificar ventas en estado PENDIENTE
 *   - Una venta ANULADA no puede cambiar de estado
 *   - El stock se descuenta solo cuando la venta pasa a COMPLETADA
 */
@Service
public class VentaService {

    private static final Logger log = LoggerFactory.getLogger(VentaService.class);

    private static final BigDecimal TASA_IVA = new BigDecimal("0.19");
    private static final BigDecimal CIEN = new BigDecimal("100");

    @Autowired
    private VentaRepository ventaRepository;

    @Autowired
    private CatalogoClient catalogoClient;

    @Autowired
    private ClienteClient clienteClient;

    @Autowired
    private EmpleadoClient empleadoClient;

    @Autowired
    private PromocionClient promocionClient;

    @Autowired
    private InventarioClient inventarioClient;

    // ════════════════════════════════════════════════════════════
    // OPERACIONES DE CONSULTA
    // ════════════════════════════════════════════════════════════

    public List<VentaResponseDTO> listarTodas() {
        log.info("Listando todas las ventas");
        return ventaRepository.findAll().stream()
                .map(this::convertirAResponseDTO)
                .collect(Collectors.toList());
    }

    public VentaResponseDTO obtenerPorId(Long id) {
        log.info("Buscando venta con ID: {}", id);
        Venta venta = ventaRepository.findById(id)
                .orElseThrow(() -> {
                    log.warn("Venta con ID {} no encontrada", id);
                    return new RecursoNoEncontradoException(
                            "Venta con ID " + id + " no encontrada");
                });
        return convertirAResponseDTO(venta);
    }

    public VentaResponseDTO obtenerPorNumeroVenta(String numeroVenta) {
        log.info("Buscando venta con número: {}", numeroVenta);
        Venta venta = ventaRepository.findByNumeroVenta(numeroVenta)
                .orElseThrow(() -> new RecursoNoEncontradoException(
                        "Venta con número " + numeroVenta + " no encontrada"));
        return convertirAResponseDTO(venta);
    }

    public List<VentaResponseDTO> listarPorCliente(Long clienteId) {
        log.info("Listando ventas del cliente ID: {}", clienteId);
        return ventaRepository.findByClienteIdOrderByFechaVentaDesc(clienteId).stream()
                .map(this::convertirAResponseDTO)
                .collect(Collectors.toList());
    }

    public List<VentaResponseDTO> listarPorEmpleado(Long empleadoId) {
        log.info("Listando ventas del empleado ID: {}", empleadoId);
        return ventaRepository.findByEmpleadoIdOrderByFechaVentaDesc(empleadoId).stream()
                .map(this::convertirAResponseDTO)
                .collect(Collectors.toList());
    }

    public List<VentaResponseDTO> listarPorEstado(EstadoVenta estado) {
        log.info("Listando ventas con estado: {}", estado);
        return ventaRepository.findByEstadoOrderByFechaVentaDesc(estado).stream()
                .map(this::convertirAResponseDTO)
                .collect(Collectors.toList());
    }

    // ════════════════════════════════════════════════════════════
    // OPERACIONES DE CREACIÓN Y MODIFICACIÓN
    // ════════════════════════════════════════════════════════════

    /**
     * Crea una nueva venta con sus detalles.
     * Orquesta llamadas a ms-empleados, ms-clientes, ms-catalogo,
     * ms-promociones y ms-inventario antes de persistir.
     */
    @Transactional
    public VentaResponseDTO crear(VentaRequestDTO dto) {
        log.info("Creando nueva venta para empleado ID: {}", dto.getEmpleadoId());

        if (dto.getDetalles() == null || dto.getDetalles().isEmpty()) {
            throw new VentaInvalidaException("La venta debe tener al menos un detalle");
        }

        validarEmpleado(dto.getEmpleadoId());
        if (dto.getClienteId() != null) {
            validarCliente(dto.getClienteId());
        }

        Venta venta = new Venta();
        venta.setNumeroVenta(generarNumeroVenta());
        venta.setClienteId(dto.getClienteId());
        venta.setEmpleadoId(dto.getEmpleadoId());
        venta.setEstado(EstadoVenta.PENDIENTE);

        BigDecimal subtotalAcumulado = BigDecimal.ZERO;
        BigDecimal descuentoAcumulado = BigDecimal.ZERO;

        for (DetalleVentaRequestDTO detalleDTO : dto.getDetalles()) {
            ProductoDTO producto = obtenerProductoValidado(detalleDTO.getProductoId());
            validarStockDisponible(producto.getId(), detalleDTO.getCantidad());

            BigDecimal precioUnitario = producto.getPrecio();
            BigDecimal descuento = resolverDescuento(detalleDTO, producto);

            DetalleVenta detalle = new DetalleVenta();
            detalle.setVenta(venta);
            detalle.setProductoId(producto.getId());
            detalle.setNombreProducto(producto.getNombre());
            detalle.setCantidad(detalleDTO.getCantidad());
            detalle.setPrecioUnitario(precioUnitario);
            detalle.setDescuentoUnitario(descuento);

            BigDecimal precioFinal = precioUnitario.subtract(descuento);
            BigDecimal subtotalLinea = precioFinal.multiply(
                    BigDecimal.valueOf(detalleDTO.getCantidad()));
            detalle.setSubtotalLinea(subtotalLinea);

            subtotalAcumulado = subtotalAcumulado.add(
                    precioUnitario.multiply(BigDecimal.valueOf(detalleDTO.getCantidad())));
            descuentoAcumulado = descuentoAcumulado.add(
                    descuento.multiply(BigDecimal.valueOf(detalleDTO.getCantidad())));

            venta.getDetalles().add(detalle);
        }

        BigDecimal baseImponible = subtotalAcumulado.subtract(descuentoAcumulado);
        BigDecimal iva = baseImponible.multiply(TASA_IVA).setScale(2, RoundingMode.HALF_UP);
        BigDecimal total = baseImponible.add(iva).setScale(2, RoundingMode.HALF_UP);

        venta.setSubtotal(subtotalAcumulado.setScale(2, RoundingMode.HALF_UP));
        venta.setDescuentoTotal(descuentoAcumulado.setScale(2, RoundingMode.HALF_UP));
        venta.setIva(iva);
        venta.setTotal(total);

        Venta guardada = ventaRepository.save(venta);
        log.info("Venta creada exitosamente. Número: {}, Total: ${}",
                guardada.getNumeroVenta(), guardada.getTotal());

        return convertirAResponseDTO(guardada);
    }

    /**
     * Marca una venta PENDIENTE como COMPLETADA y descuenta el stock
     * de cada producto vía ms-inventario (movimientos tipo SALIDA).
     */
    @Transactional
    public VentaResponseDTO completarVenta(Long id) {
        log.info("Completando venta con ID: {}", id);

        Venta venta = ventaRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException(
                        "Venta con ID " + id + " no encontrada"));

        if (venta.getEstado() != EstadoVenta.PENDIENTE) {
            log.warn("No se puede completar venta en estado: {}", venta.getEstado());
            throw new EstadoVentaInvalidoException(
                    "Solo se pueden completar ventas en estado PENDIENTE. " +
                            "Estado actual: " + venta.getEstado());
        }

        descontarStockDeDetalles(venta);

        venta.setEstado(EstadoVenta.COMPLETADA);
        Venta guardada = ventaRepository.save(venta);
        log.info("Venta {} marcada como COMPLETADA", venta.getNumeroVenta());
        return convertirAResponseDTO(guardada);
    }

    @Transactional
    public VentaResponseDTO anularVenta(Long id) {
        log.info("Anulando venta con ID: {}", id);

        Venta venta = ventaRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException(
                        "Venta con ID " + id + " no encontrada"));

        if (venta.getEstado() == EstadoVenta.ANULADA) {
            throw new EstadoVentaInvalidoException("La venta ya está anulada");
        }

        venta.setEstado(EstadoVenta.ANULADA);
        Venta guardada = ventaRepository.save(venta);
        log.info("Venta {} ANULADA", venta.getNumeroVenta());
        return convertirAResponseDTO(guardada);
    }

    // ════════════════════════════════════════════════════════════
    // INTEGRACIONES VÍA FEIGN
    // ════════════════════════════════════════════════════════════

    private void validarEmpleado(Long empleadoId) {
        try {
            EmpleadoDTO empleado = empleadoClient.obtenerEmpleadoPorId(empleadoId);
            if (Boolean.FALSE.equals(empleado.getActivo())) {
                throw new VentaInvalidaException(
                        "El empleado ID " + empleadoId + " está inactivo");
            }
            log.debug("Empleado validado: {} {}", empleado.getNombre(), empleado.getApellido());
        } catch (FeignException.NotFound ex) {
            throw new VentaInvalidaException(
                    "El empleado ID " + empleadoId + " no existe");
        } catch (FeignException ex) {
            throw new ServicioNoDisponibleException(
                    "No se pudo validar el empleado en ms-empleados", ex);
        }
    }

    private void validarCliente(Long clienteId) {
        try {
            ClienteDTO cliente = clienteClient.obtenerClientePorId(clienteId);
            if (Boolean.FALSE.equals(cliente.getActivo())) {
                throw new VentaInvalidaException(
                        "El cliente ID " + clienteId + " está inactivo");
            }
            log.debug("Cliente validado: {} {}", cliente.getNombre(), cliente.getApellido());
        } catch (FeignException.NotFound ex) {
            throw new VentaInvalidaException(
                    "El cliente ID " + clienteId + " no existe");
        } catch (FeignException ex) {
            throw new ServicioNoDisponibleException(
                    "No se pudo validar el cliente en ms-clientes", ex);
        }
    }

    private ProductoDTO obtenerProductoValidado(Long productoId) {
        try {
            ProductoDTO producto = catalogoClient.obtenerProductoPorId(productoId);
            if (Boolean.FALSE.equals(producto.getActivo())) {
                throw new VentaInvalidaException(
                        "El producto ID " + productoId + " está dado de baja");
            }
            return producto;
        } catch (FeignException.NotFound ex) {
            throw new VentaInvalidaException(
                    "El producto ID " + productoId + " no existe en el catálogo");
        } catch (FeignException ex) {
            throw new ServicioNoDisponibleException(
                    "No se pudo obtener el producto desde ms-catalogo", ex);
        }
    }

    private void validarStockDisponible(Long productoId, Integer cantidadRequerida) {
        try {
            StockDTO stock = inventarioClient.obtenerStockPorProducto(productoId);
            if (stock.getCantidadActual() < cantidadRequerida) {
                throw new VentaInvalidaException(
                        "Stock insuficiente para producto ID " + productoId +
                                ". Disponible: " + stock.getCantidadActual() +
                                ", solicitado: " + cantidadRequerida);
            }
        } catch (FeignException.NotFound ex) {
            throw new VentaInvalidaException(
                    "No existe registro de stock para el producto ID " + productoId);
        } catch (FeignException ex) {
            throw new ServicioNoDisponibleException(
                    "No se pudo validar el stock en ms-inventario", ex);
        }
    }

    /**
     * Si el cliente envió un descuento explícito, se respeta.
     * Si no, se consulta ms-promociones y se aplica la mejor promoción activa
     * para el producto o su categoría.
     */
    private BigDecimal resolverDescuento(DetalleVentaRequestDTO detalleDTO, ProductoDTO producto) {
        BigDecimal descuentoManual = detalleDTO.getDescuentoUnitario();
        if (descuentoManual != null && descuentoManual.compareTo(BigDecimal.ZERO) > 0) {
            return descuentoManual;
        }

        BigDecimal mejorPorcentaje = buscarMejorPromocion(producto);
        if (mejorPorcentaje.compareTo(BigDecimal.ZERO) == 0) {
            return BigDecimal.ZERO;
        }

        BigDecimal descuento = producto.getPrecio()
                .multiply(mejorPorcentaje)
                .divide(CIEN, 2, RoundingMode.HALF_UP);
        log.info("Aplicando promoción {}% al producto ID {} (descuento ${})",
                mejorPorcentaje, producto.getId(), descuento);
        return descuento;
    }

    private BigDecimal buscarMejorPromocion(ProductoDTO producto) {
        List<PromocionDTO> candidatas = new ArrayList<>();
        try {
            candidatas.addAll(promocionClient.listarPorProducto(producto.getId()));
            if (producto.getCategoriaId() != null) {
                candidatas.addAll(promocionClient.listarPorCategoria(producto.getCategoriaId()));
            }
        } catch (FeignException ex) {
            // Las promociones son opcionales: si el servicio cae, la venta sigue sin descuento.
            log.warn("No se pudieron consultar promociones para producto {}: {}",
                    producto.getId(), ex.getMessage());
            return BigDecimal.ZERO;
        }

        return candidatas.stream()
                .filter(p -> Boolean.TRUE.equals(p.getActivo()))
                .map(PromocionDTO::getPorcentajeDescuento)
                .filter(p -> p != null && p.compareTo(BigDecimal.ZERO) > 0)
                .max(Comparator.naturalOrder())
                .orElse(BigDecimal.ZERO);
    }

    private void descontarStockDeDetalles(Venta venta) {
        for (DetalleVenta detalle : venta.getDetalles()) {
            try {
                StockDTO stock = inventarioClient.obtenerStockPorProducto(detalle.getProductoId());
                if (stock.getCantidadActual() < detalle.getCantidad()) {
                    throw new VentaInvalidaException(
                            "Stock insuficiente al completar la venta para producto ID " +
                                    detalle.getProductoId());
                }
                MovimientoStockRequestDTO movimiento = MovimientoStockRequestDTO.builder()
                        .stockId(stock.getId())
                        .tipoMovimiento("SALIDA")
                        .cantidad(detalle.getCantidad())
                        .motivo("Venta " + venta.getNumeroVenta())
                        .build();
                inventarioClient.registrarMovimiento(movimiento);
                log.info("Stock descontado: producto {} cantidad {}",
                        detalle.getProductoId(), detalle.getCantidad());
            } catch (FeignException.NotFound ex) {
                throw new VentaInvalidaException(
                        "No existe registro de stock para producto ID " + detalle.getProductoId());
            } catch (FeignException ex) {
                throw new ServicioNoDisponibleException(
                        "Error al descontar stock en ms-inventario", ex);
            }
        }
    }

    // ════════════════════════════════════════════════════════════
    // MÉTODOS PRIVADOS
    // ════════════════════════════════════════════════════════════

    private String generarNumeroVenta() {
        String prefijo = "VTA-" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMM"));
        long ventasDelMes = ventaRepository.countByNumeroVentaStartingWith(prefijo);
        long correlativo = ventasDelMes + 1;
        return String.format("%s-%05d", prefijo, correlativo);
    }

    private VentaResponseDTO convertirAResponseDTO(Venta v) {
        List<DetalleVentaResponseDTO> detallesDTO = v.getDetalles().stream()
                .map(this::convertirDetalleAResponseDTO)
                .collect(Collectors.toList());

        return VentaResponseDTO.builder()
                .id(v.getId())
                .numeroVenta(v.getNumeroVenta())
                .clienteId(v.getClienteId())
                .empleadoId(v.getEmpleadoId())
                .subtotal(v.getSubtotal())
                .descuentoTotal(v.getDescuentoTotal())
                .iva(v.getIva())
                .total(v.getTotal())
                .estado(v.getEstado())
                .detalles(detallesDTO)
                .fechaVenta(v.getFechaVenta())
                .fechaActualizacion(v.getFechaActualizacion())
                .build();
    }

    private DetalleVentaResponseDTO convertirDetalleAResponseDTO(DetalleVenta d) {
        return DetalleVentaResponseDTO.builder()
                .id(d.getId())
                .productoId(d.getProductoId())
                .nombreProducto(d.getNombreProducto())
                .cantidad(d.getCantidad())
                .precioUnitario(d.getPrecioUnitario())
                .descuentoUnitario(d.getDescuentoUnitario())
                .subtotalLinea(d.getSubtotalLinea())
                .build();
    }
}
