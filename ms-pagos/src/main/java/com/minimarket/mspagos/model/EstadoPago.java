package com.minimarket.mspagos.model;

/**
 * Enum con los estados posibles de un pago.
 * - PENDIENTE: pago registrado pero no confirmado
 * - COMPLETADO: pago confirmado y procesado
 * - RECHAZADO: el pago fue rechazado (ej: tarjeta sin fondos)
 * - REEMBOLSADO: el pago fue devuelto al cliente
 */
public enum EstadoPago {
    PENDIENTE,
    COMPLETADO,
    RECHAZADO,
    REEMBOLSADO
}