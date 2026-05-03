package com.minimarket.mspromociones.exception;

/**
 * Excepción lanzada cuando se intenta acceder a una promoción que no existe.
 * Capturada por GlobalExceptionHandler → HTTP 404 NOT FOUND.
 */
public class RecursoNoEncontradoException extends RuntimeException {
    public RecursoNoEncontradoException(String mensaje) {
        super(mensaje);
    }
}
