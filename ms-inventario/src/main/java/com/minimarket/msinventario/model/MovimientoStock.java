package com.minimarket.msinventario.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

/**
 * Entidad que representa un movimiento de stock (entrada, salida o ajuste).
 * Cada movimiento está asociado a UN Stock específico (relación N:1).
 *
 * Sirve como historial/trazabilidad: nos permite saber QUÉ pasó con el stock,
 * CUÁNDO y POR QUÉ.
 */
@Entity
@Table(name = "movimientos_stock")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class MovimientoStock {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Stock al que afecta este movimiento.
     * @ManyToOne porque muchos movimientos pertenecen a UN stock.
     * Está en la misma BD, por eso usamos relación física con FK.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "stock_id", nullable = false)
    private Stock stock;

    /**
     * Tipo del movimiento: ENTRADA, SALIDA o AJUSTE.
     * @Enumerated(STRING) guarda el nombre del enum en la BD,
     * en lugar del índice numérico (más legible y seguro a cambios).
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "tipo_movimiento", nullable = false, length = 20)
    private TipoMovimiento tipoMovimiento;

    /**
     * Cantidad de unidades movidas.
     * Siempre positiva: el tipo (ENTRADA/SALIDA) define la dirección.
     */
    @Column(name = "cantidad", nullable = false)
    private Integer cantidad;

    /**
     * Motivo del movimiento.
     * Ej: "Compra a proveedor X", "Venta #1234", "Ajuste por inventario físico"
     */
    @Column(name = "motivo", length = 200)
    private String motivo;

    @CreationTimestamp
    @Column(name = "fecha_movimiento", nullable = false, updatable = false)
    private LocalDateTime fechaMovimiento;
}