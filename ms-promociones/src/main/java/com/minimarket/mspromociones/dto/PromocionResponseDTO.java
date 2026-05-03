package com.minimarket.mspromociones.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * DTO de salida que se devuelve al cliente al consultar promociones.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PromocionResponseDTO {

    private Long id;
    private String nombre;
    private String descripcion;
    private BigDecimal porcentajeDescuento;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime fechaInicio;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime fechaFin;

    private Long productoId;
    private Long categoriaId;
    private Boolean activo;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime fechaCreacion;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime fechaActualizacion;
}
