package com.minimarket.msproveedores.controller;

import com.minimarket.msproveedores.dto.ProveedorRequestDTO;
import com.minimarket.msproveedores.dto.ProveedorResponseDTO;
import com.minimarket.msproveedores.service.ProveedorService;
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
@RequestMapping("/api/proveedores")
@Tag(name = "Proveedores", description = "Endpoints para gestionar proveedores")
public class ProveedorController {

    private static final Logger log = LoggerFactory.getLogger(ProveedorController.class);

    @Autowired
    private ProveedorService proveedorService;

    @GetMapping
    @Operation(summary = "Listar todos los proveedores", description = "Retorna una lista con todos los proveedores registrados")
    public ResponseEntity<List<ProveedorResponseDTO>> listarTodos() {
        log.info("GET /api/proveedores");
        return ResponseEntity.ok(proveedorService.listarTodos());
    }

    @GetMapping("/activos")
    @Operation(summary = "Listar proveedores activos", description = "Retorna solo los proveedores que están activos")
    public ResponseEntity<List<ProveedorResponseDTO>> listarActivos() {
        log.info("GET /api/proveedores/activos");
        return ResponseEntity.ok(proveedorService.listarActivos());
    }

    @GetMapping("/{id}")
    @Operation(summary = "Obtener proveedor por ID", description = "Retorna un proveedor específico según su ID")
    public ResponseEntity<ProveedorResponseDTO> obtenerPorId(
            @Parameter(description = "ID del proveedor a buscar", example = "1")
            @PathVariable Long id) {
        log.info("GET /api/proveedores/{}", id);
        return ResponseEntity.ok(proveedorService.obtenerPorId(id));
    }

    @GetMapping("/ciudad/{ciudad}")
    @Operation(summary = "Listar proveedores por ciudad", description = "Retorna los proveedores de una ciudad específica")
    public ResponseEntity<List<ProveedorResponseDTO>> listarPorCiudad(
            @Parameter(description = "Nombre de la ciudad", example = "Santiago")
            @PathVariable String ciudad) {
        log.info("GET /api/proveedores/ciudad/{}", ciudad);
        return ResponseEntity.ok(proveedorService.listarPorCiudad(ciudad));
    }

    @PostMapping
    @Operation(summary = "Crear nuevo proveedor", description = "Crea un nuevo proveedor en el sistema")
    public ResponseEntity<ProveedorResponseDTO> crear(
            @Valid @RequestBody ProveedorRequestDTO dto) {
        log.info("POST /api/proveedores - creando: {}", dto.getRazonSocial());
        ProveedorResponseDTO creado = proveedorService.crear(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(creado);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Actualizar proveedor", description = "Actualiza los datos de un proveedor existente")
    public ResponseEntity<ProveedorResponseDTO> actualizar(
            @Parameter(description = "ID del proveedor a actualizar", example = "1")
            @PathVariable Long id,
            @Valid @RequestBody ProveedorRequestDTO dto) {
        log.info("PUT /api/proveedores/{}", id);
        return ResponseEntity.ok(proveedorService.actualizar(id, dto));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Dar de baja proveedor", description = "Realiza un borrado lógico del proveedor (campo activo = false)")
    public ResponseEntity<Void> darDeBaja(
            @Parameter(description = "ID del proveedor a dar de baja", example = "1")
            @PathVariable Long id) {
        log.info("DELETE /api/proveedores/{}", id);
        proveedorService.darDeBaja(id);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{id}/reactivar")
    @Operation(summary = "Reactivar proveedor", description = "Reactiva un proveedor previamente dado de baja")
    public ResponseEntity<ProveedorResponseDTO> reactionary(
            @Parameter(description = "ID del proveedor a activar", example = "1")
            @PathVariable Long id) {
        log.info("PATCH /api/proveedores/{}/reactivar", id);
        return ResponseEntity.ok(proveedorService.reactivar(id));
    }
}