package com.minimarket.msventas.client.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * Vista local de la Promocion remota (ms-promociones).
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PromocionDTO {
    private Long id;
    private String nombre;
    private BigDecimal porcentajeDescuento;
    private Long productoId;
    private Long categoriaId;
    private Boolean activo;
}
