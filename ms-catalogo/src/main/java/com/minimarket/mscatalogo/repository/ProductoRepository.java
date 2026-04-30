package com.minimarket.mscatalogo.repository;

import com.minimarket.mscatalogo.model.Producto;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repositorio de la entidad Producto.
 * Hereda de JpaRepository, lo que provee automáticamente:
 *   - save(entity)       → INSERT o UPDATE
 *   - findById(id)       → SELECT por ID
 *   - findAll()          → SELECT *
 *   - deleteById(id)     → DELETE por ID
 *   - count()            → COUNT(*)
 *   - existsById(id)     → SELECT 1 WHERE id = ?
 *
 * Spring genera la implementación de esta interfaz en tiempo de ejecución.
 */
@Repository
public interface ProductoRepository extends JpaRepository<Producto, Long> {

    /**
     * Busca un producto por su código de barra.
     * Spring Data genera la query automáticamente a partir del nombre del método.
     * → SELECT * FROM productos WHERE codigo_barra = ?
     */
    Optional<Producto> findByCodigoBarra(String codigoBarra);

    /**
     * Lista los productos activos (donde activo = true).
     * → SELECT * FROM productos WHERE activo = ?
     */
    List<Producto> findByActivoTrue();

    /**
     * Lista los productos de una categoría específica.
     * Útil para cuando ms-categorias pregunta "qué productos hay en esta categoría".
     * → SELECT * FROM productos WHERE categoria_id = ?
     */
    List<Producto> findByCategoriaId(Long categoriaId);

    /**
     * Lista los productos de un proveedor.
     * → SELECT * FROM productos WHERE proveedor_id = ?
     */
    List<Producto> findByProveedorId(Long proveedorId);

    /**
     * Verifica si existe un producto con un código de barra dado.
     * Útil para validar duplicados antes de crear.
     * → SELECT EXISTS(SELECT 1 FROM productos WHERE codigo_barra = ?)
     */
    boolean existsByCodigoBarra(String codigoBarra);
}