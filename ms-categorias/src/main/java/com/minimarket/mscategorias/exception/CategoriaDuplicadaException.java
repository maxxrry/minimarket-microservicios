package com.minimarket.mscategorias.exception;

/**
 * Excepción lanzada cuando se intenta crear una categoría con un nombre
 * o código que ya existe.
 * Será capturada por el GlobalExceptionHandler y convertida en HTTP 409.
 */
public class CategoriaDuplicadaException extends RuntimeException {

    public CategoriaDuplicadaException(String mensaje) {
        super(mensaje);
    }
}