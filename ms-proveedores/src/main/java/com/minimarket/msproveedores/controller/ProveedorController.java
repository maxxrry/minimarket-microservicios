package com.minimarket.msproveedores.controller;

import com.minimarket.msproveedores.dto.ProveedorRequestDTO;
import com.minimarket.msproveedores.dto.ProveedorResponseDTO;
import com.minimarket.msproveedores.service.ProveedorService;
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
public class ProveedorController {

    private static final Logger log = LoggerFactory.getLogger(ProveedorController.class);

    @Autowired
    private ProveedorService proveedorService;

    @GetMapping
    public ResponseEntity<List<ProveedorResponseDTO>> listarTodos() {
        log.info("GET /api/proveedores");
        return ResponseEntity.ok(proveedorService.listarTodos());
    }

    @GetMapping("/activos")
    public ResponseEntity<List<ProveedorResponseDTO>> listarActivos() {
        log.info("GET /api/proveedores/activos");
        return ResponseEntity.ok(proveedorService.listarActivos());
    }

    @GetMapping("/{id}")
    public ResponseEntity<ProveedorResponseDTO> obtenerPorId(@PathVariable Long id) {
        log.info("GET /api/proveedores/{}", id);
        return ResponseEntity.ok(proveedorService.obtenerPorId(id));
    }

    @GetMapping("/ciudad/{ciudad}")
    public ResponseEntity<List<ProveedorResponseDTO>> listarPorCiudad(
            @PathVariable String ciudad) {
        log.info("GET /api/proveedores/ciudad/{}", ciudad);
        return ResponseEntity.ok(proveedorService.listarPorCiudad(ciudad));
    }

    @PostMapping
    public ResponseEntity<ProveedorResponseDTO> crear(
            @Valid @RequestBody ProveedorRequestDTO dto) {
        log.info("POST /api/proveedores - creando: {}", dto.getRazonSocial());
        ProveedorResponseDTO creado = proveedorService.crear(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(creado);
    }

    @PutMapping("/{id}")
    public ResponseEntity<ProveedorResponseDTO> actualizar(
            @PathVariable Long id,
            @Valid @RequestBody ProveedorRequestDTO dto) {
        log.info("PUT /api/proveedores/{}", id);
        return ResponseEntity.ok(proveedorService.actualizar(id, dto));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> darDeBaja(@PathVariable Long id) {
        log.info("DELETE /api/proveedores/{}", id);
        proveedorService.darDeBaja(id);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{id}/reactivar")
    public ResponseEntity<ProveedorResponseDTO> reactivar(@PathVariable Long id) {
        log.info("PATCH /api/proveedores/{}/reactivar", id);
        return ResponseEntity.ok(proveedorService.reactivar(id));
    }
}