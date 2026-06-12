package com.minimarket.mscategorias.model;

import io.swagger.v3.oas.annotations.media.Schema;
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
@Schema(description = "Entidad que representa una categoría de productos")
public class Categoria {

    /**
     * Clave primaria autogenerada.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Schema(description = "ID único de la categoría", example = "1")
    private Long id;

    /**
     * Nombre único de la categoría. Ej: "Lácteos", "Bebidas".
     * No puede repetirse.
     */
    @Column(name = "nombre", nullable = false, unique = true, length = 50)
    @Schema(description = "Nombre de la categoría", example = "Lácteos", maxLength = 50)
    private String nombre;

    /**
     * Descripción detallada de la categoría.
     */
    @Column(name = "descripcion", length = 255)
    @Schema(description = "Descripción de la categoría", example = "Productos lácteos y derivados")
    private String descripcion;

    /**
     * Código interno único de la categoría. Ej: "LAC-001", "BEB-002".
     * Útil para sistemas internos y reportes.
     */
    @Column(name = "codigo", nullable = false, unique = true, length = 20)
    @Schema(description = "Código interno único", example = "LAC-001", maxLength = 20)
    private String codigo;

    /**
     * Indica si la categoría está activa (borrado lógico).
     * Por defecto, true al crear.
     */
    @Column(name = "activa", nullable = false)
    @Schema(description = "Indica si la categoría está activa", example = "true")
    private Boolean activa = true;

    /**
     * Fecha de creación. Hibernate la asigna automáticamente.
     */
    @CreationTimestamp
    @Column(name = "fecha_creacion", nullable = false, updatable = false)
    @Schema(description = "Fecha de creación", example = "2026-01-15 10:30:00")
    private LocalDateTime fechaCreacion;

    /**
     * Fecha de última actualización. Hibernate la actualiza automáticamente.
     */
    @UpdateTimestamp
    @Column(name = "fecha_actualizacion")
    @Schema(description = "Fecha de última actualización", example = "2026-01-20 14:45:00")
    private LocalDateTime fechaActualizacion;
}