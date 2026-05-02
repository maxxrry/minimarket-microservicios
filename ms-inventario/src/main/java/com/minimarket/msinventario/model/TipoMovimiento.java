

package com.minimarket.msinventario.model;

/**
 * Tipos de movimientos posibles sobre el stock.
 * - ENTRADA: ingreso de productos (compra a proveedor, devolución de cliente)
 * - SALIDA: egreso de productos (venta, merma, devolución a proveedor)
 * - AJUSTE: corrección manual del stock (inventario físico, errores)
 */
public enum TipoMovimiento {
    ENTRADA,
    SALIDA,
    AJUSTE
}