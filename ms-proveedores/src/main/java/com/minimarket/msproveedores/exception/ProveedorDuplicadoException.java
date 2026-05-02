package com.minimarket.msproveedores.exception;

/**
 * Excepción lanzada cuando se intenta crear un proveedor con un RUT
 * o email que ya existe en la base de datos.
 */
public class ProveedorDuplicadoException extends RuntimeException {
    public ProveedorDuplicadoException(String mensaje) {
        super(mensaje);
    }
}   