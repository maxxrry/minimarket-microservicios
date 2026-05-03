package com.minimarket.msclientes.exception;

/**
 * Excepción lanzada cuando se intenta crear un cliente con un RUT
 * o email que ya existe en la base de datos.
 * Capturada por GlobalExceptionHandler → HTTP 409 CONFLICT.
 */
public class ClienteDuplicadoException extends RuntimeException {
    public ClienteDuplicadoException(String mensaje) {
        super(mensaje);
    }
}