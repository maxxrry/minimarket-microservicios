package com.minimarket.msventas.exception;

/**
 * Excepción lanzada cuando una venta no cumple las reglas de negocio
 * (sin detalles, intentar modificar venta completada, etc.).
 */
public class VentaInvalidaException extends RuntimeException {
    public VentaInvalidaException(String mensaje) {
        super(mensaje);
    }
}