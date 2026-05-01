package com.minimarket.mscategorias.controller;

import com.minimarket.mscategorias.dto.CategoriaRequestDTO;
import com.minimarket.mscategorias.dto.CategoriaResponseDTO;
import com.minimarket.mscategorias.service.CategoriaService;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Controlador REST del microservicio ms-categorias.
 * Expone los endpoints HTTP para gestionar las categorías de productos.
 */
@RestController
@RequestMapping("/api/categorias")
public class CategoriaController {

    private static final Logger log = LoggerFactory.getLogger(CategoriaController.class);

    @Autowired
    private CategoriaService categoriaService;

    @GetMapping
    public ResponseEntity<List<CategoriaResponseDTO>> listarTodas() {
        log.info("GET /api/categorias");
        return ResponseEntity.ok(categoriaService.listarTodas());
    }

    @GetMapping("/activas")
    public ResponseEntity<List<CategoriaResponseDTO>> listarActivas() {
        log.info("GET /api/categorias/activas");
        return ResponseEntity.ok(categoriaService.listarActivas());
    }

    @GetMapping("/{id}")
    public ResponseEntity<CategoriaResponseDTO> obtenerPorId(@PathVariable Long id) {
        log.info("GET /api/categorias/{}", id);
        return ResponseEntity.ok(categoriaService.obtenerPorId(id));
    }

    @PostMapping
    public ResponseEntity<CategoriaResponseDTO> crear(
            @Valid @RequestBody CategoriaRequestDTO dto) {
        log.info("POST /api/categorias - creando: {}", dto.getNombre());
        CategoriaResponseDTO creada = categoriaService.crear(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(creada);
    }

    @PutMapping("/{id}")
    public ResponseEntity<CategoriaResponseDTO> actualizar(
            @PathVariable Long id,
            @Valid @RequestBody CategoriaRequestDTO dto) {
        log.info("PUT /api/categorias/{}", id);
        return ResponseEntity.ok(categoriaService.actualizar(id, dto));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> darDeBaja(@PathVariable Long id) {
        log.info("DELETE /api/categorias/{}", id);
        categoriaService.darDeBaja(id);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{id}/reactivar")
    public ResponseEntity<CategoriaResponseDTO> reactivar(@PathVariable Long id) {
        log.info("PATCH /api/categorias/{}/reactivar", id);
        return ResponseEntity.ok(categoriaService.reactivar(id));
    }
}