package com.minimarket.msventas.client.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Vista local del Stock remoto (ms-inventario).
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class StockDTO {
    private Long id;
    private Long productoId;
    private Integer cantidadActual;
    private Integer cantidadMinima;
}
