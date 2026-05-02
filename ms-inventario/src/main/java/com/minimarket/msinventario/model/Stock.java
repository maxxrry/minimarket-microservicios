package com.minimarket.msinventario.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Entidad que representa el Stock actual de un producto en el minimarket.
 * Cada producto del catálogo tiene UN registro de stock asociado (1:1 lógica).
 *
 * Nota: productoId es una REFERENCIA LÓGICA al microservicio ms-catalogo.
 * No usamos @ManyToOne hacia Producto porque está en otra BD (mm_catalogo).
 */
@Entity
@Table(name = "stock")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Stock {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * ID del producto al que pertenece este stock.
     * Referencia lógica al microservicio ms-catalogo.
     * Único: un producto solo puede tener UN registro de stock.
     */
    @Column(name = "producto_id", nullable = false, unique = true)
    private Long productoId;

    /**
     * Cantidad actualmente disponible en bodega.
     */
    @Column(name = "cantidad_actual", nullable = false)
    private Integer cantidadActual;

    /**
     * Stock mínimo: si la cantidad actual baja de aquí, se alerta para reposición.
     */
    @Column(name = "cantidad_minima", nullable = false)
    private Integer cantidadMinima;

    /**
     * Stock máximo: capacidad máxima de almacenamiento.
     */
    @Column(name = "cantidad_maxima", nullable = false)
    private Integer cantidadMaxima;

    /**
     * Ubicación física del producto en bodega.
     * Ej: "Bodega A - Estante 3 - Posición 5"
     */
    @Column(name = "ubicacion", length = 100)
    private String ubicacion;

    /**
     * Lista de movimientos asociados a este stock.
     * @OneToMany porque Stock y MovimientoStock están en la MISMA BD
     * (mm_inventario), por eso podemos usar relación física con FK.
     */
    @OneToMany(mappedBy = "stock", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<MovimientoStock> movimientos = new ArrayList<>();

    @CreationTimestamp
    @Column(name = "fecha_creacion", nullable = false, updatable = false)
    private LocalDateTime fechaCreacion;

    @UpdateTimestamp
    @Column(name = "fecha_actualizacion")
    private LocalDateTime fechaActualizacion;
}