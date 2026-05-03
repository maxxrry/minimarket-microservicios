package com.minimarket.mspagos.exception;

/**
 * Excepción lanzada cuando se intenta acceder a un pago que no existe.
 * Capturada por GlobalExceptionHandler → HTTP 404 NOT FOUND.
 */
public class RecursoNoEncontradoException extends RuntimeException {
    public RecursoNoEncontradoException(String mensaje) {
        super(mensaje);
    }
}