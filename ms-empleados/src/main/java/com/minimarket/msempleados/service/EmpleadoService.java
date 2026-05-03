package com.minimarket.msempleados.service;

import com.minimarket.msempleados.dto.EmpleadoRequestDTO;
import com.minimarket.msempleados.dto.EmpleadoResponseDTO;
import com.minimarket.msempleados.exception.EmpleadoDuplicadoException;
import com.minimarket.msempleados.exception.RecursoNoEncontradoException;
import com.minimarket.msempleados.model.Cargo;
import com.minimarket.msempleados.model.Empleado;
import com.minimarket.msempleados.repository.EmpleadoRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Capa de servicio: lógica de negocio de Empleados.
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class EmpleadoService {

    private final EmpleadoRepository empleadoRepository;

    @Transactional(readOnly = true)
    public List<EmpleadoResponseDTO> listarTodos() {
        log.info("Listando todos los empleados");
        return empleadoRepository.findAll().stream()
                .map(this::convertirAResponseDTO)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<EmpleadoResponseDTO> listarActivos() {
        log.info("Listando empleados activos");
        return empleadoRepository.findByActivoTrue().stream()
                .map(this::convertirAResponseDTO)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public EmpleadoResponseDTO buscarPorId(Long id) {
        log.info("Buscando empleado con ID: {}", id);
        Empleado empleado = empleadoRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException(
                        "Empleado con ID " + id + " no encontrado"));
        return convertirAResponseDTO(empleado);
    }

    @Transactional(readOnly = true)
    public EmpleadoResponseDTO buscarPorRut(String rut) {
        log.info("Buscando empleado con RUT: {}", rut);
        Empleado empleado = empleadoRepository.findByRut(rut)
                .orElseThrow(() -> new RecursoNoEncontradoException(
                        "Empleado con RUT " + rut + " no encontrado"));
        return convertirAResponseDTO(empleado);
    }

    @Transactional(readOnly = true)
    public List<EmpleadoResponseDTO> buscarPorCargo(Cargo cargo) {
        log.info("Listando empleados con cargo: {}", cargo);
        return empleadoRepository.findByCargo(cargo).stream()
                .map(this::convertirAResponseDTO)
                .collect(Collectors.toList());
    }

    /**
     * Crea un nuevo empleado.
     * Reglas: RUT único, email único, activo por defecto.
     */
    public EmpleadoResponseDTO crear(EmpleadoRequestDTO dto) {
        log.info("Creando nuevo empleado: {} {}", dto.getNombre(), dto.getApellido());

        if (empleadoRepository.existsByRut(dto.getRut())) {
            log.warn("RUT duplicado: {}", dto.getRut());
            throw new EmpleadoDuplicadoException(
                    "Ya existe un empleado con el RUT: " + dto.getRut());
        }

        if (empleadoRepository.existsByEmailIgnoreCase(dto.getEmail())) {
            log.warn("Email duplicado: {}", dto.getEmail());
            throw new EmpleadoDuplicadoException(
                    "Ya existe un empleado con el email: " + dto.getEmail());
        }

        Empleado empleado = convertirAEntidad(dto);

        if (empleado.getActivo() == null) {
            empleado.setActivo(true);
        }

        Empleado guardado = empleadoRepository.save(empleado);
        log.info("Empleado creado con ID: {}", guardado.getId());
        return convertirAResponseDTO(guardado);
    }

    public EmpleadoResponseDTO actualizar(Long id, EmpleadoRequestDTO dto) {
        log.info("Actualizando empleado con ID: {}", id);

        Empleado existente = empleadoRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException(
                        "Empleado con ID " + id + " no encontrado"));

        if (!existente.getRut().equals(dto.getRut()) &&
                empleadoRepository.existsByRut(dto.getRut())) {
            throw new EmpleadoDuplicadoException(
                    "Ya existe otro empleado con el RUT: " + dto.getRut());
        }

        if (!existente.getEmail().equalsIgnoreCase(dto.getEmail()) &&
                empleadoRepository.existsByEmailIgnoreCase(dto.getEmail())) {
            throw new EmpleadoDuplicadoException(
                    "Ya existe otro empleado con el email: " + dto.getEmail());
        }

        existente.setRut(dto.getRut());
        existente.setNombre(dto.getNombre());
        existente.setApellido(dto.getApellido());
        existente.setEmail(dto.getEmail());
        existente.setTelefono(dto.getTelefono());
        existente.setCargo(dto.getCargo());
        existente.setSueldo(dto.getSueldo());
        existente.setFechaContratacion(dto.getFechaContratacion());
        if (dto.getActivo() != null) {
            existente.setActivo(dto.getActivo());
        }

        Empleado guardado = empleadoRepository.save(existente);
        log.info("Empleado actualizado: ID {}", id);
        return convertirAResponseDTO(guardado);
    }

    /**
     * Borrado LÓGICO (soft delete). Marca activo=false.
     */
    public void desactivar(Long id) {
        log.info("Desactivando empleado con ID: {}", id);
        Empleado empleado = empleadoRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException(
                        "Empleado con ID " + id + " no encontrado"));
        empleado.setActivo(false);
        empleadoRepository.save(empleado);
        log.info("Empleado desactivado: ID {}", id);
    }

    public EmpleadoResponseDTO reactivar(Long id) {
        log.info("Reactivando empleado con ID: {}", id);
        Empleado empleado = empleadoRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException(
                        "Empleado con ID " + id + " no encontrado"));
        empleado.setActivo(true);
        Empleado guardado = empleadoRepository.save(empleado);
        log.info("Empleado reactivado: ID {}", id);
        return convertirAResponseDTO(guardado);
    }

    // ============================================
    // CONVERSIÓN
    // ============================================

    private Empleado convertirAEntidad(EmpleadoRequestDTO dto) {
        Empleado empleado = new Empleado();
        empleado.setRut(dto.getRut());
        empleado.setNombre(dto.getNombre());
        empleado.setApellido(dto.getApellido());
        empleado.setEmail(dto.getEmail());
        empleado.setTelefono(dto.getTelefono());
        empleado.setCargo(dto.getCargo());
        empleado.setSueldo(dto.getSueldo());
        empleado.setFechaContratacion(dto.getFechaContratacion());
        empleado.setActivo(dto.getActivo());
        return empleado;
    }

    private EmpleadoResponseDTO convertirAResponseDTO(Empleado empleado) {
        return new EmpleadoResponseDTO(
                empleado.getId(),
                empleado.getRut(),
                empleado.getNombre(),
                empleado.getApellido(),
                empleado.getEmail(),
                empleado.getTelefono(),
                empleado.getCargo(),
                empleado.getSueldo(),
                empleado.getFechaContratacion(),
                empleado.getActivo(),
                empleado.getFechaCreacion(),
                empleado.getFechaActualizacion()
        );
    }
}