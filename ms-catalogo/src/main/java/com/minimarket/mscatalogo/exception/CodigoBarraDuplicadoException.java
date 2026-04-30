package com.minimarket.mscatalogo.exception;

/**
 * Excepción lanzada cuando se intenta crear un producto con un código de barra
 * que ya existe en la base de datos.
 * Será capturada por el @ControllerAdvice y devuelta como HTTP 409 CONFLICT.
 */
public class CodigoBarraDuplicadoException extends RuntimeException {

    public CodigoBarraDuplicadoException(String mensaje) {
        super(mensaje);
    }
}