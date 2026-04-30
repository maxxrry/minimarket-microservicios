package com.minimarket.mscatalogo.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Entidad que representa un Producto del minimarket.
 * Mapea a la tabla "productos" en la base de datos mm_catalogo.
 */
@Entity
@Table(name = "productos")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Producto {

    /**
     * Clave primaria autogenerada.
     * IDENTITY delega la generación al motor de BD (AUTO_INCREMENT en MySQL).
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Nombre del producto. Ej: "Coca-Cola 1.5L".
     * No puede ser null y máximo 100 caracteres.
     */
    @Column(name = "nombre", nullable = false, length = 100)
    private String nombre;

    /**
     * Descripción detallada del producto.
     * Permite hasta 500 caracteres.
     */
    @Column(name = "descripcion", length = 500)
    private String descripcion;

    /**
     * Precio del producto.
     * Usamos BigDecimal en lugar de double para evitar errores de redondeo
     * en operaciones decimales (estándar para valores monetarios).
     */
    @Column(name = "precio", nullable = false, precision = 10, scale = 2)
    private BigDecimal precio;

    /**
     * Código de barras único del producto.
     * No se puede repetir en la BD (constraint UNIQUE).
     */
    @Column(name = "codigo_barra", unique = true, length = 50)
    private String codigoBarra;

    /**
     * ID de la categoría a la que pertenece el producto.
     * Es una REFERENCIA LÓGICA al microservicio ms-categorias.
     * No usamos @ManyToOne porque están en bases de datos separadas.
     */
    @Column(name = "categoria_id", nullable = false)
    private Long categoriaId;

    /**
     * ID del proveedor del producto.
     * REFERENCIA LÓGICA al microservicio ms-proveedores.
     */
    @Column(name = "proveedor_id", nullable = false)
    private Long proveedorId;

    /**
     * Indica si el producto está activo o fue dado de baja (borrado lógico).
     * Por defecto se crea como activo (true).
     */
    @Column(name = "activo", nullable = false)
    private Boolean activo = true;

    /**
     * Fecha y hora de creación del registro.
     * Hibernate la asigna automáticamente al insertar.
     */
    @CreationTimestamp
    @Column(name = "fecha_creacion", nullable = false, updatable = false)
    private LocalDateTime fechaCreacion;

    /**
     * Fecha y hora de la última actualización.
     * Hibernate la actualiza automáticamente al hacer update.
     */
    @UpdateTimestamp
    @Column(name = "fecha_actualizacion")
    private LocalDateTime fechaActualizacion;
}