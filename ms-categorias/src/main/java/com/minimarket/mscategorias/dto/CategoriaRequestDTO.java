package com.minimarket.mscategorias.dto;

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
public class CategoriaRequestDTO {

    /**
     * Nombre de la categoría. Obligatorio y único.
     */
    @NotBlank(message = "El nombre de la categoría es obligatorio")
    @Size(min = 2, max = 50, message = "El nombre debe tener entre 2 y 50 caracteres")
    private String nombre;

    /**
     * Descripción de la categoría. Opcional.
     */
    @Size(max = 255, message = "La descripción no puede superar los 255 caracteres")
    private String descripcion;

    /**
     * Código interno de la categoría. Obligatorio.
     * Formato: 3 letras + guion + 3 dígitos (ej: LAC-001, BEB-002).
     */
    @NotBlank(message = "El código de la categoría es obligatorio")
    @Pattern(regexp = "^[A-Z]{3}-[0-9]{3}$",
            message = "El código debe seguir el formato XXX-000 (ej: LAC-001)")
    private String codigo;

    /**
     * Estado activo/inactivo de la categoría.
     * Si no se envía, el Service la marca como activa por defecto.
     */
    private Boolean activa;
}