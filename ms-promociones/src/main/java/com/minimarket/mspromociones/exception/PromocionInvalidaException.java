package com.minimarket.mspromociones.exception;

/**
 * Excepción lanzada cuando una promoción no cumple las reglas de negocio:
 *  - Fecha inicio posterior a fecha fin
 *  - Sin producto ni categoría asociada
 * Capturada por GlobalExceptionHandler → HTTP 400 BAD REQUEST.
 */
public class PromocionInvalidaException extends RuntimeException {
    public PromocionInvalidaException(String mensaje) {
        super(mensaje);
    }
}
