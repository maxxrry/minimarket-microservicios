package com.minimarket.msinventario.exception;

/**
 * Excepción lanzada cuando se intenta hacer una SALIDA de stock
 * por una cantidad mayor a la disponible.
 */
public class StockInsuficienteException extends RuntimeException {
    public StockInsuficienteException(String mensaje) {
        super(mensaje);
    }
}