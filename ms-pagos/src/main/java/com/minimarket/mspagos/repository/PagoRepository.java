package com.minimarket.mspagos.repository;

import com.minimarket.mspagos.model.EstadoPago;
import com.minimarket.mspagos.model.MetodoPago;
import com.minimarket.mspagos.model.Pago;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repositorio JPA para la entidad Pago.
 */
@Repository
public interface PagoRepository extends JpaRepository<Pago, Long> {

    boolean existsByNumeroTransaccion(String numeroTransaccion);

    Optional<Pago> findByNumeroTransaccion(String numeroTransaccion);

    List<Pago> findByVentaId(Long ventaId);

    List<Pago> findByEstado(EstadoPago estado);

    List<Pago> findByMetodoPago(MetodoPago metodoPago);
}