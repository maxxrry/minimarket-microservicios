package com.minimarket.msinventario.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class StockResponseDTO {

    private Long id;
    private Long productoId;
    private Integer cantidadActual;
    private Integer cantidadMinima;
    private Integer cantidadMaxima;
    private String ubicacion;
    private Boolean alertaReposicion;  // true si cantidadActual <= cantidadMinima

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime fechaCreacion;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime fechaActualizacion;
}