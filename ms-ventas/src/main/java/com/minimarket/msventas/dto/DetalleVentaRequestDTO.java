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

    @NotBlank(message = "El nombre del producto es obligatorio")
    @Size(max = 100, message = "El nombre no puede superar los 100 caracteres")
    private String nombreProducto;

    @NotNull(message = "La cantidad es obligatoria")
    @Min(value = 1, message = "La cantidad debe ser al menos 1")
    private Integer cantidad;

    @NotNull(message = "El precio unitario es obligatorio")
    @DecimalMin(value = "0.01", message = "El precio unitario debe ser mayor a cero")
    private BigDecimal precioUnitario;

    @DecimalMin(value = "0.00", message = "El descuento no puede ser negativo")
    private BigDecimal descuentoUnitario = BigDecimal.ZERO;
}