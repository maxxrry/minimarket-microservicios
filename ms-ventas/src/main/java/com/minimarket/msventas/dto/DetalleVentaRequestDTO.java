package com.minimarket.msventas.dto;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * DTO de entrada para cada línea de la venta.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class DetalleVentaRequestDTO {

    @NotNull(message = "El ID del producto es obligatorio")
    @Positive(message = "El ID del producto debe ser positivo")
    private Long productoId;

    /**
     * Opcional. Si no se envía, se obtiene desde ms-catalogo vía Feign.
     * Si se envía, se ignora y se usa el valor del catálogo (fuente de verdad).
     */
    @Size(max = 100, message = "El nombre no puede superar los 100 caracteres")
    private String nombreProducto;

    @NotNull(message = "La cantidad es obligatoria")
    @Min(value = 1, message = "La cantidad debe ser al menos 1")
    private Integer cantidad;

    /**
     * Opcional. Si no se envía, se obtiene desde ms-catalogo vía Feign.
     */
    @DecimalMin(value = "0.01", message = "El precio unitario debe ser mayor a cero")
    private BigDecimal precioUnitario;

    /**
     * Opcional. Si es null o cero, se intenta aplicar la mejor promoción
     * activa para el producto/categoría (vía ms-promociones).
     */
    @DecimalMin(value = "0.00", message = "El descuento no puede ser negativo")
    private BigDecimal descuentoUnitario;
}