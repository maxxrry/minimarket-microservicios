package com.minimarket.msventas.service;

import com.minimarket.msventas.dto.DetalleVentaRequestDTO;
import com.minimarket.msventas.dto.DetalleVentaResponseDTO;
import com.minimarket.msventas.dto.VentaRequestDTO;
import com.minimarket.msventas.dto.VentaResponseDTO;
import com.minimarket.msventas.exception.EstadoVentaInvalidoException;
import com.minimarket.msventas.exception.RecursoNoEncontradoException;
import com.minimarket.msventas.exception.VentaInvalidaException;
import com.minimarket.msventas.model.DetalleVenta;
import com.minimarket.msventas.model.EstadoVenta;
import com.minimarket.msventas.model.Venta;
import com.minimarket.msventas.repository.VentaRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Capa de servicio del microservicio ms-ventas.
 * Gestiona la creación, consulta y cambios de estado de las ventas.
 *
 * Reglas de negocio principales:
 *   - El número de venta se genera automáticamente (VTA-YYYYMM-NNNNN)
 *   - El subtotal, IVA y total se calculan automáticamente
 *   - El IVA en Chile es 19%
 *   - Solo se pueden modificar ventas en estado PENDIENTE
 *   - Una venta ANULADA no puede cambiar de estado
 */
@Service
public class VentaService {

    private static final Logger log = LoggerFactory.getLogger(VentaService.class);

    /**
     * IVA en Chile: 19%.
     */
    private static final BigDecimal TASA_IVA = new BigDecimal("0.19");

    @Autowired
    private VentaRepository ventaRepository;

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
     * Es @Transactional: si falla cualquier paso, se revierte todo.
     *
     * Pasos:
     *   1. Validar que tenga al menos un detalle
     *   2. Generar número de venta correlativo
     *   3. Crear la entidad Venta
     *   4. Crear cada DetalleVenta y calcular su subtotal de línea
     *   5. Calcular subtotal, descuento total, IVA y total de la venta
     *   6. Guardar todo en cascada
     */
    @Transactional
    public VentaResponseDTO crear(VentaRequestDTO dto) {
        log.info("Creando nueva venta para empleado ID: {}", dto.getEmpleadoId());

        // Validación adicional (Bean Validation ya valida que no esté vacío)
        if (dto.getDetalles() == null || dto.getDetalles().isEmpty()) {
            throw new VentaInvalidaException("La venta debe tener al menos un detalle");
        }

        // Crear la venta base
        Venta venta = new Venta();
        venta.setNumeroVenta(generarNumeroVenta());
        venta.setClienteId(dto.getClienteId());
        venta.setEmpleadoId(dto.getEmpleadoId());
        venta.setEstado(EstadoVenta.PENDIENTE);

        // Crear los detalles y calcular subtotales de línea
        BigDecimal subtotalAcumulado = BigDecimal.ZERO;
        BigDecimal descuentoAcumulado = BigDecimal.ZERO;

        for (DetalleVentaRequestDTO detalleDTO : dto.getDetalles()) {
            DetalleVenta detalle = new DetalleVenta();
            detalle.setVenta(venta);
            detalle.setProductoId(detalleDTO.getProductoId());
            detalle.setNombreProducto(detalleDTO.getNombreProducto());
            detalle.setCantidad(detalleDTO.getCantidad());
            detalle.setPrecioUnitario(detalleDTO.getPrecioUnitario());

            BigDecimal descuento = detalleDTO.getDescuentoUnitario() != null
                    ? detalleDTO.getDescuentoUnitario()
                    : BigDecimal.ZERO;
            detalle.setDescuentoUnitario(descuento);

            // Calcular subtotal de línea: (precio - descuento) × cantidad
            BigDecimal precioFinal = detalleDTO.getPrecioUnitario().subtract(descuento);
            BigDecimal subtotalLinea = precioFinal.multiply(
                    BigDecimal.valueOf(detalleDTO.getCantidad()));
            detalle.setSubtotalLinea(subtotalLinea);

            // Sumar a los acumuladores
            subtotalAcumulado = subtotalAcumulado.add(
                    detalleDTO.getPrecioUnitario()
                            .multiply(BigDecimal.valueOf(detalleDTO.getCantidad())));
            descuentoAcumulado = descuentoAcumulado.add(
                    descuento.multiply(BigDecimal.valueOf(detalleDTO.getCantidad())));

            venta.getDetalles().add(detalle);
        }

        // Calcular totales de la venta
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
     * Marca una venta PENDIENTE como COMPLETADA.
     * Solo se puede completar una venta que esté pendiente.
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

        venta.setEstado(EstadoVenta.COMPLETADA);
        Venta guardada = ventaRepository.save(venta);
        log.info("Venta {} marcada como COMPLETADA", venta.getNumeroVenta());
        return convertirAResponseDTO(guardada);
    }

    /**
     * Anula una venta. Una vez anulada NO puede revertirse.
     * No se puede anular una venta ya anulada.
     */
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
    // MÉTODOS PRIVADOS
    // ════════════════════════════════════════════════════════════

    /**
     * Genera el número de venta correlativo único.
     * Formato: VTA-YYYYMM-NNNNN (ej: VTA-202604-00001)
     */
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