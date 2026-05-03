package com.minimarket.msreportes.dto;

import com.minimarket.msreportes.model.TipoReporte;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ReporteRequestDTO {

    @NotNull(message = "El tipo de reporte es obligatorio")
    private TipoReporte tipoReporte;

    @Size(max = 255, message = "La descripción no puede superar los 255 caracteres")
    private String descripcion;

    @Size(max = 500, message = "Los parámetros no pueden superar los 500 caracteres")
    private String parametros;

    @NotNull(message = "El ID del empleado que genera el reporte es obligatorio")
    @Positive(message = "El ID del empleado debe ser positivo")
    private Long generadoPor;
}