package com.minimarket.mscategorias.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * DTO de salida que se devuelve al cliente al consultar categorías.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "DTO de respuesta con los datos de una categoría")
public class CategoriaResponseDTO {

    @Schema(description = "ID de la categoría", example = "1")
    private Long id;

    @Schema(description = "Nombre de la categoría", example = "Lácteos")
    private String nombre;

    @Schema(description = "Descripción de la categoría", example = "Productos lácteos y derivados")
    private String descripcion;

    @Schema(description = "Código interno", example = "LAC-001")
    private String codigo;

    @Schema(description = "Indica si está activa", example = "true")
    private Boolean activa;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Schema(description = "Fecha de creación", example = "2026-01-15 10:30:00")
    private LocalDateTime fechaCreacion;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Schema(description = "Fecha de última actualización", example = "2026-01-20 14:45:00")
    private LocalDateTime fechaActualizacion;
}