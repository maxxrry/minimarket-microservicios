package com.minimarket.msempleados.exception;

/**
 * Excepción lanzada cuando se intenta crear un empleado con un RUT
 * o email que ya existe en la base de datos.
 * Capturada por GlobalExceptionHandler → HTTP 409 CONFLICT.
 */
public class EmpleadoDuplicadoException extends RuntimeException {
    public EmpleadoDuplicadoException(String mensaje) {
        super(mensaje);
    }
}