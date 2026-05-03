package com.minimarket.msreportes.exception;

public class ReporteInvalidoException extends RuntimeException {
    public ReporteInvalidoException(String mensaje) {
        super(mensaje);
    }
}