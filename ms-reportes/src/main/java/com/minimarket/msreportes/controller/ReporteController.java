package com.minimarket.msreportes.controller;

import com.minimarket.msreportes.dto.ReporteRequestDTO;
import com.minimarket.msreportes.dto.ReporteResponseDTO;
import com.minimarket.msreportes.model.TipoReporte;
import com.minimarket.msreportes.service.ReporteService;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/reportes")
public class ReporteController {

    private static final Logger log = LoggerFactory.getLogger(ReporteController.class);

    @Autowired
    private ReporteService reporteService;

    @GetMapping
    public ResponseEntity<List<ReporteResponseDTO>> listarTodos() {
        log.info("GET /api/reportes");
        return ResponseEntity.ok(reporteService.listarTodos());
    }

    @GetMapping("/{id}")
    public ResponseEntity<ReporteResponseDTO> obtenerPorId(@PathVariable Long id) {
        log.info("GET /api/reportes/{}", id);
        return ResponseEntity.ok(reporteService.obtenerPorId(id));
    }

    @GetMapping("/tipo/{tipo}")
    public ResponseEntity<List<ReporteResponseDTO>> listarPorTipo(
            @PathVariable TipoReporte tipo) {
        log.info("GET /api/reportes/tipo/{}", tipo);
        return ResponseEntity.ok(reporteService.listarPorTipo(tipo));
    }

    @GetMapping("/empleado/{empleadoId}")
    public ResponseEntity<List<ReporteResponseDTO>> listarPorEmpleado(
            @PathVariable Long empleadoId) {
        log.info("GET /api/reportes/empleado/{}", empleadoId);
        return ResponseEntity.ok(reporteService.listarPorEmpleado(empleadoId));
    }

    @PostMapping
    public ResponseEntity<ReporteResponseDTO> crear(
            @Valid @RequestBody ReporteRequestDTO dto) {
        log.info("POST /api/reportes - tipo: {}", dto.getTipoReporte());
        ReporteResponseDTO creado = reporteService.crear(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(creado);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {
        log.info("DELETE /api/reportes/{}", id);
        reporteService.eliminar(id);
        return ResponseEntity.noContent().build();
    }
}