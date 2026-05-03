package com.minimarket.mspromociones.repository;

import com.minimarket.mspromociones.model.Promocion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PromocionRepository extends JpaRepository<Promocion, Long> {

    /**
     * Lista las promociones activas.
     */
    List<Promocion> findByActivoTrue();

    /**
     * Lista las promociones activas asociadas a un producto específico.
     * Usado por ms-ventas vía Feign para aplicar descuentos.
     */
    List<Promocion> findByProductoIdAndActivoTrue(Long productoId);

    /**
     * Lista las promociones activas asociadas a una categoría específica.
     * Usado por ms-ventas vía Feign para aplicar descuentos por categoría.
     */
    List<Promocion> findByCategoriaIdAndActivoTrue(Long categoriaId);
}
