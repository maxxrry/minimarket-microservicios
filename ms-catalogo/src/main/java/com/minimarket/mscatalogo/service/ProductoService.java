package com.minimarket.mscatalogo.service;

import com.minimarket.mscatalogo.exception.RecursoNoEncontradoException;
import com.minimarket.mscatalogo.exception.CodigoBarraDuplicadoException;
import com.minimarket.mscatalogo.model.Producto;
import com.minimarket.mscatalogo.repository.ProductoRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;

/**
 * Capa de servicio del microservicio ms-catalogo.
 * Contiene la LÓGICA DE NEGOCIO de los productos.
 * El Controller delega aquí, y este Service usa el Repository para persistir.
 */
@Service
public class ProductoService {

    // Logger para registrar eventos de la capa de servicio (SLF4J)
    private static final Logger log = LoggerFactory.getLogger(ProductoService.class);

    @Autowired
    private ProductoRepository productoRepository;

    /**
     * Lista todos los productos del catálogo.
     */
    public List<Producto> listarTodos() {
        log.info("Listando todos los productos del catálogo");
        return productoRepository.findAll();
    }

    /**
     * Lista solo los productos activos (no dados de baja).
     * Regla de negocio: en operaciones de venta solo se muestran productos activos.
     */
    public List<Producto> listarActivos() {
        log.info("Listando productos activos");
        return productoRepository.findByActivoTrue();
    }

    /**
     * Busca un producto por su ID.
     * Si no existe, lanza una excepción personalizada que será capturada
     * por el @ControllerAdvice y convertida en respuesta HTTP 404.
     */
    public Producto obtenerPorId(Long id) {
        log.info("Buscando producto con ID: {}", id);
        return productoRepository.findById(id)
                .orElseThrow(() -> {
                    log.warn("Producto con ID {} no encontrado", id);
                    return new RecursoNoEncontradoException(
                            "Producto con ID " + id + " no encontrado");
                });
    }

    /**
     * Crea un producto nuevo.
     * Reglas de negocio aplicadas:
     *   1. El código de barra no se puede repetir.
     *   2. El precio no puede ser negativo ni cero.
     *   3. Por defecto el producto se crea como activo.
     */
    public Producto crear(Producto producto) {
        log.info("Creando nuevo producto: {}", producto.getNombre());

        // Regla 1: Validar código de barra único
        if (producto.getCodigoBarra() != null &&
                productoRepository.existsByCodigoBarra(producto.getCodigoBarra())) {
            log.warn("Intento de crear producto con código de barra duplicado: {}",
                    producto.getCodigoBarra());
            throw new CodigoBarraDuplicadoException(
                    "Ya existe un producto con el código de barra: " + producto.getCodigoBarra());
        }

        // Regla 2: Validar precio positivo
        if (producto.getPrecio().compareTo(BigDecimal.ZERO) <= 0) {
            log.warn("Intento de crear producto con precio inválido: {}", producto.getPrecio());
            throw new IllegalArgumentException("El precio debe ser mayor a cero");
        }

        // Regla 3: Por defecto activo si no se especifica
        if (producto.getActivo() == null) {
            producto.setActivo(true);
        }

        Producto guardado = productoRepository.save(producto);
        log.info("Producto creado exitosamente con ID: {}", guardado.getId());
        return guardado;
    }

    /**
     * Actualiza un producto existente.
     * Si no existe, lanza excepción.
     */
    public Producto actualizar(Long id, Producto productoActualizado) {
        log.info("Actualizando producto con ID: {}", id);

        // Verifica que exista (reutiliza obtenerPorId)
        Producto existente = obtenerPorId(id);

        // Actualiza solo los campos modificables
        existente.setNombre(productoActualizado.getNombre());
        existente.setDescripcion(productoActualizado.getDescripcion());
        existente.setPrecio(productoActualizado.getPrecio());
        existente.setCategoriaId(productoActualizado.getCategoriaId());
        existente.setProveedorId(productoActualizado.getProveedorId());

        // Validación: precio positivo
        if (existente.getPrecio().compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("El precio debe ser mayor a cero");
        }

        Producto guardado = productoRepository.save(existente);
        log.info("Producto actualizado exitosamente: ID {}", id);
        return guardado;
    }

    /**
     * Borrado LÓGICO de un producto (soft delete).
     * No elimina el registro, solo marca activo=false.
     * Esto preserva el histórico para reportes y auditoría.
     */
    public void darDeBaja(Long id) {
        log.info("Dando de baja producto con ID: {}", id);
        Producto producto = obtenerPorId(id);
        producto.setActivo(false);
        productoRepository.save(producto);
        log.info("Producto con ID {} dado de baja exitosamente", id);
    }

    /**
     * Reactivar un producto previamente dado de baja.
     */
    public Producto reactivar(Long id) {
        log.info("Reactivando producto con ID: {}", id);
        Producto producto = obtenerPorId(id);
        producto.setActivo(true);
        return productoRepository.save(producto);
    }

    /**
     * Lista productos por categoría.
     * Usado por ms-categorias cuando consulta productos asociados.
     */
    public List<Producto> listarPorCategoria(Long categoriaId) {
        log.info("Listando productos de la categoría: {}", categoriaId);
        return productoRepository.findByCategoriaId(categoriaId);
    }
}