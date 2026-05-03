package com.minimarket.msempleados.controller;

import com.minimarket.msempleados.dto.EmpleadoRequestDTO;
import com.minimarket.msempleados.dto.EmpleadoResponseDTO;
import com.minimarket.msempleados.model.Cargo;
import com.minimarket.msempleados.service.EmpleadoService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Controlador REST de Empleados.
 */
@RestController
@RequestMapping("/api/empleados")
@RequiredArgsConstructor
public class EmpleadoController {

    private final EmpleadoService empleadoService;

    @GetMapping
    public ResponseEntity<List<EmpleadoResponseDTO>> listarTodos() {
        return ResponseEntity.ok(empleadoService.listarTodos());
    }

    @GetMapping("/activos")
    public ResponseEntity<List<EmpleadoResponseDTO>> listarActivos() {
        return ResponseEntity.ok(empleadoService.listarActivos());
    }

    @GetMapping("/{id}")
    public ResponseEntity<EmpleadoResponseDTO> buscarPorId(@PathVariable Long id) {
        return ResponseEntity.ok(empleadoService.buscarPorId(id));
    }

    @GetMapping("/rut/{rut}")
    public ResponseEntity<EmpleadoResponseDTO> buscarPorRut(@PathVariable String rut) {
        return ResponseEntity.ok(empleadoService.buscarPorRut(rut));
    }

    @GetMapping("/cargo/{cargo}")
    public ResponseEntity<List<EmpleadoResponseDTO>> buscarPorCargo(
            @PathVariable Cargo cargo) {
        return ResponseEntity.ok(empleadoService.buscarPorCargo(cargo));
    }

    @PostMapping
    public ResponseEntity<EmpleadoResponseDTO> crear(
            @Valid @RequestBody EmpleadoRequestDTO dto) {
        EmpleadoResponseDTO creado = empleadoService.crear(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(creado);
    }

    @PutMapping("/{id}")
    public ResponseEntity<EmpleadoResponseDTO> actualizar(
            @PathVariable Long id,
            @Valid @RequestBody EmpleadoRequestDTO dto) {
        return ResponseEntity.ok(empleadoService.actualizar(id, dto));
    }

    /**
     * Borrado LÓGICO (soft delete). Marca activo=false.
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> desactivar(@PathVariable Long id) {
        empleadoService.desactivar(id);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{id}/reactivar")
    public ResponseEntity<EmpleadoResponseDTO> reactivar(@PathVariable Long id) {
        return ResponseEntity.ok(empleadoService.reactivar(id));
    }
}