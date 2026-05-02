package com.minimarket.msventas.exception;

/**
 * Excepción lanzada cuando se intenta hacer una transición de estado inválida.
 * Ej: pasar de ANULADA a COMPLETADA.
 */
public class EstadoVentaInvalidoException extends RuntimeException {
    public EstadoVentaInvalidoException(String mensaje) {
        super(mensaje);
    }
} 