package com.minimarket.mspromociones.dto;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * DTO de entrada para crear o actualizar una Promoción.
 * Las validaciones se aplican antes de llegar al Service.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PromocionRequestDTO {

    @NotBlank(message = "El nombre de la promoción es obligatorio")
    @Size(min = 3, max = 100, message = "El nombre debe tener entre 3 y 100 caracteres")
    private String nombre;

    @Size(max = 255, message = "La descripción no puede superar los 255 caracteres")
    private String descripcion;

    @NotNull(message = "El porcentaje de descuento es obligatorio")
    @DecimalMin(value = "1.0", message = "El descuento mínimo es 1%")
    @DecimalMax(value = "100.0", message = "El descuento máximo es 100%")
    private BigDecimal porcentajeDescuento;

    @NotNull(message = "La fecha de inicio es obligatoria")
    @FutureOrPresent(message = "La fecha de inicio no puede ser en el pasado")
    private LocalDateTime fechaInicio;

    @NotNull(message = "La fecha de fin es obligatoria")
    @Future(message = "La fecha de fin debe ser en el futuro")
    private LocalDateTime fechaFin;

    /**
     * Referencia lógica al producto. Opcional.
     * Si es null, la promoción aplica a una categoría completa (categoriaId).
     */
    private Long productoId;

    /**
     * Referencia lógica a la categoría. Opcional.
     * Si es null, la promoción aplica a un producto específico (productoId).
     */
    private Long categoriaId;
}
