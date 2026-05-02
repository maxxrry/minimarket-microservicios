package com.minimarket.msproveedores.service;

import com.minimarket.msproveedores.dto.ProveedorRequestDTO;
import com.minimarket.msproveedores.dto.ProveedorResponseDTO;
import com.minimarket.msproveedores.exception.ProveedorDuplicadoException;
import com.minimarket.msproveedores.exception.RecursoNoEncontradoException;
import com.minimarket.msproveedores.model.Proveedor;
import com.minimarket.msproveedores.repository.ProveedorRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class ProveedorService {

    private static final Logger log = LoggerFactory.getLogger(ProveedorService.class);

    @Autowired
    private ProveedorRepository proveedorRepository;

    public List<ProveedorResponseDTO> listarTodos() {
        log.info("Listando todos los proveedores");
        return proveedorRepository.findAll().stream()
                .map(this::convertirAResponseDTO)
                .collect(Collectors.toList());
    }

    public List<ProveedorResponseDTO> listarActivos() {
        log.info("Listando proveedores activos");
        return proveedorRepository.findByActivoTrue().stream()
                .map(this::convertirAResponseDTO)
                .collect(Collectors.toList());
    }

    public ProveedorResponseDTO obtenerPorId(Long id) {
        log.info("Buscando proveedor con ID: {}", id);
        Proveedor proveedor = proveedorRepository.findById(id)
                .orElseThrow(() -> {
                    log.warn("Proveedor con ID {} no encontrado", id);
                    return new RecursoNoEncontradoException(
                            "Proveedor con ID " + id + " no encontrado");
                });
        return convertirAResponseDTO(proveedor);
    }

    public List<ProveedorResponseDTO> listarPorCiudad(String ciudad) {
        log.info("Listando proveedores de la ciudad: {}", ciudad);
        return proveedorRepository.findByCiudadIgnoreCase(ciudad).stream()
                .map(this::convertirAResponseDTO)
                .collect(Collectors.toList());
    }

    /**
     * Crea un nuevo proveedor.
     * Reglas de negocio:
     *   1. RUT único.
     *   2. Email único.
     *   3. Por defecto activo.
     */
    public ProveedorResponseDTO crear(ProveedorRequestDTO dto) {
        log.info("Creando nuevo proveedor: {}", dto.getRazonSocial());

        // Regla 1: RUT único
        if (proveedorRepository.existsByRut(dto.getRut())) {
            log.warn("RUT duplicado: {}", dto.getRut());
            throw new ProveedorDuplicadoException(
                    "Ya existe un proveedor con el RUT: " + dto.getRut());
        }

        // Regla 2: email único
        if (proveedorRepository.existsByEmailIgnoreCase(dto.getEmail())) {
            log.warn("Email duplicado: {}", dto.getEmail());
            throw new ProveedorDuplicadoException(
                    "Ya existe un proveedor con el email: " + dto.getEmail());
        }

        Proveedor proveedor = convertirAEntidad(dto);

        // Regla 3: activo por defecto
        if (proveedor.getActivo() == null) {
            proveedor.setActivo(true);
        }

        Proveedor guardado = proveedorRepository.save(proveedor);
        log.info("Proveedor creado exitosamente con ID: {}", guardado.getId());
        return convertirAResponseDTO(guardado);
    }

    public ProveedorResponseDTO actualizar(Long id, ProveedorRequestDTO dto) {
        log.info("Actualizando proveedor con ID: {}", id);

        Proveedor existente = proveedorRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException(
                        "Proveedor con ID " + id + " no encontrado"));

        // Validar RUT duplicado si cambió
        if (!existente.getRut().equals(dto.getRut()) &&
                proveedorRepository.existsByRut(dto.getRut())) {
            throw new ProveedorDuplicadoException(
                    "Ya existe otro proveedor con el RUT: " + dto.getRut());
        }

        // Validar email duplicado si cambió
        if (!existente.getEmail().equalsIgnoreCase(dto.getEmail()) &&
                proveedorRepository.existsByEmailIgnoreCase(dto.getEmail())) {
            throw new ProveedorDuplicadoException(
                    "Ya existe otro proveedor con el email: " + dto.getEmail());
        }

        existente.setRazonSocial(dto.getRazonSocial());
        existente.setRut(dto.getRut());
        existente.setNombreContacto(dto.getNombreContacto());
        existente.setEmail(dto.getEmail());
        existente.setTelefono(dto.getTelefono());
        existente.setDireccion(dto.getDireccion());
        existente.setCiudad(dto.getCiudad());
        if (dto.getActivo() != null) {
            existente.setActivo(dto.getActivo());
        }

        Proveedor guardado = proveedorRepository.save(existente);
        log.info("Proveedor actualizado: ID {}", id);
        return convertirAResponseDTO(guardado);
    }

    public void darDeBaja(Long id) {
        log.info("Dando de baja proveedor con ID: {}", id);
        Proveedor proveedor = proveedorRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException(
                        "Proveedor con ID " + id + " no encontrado"));
        proveedor.setActivo(false);
        proveedorRepository.save(proveedor);
        log.info("Proveedor con ID {} dado de baja", id);
    }

    public ProveedorResponseDTO reactivar(Long id) {
        log.info("Reactivando proveedor con ID: {}", id);
        Proveedor proveedor = proveedorRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException(
                        "Proveedor con ID " + id + " no encontrado"));
        proveedor.setActivo(true);
        return convertirAResponseDTO(proveedorRepository.save(proveedor));
    }

    // ════════════════════════════════════════════════
    // CONVERSIONES ENTRE ENTIDAD Y DTO
    // ════════════════════════════════════════════════

    private ProveedorResponseDTO convertirAResponseDTO(Proveedor p) {
        return ProveedorResponseDTO.builder()
                .id(p.getId())
                .razonSocial(p.getRazonSocial())
                .rut(p.getRut())
                .nombreContacto(p.getNombreContacto())
                .email(p.getEmail())
                .telefono(p.getTelefono())
                .direccion(p.getDireccion())
                .ciudad(p.getCiudad())
                .activo(p.getActivo())
                .fechaCreacion(p.getFechaCreacion())
                .fechaActualizacion(p.getFechaActualizacion())
                .build();
    }

    private Proveedor convertirAEntidad(ProveedorRequestDTO dto) {
        Proveedor p = new Proveedor();
        p.setRazonSocial(dto.getRazonSocial());
        p.setRut(dto.getRut());
        p.setNombreContacto(dto.getNombreContacto());
        p.setEmail(dto.getEmail());
        p.setTelefono(dto.getTelefono());
        p.setDireccion(dto.getDireccion());
        p.setCiudad(dto.getCiudad());
        p.setActivo(dto.getActivo());
        return p;
    }
}