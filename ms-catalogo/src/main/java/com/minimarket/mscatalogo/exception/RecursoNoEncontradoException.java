package com.minimarket.mscatalogo.exception;

/**
 * Excepción lanzada cuando se intenta acceder a un recurso que no existe.
 * Será capturada por el @ControllerAdvice y devuelta como HTTP 404 NOT FOUND.
 */
public class RecursoNoEncontradoException extends RuntimeException {

    public RecursoNoEncontradoException(String mensaje) {
        super(mensaje);
    }
}