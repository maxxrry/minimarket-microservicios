package com.minimarket.mspagos.controller;

import com.minimarket.mspagos.dto.CambioEstadoDTO;
import com.minimarket.mspagos.dto.PagoRequestDTO;
import com.minimarket.mspagos.dto.PagoResponseDTO;
import com.minimarket.mspagos.model.EstadoPago;
import com.minimarket.mspagos.model.MetodoPago;
import com.minimarket.mspagos.service.PagoService;
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
@RequiredArgsConstructor
public class PagoController {

    private final PagoService pagoService;

    @GetMapping
    public ResponseEntity<List<PagoResponseDTO>> listarTodos() {
        return ResponseEntity.ok(pagoService.listarTodos());
    }

    @GetMapping("/{id}")
    public ResponseEntity<PagoResponseDTO> buscarPorId(@PathVariable Long id) {
        return ResponseEntity.ok(pagoService.buscarPorId(id));
    }

    @GetMapping("/transaccion/{numero}")
    public ResponseEntity<PagoResponseDTO> buscarPorNumeroTransaccion(
            @PathVariable String numero) {
        return ResponseEntity.ok(pagoService.buscarPorNumeroTransaccion(numero));
    }

    @GetMapping("/venta/{ventaId}")
    public ResponseEntity<List<PagoResponseDTO>> buscarPorVenta(
            @PathVariable Long ventaId) {
        return ResponseEntity.ok(pagoService.buscarPorVenta(ventaId));
    }

    @GetMapping("/estado/{estado}")
    public ResponseEntity<List<PagoResponseDTO>> buscarPorEstado(
            @PathVariable EstadoPago estado) {
        return ResponseEntity.ok(pagoService.buscarPorEstado(estado));
    }

    @GetMapping("/metodo/{metodoPago}")
    public ResponseEntity<List<PagoResponseDTO>> buscarPorMetodoPago(
            @PathVariable MetodoPago metodoPago) {
        return ResponseEntity.ok(pagoService.buscarPorMetodoPago(metodoPago));
    }

    @PostMapping
    public ResponseEntity<PagoResponseDTO> crear(
            @Valid @RequestBody PagoRequestDTO dto) {
        PagoResponseDTO creado = pagoService.crear(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(creado);
    }

    @PutMapping("/{id}")
    public ResponseEntity<PagoResponseDTO> actualizar(
            @PathVariable Long id,
            @Valid @RequestBody PagoRequestDTO dto) {
        return ResponseEntity.ok(pagoService.actualizar(id, dto));
    }

    /**
     * Cambia el estado del pago (con validación de transiciones permitidas).
     */
    @PatchMapping("/{id}/estado")
    public ResponseEntity<PagoResponseDTO> cambiarEstado(
            @PathVariable Long id,
            @Valid @RequestBody CambioEstadoDTO dto) {
        return ResponseEntity.ok(pagoService.cambiarEstado(id, dto.getNuevoEstado()));
    }

    /**
     * Solo elimina pagos en estado PENDIENTE.
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {
        pagoService.eliminar(id);
        return ResponseEntity.noContent().build();
    }
}