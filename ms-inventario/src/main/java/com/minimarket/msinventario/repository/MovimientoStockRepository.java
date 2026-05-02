package com.minimarket.msinventario.repository;

import com.minimarket.msinventario.model.MovimientoStock;
import com.minimarket.msinventario.model.TipoMovimiento;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MovimientoStockRepository extends JpaRepository<MovimientoStock, Long> {

    /**
     * Lista todos los movimientos de un stock específico, ordenados por fecha desc.
     */
    List<MovimientoStock> findByStockIdOrderByFechaMovimientoDesc(Long stockId);

    /**
     * Lista los movimientos por tipo (ENTRADA, SALIDA, AJUSTE).
     */
    List<MovimientoStock> findByTipoMovimiento(TipoMovimiento tipo);
}