package com.minimarket.msventas.client.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * Vista local del Producto remoto (ms-catalogo).
 * Solo expone los campos que ms-ventas necesita.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProductoDTO {
    private Long id;
    private String nombre;
    private BigDecimal precio;
    private Long categoriaId;
    private Boolean activo;
}
