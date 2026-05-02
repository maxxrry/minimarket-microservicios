package com.minimarket.msventas.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * Entidad que representa un Detalle de Venta (línea de la boleta).
 * Cada DetalleVenta representa UN producto vendido dentro de una Venta.
 *
 * Snapshot histórico:
 *   - nombreProducto y precioUnitario se congelan al momento de la venta.
 *   - Aunque el producto cambie en ms-catalogo, esta línea mantiene los datos originales.
 */
@Entity
@Table(name = "detalles_venta")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class DetalleVenta {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Venta a la que pertenece este detalle.
     * @ManyToOne porque muchos detalles pertenecen a UNA venta.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "venta_id", nullable = false)
    private Venta venta;

    /**
     * ID del producto vendido.
     * Referencia lógica a ms-catalogo.
     */
    @Column(name = "producto_id", nullable = false)
    private Long productoId;

    /**
     * Nombre del producto AL MOMENTO DE LA VENTA (snapshot).
     * No se actualiza si el producto cambia su nombre en ms-catalogo.
     */
    @Column(name = "nombre_producto", nullable = false, length = 100)
    private String nombreProducto;

    /**
     * Cantidad de unidades vendidas.
     */
    @Column(name = "cantidad", nullable = false)
    private Integer cantidad;

    /**
     * Precio unitario AL MOMENTO DE LA VENTA (snapshot).
     */
    @Column(name = "precio_unitario", nullable = false, precision = 10, scale = 2)
    private BigDecimal precioUnitario;

    /**
     * Descuento aplicado por unidad.
     */
    @Column(name = "descuento_unitario", nullable = false, precision = 10, scale = 2)
    private BigDecimal descuentoUnitario;

    /**
     * Subtotal de esta línea: (precioUnitario - descuentoUnitario) × cantidad.
     */
    @Column(name = "subtotal_linea", nullable = false, precision = 10, scale = 2)
    private BigDecimal subtotalLinea;
}