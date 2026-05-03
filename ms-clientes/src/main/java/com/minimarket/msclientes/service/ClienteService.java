package com.minimarket.msclientes.service;

import com.minimarket.msclientes.dto.ClienteRequestDTO;
import com.minimarket.msclientes.dto.ClienteResponseDTO;
import com.minimarket.msclientes.exception.ClienteDuplicadoException;
import com.minimarket.msclientes.exception.RecursoNoEncontradoException;
import com.minimarket.msclientes.model.Cliente;
import com.minimarket.msclientes.repository.ClienteRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Capa de servicio: contiene la lógica de negocio de Clientes.
 * - Valida reglas de negocio (RUT único, email único)
 * - Convierte entre Entity y DTOs
 * - Implementa borrado lógico (soft delete)
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class ClienteService {

    private final ClienteRepository clienteRepository;

    @Transactional(readOnly = true)
    public List<ClienteResponseDTO> listarTodos() {
        log.info("Listando todos los clientes");
        return clienteRepository.findAll().stream()
                .map(this::convertirAResponseDTO)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<ClienteResponseDTO> listarActivos() {
        log.info("Listando clientes activos");
        return clienteRepository.findByActivoTrue().stream()
                .map(this::convertirAResponseDTO)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public ClienteResponseDTO buscarPorId(Long id) {
        log.info("Buscando cliente con ID: {}", id);
        Cliente cliente = clienteRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException(
                        "Cliente con ID " + id + " no encontrado"));
        return convertirAResponseDTO(cliente);
    }

    @Transactional(readOnly = true)
    public ClienteResponseDTO buscarPorRut(String rut) {
        log.info("Buscando cliente con RUT: {}", rut);
        Cliente cliente = clienteRepository.findByRut(rut)
                .orElseThrow(() -> new RecursoNoEncontradoException(
                        "Cliente con RUT " + rut + " no encontrado"));
        return convertirAResponseDTO(cliente);
    }

    /**
     * Crea un nuevo cliente.
     * Reglas: RUT único, email único, activo por defecto.
     */
    public ClienteResponseDTO crear(ClienteRequestDTO dto) {
        log.info("Creando nuevo cliente: {} {}", dto.getNombre(), dto.getApellido());

        if (clienteRepository.existsByRut(dto.getRut())) {
            log.warn("RUT duplicado: {}", dto.getRut());
            throw new ClienteDuplicadoException(
                    "Ya existe un cliente con el RUT: " + dto.getRut());
        }

        if (clienteRepository.existsByEmailIgnoreCase(dto.getEmail())) {
            log.warn("Email duplicado: {}", dto.getEmail());
            throw new ClienteDuplicadoException(
                    "Ya existe un cliente con el email: " + dto.getEmail());
        }

        Cliente cliente = convertirAEntidad(dto);

        if (cliente.getActivo() == null) {
            cliente.setActivo(true);
        }

        Cliente guardado = clienteRepository.save(cliente);
        log.info("Cliente creado exitosamente con ID: {}", guardado.getId());
        return convertirAResponseDTO(guardado);
    }

    public ClienteResponseDTO actualizar(Long id, ClienteRequestDTO dto) {
        log.info("Actualizando cliente con ID: {}", id);

        Cliente existente = clienteRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException(
                        "Cliente con ID " + id + " no encontrado"));

        if (!existente.getRut().equals(dto.getRut()) &&
                clienteRepository.existsByRut(dto.getRut())) {
            throw new ClienteDuplicadoException(
                    "Ya existe otro cliente con el RUT: " + dto.getRut());
        }

        if (!existente.getEmail().equalsIgnoreCase(dto.getEmail()) &&
                clienteRepository.existsByEmailIgnoreCase(dto.getEmail())) {
            throw new ClienteDuplicadoException(
                    "Ya existe otro cliente con el email: " + dto.getEmail());
        }

        existente.setRut(dto.getRut());
        existente.setNombre(dto.getNombre());
        existente.setApellido(dto.getApellido());
        existente.setEmail(dto.getEmail());
        existente.setTelefono(dto.getTelefono());
        existente.setDireccion(dto.getDireccion());
        existente.setFechaNacimiento(dto.getFechaNacimiento());
        if (dto.getActivo() != null) {
            existente.setActivo(dto.getActivo());
        }

        Cliente guardado = clienteRepository.save(existente);
        log.info("Cliente actualizado: ID {}", id);
        return convertirAResponseDTO(guardado);
    }

    /**
     * Borrado LÓGICO de un cliente (soft delete).
     * No elimina el registro, solo marca activo=false.
     */
    public void desactivar(Long id) {
        log.info("Desactivando cliente con ID: {}", id);
        Cliente cliente = clienteRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException(
                        "Cliente con ID " + id + " no encontrado"));
        cliente.setActivo(false);
        clienteRepository.save(cliente);
        log.info("Cliente desactivado: ID {}", id);
    }

    public ClienteResponseDTO reactivar(Long id) {
        log.info("Reactivando cliente con ID: {}", id);
        Cliente cliente = clienteRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException(
                        "Cliente con ID " + id + " no encontrado"));
        cliente.setActivo(true);
        Cliente guardado = clienteRepository.save(cliente);
        log.info("Cliente reactivado: ID {}", id);
        return convertirAResponseDTO(guardado);
    }

    // ============================================
    // MÉTODOS DE CONVERSIÓN
    // ============================================

    private Cliente convertirAEntidad(ClienteRequestDTO dto) {
        Cliente cliente = new Cliente();
        cliente.setRut(dto.getRut());
        cliente.setNombre(dto.getNombre());
        cliente.setApellido(dto.getApellido());
        cliente.setEmail(dto.getEmail());
        cliente.setTelefono(dto.getTelefono());
        cliente.setDireccion(dto.getDireccion());
        cliente.setFechaNacimiento(dto.getFechaNacimiento());
        cliente.setActivo(dto.getActivo());
        return cliente;
    }

    private ClienteResponseDTO convertirAResponseDTO(Cliente cliente) {
        return new ClienteResponseDTO(
                cliente.getId(),
                cliente.getRut(),
                cliente.getNombre(),
                cliente.getApellido(),
                cliente.getEmail(),
                cliente.getTelefono(),
                cliente.getDireccion(),
                cliente.getFechaNacimiento(),
                cliente.getActivo(),
                cliente.getFechaRegistro(),
                cliente.getFechaActualizacion()
        );
    }
}