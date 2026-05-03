package com.minimarket.msclientes.repository;

import com.minimarket.msclientes.model.Cliente;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repositorio JPA para la entidad Cliente.
 * Spring Data JPA genera automáticamente las implementaciones.
 */
@Repository
public interface ClienteRepository extends JpaRepository<Cliente, Long> {

    boolean existsByRut(String rut);

    boolean existsByEmailIgnoreCase(String email);

    Optional<Cliente> findByRut(String rut);

    Optional<Cliente> findByEmailIgnoreCase(String email);

    List<Cliente> findByActivoTrue();

    List<Cliente> findByActivoFalse();
}