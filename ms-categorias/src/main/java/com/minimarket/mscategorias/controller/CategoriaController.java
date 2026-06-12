package com.minimarket.mscategorias.controller;

import com.minimarket.mscategorias.dto.CategoriaRequestDTO;
import com.minimarket.mscategorias.dto.CategoriaResponseDTO;
import com.minimarket.mscategorias.service.CategoriaService;
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
 * Controlador REST del microservicio ms-categorias.
 * Expone los endpoints HTTP para gestionar las categorías de productos.
 */
@RestController
@RequestMapping("/api/categorias")
@Tag(name = "Categorías", description = "Endpoints para gestionar categorías de productos")
public class CategoriaController {

    private static final Logger log = LoggerFactory.getLogger(CategoriaController.class);

    @Autowired
    private CategoriaService categoriaService;

    @GetMapping
    @Operation(summary = "Listar todas las categorías", description = "Retorna una lista con todas las categorías registradas")
    public ResponseEntity<List<CategoriaResponseDTO>> listarTodas() {
        log.info("GET /api/categorias");
        return ResponseEntity.ok(categoriaService.listarTodas());
    }

    @GetMapping("/activas")
    @Operation(summary = "Listar categorías activas", description = "Retorna solo las categorías que están activas")
    public ResponseEntity<List<CategoriaResponseDTO>> listarActivas() {
        log.info("GET /api/categorias/activas");
        return ResponseEntity.ok(categoriaService.listarActivas());
    }

    @GetMapping("/{id}")
    @Operation(summary = "Obtener categoría por ID", description = "Retorna una categoría específica según su ID")
    public ResponseEntity<CategoriaResponseDTO> obtenerPorId(
            @Parameter(description = "ID de la categoría a buscar", example = "1")
            @PathVariable Long id) {
        log.info("GET /api/categorias/{}", id);
        return ResponseEntity.ok(categoriaService.obtenerPorId(id));
    }

    @PostMapping
    @Operation(summary = "Crear nueva categoría", description = "Crea una nueva categoría en el sistema. El código debe ser único.")
    public ResponseEntity<CategoriaResponseDTO> crear(
            @Valid @RequestBody CategoriaRequestDTO dto) {
        log.info("POST /api/categorias - creando: {}", dto.getNombre());
        CategoriaResponseDTO creada = categoriaService.crear(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(creada);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Actualizar categoría", description = "Actualiza los datos de una categoría existente")
    public ResponseEntity<CategoriaResponseDTO> actualizar(
            @Parameter(description = "ID de la categoría a actualizar", example = "1")
            @PathVariable Long id,
            @Valid @RequestBody CategoriaRequestDTO dto) {
        log.info("PUT /api/categorias/{}", id);
        return ResponseEntity.ok(categoriaService.actualizar(id, dto));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Dar de baja categoría", description = "Realiza un borrado lógico de la categoría (campo activa = false)")
    public ResponseEntity<Void> darDeBaja(
            @Parameter(description = "ID de la categoría a dar de baja", example = "1")
            @PathVariable Long id) {
        log.info("DELETE /api/categorias/{}", id);
        categoriaService.darDeBaja(id);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{id}/reactivar")
    @Operation(summary = "Reactivar categoría", description = "Reactiva una categoría previamente dada de baja")
    public ResponseEntity<CategoriaResponseDTO> reactivar(
            @Parameter(description = "ID de la categoría a activar", example = "1")
            @PathVariable Long id) {
        log.info("PATCH /api/categorias/{}/reactivar", id);
        return ResponseEntity.ok(categoriaService.reactivar(id));
    }
}