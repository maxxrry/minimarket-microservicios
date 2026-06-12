package com.minimarket.mspromociones.controller;

import com.minimarket.mspromociones.dto.PromocionRequestDTO;
import com.minimarket.mspromociones.dto.PromocionResponseDTO;
import com.minimarket.mspromociones.service.PromocionService;
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

/**
 * Controlador REST del microservicio ms-promociones.
 * Expone los endpoints HTTP para gestionar las promociones del minimarket.
 */
@RestController
@RequestMapping("/api/promociones")
@Tag(name = "Promociones", description = "Endpoints para gestionar promociones y descuentos")
public class PromocionController {

    private static final Logger log = LoggerFactory.getLogger(PromocionController.class);

    @Autowired
    private PromocionService promocionService;

    @GetMapping
    @Operation(summary = "Listar todas las promociones", description = "Retorna una lista con todas las promociones registradas")
    public ResponseEntity<List<PromocionResponseDTO>> listarTodos() {
        log.info("GET /api/promociones");
        return ResponseEntity.ok(promocionService.listarTodos());
    }

    @GetMapping("/activas")
    @Operation(summary = "Listar promociones activas", description = "Retorna solo las promociones que están activas")
    public ResponseEntity<List<PromocionResponseDTO>> listarActivas() {
        log.info("GET /api/promociones/activas");
        return ResponseEntity.ok(promocionService.listarActivas());
    }

    @GetMapping("/{id}")
    @Operation(summary = "Obtener promoción por ID", description = "Retorna una promoción específica según su ID")
    public ResponseEntity<PromocionResponseDTO> obtenerPorId(
            @Parameter(description = "ID de la promoción a buscar", example = "1")
            @PathVariable Long id) {
        log.info("GET /api/promociones/{}", id);
        return ResponseEntity.ok(promocionService.obtenerPorId(id));
    }

    /**
     * Endpoint para comunicación inter-servicios (usado por ms-ventas vía Feign).
     * Lista las promociones activas asociadas a un producto específico.
     */
    @GetMapping("/producto/{productoId}")
    @Operation(summary = "Listar promociones por producto", description = "Retorna las promociones activas de un producto específico (para comunicación inter-servicios)")
    public ResponseEntity<List<PromocionResponseDTO>> listarPorProducto(
            @Parameter(description = "ID del producto", example = "1")
            @PathVariable Long productoId) {
        log.info("GET /api/promociones/producto/{}", productoId);
        return ResponseEntity.ok(promocionService.listarPorProducto(productoId));
    }

    /**
     * Endpoint para comunicación inter-servicios (usado por ms-ventas vía Feign).
     * Lista las promociones activas asociadas a una categoría específica.
     */
    @GetMapping("/categoria/{categoriaId}")
    @Operation(summary = "Listar promociones por categoría", description = "Retorna las promociones activas de una categoría específica (para comunicación inter-servicios)")
    public ResponseEntity<List<PromocionResponseDTO>> listarPorCategoria(
            @Parameter(description = "ID de la categoría", example = "1")
            @PathVariable Long categoriaId) {
        log.info("GET /api/promociones/categoria/{}", categoriaId);
        return ResponseEntity.ok(promocionService.listarPorCategoria(categoriaId));
    }

    @PostMapping
    @Operation(summary = "Crear nueva promoción", description = "Crea una nueva promoción en el sistema")
    public ResponseEntity<PromocionResponseDTO> crear(
            @Valid @RequestBody PromocionRequestDTO dto) {
        log.info("POST /api/promociones - creando: {}", dto.getNombre());
        PromocionResponseDTO creado = promocionService.crear(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(creado);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Actualizar promoción", description = "Actualiza los datos de una promoción existente")
    public ResponseEntity<PromocionResponseDTO> actualizar(
            @Parameter(description = "ID de la promoción a actualizar", example = "1")
            @PathVariable Long id,
            @Valid @RequestBody PromocionRequestDTO dto) {
        log.info("PUT /api/promociones/{}", id);
        return ResponseEntity.ok(promocionService.actualizar(id, dto));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Dar de baja promoción", description = "Realiza un borrado lógico de la promoción (campo activa = false)")
    public ResponseEntity<Void> darDeBaja(
            @Parameter(description = "ID de la promoción a dar de baja", example = "1")
            @PathVariable Long id) {
        log.info("DELETE /api/promociones/{}", id);
        promocionService.darDeBaja(id);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{id}/reactivar")
    @Operation(summary = "Reactivar promoción", description = "Reactiva una promoción previamente dada de baja")
    public ResponseEntity<PromocionResponseDTO> reactionary(
            @Parameter(description = "ID de la promoción a activar", example = "1")
            @PathVariable Long id) {
        log.info("PATCH /api/promociones/{}/reactivar", id);
        return ResponseEntity.ok(promocionService.reactivar(id));
    }
}
