package com.minimarket.msventas.client.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Cuerpo para registrar un movimiento de stock en ms-inventario.
 * El enum TipoMovimiento se envía como String ("SALIDA", "ENTRADA", "AJUSTE")
 * y Jackson lo deserializa al enum remoto.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MovimientoStockRequestDTO {
    private Long stockId;
    private String tipoMovimiento;
    private Integer cantidad;
    private String motivo;
}
