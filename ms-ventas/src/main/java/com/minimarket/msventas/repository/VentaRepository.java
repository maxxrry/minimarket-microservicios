package com.minimarket.msventas.repository;

import com.minimarket.msventas.model.EstadoVenta;
import com.minimarket.msventas.model.Venta;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface VentaRepository extends JpaRepository<Venta, Long> {

    /**
     * Busca una venta por su número correlativo único.
     */
    Optional<Venta> findByNumeroVenta(String numeroVenta);

    /**
     * Lista las ventas de un cliente específico.
     */
    List<Venta> findByClienteIdOrderByFechaVentaDesc(Long clienteId);

    /**
     * Lista las ventas de un empleado específico (útil para reportes de productividad).
     */
    List<Venta> findByEmpleadoIdOrderByFechaVentaDesc(Long empleadoId);

    /**
     * Lista las ventas según su estado (PENDIENTE, COMPLETADA, ANULADA).
     */
    List<Venta> findByEstadoOrderByFechaVentaDesc(EstadoVenta estado);

    /**
     * Lista las ventas en un rango de fechas (útil para reportes diarios/mensuales).
     */
    List<Venta> findByFechaVentaBetweenOrderByFechaVentaDesc(
            LocalDateTime desde, LocalDateTime hasta);

    /**
     * Verifica si existe una venta con el número dado.
     */
    boolean existsByNumeroVenta(String numeroVenta);

    /**
     * Cuenta cuántas ventas hay en un mes específico (para generar el correlativo).
     */
    long countByNumeroVentaStartingWith(String prefijo);
}