package com.minimarket.msclientes.controller;

import com.minimarket.msclientes.dto.ClienteRequestDTO;
import com.minimarket.msclientes.dto.ClienteResponseDTO;
import com.minimarket.msclientes.service.ClienteService;
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
 * Controlador REST de Clientes.
 * Expone los endpoints HTTP del microservicio.
 */
@RestController
@RequestMapping("/api/clientes")
@Tag(name = "Clientes", description = "Endpoints para gestionar clientes registrados")
@RequiredArgsConstructor
public class ClienteController {

    private final ClienteService clienteService;

    @GetMapping
    @Operation(summary = "Listar todos los clientes", description = "Retorna una lista con todos los clientes registrados")
    public ResponseEntity<List<ClienteResponseDTO>> listarTodos() {
        return ResponseEntity.ok(clienteService.listarTodos());
    }

    @GetMapping("/activos")
    @Operation(summary = "Listar clientes activos", description = "Retorna solo los clientes que están activos")
    public ResponseEntity<List<ClienteResponseDTO>> listarActivos() {
        return ResponseEntity.ok(clienteService.listarActivos());
    }

    @GetMapping("/{id}")
    @Operation(summary = "Obtener cliente por ID", description = "Retorna un cliente específico según su ID")
    public ResponseEntity<ClienteResponseDTO> buscarPorId(
            @Parameter(description = "ID del cliente a buscar", example = "1")
            @PathVariable Long id) {
        return ResponseEntity.ok(clienteService.buscarPorId(id));
    }

    @GetMapping("/rut/{rut}")
    @Operation(summary = "Buscar cliente por RUT", description = "Retorna un cliente específico según su RUT")
    public ResponseEntity<ClienteResponseDTO> buscarPorRut(
            @Parameter(description = "RUT del cliente", example = "12345678-9")
            @PathVariable String rut) {
        return ResponseEntity.ok(clienteService.buscarPorRut(rut));
    }

    @PostMapping
    @Operation(summary = "Crear nuevo cliente", description = "Crea un nuevo cliente en el sistema. El RUT debe ser único.")
    public ResponseEntity<ClienteResponseDTO> crear(
            @Valid @RequestBody ClienteRequestDTO dto) {
        ClienteResponseDTO creado = clienteService.crear(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(creado);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Actualizar cliente", description = "Actualiza los datos de un cliente existente")
    public ResponseEntity<ClienteResponseDTO> actualizar(
            @Parameter(description = "ID del cliente a actualizar", example = "1")
            @PathVariable Long id,
            @Valid @RequestBody ClienteRequestDTO dto) {
        return ResponseEntity.ok(clienteService.actualizar(id, dto));
    }

    /**
     * Borrado LÓGICO (soft delete). Marca activo=false.
     */
    @DeleteMapping("/{id}")
    @Operation(summary = "Desactivar cliente", description = "Realiza un borrado lógico del cliente (campo activo = false)")
    public ResponseEntity<Void> desactivar(
            @Parameter(description = "ID del cliente a desactivar", example = "1")
            @PathVariable Long id) {
        clienteService.desactivar(id);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{id}/reactivar")
    @Operation(summary = "Reactivar cliente", description = "Reactiva un cliente previamente desactivado")
    public ResponseEntity<ClienteResponseDTO> reaccionar(
            @Parameter(description = "ID del cliente a activar", example = "1")
            @PathVariable Long id) {
        return ResponseEntity.ok(clienteService.reactivar(id));
    }
}