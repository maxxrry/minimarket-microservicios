package com.minimarket.mscatalogo.dto;

import io.swagger.v3.oas.annotations.media.Schema;
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
@Schema(description = "DTO para crear o actualizar un producto")
public class ProductoRequestDTO {

    /**
     * Nombre del producto. Obligatorio.
     * Mínimo 2 caracteres, máximo 100.
     */
    @NotBlank(message = "El nombre del producto es obligatorio")
    @Size(min = 2, max = 100, message = "El nombre debe tener entre 2 y 100 caracteres")
    @Schema(description = "Nombre del producto", example = "Coca-Cola 1.5L", required = true)
    private String nombre;

    /**
     * Descripción detallada del producto. Opcional.
     * Máximo 500 caracteres.
     */
    @Size(max = 500, message = "La descripción no puede superar los 500 caracteres")
    @Schema(description = "Descripción detallada del producto", example = "Bebida gaseosa cola")
    private String descripcion;

    /**
     * Precio del producto. Obligatorio y debe ser mayor a cero.
     * Usamos BigDecimal por precisión decimal en valores monetarios.
     */
    @NotNull(message = "El precio es obligatorio")
    @DecimalMin(value = "0.01", message = "El precio debe ser mayor a cero")
    @DecimalMax(value = "99999999.99", message = "El precio supera el máximo permitido")
    @Schema(description = "Precio del producto", example = "2490.00", required = true)
    private BigDecimal precio;

    /**
     * Código de barras único. Obligatorio.
     * Solo dígitos, entre 8 y 20 caracteres.
     */
    @NotBlank(message = "El código de barra es obligatorio")
    @Pattern(regexp = "^[0-9]{8,20}$",
            message = "El código de barra debe contener solo dígitos (8 a 20 caracteres)")
    @Schema(description = "Código de barras único (8-20 dígitos)", example = "7800000000001", required = true)
    private String codigoBarra;

    /**
     * ID de la categoría asociada. Obligatorio.
     */
    @NotNull(message = "La categoría es obligatoria")
    @Positive(message = "El ID de categoría debe ser positivo")
    @Schema(description = "ID de la categoría del producto", example = "1", required = true)
    private Long categoriaId;

    /**
     * ID del proveedor. Obligatorio.
     */
    @NotNull(message = "El proveedor es obligatorio")
    @Positive(message = "El ID de proveedor debe ser positivo")
    @Schema(description = "ID del proveedor del producto", example = "1", required = true)
    private Long proveedorId;

    /**
     * Indica si el producto se crea como activo.
     * Opcional: si no se envía, el Service lo marca como activo por defecto.
     */
    @Schema(description = "Indica si el producto está activo", example = "true")
    private Boolean activo;
}