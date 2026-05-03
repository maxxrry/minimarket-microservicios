package com.minimarket.mspromociones.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Entidad que representa una Promoción del minimarket.
 * Una promoción puede aplicarse a un producto específico O a una categoría completa.
 * Tiene fechas de vigencia y un porcentaje de descuento.
 */
@Entity
@Table(name = "promociones")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Promocion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String nombre;

    @Column(length = 255)
    private String descripcion;

    /**
     * Porcentaje de descuento. Va de 1 a 100.
     * Usamos BigDecimal por precisión decimal (igual que precios).
     */
    @Column(name = "porcentaje_descuento", nullable = false, precision = 5, scale = 2)
    private BigDecimal porcentajeDescuento;

    @Column(name = "fecha_inicio", nullable = false)
    private LocalDateTime fechaInicio;

    @Column(name = "fecha_fin", nullable = false)
    private LocalDateTime fechaFin;

    /**
     * Referencia lógica al microservicio ms-catalogo.
     * La promoción puede aplicarse a un producto específico (productoId)
     * O a una categoría completa (categoriaId).
     */
    @Column(name = "producto_id")
    private Long productoId;

    /**
     * Referencia lógica al microservicio ms-categorias.
     */
    @Column(name = "categoria_id")
    private Long categoriaId;

    @Column(nullable = false)
    private Boolean activo = true;

    @CreationTimestamp
    @Column(name = "fecha_creacion", updatable = false)
    private LocalDateTime fechaCreacion;

    @UpdateTimestamp
    @Column(name = "fecha_actualizacion")
    private LocalDateTime fechaActualizacion;
}
