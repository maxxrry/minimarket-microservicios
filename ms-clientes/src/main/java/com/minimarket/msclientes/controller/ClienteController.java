package com.minimarket.msclientes.controller;

import com.minimarket.msclientes.dto.ClienteRequestDTO;
import com.minimarket.msclientes.dto.ClienteResponseDTO;
import com.minimarket.msclientes.service.ClienteService;
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
@RequiredArgsConstructor
public class ClienteController {

    private final ClienteService clienteService;

    @GetMapping
    public ResponseEntity<List<ClienteResponseDTO>> listarTodos() {
        return ResponseEntity.ok(clienteService.listarTodos());
    }

    @GetMapping("/activos")
    public ResponseEntity<List<ClienteResponseDTO>> listarActivos() {
        return ResponseEntity.ok(clienteService.listarActivos());
    }

    @GetMapping("/{id}")
    public ResponseEntity<ClienteResponseDTO> buscarPorId(@PathVariable Long id) {
        return ResponseEntity.ok(clienteService.buscarPorId(id));
    }

    @GetMapping("/rut/{rut}")
    public ResponseEntity<ClienteResponseDTO> buscarPorRut(@PathVariable String rut) {
        return ResponseEntity.ok(clienteService.buscarPorRut(rut));
    }

    @PostMapping
    public ResponseEntity<ClienteResponseDTO> crear(
            @Valid @RequestBody ClienteRequestDTO dto) {
        ClienteResponseDTO creado = clienteService.crear(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(creado);
    }

    @PutMapping("/{id}")
    public ResponseEntity<ClienteResponseDTO> actualizar(
            @PathVariable Long id,
            @Valid @RequestBody ClienteRequestDTO dto) {
        return ResponseEntity.ok(clienteService.actualizar(id, dto));
    }

    /**
     * Borrado LÓGICO (soft delete). Marca activo=false.
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> desactivar(@PathVariable Long id) {
        clienteService.desactivar(id);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{id}/reactivar")
    public ResponseEntity<ClienteResponseDTO> reactivar(@PathVariable Long id) {
        return ResponseEntity.ok(clienteService.reactivar(id));
    }
}