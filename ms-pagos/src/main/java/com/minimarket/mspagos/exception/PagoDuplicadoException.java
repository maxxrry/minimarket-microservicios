
package com.minimarket.mspagos.exception;

/**
 * Excepción lanzada cuando se intenta crear un pago con un
 * número de transacción que ya existe.
 * Capturada por GlobalExceptionHandler → HTTP 409 CONFLICT.
 */
public class PagoDuplicadoException extends RuntimeException {
    public PagoDuplicadoException(String mensaje) {
        super(mensaje);
    }
}