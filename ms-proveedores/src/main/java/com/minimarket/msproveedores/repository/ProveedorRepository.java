package com.minimarket.msproveedores.repository;

import com.minimarket.msproveedores.model.Proveedor;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ProveedorRepository extends JpaRepository<Proveedor, Long> {

    /**
     * Busca un proveedor por su RUT.
     * → SELECT * FROM proveedores WHERE rut = ?
     */
    Optional<Proveedor> findByRut(String rut);

    /**
     * Busca un proveedor por su email (case-insensitive).
     */
    Optional<Proveedor> findByEmailIgnoreCase(String email);

    /**
     * Lista los proveedores activos.
     */
    List<Proveedor> findByActivoTrue();

    /**
     * Lista los proveedores de una ciudad específica.
     */
    List<Proveedor> findByCiudadIgnoreCase(String ciudad);

    /**
     * Verifica si existe un proveedor con el RUT dado.
     */
    boolean existsByRut(String rut);

    /**
     * Verifica si existe un proveedor con el email dado.
     */
    boolean existsByEmailIgnoreCase(String email);
}