package com.minimarket.msventas.repository;

import com.minimarket.msventas.model.DetalleVenta;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DetalleVentaRepository extends JpaRepository<DetalleVenta, Long> {

    /**
     * Lista los detalles de una venta específica.
     */
    List<DetalleVenta> findByVentaId(Long ventaId);

    /**
     * Lista los detalles que incluyen un producto específico
     * (útil para reportes de "qué productos se vendieron más").
     */
    List<DetalleVenta> findByProductoId(Long productoId);
}