package com.minimarket.mscategorias.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO de entrada para crear o actualizar una Categoría.
 * Las validaciones se aplican antes de llegar al Service.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "DTO para crear o actualizar una categoría")
public class CategoriaRequestDTO {

    /**
     * Nombre de la categoría. Obligatorio y único.
     */
    @NotBlank(message = "El nombre de la categoría es obligatorio")
    @Size(min = 2, max = 50, message = "El nombre debe tener entre 2 y 50 caracteres")
    @Schema(description = "Nombre de la categoría", example = "Lácteos", required = true)
    private String nombre;

    /**
     * Descripción de la categoría. Opcional.
     */
    @Size(max = 255, message = "La descripción no puede superar los 255 caracteres")
    @Schema(description = "Descripción de la categoría", example = "Productos lácteos y derivados")
    private String descripcion;

    /**
     * Código interno de la categoría. Obligatorio.
     * Formato: 3 letras + guion + 3 dígitos (ej: LAC-001, BEB-002).
     */
    @NotBlank(message = "El código de la categoría es obligatorio")
    @Pattern(regexp = "^[A-Z]{3}-[0-9]{3}$",
            message = "El código debe seguir el formato XXX-000 (ej: LAC-001)")
    @Schema(description = "Código interno (formato XXX-000)", example = "LAC-001", required = true)
    private String codigo;

    /**
     * Estado activo/inactivo de la categoría.
     * Si no se envía, el Service la marca como activa por defecto.
     */
    @Schema(description = "Indica si la categoría está activa", example = "true")
    private Boolean activa;
}