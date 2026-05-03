package com.minimarket.msempleados.repository;

import com.minimarket.msempleados.model.Cargo;
import com.minimarket.msempleados.model.Empleado;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repositorio JPA para la entidad Empleado.
 */
@Repository
public interface EmpleadoRepository extends JpaRepository<Empleado, Long> {

    boolean existsByRut(String rut);

    boolean existsByEmailIgnoreCase(String email);

    Optional<Empleado> findByRut(String rut);

    Optional<Empleado> findByEmailIgnoreCase(String email);

    List<Empleado> findByActivoTrue();

    List<Empleado> findByActivoFalse();

    List<Empleado> findByCargo(Cargo cargo);
}