package com.minimarket.mscategorias.exception;

/**
 * Excepción lanzada cuando se intenta acceder a una categoría que no existe.
 * Será capturada por el GlobalExceptionHandler y convertida en HTTP 404.
 */
public class RecursoNoEncontradoException extends RuntimeException {

    public RecursoNoEncontradoException(String mensaje) {
        super(mensaje);
    }
}
