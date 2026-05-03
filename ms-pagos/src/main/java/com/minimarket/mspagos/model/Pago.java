package com.minimarket.mspagos.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Entidad que representa un Pago realizado en el minimarket.
 * Cada pago se asocia lógicamente a una venta (ventaId) que vive en ms-ventas.
 */
@Entity
@Table(name = "pagos")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Pago {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Número único de transacción (ej: TRX-20251103-0001).
     * Sirve para trazabilidad y consultas externas.
     */
    @Column(name = "numero_transaccion", nullable = false, unique = true, length = 30)
    private String numeroTransaccion;

    /**
     * Monto total del pago. Usamos BigDecimal por precisión decimal.
     */
    @Column(name = "monto", nullable = false, precision = 12, scale = 2)
    private BigDecimal monto;

    /**
     * Método de pago utilizado.
     * Persistido como STRING para que sea legible en MySQL.
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "metodo_pago", nullable = false, length = 20)
    private MetodoPago metodoPago;

    /**
     * Estado actual del pago. Por defecto PENDIENTE al crear.
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "estado", nullable = false, length = 20)
    private EstadoPago estado = EstadoPago.PENDIENTE;

    /**
     * ID de la venta asociada (referencia LÓGICA a ms-ventas, no FK física).
     */
    @Column(name = "venta_id", nullable = false)
    private Long ventaId;

    @Column(name = "observaciones", length = 300)
    private String observaciones;

    @CreationTimestamp
    @Column(name = "fecha_creacion", nullable = false, updatable = false)
    private LocalDateTime fechaCreacion;

    @UpdateTimestamp
    @Column(name = "fecha_actualizacion")
    private LocalDateTime fechaActualizacion;
}