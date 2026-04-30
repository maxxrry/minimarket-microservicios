package com.minimarket.mscatalogo.dto;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * DTO de entrada para crear o actualizar un Producto.
 * El cliente envía un JSON con esta estructura.
 * Las validaciones de Bean Validation (JSR 380) se aplican antes
 * de que el dato llegue al Service.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProductoRequestDTO {

    /**
     * Nombre del producto. Obligatorio.
     * Mínimo 2 caracteres, máximo 100.
     */
    @NotBlank(message = "El nombre del producto es obligatorio")
    @Size(min = 2, max = 100, message = "El nombre debe tener entre 2 y 100 caracteres")
    private String nombre;

    /**
     * Descripción detallada del producto. Opcional.
     * Máximo 500 caracteres.
     */
    @Size(max = 500, message = "La descripción no puede superar los 500 caracteres")
    private String descripcion;

    /**
     * Precio del producto. Obligatorio y debe ser mayor a cero.
     * Usamos BigDecimal por precisión decimal en valores monetarios.
     */
    @NotNull(message = "El precio es obligatorio")
    @DecimalMin(value = "0.01", message = "El precio debe ser mayor a cero")
    @DecimalMax(value = "99999999.99", message = "El precio supera el máximo permitido")
    private BigDecimal precio;

    /**
     * Código de barras único. Obligatorio.
     * Solo dígitos, entre 8 y 20 caracteres.
     */
    @NotBlank(message = "El código de barra es obligatorio")
    @Pattern(regexp = "^[0-9]{8,20}$",
            message = "El código de barra debe contener solo dígitos (8 a 20 caracteres)")
    private String codigoBarra;

    /**
     * ID de la categoría asociada. Obligatorio.
     */
    @NotNull(message = "La categoría es obligatoria")
    @Positive(message = "El ID de categoría debe ser positivo")
    private Long categoriaId;

    /**
     * ID del proveedor. Obligatorio.
     */
    @NotNull(message = "El proveedor es obligatorio")
    @Positive(message = "El ID de proveedor debe ser positivo")
    private Long proveedorId;

    /**
     * Indica si el producto se crea como activo.
     * Opcional: si no se envía, el Service lo marca como activo por defecto.
     */
    private Boolean activo;
}