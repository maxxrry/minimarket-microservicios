package com.minimarket.msventas.exception;

/**
 * Lanzada cuando un microservicio remoto no responde o falla la conexión
 * (timeout, host inalcanzable, error 5xx).
 */
public class ServicioNoDisponibleException extends RuntimeException {
    public ServicioNoDisponibleException(String mensaje) {
        super(mensaje);
    }

    public ServicioNoDisponibleException(String mensaje, Throwable causa) {
        super(mensaje, causa);
    }
}
