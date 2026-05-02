package com.minimarket.msinventario.exception;

/**
 * Excepción lanzada cuando se intenta crear un Stock para un producto
 * que ya tiene registro de stock.
 */
public class StockDuplicadoException extends RuntimeException {
    public StockDuplicadoException(String mensaje) {
        super(mensaje);
    }
}