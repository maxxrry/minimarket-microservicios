package com.minimarket.msinventario.repository;

import com.minimarket.msinventario.model.Stock;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface StockRepository extends JpaRepository<Stock, Long> {

    /**
     * Busca el stock asociado a un producto específico.
     */
    Optional<Stock> findByProductoId(Long productoId);

    /**
     * Verifica si existe stock registrado para un producto.
     */
    boolean existsByProductoId(Long productoId);

    /**
     * Lista los stocks que están bajo el nivel mínimo (alerta de reposición).
     * → SELECT * FROM stock WHERE cantidad_actual <= cantidad_minima
     */
    List<Stock> findByCantidadActualLessThanEqual(Integer umbral);
}