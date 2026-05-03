package com.minimarket.mspagos.exception;

/**
 * Excepción lanzada cuando se intenta hacer una transición
 * de estado no permitida (ej: intentar reembolsar un pago rechazado).
 * Capturada por GlobalExceptionHandler → HTTP 400 BAD REQUEST.
 */
public class EstadoInvalidoException extends RuntimeException {
    public EstadoInvalidoException(String mensaje) {
        super(mensaje);
    }
}