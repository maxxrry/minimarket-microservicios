package com.minimarket.mspromociones.controller;

import com.minimarket.mspromociones.dto.PromocionRequestDTO;
import com.minimarket.mspromociones.dto.PromocionResponseDTO;
import com.minimarket.mspromociones.service.PromocionService;
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
public class PromocionController {

    private static final Logger log = LoggerFactory.getLogger(PromocionController.class);

    @Autowired
    private PromocionService promocionService;

    @GetMapping
    public ResponseEntity<List<PromocionResponseDTO>> listarTodos() {
        log.info("GET /api/promociones");
        return ResponseEntity.ok(promocionService.listarTodos());
    }

    @GetMapping("/activas")
    public ResponseEntity<List<PromocionResponseDTO>> listarActivas() {
        log.info("GET /api/promociones/activas");
        return ResponseEntity.ok(promocionService.listarActivas());
    }

    @GetMapping("/{id}")
    public ResponseEntity<PromocionResponseDTO> obtenerPorId(@PathVariable Long id) {
        log.info("GET /api/promociones/{}", id);
        return ResponseEntity.ok(promocionService.obtenerPorId(id));
    }

    /**
     * Endpoint para comunicación inter-servicios (usado por ms-ventas vía Feign).
     * Lista las promociones activas asociadas a un producto específico.
     */
    @GetMapping("/producto/{productoId}")
    public ResponseEntity<List<PromocionResponseDTO>> listarPorProducto(
            @PathVariable Long productoId) {
        log.info("GET /api/promociones/producto/{}", productoId);
        return ResponseEntity.ok(promocionService.listarPorProducto(productoId));
    }

    /**
     * Endpoint para comunicación inter-servicios (usado por ms-ventas vía Feign).
     * Lista las promociones activas asociadas a una categoría específica.
     */
    @GetMapping("/categoria/{categoriaId}")
    public ResponseEntity<List<PromocionResponseDTO>> listarPorCategoria(
            @PathVariable Long categoriaId) {
        log.info("GET /api/promociones/categoria/{}", categoriaId);
        return ResponseEntity.ok(promocionService.listarPorCategoria(categoriaId));
    }

    @PostMapping
    public ResponseEntity<PromocionResponseDTO> crear(
            @Valid @RequestBody PromocionRequestDTO dto) {
        log.info("POST /api/promociones - creando: {}", dto.getNombre());
        PromocionResponseDTO creado = promocionService.crear(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(creado);
    }

    @PutMapping("/{id}")
    public ResponseEntity<PromocionResponseDTO> actualizar(
            @PathVariable Long id,
            @Valid @RequestBody PromocionRequestDTO dto) {
        log.info("PUT /api/promociones/{}", id);
        return ResponseEntity.ok(promocionService.actualizar(id, dto));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> darDeBaja(@PathVariable Long id) {
        log.info("DELETE /api/promociones/{}", id);
        promocionService.darDeBaja(id);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{id}/reactivar")
    public ResponseEntity<PromocionResponseDTO> reactivar(@PathVariable Long id) {
        log.info("PATCH /api/promociones/{}/reactivar", id);
        return ResponseEntity.ok(promocionService.reactivar(id));
    }
}
