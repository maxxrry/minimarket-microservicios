package com.minimarket.msempleados.controller;

import com.minimarket.msempleados.dto.EmpleadoRequestDTO;
import com.minimarket.msempleados.dto.EmpleadoResponseDTO;
import com.minimarket.msempleados.model.Cargo;
import com.minimarket.msempleados.service.EmpleadoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
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
@Tag(name = "Empleados", description = "Endpoints para gestionar empleados del minimarket")
@RequiredArgsConstructor
public class EmpleadoController {

    private final EmpleadoService empleadoService;

    @GetMapping
    @Operation(summary = "Listar todos los empleados", description = "Retorna una lista con todos los empleados registrados")
    public ResponseEntity<List<EmpleadoResponseDTO>> listarTodos() {
        return ResponseEntity.ok(empleadoService.listarTodos());
    }

    @GetMapping("/activos")
    @Operation(summary = "Listar empleados activos", description = "Retorna solo los empleados que están activos")
    public ResponseEntity<List<EmpleadoResponseDTO>> listarActivos() {
        return ResponseEntity.ok(empleadoService.listarActivos());
    }

    @GetMapping("/{id}")
    @Operation(summary = "Obtener empleado por ID", description = "Retorna un empleado específico según su ID")
    public ResponseEntity<EmpleadoResponseDTO> buscarPorId(
            @Parameter(description = "ID del empleado a buscar", example = "1")
            @PathVariable Long id) {
        return ResponseEntity.ok(empleadoService.buscarPorId(id));
    }

    @GetMapping("/rut/{rut}")
    @Operation(summary = "Buscar empleado por RUT", description = "Retorna un empleado específico según su RUT")
    public ResponseEntity<EmpleadoResponseDTO> buscarPorRut(
            @Parameter(description = "RUT del empleado", example = "12345678-9")
            @PathVariable String rut) {
        return ResponseEntity.ok(empleadoService.buscarPorRut(rut));
    }

    @GetMapping("/cargo/{cargo}")
    @Operation(summary = "Buscar empleados por cargo", description = "Retorna los empleados que tienen un cargo específico")
    public ResponseEntity<List<EmpleadoResponseDTO>> buscarPorCargo(
            @Parameter(description = "Cargo del empleado", example = "Cajero")
            @PathVariable Cargo cargo) {
        return ResponseEntity.ok(empleadoService.buscarPorCargo(cargo));
    }

    @PostMapping
    @Operation(summary = "Crear nuevo empleado", description = "Crea un nuevo empleado en el sistema. El RUT debe ser único.")
    public ResponseEntity<EmpleadoResponseDTO> crear(
            @Valid @RequestBody EmpleadoRequestDTO dto) {
        EmpleadoResponseDTO creado = empleadoService.crear(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(creado);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Actualizar empleado", description = "Actualiza los datos de un empleado existente")
    public ResponseEntity<EmpleadoResponseDTO> actualizar(
            @Parameter(description = "ID del empleado a actualizar", example = "1")
            @PathVariable Long id,
            @Valid @RequestBody EmpleadoRequestDTO dto) {
        return ResponseEntity.ok(empleadoService.actualizar(id, dto));
    }

    /**
     * Borrado LÓGICO (soft delete). Marca activo=false.
     */
    @DeleteMapping("/{id}")
    @Operation(summary = "Desactivar empleado", description = "Realiza un borrado lógico del empleado (campo activo = false)")
    public ResponseEntity<Void> desactivar(
            @Parameter(description = "ID del empleado a desactivar", example = "1")
            @PathVariable Long id) {
        empleadoService.desactivar(id);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{id}/reactivar")
    @Operation(summary = "Reactivar empleado", description = "Reactiva un empleado previamente desactivado")
    public ResponseEntity<EmpleadoResponseDTO> reaccionar(
            @Parameter(description = "ID del empleado a activar", example = "1")
            @PathVariable Long id) {
        return ResponseEntity.ok(empleadoService.reactivar(id));
    }
}