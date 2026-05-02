package com.minimarket.msventas.model;

/**
 * Estados posibles de una venta en el minimarket.
 *
 * - PENDIENTE: la venta se inició pero aún no se confirma el pago.
 *              Puede modificarse o anularse sin consecuencias.
 *
 * - COMPLETADA: la venta fue pagada y procesada exitosamente.
 *               No se puede modificar, solo anular si corresponde.
 *
 * - ANULADA: la venta fue anulada (devolución, error, etc.).
 *            No suma a estadísticas ni reportes.
 */
public enum EstadoVenta {
    PENDIENTE,
    COMPLETADA,
    ANULADA
}