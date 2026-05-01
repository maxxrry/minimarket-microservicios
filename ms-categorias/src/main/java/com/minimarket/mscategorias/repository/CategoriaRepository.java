package com.minimarket.mscategorias.repository;

import com.minimarket.mscategorias.model.Categoria;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repositorio de la entidad Categoria.
 * Provee CRUD automático mediante JpaRepository y métodos derivados.
 */
@Repository
public interface CategoriaRepository extends JpaRepository<Categoria, Long> {

    /**
     * Busca una categoría por su nombre (case-insensitive).
     * → SELECT * FROM categorias WHERE LOWER(nombre) = LOWER(?)
     */
    Optional<Categoria> findByNombreIgnoreCase(String nombre);

    /**
     * Busca una categoría por su código interno.
     * → SELECT * FROM categorias WHERE codigo = ?
     */
    Optional<Categoria> findByCodigo(String codigo);

    /**
     * Lista las categorías activas.
     * → SELECT * FROM categorias WHERE activa = true
     */
    List<Categoria> findByActivaTrue();

    /**
     * Verifica si existe una categoría con el nombre dado.
     * Útil para validar duplicados antes de crear.
     */
    boolean existsByNombreIgnoreCase(String nombre);

    /**
     * Verifica si existe una categoría con el código dado.
     */
    boolean existsByCodigo(String codigo);
}