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
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Capa de servicio: lógica de negocio de Pagos.
 * - Valida número de transacción único
 * - Valida transiciones de estado permitidas
 * - Convierte entre Entity y DTOs
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class PagoService {

    private final PagoRepository pagoRepository;

    @Transactional(readOnly = true)
    public List<PagoResponseDTO> listarTodos() {
        log.info("Listando todos los pagos");
        return pagoRepository.findAll().stream()
                .map(this::convertirAResponseDTO)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public PagoResponseDTO buscarPorId(Long id) {
        log.info("Buscando pago con ID: {}", id);
        Pago pago = pagoRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException(
                        "Pago con ID " + id + " no encontrado"));
        return convertirAResponseDTO(pago);
    }

    @Transactional(readOnly = true)
    public PagoResponseDTO buscarPorNumeroTransaccion(String numero) {
        log.info("Buscando pago con número de transacción: {}", numero);
        Pago pago = pagoRepository.findByNumeroTransaccion(numero)
                .orElseThrow(() -> new RecursoNoEncontradoException(
                        "Pago con número de transacción " + numero + " no encontrado"));
        return convertirAResponseDTO(pago);
    }

    @Transactional(readOnly = true)
    public List<PagoResponseDTO> buscarPorVenta(Long ventaId) {
        log.info("Listando pagos de la venta: {}", ventaId);
        return pagoRepository.findByVentaId(ventaId).stream()
                .map(this::convertirAResponseDTO)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<PagoResponseDTO> buscarPorEstado(EstadoPago estado) {
        log.info("Listando pagos con estado: {}", estado);
        return pagoRepository.findByEstado(estado).stream()
                .map(this::convertirAResponseDTO)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<PagoResponseDTO> buscarPorMetodoPago(MetodoPago metodoPago) {
        log.info("Listando pagos con método: {}", metodoPago);
        return pagoRepository.findByMetodoPago(metodoPago).stream()
                .map(this::convertirAResponseDTO)
                .collect(Collectors.toList());
    }

    /**
     * Crea un nuevo pago.
     * Reglas: número de transacción único, estado PENDIENTE por defecto.
     */
    public PagoResponseDTO crear(PagoRequestDTO dto) {
        log.info("Creando nuevo pago. Transacción: {}", dto.getNumeroTransaccion());

        if (pagoRepository.existsByNumeroTransaccion(dto.getNumeroTransaccion())) {
            log.warn("Número de transacción duplicado: {}", dto.getNumeroTransaccion());
            throw new PagoDuplicadoException(
                    "Ya existe un pago con el número de transacción: "
                            + dto.getNumeroTransaccion());
        }

        Pago pago = new Pago();
        pago.setNumeroTransaccion(dto.getNumeroTransaccion());
        pago.setMonto(dto.getMonto());
        pago.setMetodoPago(dto.getMetodoPago());
        pago.setVentaId(dto.getVentaId());
        pago.setObservaciones(dto.getObservaciones());
        pago.setEstado(EstadoPago.PENDIENTE);

        Pago guardado = pagoRepository.save(pago);
        log.info("Pago creado con ID: {}", guardado.getId());
        return convertirAResponseDTO(guardado);
    }

    public PagoResponseDTO actualizar(Long id, PagoRequestDTO dto) {
        log.info("Actualizando pago con ID: {}", id);

        Pago existente = pagoRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException(
                        "Pago con ID " + id + " no encontrado"));

        // Solo se permite actualizar pagos en estado PENDIENTE
        if (existente.getEstado() != EstadoPago.PENDIENTE) {
            throw new EstadoInvalidoException(
                    "Solo se pueden actualizar pagos en estado PENDIENTE. " +
                            "Estado actual: " + existente.getEstado());
        }

        // Validar número de transacción duplicado si cambió
        if (!existente.getNumeroTransaccion().equals(dto.getNumeroTransaccion()) &&
                pagoRepository.existsByNumeroTransaccion(dto.getNumeroTransaccion())) {
            throw new PagoDuplicadoException(
                    "Ya existe otro pago con el número de transacción: "
                            + dto.getNumeroTransaccion());
        }

        existente.setNumeroTransaccion(dto.getNumeroTransaccion());
        existente.setMonto(dto.getMonto());
        existente.setMetodoPago(dto.getMetodoPago());
        existente.setVentaId(dto.getVentaId());
        existente.setObservaciones(dto.getObservaciones());

        Pago guardado = pagoRepository.save(existente);
        log.info("Pago actualizado: ID {}", id);
        return convertirAResponseDTO(guardado);
    }

    /**
     * Cambia el estado de un pago validando transiciones permitidas.
     *
     * Reglas de transición:
     *  - PENDIENTE   → COMPLETADO, RECHAZADO
     *  - COMPLETADO  → REEMBOLSADO
     *  - RECHAZADO   → (ninguna, estado final)
     *  - REEMBOLSADO → (ninguna, estado final)
     */
    public PagoResponseDTO cambiarEstado(Long id, EstadoPago nuevoEstado) {
        log.info("Cambiando estado del pago {} a {}", id, nuevoEstado);

        Pago pago = pagoRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException(
                        "Pago con ID " + id + " no encontrado"));

        validarTransicionEstado(pago.getEstado(), nuevoEstado);

        pago.setEstado(nuevoEstado);
        Pago guardado = pagoRepository.save(pago);
        log.info("Pago {} cambió de estado a {}", id, nuevoEstado);
        return convertirAResponseDTO(guardado);
    }

    /**
     * Borrado físico SOLO si el pago está en estado PENDIENTE.
     * Pagos completados, rechazados o reembolsados NO se eliminan
     * (necesarios para auditoría contable).
     */
    public void eliminar(Long id) {
        log.info("Eliminando pago con ID: {}", id);
        Pago pago = pagoRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException(
                        "Pago con ID " + id + " no encontrado"));

        if (pago.getEstado() != EstadoPago.PENDIENTE) {
            throw new EstadoInvalidoException(
                    "Solo se pueden eliminar pagos en estado PENDIENTE. " +
                            "Estado actual: " + pago.getEstado());
        }

        pagoRepository.delete(pago);
        log.info("Pago eliminado: ID {}", id);
    }

    // ============================================
    // VALIDACIÓN DE TRANSICIONES DE ESTADO
    // ============================================

    private void validarTransicionEstado(EstadoPago actual, EstadoPago nuevo) {
        if (actual == nuevo) {
            throw new EstadoInvalidoException(
                    "El pago ya se encuentra en estado " + actual);
        }

        boolean transicionValida = switch (actual) {
            case PENDIENTE  -> nuevo == EstadoPago.COMPLETADO || nuevo == EstadoPago.RECHAZADO;
            case COMPLETADO -> nuevo == EstadoPago.REEMBOLSADO;
            case RECHAZADO, REEMBOLSADO -> false;
        };

        if (!transicionValida) {
            throw new EstadoInvalidoException(
                    "Transición de estado no permitida: " + actual + " → " + nuevo);
        }
    }

    // ============================================
    // CONVERSIÓN
    // ============================================

    private PagoResponseDTO convertirAResponseDTO(Pago pago) {
        return new PagoResponseDTO(
                pago.getId(),
                pago.getNumeroTransaccion(),
                pago.getMonto(),
                pago.getMetodoPago(),
                pago.getEstado(),
                pago.getVentaId(),
                pago.getObservaciones(),
                pago.getFechaCreacion(),
                pago.getFechaActualizacion()
        );
    }
}