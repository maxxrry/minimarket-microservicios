package com.minimarket.msventas.controller;

import com.minimarket.msventas.dto.VentaRequestDTO;
import com.minimarket.msventas.dto.VentaResponseDTO;
import com.minimarket.msventas.model.EstadoVenta;
import com.minimarket.msventas.service.VentaService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/ventas")
@Tag(name = "Ventas", description = "Endpoints para gestionar ventas del minimarket")
public class VentaController {

    private static final Logger log = LoggerFactory.getLogger(VentaController.class);

    @Autowired
    private VentaService ventaService;

    @GetMapping
    @Operation(summary = "Listar todas las ventas", description = "Retorna una lista con todas las ventas registradas")
    public ResponseEntity<List<VentaResponseDTO>> listarTodas() {
        log.info("GET /api/ventas");
        return ResponseEntity.ok(ventaService.listarTodas());
    }

    @GetMapping("/{id}")
    @Operation(summary = "Obtener venta por ID", description = "Retorna una venta específica según su ID")
    public ResponseEntity<VentaResponseDTO> obtenerPorId(
            @Parameter(description = "ID de la venta a buscar", example = "1")
            @PathVariable Long id) {
        log.info("GET /api/ventas/{}", id);
        return ResponseEntity.ok(ventaService.obtenerPorId(id));
    }

    @GetMapping("/numero/{numeroVenta}")
    @Operation(summary = "Buscar venta por número", description = "Retorna una venta según su número de venta")
    public ResponseEntity<VentaResponseDTO> obtenerPorNumero(
            @Parameter(description = "Número de venta", example = "VENTA-20260115-001")
            @PathVariable String numeroVenta) {
        log.info("GET /api/ventas/numero/{}", numeroVenta);
        return ResponseEntity.ok(ventaService.obtenerPorNumeroVenta(numeroVenta));
    }

    @GetMapping("/cliente/{clienteId}")
    @Operation(summary = "Listar ventas por cliente", description = "Retorna las ventas de un cliente específico")
    public ResponseEntity<List<VentaResponseDTO>> listarPorCliente(
            @Parameter(description = "ID del cliente", example = "1")
            @PathVariable Long clienteId) {
        log.info("GET /api/ventas/cliente/{}", clienteId);
        return ResponseEntity.ok(ventaService.listarPorCliente(clienteId));
    }

    @GetMapping("/empleado/{empleadoId}")
    @Operation(summary = "Listar ventas por empleado", description = "Retorna las ventas realizadas por un empleado específico")
    public ResponseEntity<List<VentaResponseDTO>> listarPorEmpleado(
            @Parameter(description = "ID del empleado", example = "1")
            @PathVariable Long empleadoId) {
        log.info("GET /api/ventas/empleado/{}", empleadoId);
        return ResponseEntity.ok(ventaService.listarPorEmpleado(empleadoId));
    }

    @GetMapping("/estado/{estado}")
    @Operation(summary = "Listar ventas por estado", description = "Retorna las ventas con un estado específico")
    public ResponseEntity<List<VentaResponseDTO>> listarPorEstado(
            @Parameter(description = "Estado de la venta", example = "COMPLETADA")
            @PathVariable EstadoVenta estado) {
        log.info("GET /api/ventas/estado/{}", estado);
        return ResponseEntity.ok(ventaService.listarPorEstado(estado));
    }

    @PostMapping
    @Operation(summary = "Crear nueva venta", description = "Registra una nueva venta en el sistema")
    public ResponseEntity<VentaResponseDTO> crear(
            @Valid @RequestBody VentaRequestDTO dto) {
        log.info("POST /api/ventas - empleado ID: {}", dto.getEmpleadoId());
        VentaResponseDTO creada = ventaService.crear(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(creada);
    }

    @PatchMapping("/{id}/completar")
    @Operation(summary = "Completar venta", description = "Marca una venta como completada")
    public ResponseEntity<VentaResponseDTO> completarVenta(
            @Parameter(description = "ID de la venta a completar", example = "1")
            @PathVariable Long id) {
        log.info("PATCH /api/ventas/{}/completar", id);
        return ResponseEntity.ok(ventaService.completarVenta(id));
    }

    @PatchMapping("/{id}/anular")
    @Operation(summary = "Anular venta", description = "Anula una venta existente")
    public ResponseEntity<VentaResponseDTO> anularVenta(
            @Parameter(description = "ID de la venta a anular", example = "1")
            @PathVariable Long id) {
        log.info("PATCH /api/ventas/{}/anular", id);
        return ResponseEntity.ok(ventaService.anularVenta(id));
    }
}