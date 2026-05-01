package com.minimarket.mscategorias.service;

import com.minimarket.mscategorias.dto.CategoriaRequestDTO;
import com.minimarket.mscategorias.dto.CategoriaResponseDTO;
import com.minimarket.mscategorias.exception.CategoriaDuplicadaException;
import com.minimarket.mscategorias.exception.RecursoNoEncontradoException;
import com.minimarket.mscategorias.model.Categoria;
import com.minimarket.mscategorias.repository.CategoriaRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Capa de servicio del microservicio ms-categorias.
 * Contiene la LÓGICA DE NEGOCIO de las categorías.
 */
@Service
public class CategoriaService {

    private static final Logger log = LoggerFactory.getLogger(CategoriaService.class);

    @Autowired
    private CategoriaRepository categoriaRepository;

    /**
     * Lista todas las categorías del sistema.
     */
    public List<CategoriaResponseDTO> listarTodas() {
        log.info("Listando todas las categorías");
        return categoriaRepository.findAll().stream()
                .map(this::convertirAResponseDTO)
                .collect(Collectors.toList());
    }

    /**
     * Lista solo las categorías activas.
     */
    public List<CategoriaResponseDTO> listarActivas() {
        log.info("Listando categorías activas");
        return categoriaRepository.findByActivaTrue().stream()
                .map(this::convertirAResponseDTO)
                .collect(Collectors.toList());
    }

    /**
     * Busca una categoría por ID.
     * Si no existe, lanza RecursoNoEncontradoException (HTTP 404).
     */
    public CategoriaResponseDTO obtenerPorId(Long id) {
        log.info("Buscando categoría con ID: {}", id);
        Categoria categoria = categoriaRepository.findById(id)
                .orElseThrow(() -> {
                    log.warn("Categoría con ID {} no encontrada", id);
                    return new RecursoNoEncontradoException(
                            "Categoría con ID " + id + " no encontrada");
                });
        return convertirAResponseDTO(categoria);
    }

    /**
     * Crea una nueva categoría.
     * Reglas de negocio:
     *   1. El nombre no puede estar duplicado (case-insensitive).
     *   2. El código no puede estar duplicado.
     *   3. Por defecto se crea como activa.
     */
    public CategoriaResponseDTO crear(CategoriaRequestDTO dto) {
        log.info("Creando nueva categoría: {}", dto.getNombre());

        // Regla 1: nombre único
        if (categoriaRepository.existsByNombreIgnoreCase(dto.getNombre())) {
            log.warn("Nombre de categoría duplicado: {}", dto.getNombre());
            throw new CategoriaDuplicadaException(
                    "Ya existe una categoría con el nombre: " + dto.getNombre());
        }

        // Regla 2: código único
        if (categoriaRepository.existsByCodigo(dto.getCodigo())) {
            log.warn("Código de categoría duplicado: {}", dto.getCodigo());
            throw new CategoriaDuplicadaException(
                    "Ya existe una categoría con el código: " + dto.getCodigo());
        }

        Categoria categoria = convertirAEntidad(dto);

        // Regla 3: activa por defecto
        if (categoria.getActiva() == null) {
            categoria.setActiva(true);
        }

        Categoria guardada = categoriaRepository.save(categoria);
        log.info("Categoría creada exitosamente con ID: {}", guardada.getId());
        return convertirAResponseDTO(guardada);
    }

    /**
     * Actualiza una categoría existente.
     */
    public CategoriaResponseDTO actualizar(Long id, CategoriaRequestDTO dto) {
        log.info("Actualizando categoría con ID: {}", id);

        Categoria existente = categoriaRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException(
                        "Categoría con ID " + id + " no encontrada"));

        // Si cambió el nombre, validar duplicado
        if (!existente.getNombre().equalsIgnoreCase(dto.getNombre()) &&
                categoriaRepository.existsByNombreIgnoreCase(dto.getNombre())) {
            throw new CategoriaDuplicadaException(
                    "Ya existe otra categoría con el nombre: " + dto.getNombre());
        }

        // Si cambió el código, validar duplicado
        if (!existente.getCodigo().equals(dto.getCodigo()) &&
                categoriaRepository.existsByCodigo(dto.getCodigo())) {
            throw new CategoriaDuplicadaException(
                    "Ya existe otra categoría con el código: " + dto.getCodigo());
        }

        existente.setNombre(dto.getNombre());
        existente.setDescripcion(dto.getDescripcion());
        existente.setCodigo(dto.getCodigo());
        if (dto.getActiva() != null) {
            existente.setActiva(dto.getActiva());
        }

        Categoria guardada = categoriaRepository.save(existente);
        log.info("Categoría actualizada: ID {}", id);
        return convertirAResponseDTO(guardada);
    }

    /**
     * Da de baja una categoría (borrado lógico).
     */
    public void darDeBaja(Long id) {
        log.info("Dando de baja categoría con ID: {}", id);
        Categoria categoria = categoriaRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException(
                        "Categoría con ID " + id + " no encontrada"));
        categoria.setActiva(false);
        categoriaRepository.save(categoria);
        log.info("Categoría con ID {} dada de baja", id);
    }

    /**
     * Reactiva una categoría previamente dada de baja.
     */
    public CategoriaResponseDTO reactivar(Long id) {
        log.info("Reactivando categoría con ID: {}", id);
        Categoria categoria = categoriaRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException(
                        "Categoría con ID " + id + " no encontrada"));
        categoria.setActiva(true);
        return convertirAResponseDTO(categoriaRepository.save(categoria));
    }

    // ════════════════════════════════════════════════
    // CONVERSIONES ENTRE ENTIDAD Y DTO
    // ════════════════════════════════════════════════

    private CategoriaResponseDTO convertirAResponseDTO(Categoria c) {
        return CategoriaResponseDTO.builder()
                .id(c.getId())
                .nombre(c.getNombre())
                .descripcion(c.getDescripcion())
                .codigo(c.getCodigo())
                .activa(c.getActiva())
                .fechaCreacion(c.getFechaCreacion())
                .fechaActualizacion(c.getFechaActualizacion())
                .build();
    }

    private Categoria convertirAEntidad(CategoriaRequestDTO dto) {
        Categoria c = new Categoria();
        c.setNombre(dto.getNombre());
        c.setDescripcion(dto.getDescripcion());
        c.setCodigo(dto.getCodigo());
        c.setActiva(dto.getActiva());
        return c;
    }
}