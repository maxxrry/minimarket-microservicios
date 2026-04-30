package com.minimarket.mscatalogo.controller;

import com.minimarket.mscatalogo.model.Producto;
import com.minimarket.mscatalogo.service.ProductoService;
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
 * Expone los endpoints HTTP para gestionar productos.
 * Delega toda la lógica de negocio al ProductoService.
 */
@RestController
@RequestMapping("/api/productos")
public class ProductoController {

    private static final Logger log = LoggerFactory.getLogger(ProductoController.class);

    @Autowired
    private ProductoService productoService;

    /**
     * GET /api/productos
     * Lista todos los productos del catálogo.
     */
    @GetMapping
    public ResponseEntity<List<Producto>> listarTodos() {
        log.info("GET /api/productos");
        List<Producto> productos = productoService.listarTodos();
        return ResponseEntity.ok(productos);
    }

    /**
     * GET /api/productos/activos
     * Lista solo los productos que están activos.
     */
    @GetMapping("/activos")
    public ResponseEntity<List<Producto>> listarActivos() {
        log.info("GET /api/productos/activos");
        List<Producto> productos = productoService.listarActivos();
        return ResponseEntity.ok(productos);
    }

    /**
     * GET /api/productos/{id}
     * Obtiene un producto específico por su ID.
     */
    @GetMapping("/{id}")
    public ResponseEntity<Producto> obtenerPorId(@PathVariable Long id) {
        log.info("GET /api/productos/{}", id);
        Producto producto = productoService.obtenerPorId(id);
        return ResponseEntity.ok(producto);
    }

    /**
     * GET /api/productos/categoria/{categoriaId}
     * Lista los productos asociados a una categoría.
     * Endpoint usado por ms-categorias en comunicación inter-servicios.
     */
    @GetMapping("/categoria/{categoriaId}")
    public ResponseEntity<List<Producto>> listarPorCategoria(@PathVariable Long categoriaId) {
        log.info("GET /api/productos/categoria/{}", categoriaId);
        List<Producto> productos = productoService.listarPorCategoria(categoriaId);
        return ResponseEntity.ok(productos);
    }

    /**
     * POST /api/productos
     * Crea un nuevo producto en el catálogo.
     * El JSON del body se valida automáticamente con @Valid (Bean Validation).
     */
    @PostMapping
    public ResponseEntity<Producto> crear(@Valid @RequestBody Producto producto) {
        log.info("POST /api/productos - creando: {}", producto.getNombre());
        Producto creado = productoService.crear(producto);
        return ResponseEntity.status(HttpStatus.CREATED).body(creado);
    }

    /**
     * PUT /api/productos/{id}
     * Actualiza un producto existente.
     */
    @PutMapping("/{id}")
    public ResponseEntity<Producto> actualizar(
            @PathVariable Long id,
            @Valid @RequestBody Producto producto) {
        log.info("PUT /api/productos/{}", id);
        Producto actualizado = productoService.actualizar(id, producto);
        return ResponseEntity.ok(actualizado);
    }

    /**
     * DELETE /api/productos/{id}
     * Da de baja un producto (borrado lógico).
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> darDeBaja(@PathVariable Long id) {
        log.info("DELETE /api/productos/{}", id);
        productoService.darDeBaja(id);
        return ResponseEntity.noContent().build();
    }

    /**
     * PATCH /api/productos/{id}/reactivar
     * Reactiva un producto previamente dado de baja.
     */
    @PatchMapping("/{id}/reactivar")
    public ResponseEntity<Producto> reactivar(@PathVariable Long id) {
        log.info("PATCH /api/productos/{}/reactivar", id);
        Producto producto = productoService.reactivar(id);
        return ResponseEntity.ok(producto);
    }
}
