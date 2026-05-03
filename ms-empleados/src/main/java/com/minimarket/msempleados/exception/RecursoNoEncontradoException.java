package com.minimarket.msempleados.exception;

/**
 * Excepción lanzada cuando se intenta acceder a un empleado que no existe.
 * Capturada por GlobalExceptionHandler → HTTP 404 NOT FOUND.
 */
public class RecursoNoEncontradoException extends RuntimeException {
    public RecursoNoEncontradoException(String mensaje) {
        super(mensaje);
    }
}