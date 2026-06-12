package com.minimarket.mscatalogo.controller;

import com.minimarket.mscatalogo.dto.ProductoRequestDTO;
import com.minimarket.mscatalogo.dto.ProductoResponseDTO;
import com.minimarket.mscatalogo.service.ProductoService;
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
 * Controlador REST del microservicio ms-catalogo.
 * Expone endpoints HTTP para gestionar productos.
 */
@RestController
@RequestMapping("/api/productos")
@Tag(name = "Productos", description = "Endpoints para gestionar productos del minimarket")
public class ProductoController {

    private static final Logger log = LoggerFactory.getLogger(ProductoController.class);

    @Autowired
    private ProductoService productoService;

    @GetMapping
    @Operation(summary = "Listar todos los productos", description = "Retorna una lista con todos los productos registrados en el sistema")
    public ResponseEntity<List<ProductoResponseDTO>> listarTodos() {
        log.info("GET /api/productos");
        return ResponseEntity.ok(productoService.listarTodos());
    }

    @GetMapping("/activos")
    @Operation(summary = "Listar productos activos", description = "Retorna solo los productos que están activos (no dados de baja)")
    public ResponseEntity<List<ProductoResponseDTO>> listarActivos() {
        log.info("GET /api/productos/activos");
        return ResponseEntity.ok(productoService.listarActivos());
    }

    @GetMapping("/{id}")
    @Operation(summary = "Obtener producto por ID", description = "Retorna un producto específico según su ID")
    public ResponseEntity<ProductoResponseDTO> obtenerPorId(
            @Parameter(description = "ID del producto a buscar", example = "1")
            @PathVariable Long id) {
        log.info("GET /api/productos/{}", id);
        return ResponseEntity.ok(productoService.obtenerPorId(id));
    }

    @GetMapping("/categoria/{categoriaId}")
    @Operation(summary = "Listar productos por categoría", description = "Retorna los productos que pertenecen a una categoría específica")
    public ResponseEntity<List<ProductoResponseDTO>> listarPorCategoria(
            @Parameter(description = "ID de la categoría", example = "1")
            @PathVariable Long categoriaId) {
        log.info("GET /api/productos/categoria/{}", categoriaId);
        return ResponseEntity.ok(productoService.listarPorCategoria(categoriaId));
    }

    @PostMapping
    @Operation(summary = "Crear nuevo producto", description = "Crea un nuevo producto en el sistema. El código de barras debe ser único.")
    public ResponseEntity<ProductoResponseDTO> crear(
            @Valid @RequestBody ProductoRequestDTO dto) {
        log.info("POST /api/productos - creando: {}", dto.getNombre());
        ProductoResponseDTO creado = productoService.crear(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(creado);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Actualizar producto", description = "Actualiza los datos de un producto existente")
    public ResponseEntity<ProductoResponseDTO> actualizar(
            @Parameter(description = "ID del producto a actualizar", example = "1")
            @PathVariable Long id,
            @Valid @RequestBody ProductoRequestDTO dto) {
        log.info("PUT /api/productos/{}", id);
        return ResponseEntity.ok(productoService.actualizar(id, dto));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Dar de baja producto", description = "Realiza un borrado lógico del producto (campo activo = false)")
    public ResponseEntity<Void> darDeBaja(
            @Parameter(description = "ID del producto a dar de baja", example = "1")
            @PathVariable Long id) {
        log.info("DELETE /api/productos/{}", id);
        productoService.darDeBaja(id);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{id}/reactivar")
    @Operation(summary = "Reactivar producto", description = "Reactiva un producto previamente dado de baja")
    public ResponseEntity<ProductoResponseDTO> reactivar(
            @Parameter(description = "ID del producto a activar", example = "1")
            @PathVariable Long id) {
        log.info("PATCH /api/productos/{}/reactivar", id);
        return ResponseEntity.ok(productoService.reactivar(id));
    }
}