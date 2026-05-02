package com.minimarket.msventas.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * DTO de entrada para crear una venta completa con sus detalles.
 * El @Valid en la lista propaga la validación a cada detalle.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class VentaRequestDTO {

    /**
     * Cliente registrado (opcional). Null = venta al público general.
     */
    @Positive(message = "El ID del cliente debe ser positivo si se proporciona")
    private Long clienteId;

    @NotNull(message = "El ID del empleado es obligatorio")
    @Positive(message = "El ID del empleado debe ser positivo")
    private Long empleadoId;

    @NotEmpty(message = "La venta debe tener al menos un detalle")
    @Valid  // ← propaga la validación a cada DetalleVentaRequestDTO de la lista
    private List<DetalleVentaRequestDTO> detalles;
}