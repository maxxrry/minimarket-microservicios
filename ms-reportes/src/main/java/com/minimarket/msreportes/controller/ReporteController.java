package com.minimarket.msreportes.controller;

import com.minimarket.msreportes.dto.ReporteRequestDTO;
import com.minimarket.msreportes.dto.ReporteResponseDTO;
import com.minimarket.msreportes.model.TipoReporte;
import com.minimarket.msreportes.service.ReporteService;
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
@RequestMapping("/api/reportes")
@Tag(name = "Reportes", description = "Endpoints para generar y gestionar reportes")
public class ReporteController {

    private static final Logger log = LoggerFactory.getLogger(ReporteController.class);

    @Autowired
    private ReporteService reporteService;

    @GetMapping
    @Operation(summary = "Listar todos los reportes", description = "Retorna una lista con todos los reportes generados")
    public ResponseEntity<List<ReporteResponseDTO>> listarTodos() {
        log.info("GET /api/reportes");
        return ResponseEntity.ok(reporteService.listarTodos());
    }

    @GetMapping("/{id}")
    @Operation(summary = "Obtener reporte por ID", description = "Retorna un reporte específico según su ID")
    public ResponseEntity<ReporteResponseDTO> obtenerPorId(
            @Parameter(description = "ID del reporte a buscar", example = "1")
            @PathVariable Long id) {
        log.info("GET /api/reportes/{}", id);
        return ResponseEntity.ok(reporteService.obtenerPorId(id));
    }

    @GetMapping("/tipo/{tipo}")
    @Operation(summary = "Listar reportes por tipo", description = "Retorna los reportes de un tipo específico")
    public ResponseEntity<List<ReporteResponseDTO>> listarPorTipo(
            @Parameter(description = "Tipo de reporte", example = "VENTAS")
            @PathVariable TipoReporte tipo) {
        log.info("GET /api/reportes/tipo/{}", tipo);
        return ResponseEntity.ok(reporteService.listarPorTipo(tipo));
    }

    @GetMapping("/empleado/{empleadoId}")
    @Operation(summary = "Listar reportes por empleado", description = "Retorna los reportes generados por un empleado específico")
    public ResponseEntity<List<ReporteResponseDTO>> listarPorEmpleado(
            @Parameter(description = "ID del empleado", example = "1")
            @PathVariable Long empleadoId) {
        log.info("GET /api/reportes/empleado/{}", empleadoId);
        return ResponseEntity.ok(reporteService.listarPorEmpleado(empleadoId));
    }

    @PostMapping
    @Operation(summary = "Generar nuevo reporte", description = "Genera un nuevo reporte en el sistema")
    public ResponseEntity<ReporteResponseDTO> crear(
            @Valid @RequestBody ReporteRequestDTO dto) {
        log.info("POST /api/reportes - tipo: {}", dto.getTipoReporte());
        ReporteResponseDTO creado = reporteService.crear(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(creado);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Eliminar reporte", description = "Elimina un reporte del sistema")
    public ResponseEntity<Void> eliminar(
            @Parameter(description = "ID del reporte a eliminar", example = "1")
            @PathVariable Long id) {
        log.info("DELETE /api/reportes/{}", id);
        reporteService.eliminar(id);
        return ResponseEntity.noContent().build();
    }
}