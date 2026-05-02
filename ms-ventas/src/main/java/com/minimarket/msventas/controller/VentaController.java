package com.minimarket.msventas.controller;

import com.minimarket.msventas.dto.VentaRequestDTO;
import com.minimarket.msventas.dto.VentaResponseDTO;
import com.minimarket.msventas.model.EstadoVenta;
import com.minimarket.msventas.service.VentaService;
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
public class VentaController {

    private static final Logger log = LoggerFactory.getLogger(VentaController.class);

    @Autowired
    private VentaService ventaService;

    @GetMapping
    public ResponseEntity<List<VentaResponseDTO>> listarTodas() {
        log.info("GET /api/ventas");
        return ResponseEntity.ok(ventaService.listarTodas());
    }

    @GetMapping("/{id}")
    public ResponseEntity<VentaResponseDTO> obtenerPorId(@PathVariable Long id) {
        log.info("GET /api/ventas/{}", id);
        return ResponseEntity.ok(ventaService.obtenerPorId(id));
    }

    @GetMapping("/numero/{numeroVenta}")
    public ResponseEntity<VentaResponseDTO> obtenerPorNumero(
            @PathVariable String numeroVenta) {
        log.info("GET /api/ventas/numero/{}", numeroVenta);
        return ResponseEntity.ok(ventaService.obtenerPorNumeroVenta(numeroVenta));
    }

    @GetMapping("/cliente/{clienteId}")
    public ResponseEntity<List<VentaResponseDTO>> listarPorCliente(
            @PathVariable Long clienteId) {
        log.info("GET /api/ventas/cliente/{}", clienteId);
        return ResponseEntity.ok(ventaService.listarPorCliente(clienteId));
    }

    @GetMapping("/empleado/{empleadoId}")
    public ResponseEntity<List<VentaResponseDTO>> listarPorEmpleado(
            @PathVariable Long empleadoId) {
        log.info("GET /api/ventas/empleado/{}", empleadoId);
        return ResponseEntity.ok(ventaService.listarPorEmpleado(empleadoId));
    }

    @GetMapping("/estado/{estado}")
    public ResponseEntity<List<VentaResponseDTO>> listarPorEstado(
            @PathVariable EstadoVenta estado) {
        log.info("GET /api/ventas/estado/{}", estado);
        return ResponseEntity.ok(ventaService.listarPorEstado(estado));
    }

    @PostMapping
    public ResponseEntity<VentaResponseDTO> crear(@Valid @RequestBody VentaRequestDTO dto) {
        log.info("POST /api/ventas - empleado ID: {}", dto.getEmpleadoId());
        VentaResponseDTO creada = ventaService.crear(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(creada);
    }

    @PatchMapping("/{id}/completar")
    public ResponseEntity<VentaResponseDTO> completarVenta(@PathVariable Long id) {
        log.info("PATCH /api/ventas/{}/completar", id);
        return ResponseEntity.ok(ventaService.completarVenta(id));
    }

    @PatchMapping("/{id}/anular")
    public ResponseEntity<VentaResponseDTO> anularVenta(@PathVariable Long id) {
        log.info("PATCH /api/ventas/{}/anular", id);
        return ResponseEntity.ok(ventaService.anularVenta(id));
    }
}