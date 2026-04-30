package com.minimarket.mscatalogo.service;

import com.minimarket.mscatalogo.dto.ProductoRequestDTO;
import com.minimarket.mscatalogo.dto.ProductoResponseDTO;
import com.minimarket.mscatalogo.exception.CodigoBarraDuplicadoException;
import com.minimarket.mscatalogo.exception.RecursoNoEncontradoException;
import com.minimarket.mscatalogo.model.Producto;
import com.minimarket.mscatalogo.repository.ProductoRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Capa de servicio del microservicio ms-catalogo.
 * Contiene la LÓGICA DE NEGOCIO de los productos.
 * Recibe DTOs de entrada, opera con entidades, devuelve DTOs de salida.
 */
@Service
public class ProductoService {

    private static final Logger log = LoggerFactory.getLogger(ProductoService.class);

    @Autowired
    private ProductoRepository productoRepository;

    /**
     * Lista todos los productos del catálogo.
     */
    public List<ProductoResponseDTO> listarTodos() {
        log.info("Listando todos los productos del catálogo");
        return productoRepository.findAll().stream()
                .map(this::convertirAResponseDTO)
                .collect(Collectors.toList());
    }

    /**
     * Lista solo los productos activos.
     */
    public List<ProductoResponseDTO> listarActivos() {
        log.info("Listando productos activos");
        return productoRepository.findByActivoTrue().stream()
                .map(this::convertirAResponseDTO)
                .collect(Collectors.toList());
    }

    /**
     * Busca un producto por ID.
     * Si no existe, lanza RecursoNoEncontradoException (HTTP 404).
     */
    public ProductoResponseDTO obtenerPorId(Long id) {
        log.info("Buscando producto con ID: {}", id);
        Producto producto = productoRepository.findById(id)
                .orElseThrow(() -> {
                    log.warn("Producto con ID {} no encontrado", id);
                    return new RecursoNoEncontradoException(
                            "Producto con ID " + id + " no encontrado");
                });
        return convertirAResponseDTO(producto);
    }

    /**
     * Crea un nuevo producto.
     * Reglas de negocio:
     *   1. Código de barra único.
     *   2. Por defecto activo si no se especifica.
     */
    public ProductoResponseDTO crear(ProductoRequestDTO dto) {
        log.info("Creando nuevo producto: {}", dto.getNombre());

        // Regla 1: validar código de barra único
        if (productoRepository.existsByCodigoBarra(dto.getCodigoBarra())) {
            log.warn("Código de barra duplicado: {}", dto.getCodigoBarra());
            throw new CodigoBarraDuplicadoException(
                    "Ya existe un producto con el código de barra: " + dto.getCodigoBarra());
        }

        // Convertir DTO a entidad
        Producto producto = convertirAEntidad(dto);

        // Regla 2: activo por defecto
        if (producto.getActivo() == null) {
            producto.setActivo(true);
        }

        Producto guardado = productoRepository.save(producto);
        log.info("Producto creado exitosamente con ID: {}", guardado.getId());
        return convertirAResponseDTO(guardado);
    }

    /**
     * Actualiza un producto existente.
     */
    public ProductoResponseDTO actualizar(Long id, ProductoRequestDTO dto) {
        log.info("Actualizando producto con ID: {}", id);

        Producto existente = productoRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException(
                        "Producto con ID " + id + " no encontrado"));

        // Si cambió el código de barra, validar que el nuevo no exista
        if (!existente.getCodigoBarra().equals(dto.getCodigoBarra()) &&
                productoRepository.existsByCodigoBarra(dto.getCodigoBarra())) {
            throw new CodigoBarraDuplicadoException(
                    "Ya existe otro producto con el código de barra: " + dto.getCodigoBarra());
        }

        // Actualizar campos
        existente.setNombre(dto.getNombre());
        existente.setDescripcion(dto.getDescripcion());
        existente.setPrecio(dto.getPrecio());
        existente.setCodigoBarra(dto.getCodigoBarra());
        existente.setCategoriaId(dto.getCategoriaId());
        existente.setProveedorId(dto.getProveedorId());
        if (dto.getActivo() != null) {
            existente.setActivo(dto.getActivo());
        }

        Producto guardado = productoRepository.save(existente);
        log.info("Producto actualizado: ID {}", id);
        return convertirAResponseDTO(guardado);
    }

    /**
     * Borrado LÓGICO de un producto.
     */
    public void darDeBaja(Long id) {
        log.info("Dando de baja producto con ID: {}", id);
        Producto producto = productoRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException(
                        "Producto con ID " + id + " no encontrado"));
        producto.setActivo(false);
        productoRepository.save(producto);
        log.info("Producto con ID {} dado de baja", id);
    }

    /**
     * Reactivar un producto previamente dado de baja.
     */
    public ProductoResponseDTO reactivar(Long id) {
        log.info("Reactivando producto con ID: {}", id);
        Producto producto = productoRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException(
                        "Producto con ID " + id + " no encontrado"));
        producto.setActivo(true);
        return convertirAResponseDTO(productoRepository.save(producto));
    }

    /**
     * Lista productos por categoría.
     */
    public List<ProductoResponseDTO> listarPorCategoria(Long categoriaId) {
        log.info("Listando productos de la categoría: {}", categoriaId);
        return productoRepository.findByCategoriaId(categoriaId).stream()
                .map(this::convertirAResponseDTO)
                .collect(Collectors.toList());
    }

    // ════════════════════════════════════════════════════════════
    // MÉTODOS PRIVADOS DE CONVERSIÓN ENTRE ENTIDAD Y DTO
    // ════════════════════════════════════════════════════════════

    /**
     * Convierte una entidad Producto en su DTO de respuesta.
     */
    private ProductoResponseDTO convertirAResponseDTO(Producto p) {
        return ProductoResponseDTO.builder()
                .id(p.getId())
                .nombre(p.getNombre())
                .descripcion(p.getDescripcion())
                .precio(p.getPrecio())
                .codigoBarra(p.getCodigoBarra())
                .categoriaId(p.getCategoriaId())
                .proveedorId(p.getProveedorId())
                .activo(p.getActivo())
                .fechaCreacion(p.getFechaCreacion())
                .fechaActualizacion(p.getFechaActualizacion())
                .build();
    }

    /**
     * Convierte un DTO de entrada en una entidad Producto.
     */
    private Producto convertirAEntidad(ProductoRequestDTO dto) {
        Producto p = new Producto();
        p.setNombre(dto.getNombre());
        p.setDescripcion(dto.getDescripcion());
        p.setPrecio(dto.getPrecio());
        p.setCodigoBarra(dto.getCodigoBarra());
        p.setCategoriaId(dto.getCategoriaId());
        p.setProveedorId(dto.getProveedorId());
        p.setActivo(dto.getActivo());
        return p;
    }
}