package com.minimarket.mspromociones.service;

import com.minimarket.mspromociones.dto.PromocionRequestDTO;
import com.minimarket.mspromociones.dto.PromocionResponseDTO;
import com.minimarket.mspromociones.exception.PromocionInvalidaException;
import com.minimarket.mspromociones.exception.RecursoNoEncontradoException;
import com.minimarket.mspromociones.model.Promocion;
import com.minimarket.mspromociones.repository.PromocionRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Capa de servicio del microservicio ms-promociones.
 * Contiene la lógica de negocio para crear, consultar y modificar promociones.
 *
 * Reglas de negocio:
 *   - La fecha de inicio debe ser anterior a la fecha de fin.
 *   - La promoción debe asociarse a un producto O a una categoría (no ambos null).
 *   - Por defecto se crea como activa.
 */
@Service
public class PromocionService {

    private static final Logger log = LoggerFactory.getLogger(PromocionService.class);

    @Autowired
    private PromocionRepository promocionRepository;

    public List<PromocionResponseDTO> listarTodos() {
        log.info("Listando todas las promociones");
        return promocionRepository.findAll().stream()
                .map(this::convertirAResponseDTO)
                .collect(Collectors.toList());
    }

    public List<PromocionResponseDTO> listarActivas() {
        log.info("Listando promociones activas");
        return promocionRepository.findByActivoTrue().stream()
                .map(this::convertirAResponseDTO)
                .collect(Collectors.toList());
    }

    public PromocionResponseDTO obtenerPorId(Long id) {
        log.info("Buscando promoción por ID: {}", id);
        Promocion p = promocionRepository.findById(id)
                .orElseThrow(() -> {
                    log.warn("Promoción con ID {} no encontrada", id);
                    return new RecursoNoEncontradoException("Promoción no encontrada con ID: " + id);
                });
        return convertirAResponseDTO(p);
    }

    /**
     * Lista las promociones activas asociadas a un producto específico.
     * Endpoint usado por ms-ventas vía Feign.
     */
    public List<PromocionResponseDTO> listarPorProducto(Long productoId) {
        log.info("Listando promociones del producto: {}", productoId);
        return promocionRepository.findByProductoIdAndActivoTrue(productoId).stream()
                .map(this::convertirAResponseDTO)
                .collect(Collectors.toList());
    }

    /**
     * Lista las promociones activas asociadas a una categoría específica.
     * Endpoint usado por ms-ventas vía Feign.
     */
    public List<PromocionResponseDTO> listarPorCategoria(Long categoriaId) {
        log.info("Listando promociones de la categoría: {}", categoriaId);
        return promocionRepository.findByCategoriaIdAndActivoTrue(categoriaId).stream()
                .map(this::convertirAResponseDTO)
                .collect(Collectors.toList());
    }

    public PromocionResponseDTO crear(PromocionRequestDTO dto) {
        log.info("Creando nueva promoción: {}", dto.getNombre());

        validarLogicaPromocion(dto);

        Promocion p = convertirAEntidad(dto);
        p.setActivo(true);
        Promocion guardada = promocionRepository.save(p);
        log.info("Promoción creada exitosamente con ID: {}", guardada.getId());
        return convertirAResponseDTO(guardada);
    }

    public PromocionResponseDTO actualizar(Long id, PromocionRequestDTO dto) {
        log.info("Actualizando promoción ID: {}", id);
        Promocion p = promocionRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException(
                        "No se puede actualizar. Promoción con ID " + id + " no existe"));

        validarLogicaPromocion(dto);

        p.setNombre(dto.getNombre());
        p.setDescripcion(dto.getDescripcion());
        p.setPorcentajeDescuento(dto.getPorcentajeDescuento());
        p.setFechaInicio(dto.getFechaInicio());
        p.setFechaFin(dto.getFechaFin());
        p.setProductoId(dto.getProductoId());
        p.setCategoriaId(dto.getCategoriaId());

        Promocion actualizada = promocionRepository.save(p);
        log.info("Promoción ID {} actualizada", id);
        return convertirAResponseDTO(actualizada);
    }

    public void darDeBaja(Long id) {
        log.info("Dando de baja promoción ID: {}", id);
        Promocion p = promocionRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException(
                        "No se encontró la promoción con ID " + id));
        p.setActivo(false);
        promocionRepository.save(p);
        log.info("Promoción ID {} dada de baja", id);
    }

    public PromocionResponseDTO reactivar(Long id) {
        log.info("Reactivando promoción ID: {}", id);
        Promocion p = promocionRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException(
                        "No se encontró la promoción con ID " + id));
        p.setActivo(true);
        return convertirAResponseDTO(promocionRepository.save(p));
    }

    // ════════════════════════════════════════════════════════════
    // MÉTODOS PRIVADOS
    // ════════════════════════════════════════════════════════════

    /**
     * Valida las reglas de negocio antes de crear o actualizar.
     */
    private void validarLogicaPromocion(PromocionRequestDTO dto) {
        // La fecha inicio debe ser ESTRICTAMENTE anterior a la fecha fin
        if (!dto.getFechaInicio().isBefore(dto.getFechaFin())) {
            throw new PromocionInvalidaException(
                    "La fecha de inicio debe ser anterior a la fecha de fin.");
        }
        // Debe estar asociada a un producto O a una categoría (no ambos null)
        if (dto.getProductoId() == null && dto.getCategoriaId() == null) {
            throw new PromocionInvalidaException(
                    "La promoción debe estar asociada a un producto o a una categoría.");
        }
    }

    private PromocionResponseDTO convertirAResponseDTO(Promocion p) {
        return PromocionResponseDTO.builder()
                .id(p.getId())
                .nombre(p.getNombre())
                .descripcion(p.getDescripcion())
                .porcentajeDescuento(p.getPorcentajeDescuento())
                .fechaInicio(p.getFechaInicio())
                .fechaFin(p.getFechaFin())
                .productoId(p.getProductoId())
                .categoriaId(p.getCategoriaId())
                .activo(p.getActivo())
                .fechaCreacion(p.getFechaCreacion())
                .fechaActualizacion(p.getFechaActualizacion())
                .build();
    }

    private Promocion convertirAEntidad(PromocionRequestDTO dto) {
        Promocion p = new Promocion();
        p.setNombre(dto.getNombre());
        p.setDescripcion(dto.getDescripcion());
        p.setPorcentajeDescuento(dto.getPorcentajeDescuento());
        p.setFechaInicio(dto.getFechaInicio());
        p.setFechaFin(dto.getFechaFin());
        p.setProductoId(dto.getProductoId());
        p.setCategoriaId(dto.getCategoriaId());
        return p;
    }
}
