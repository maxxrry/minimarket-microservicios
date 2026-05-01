package com.minimarket.mscategorias.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

/**
 * Entidad que representa una Categoría de productos del minimarket.
 * Mapea a la tabla "categorias" en la base de datos mm_categorias.
 *
 * Ejemplos de categorías: Lácteos, Abarrotes, Bebidas, Aseo, Panadería.
 */
@Entity
@Table(name = "categorias")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Categoria {

    /**
     * Clave primaria autogenerada.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Nombre único de la categoría. Ej: "Lácteos", "Bebidas".
     * No puede repetirse.
     */
    @Column(name = "nombre", nullable = false, unique = true, length = 50)
    private String nombre;

    /**
     * Descripción detallada de la categoría.
     */
    @Column(name = "descripcion", length = 255)
    private String descripcion;

    /**
     * Código interno único de la categoría. Ej: "LAC-001", "BEB-002".
     * Útil para sistemas internos y reportes.
     */
    @Column(name = "codigo", nullable = false, unique = true, length = 20)
    private String codigo;

    /**
     * Indica si la categoría está activa (borrado lógico).
     * Por defecto, true al crear.
     */
    @Column(name = "activa", nullable = false)
    private Boolean activa = true;

    /**
     * Fecha de creación. Hibernate la asigna automáticamente.
     */
    @CreationTimestamp
    @Column(name = "fecha_creacion", nullable = false, updatable = false)
    private LocalDateTime fechaCreacion;

    /**
     * Fecha de última actualización. Hibernate la actualiza automáticamente.
     */
    @UpdateTimestamp
    @Column(name = "fecha_actualizacion")
    private LocalDateTime fechaActualizacion;
}