package com.minimarket.msreportes.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.minimarket.msreportes.model.TipoReporte;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ReporteResponseDTO {

    private Long id;
    private TipoReporte tipoReporte;
    private String descripcion;
    private String parametros;
    private String resultadoJson;
    private Long generadoPor;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime fechaGeneracion;
}