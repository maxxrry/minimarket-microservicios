package com.minimarket.mspagos.controller;

import com.minimarket.mspagos.dto.CambioEstadoDTO;
import com.minimarket.mspagos.dto.PagoRequestDTO;
import com.minimarket.mspagos.dto.PagoResponseDTO;
import com.minimarket.mspagos.model.EstadoPago;
import com.minimarket.mspagos.model.MetodoPago;
import com.minimarket.mspagos.service.PagoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Controlador REST de Pagos.
 */
@RestController
@RequestMapping("/api/pagos")
@Tag(name = "Pagos", description = "Endpoints para gestionar pagos")
@RequiredArgsConstructor
public class PagoController {

    private final PagoService pagoService;

    @GetMapping
    @Operation(summary = "Listar todos los pagos", description = "Retorna una lista con todos los pagos registrados")
    public ResponseEntity<List<PagoResponseDTO>> listarTodos() {
        return ResponseEntity.ok(pagoService.listarTodos());
    }

    @GetMapping("/{id}")
    @Operation(summary = "Obtener pago por ID", description = "Retorna un pago específico según su ID")
    public ResponseEntity<PagoResponseDTO> buscarPorId(
            @Parameter(description = "ID del pago a buscar", example = "1")
            @PathVariable Long id) {
        return ResponseEntity.ok(pagoService.buscarPorId(id));
    }

    @GetMapping("/transaccion/{numero}")
    @Operation(summary = "Buscar pago por número de transacción", description = "Retorna un pago según su número de transacción")
    public ResponseEntity<PagoResponseDTO> buscarPorNumeroTransaccion(
            @Parameter(description = "Número de transacción", example = "TXN-20260115-001")
            @PathVariable String numero) {
        return ResponseEntity.ok(pagoService.buscarPorNumeroTransaccion(numero));
    }

    @GetMapping("/venta/{ventaId}")
    @Operation(summary = "Buscar pagos por ID de venta", description = "Retorna los pagos asociados a una venta")
    public ResponseEntity<List<PagoResponseDTO>> buscarPorVenta(
            @Parameter(description = "ID de la venta", example = "1")
            @PathVariable Long ventaId) {
        return ResponseEntity.ok(pagoService.buscarPorVenta(ventaId));
    }

    @GetMapping("/estado/{estado}")
    @Operation(summary = "Buscar pagos por estado", description = "Retorna los pagos con un estado específico")
    public ResponseEntity<List<PagoResponseDTO>> buscarPorEstado(
            @Parameter(description = "Estado del pago", example = "PENDIENTE")
            @PathVariable EstadoPago estado) {
        return ResponseEntity.ok(pagoService.buscarPorEstado(estado));
    }

    @GetMapping("/metodo/{metodoPago}")
    @Operation(summary = "Buscar pagos por método", description = "Retorna los pagos realizados con un método específico")
    public ResponseEntity<List<PagoResponseDTO>> buscarPorMetodoPago(
            @Parameter(description = "Método de pago", example = "EFECTIVO")
            @PathVariable MetodoPago metodoPago) {
        return ResponseEntity.ok(pagoService.buscarPorMetodoPago(metodoPago));
    }

    @PostMapping
    @Operation(summary = "Crear nuevo pago", description = "Crea un nuevo pago en el sistema")
    public ResponseEntity<PagoResponseDTO> crear(
            @Valid @RequestBody PagoRequestDTO dto) {
        PagoResponseDTO creado = pagoService.crear(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(creado);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Actualizar pago", description = "Actualiza los datos de un pago existente")
    public ResponseEntity<PagoResponseDTO> actualizar(
            @Parameter(description = "ID del pago a actualizar", example = "1")
            @PathVariable Long id,
            @Valid @RequestBody PagoRequestDTO dto) {
        return ResponseEntity.ok(pagoService.actualizar(id, dto));
    }

    /**
     * Cambia el estado del pago (con validación de transiciones permitidas).
     */
    @PatchMapping("/{id}/estado")
    @Operation(summary = "Cambiar estado del pago", description = "Cambia el estado de un pago con validación de transiciones")
    public ResponseEntity<PagoResponseDTO> cambiarEstado(
            @Parameter(description = "ID del pago", example = "1")
            @PathVariable Long id,
            @Valid @RequestBody CambioEstadoDTO dto) {
        return ResponseEntity.ok(pagoService.cambiarEstado(id, dto.getNuevoEstado()));
    }

    /**
     * Solo elimina pagos en estado PENDIENTE.
     */
    @DeleteMapping("/{id}")
    @Operation(summary = "Eliminar pago", description = "Elimina un pago (solo si está en estado PENDIENTE)")
    public ResponseEntity<Void> eliminar(
            @Parameter(description = "ID del pago a eliminar", example = "1")
            @PathVariable Long id) {
        pagoService.eliminar(id);
        return ResponseEntity.noContent().build();
    }
}