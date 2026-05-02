package com.minimarket.msventas.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Entidad que representa una Venta (boleta o factura) del minimarket.
 * Es la entidad maestra: contiene los datos generales y agrupa los detalles.
 *
 * Referencias lógicas (no son @ManyToOne porque están en otros microservicios):
 *   - clienteId  → ms-clientes
 *   - empleadoId → ms-empleados
 *
 * Relación física interna:
 *   - detalles (@OneToMany con DetalleVenta) → misma BD mm_ventas
 */
@Entity
@Table(name = "ventas")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Venta {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Número de boleta correlativo único.
     * Formato: VTA-YYYYMM-NNNNN (ej: VTA-202604-00001).
     */
    @Column(name = "numero_venta", nullable = false, unique = true, length = 30)
    private String numeroVenta;

    /**
     * ID del cliente que realiza la compra.
     * Referencia lógica a ms-clientes. Puede ser null (cliente sin registrar).
     */
    @Column(name = "cliente_id")
    private Long clienteId;

    /**
     * ID del empleado/cajero que procesó la venta.
     * Referencia lógica a ms-empleados. Obligatorio.
     */
    @Column(name = "empleado_id", nullable = false)
    private Long empleadoId;

    /**
     * Suma de los subtotales de todos los detalles, antes de descuentos e IVA.
     */
    @Column(name = "subtotal", nullable = false, precision = 10, scale = 2)
    private BigDecimal subtotal;

    /**
     * Total de descuentos aplicados a la venta.
     */
    @Column(name = "descuento_total", nullable = false, precision = 10, scale = 2)
    private BigDecimal descuentoTotal;

    /**
     * IVA del 19% calculado sobre (subtotal - descuento).
     */
    @Column(name = "iva", nullable = false, precision = 10, scale = 2)
    private BigDecimal iva;

    /**
     * Total final a pagar: (subtotal - descuento) + IVA.
     */
    @Column(name = "total", nullable = false, precision = 10, scale = 2)
    private BigDecimal total;

    /**
     * Estado actual de la venta.
     * Por defecto al crear es PENDIENTE.
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "estado", nullable = false, length = 20)
    private EstadoVenta estado = EstadoVenta.PENDIENTE;

    /**
     * Lista de productos vendidos.
     * Cascade ALL: cuando guardo una Venta, también se guardan sus detalles.
     * orphanRemoval: si un detalle se quita de la lista, se elimina de la BD.
     */
    @OneToMany(mappedBy = "venta",
            cascade = CascadeType.ALL,
            orphanRemoval = true,
            fetch = FetchType.LAZY)
    private List<DetalleVenta> detalles = new ArrayList<>();

    @CreationTimestamp
    @Column(name = "fecha_venta", nullable = false, updatable = false)
    private LocalDateTime fechaVenta;

    @UpdateTimestamp
    @Column(name = "fecha_actualizacion")
    private LocalDateTime fechaActualizacion;
}